package org.example.mainwindow;

import go.graphics.ImageData;
import jsettlers.graphics.image.Image;
import jsettlers.graphics.image.NullImage;
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

    public final HashMap<Long, Texture> textureList;
    public final Texture landscapeAtlas;


    public AssetManager() {

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

        Texture cached = this.textureList.get(locator.toHash());

        if (cached != null) {
            return cached;
        }

        // Temporary: pull pixels from the legacy ImageProvider until AssetManager owns DAT decoding.
        Image image = ImageProvider.getInstance()
            .getSettlerSequence(locator.fileIndex(), locator.sequenceIndex())
            .getImageSafe(locator.spriteIndex(), null);

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

        this.textureList.put(locator.toHash(), texture);
        return texture;
    }
}