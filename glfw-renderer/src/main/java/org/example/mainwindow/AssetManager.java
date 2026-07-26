package org.example.mainwindow;

import go.graphics.ImageData;
import jsettlers.graphics.image.Image;
import jsettlers.graphics.image.NullImage;
import jsettlers.graphics.image.SettlerImage;
import jsettlers.graphics.image.SingleImage;
import jsettlers.graphics.map.draw.ImageProvider;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;


/**
 * Owns GPU texture caches for the renderer.
 * <p>
 * Eventually this will hold a list of {@code AssetFile} entries (one per DAT file).
 * For now we stay compatible with the legacy jsettlers {@code ImageProvider} and only
 * cache OpenGL textures produced from it.
 */
public class AssetManager {

    /** Cache-key bit distinguishing a shadow texture from its body (bits 0–47 hold the locator). */
    // this bit stores whether a sprite locator has shadow or not
    private static final long SHADOW_CACHE_BIT = 1L << 48;

    public final HashMap<Long, Texture> textureList;
    public final Texture landscapeAtlas;


    public AssetManager() {

        // todo: introduce sprite - shadow mapping lists
        // this is because not all sprite sequences have a corresponding shadow sequence
        // some sprites have fewer shadows, some sprite sequences have misaligned shadow sequences
        // es.: asset file 13, sprite sequence 0 corresponds to shadow sequence 3

        this.textureList = new HashMap<>();
        this.landscapeAtlas = AssetManager.generateLandscapeAtlas();

        return;
    }


    /**
     * Builds the landscape atlas texture if missing and caches it on this manager.
     */
    public static Texture generateLandscapeAtlas() {

        int textureSize = 1024;
        long startTime = System.currentTimeMillis();
        int[] data = new int[textureSize * textureSize];

        try {
            LandscapeLayer.decodeImage(data);
        }

        catch (IOException exception) {
            exception.printStackTrace();
        }

        IntBuffer pixelBuffer = ByteBuffer
            .allocateDirect(textureSize * textureSize * 4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer();

        pixelBuffer.put(data).rewind();

        Texture atlas = new Texture(textureSize, textureSize, 0, 0, pixelBuffer);

        System.out.println("Background texture generated in " + (System.currentTimeMillis() - startTime) + "ms");
        return atlas;
    }


    /**
     * Returns a cached GPU texture for {@code locator}, loading pixel data from the
     * legacy {@link ImageProvider} on first use.
     * <p>
     * Temporary bridge during migration away from the old renderer asset path.
     */
    public Texture getOrCreateTexture(AssetLocator locator) {

        long locatorHash = locator.toHash();
        Texture cached = this.textureList.get(locatorHash);

        if (cached != null) {
            return cached;
        }

        Image image = ImageProvider.getInstance()
            .getSettlerSequence(locator.fileIndex(), locator.sequenceIndex())
            .getImageSafe(locator.spriteIndex(), null);

        return this.createAndCacheTexture(locatorHash, image);
    }


    /**
     * Returns the paired shadow texture for the body frame identified by {@code locator},
     * or {@code null} when that frame has no shadow.
     * <p>
     * Shadow pairing comes from the legacy DAT loader via {@link SettlerImage#getShadow()}.
     */
    public Texture getSpriteShadow(AssetLocator locator) {

        // todo: don't alter the locator hash; use the locator as is and only use the hash when inserting into textureList
        // todo: do we really need to include the shadow bit into the locator hash?
        long shadowLocator = locator.toHash() | SHADOW_CACHE_BIT;

        if (this.textureList.containsKey(shadowLocator)) {
            return this.textureList.get(shadowLocator);
        }

        Image image = ImageProvider.getInstance()
            .getSettlerSequence(locator.fileIndex(), locator.sequenceIndex())
            .getImageSafe(locator.spriteIndex(), null);

        SingleImage shadowImage = null;
        if (image instanceof SettlerImage settlerImage) {
            shadowImage = settlerImage.getShadow();
        }

        Texture shadow = this.createAndCacheTexture(shadowLocator, shadowImage);
        // Record a miss so we do not re-probe SettlerImage every frame.
        if (shadow == null) {
            this.textureList.put(shadowLocator, null);
        }

        return shadow;
    }


    private Texture createAndCacheTexture(long locatorHash, Image image) {

        // todo: make createAndCacheTexture get an AssetLocator not locator hash
        // note: this is a temporary solution because now the shadow bit is baked into the locator hash
        // this is because we don't have any way to resolve a shadow locator from a sprite locator
        // we currently use ImageProvider to get the SingleImage based on the locator
        // SingleImage contains both the sprite pixel data and the shadow pixel data
        // later we'll need to write a spite - shadow mapping table and resolve shadows based on sprite locators

        if ((image instanceof SingleImage) == false || image instanceof NullImage) {
            return null;
        }

        SingleImage singleImage = (SingleImage) image;

        if (singleImage.getWidth() <= 0 || singleImage.getHeight() <= 0) {
            return null;
        }

        ImageData pixelData = singleImage.getData();
        if (pixelData == null || pixelData.getWidth() <= 0 || pixelData.getHeight() <= 0) {
            return null;
        }

        IntBuffer buffer = pixelData.getReadData32();
        buffer.rewind();

        int width = pixelData.getWidth();
        int height = pixelData.getHeight();

        IntBuffer pixelBuffer = ByteBuffer
            .allocateDirect(width * height * 4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer();

        // Flip vertically: image rows are top-down; GL textures are bottom-up.
        for (int y = 0; y < height; y++) {
            int srcRow = (height - 1 - y) * width;
            for (int x = 0; x < width; x++) {
                pixelBuffer.put(buffer.get(srcRow + x));
            }
        }

        pixelBuffer.rewind();

        Texture texture = new Texture(width, height, singleImage.getOffsetX(), singleImage.getOffsetY(), pixelBuffer);
        this.textureList.put(locatorHash, texture);
        return texture;
    }
}