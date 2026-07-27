package org.example.mainwindow;

import go.graphics.ImageData;
import jsettlers.common.images.EImageLinkType;
import jsettlers.graphics.image.Image;
import jsettlers.graphics.image.NullImage;
import jsettlers.graphics.image.SettlerImage;
import jsettlers.graphics.image.SingleImage;
import jsettlers.graphics.map.draw.ImageProvider;
import org.example.assetmanager.AssetType;

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

    /**
     * Sparse sprite-sequence → shadow-sequence remaps.
     * Key: {@link AssetLocator#toSequenceHash()} (file + section + sequence).
     * Value: absolute shadow sequence, or {@code -1} for no shadow.
     * Missing key: naive identity (same sequence index).
     */
    public final HashMap<Long, Integer> shadowLookupTable;

    public final HashMap<Long, Texture> textureList;
    public final Texture landscapeAtlas;


    public AssetManager() {

        // todo: introduce sprite - shadow mapping lists
        // this is because not all sprite sequences have a corresponding shadow sequence
        // some sprites have fewer shadows, some sprite sequences have misaligned shadow sequences
        // es.: asset file 13, sprite sequence 0 corresponds to shadow sequence 3

        this.textureList = new HashMap<>();
        this.shadowLookupTable = AssetManager.buildShadowLookupTable();
        this.landscapeAtlas = AssetManager.generateLandscapeAtlas();
    }


    /**
     * Ports legacy {@code ShadowMapping*} + {@code AdvancedDatFileReader} shadow hacks
     * into a flat sequence-hash → absolute shadow-sequence map.
     */
    private static HashMap<Long, Integer> buildShadowLookupTable() {

        // todo: cleanup this function

        HashMap<Long, Integer> table = new HashMap<>();

        // File 1 — ShadowMapping1 (gaps at 26 and 33; later indices shift down)
        table.put(seqKey(1, 26), -1);
        for (int seq = 27; seq <= 32; seq++) {
            table.put(seqKey(1, seq), seq - 1);
        }
        table.put(seqKey(1, 33), -1);
        for (int seq = 34; seq < 512; seq++) {
            table.put(seqKey(1, seq), seq - 2);
        }

        // File 6 — ShadowMapping6 (donkey fix)
        for (int seq = 15; seq < 512; seq++) {
            table.put(seqKey(6, seq), seq - 8);
        }

        // File 11 — specialist shift (settlerStarts.length == 239)
        for (int seq = 13; seq <= 171; seq++) {
            table.put(seqKey(11, seq), seq - 13);
        }

        // File 13 — building shadow realignment (shadowDifference == 26)
        for (int seq = 0; seq < 27; seq++) {
            table.put(seqKey(13, seq), seq + 3);
        }
        for (int seq = 27; seq < 36; seq++) {
            table.put(seqKey(13, seq), seq == 28 ? -1 : seq + 2);
        }
        for (int seq = 36; seq < 44; seq++) {
            table.put(seqKey(13, seq), -1);
        }
        table.put(seqKey(13, 44), 38); // dock
        table.put(seqKey(13, 45), 39); // harbour
        for (int seq = 46; seq < 512; seq++) {
            table.put(seqKey(13, seq), -1);
        }

        // File 22 — ShadowMapping22
        table.put(seqKey(22, 0), 19);
        table.put(seqKey(22, 1), -1);
        for (int seq = 2; seq <= 7; seq++) {
            table.put(seqKey(22, seq), seq + 18);
        }
        for (int seq = 8; seq <= 13; seq++) {
            table.put(seqKey(22, seq), -1);
        }
        for (int seq = 14; seq < 512; seq++) {
            table.put(seqKey(22, seq), seq - 14);
        }

        // File 36 — ship shadow tweaks (shadowDifference == 28)
        table.put(seqKey(36, 2), -1); // roman cargo ship front
        table.put(seqKey(36, 4), 1);  // roman ferry
        table.put(seqKey(36, 6), -1); // roman ferry front

        // File 42 — ShadowMapping42
        for (int seq = 0; seq <= 6; seq++) {
            table.put(seqKey(42, seq), seq + 19);
        }
        for (int seq = 7; seq < 512; seq++) {
            table.put(seqKey(42, seq), seq - 6);
        }

        return table;
    }


    private static long seqKey(int fileIndex, int sequenceIndex) {
        // todo: replace this method with calling AssetLocator in place
        // todo: use AssetType instead of EImageLinkType
        return new AssetLocator(fileIndex, EImageLinkType.SETTLER.ordinal(), sequenceIndex, 0).toHash() & 0xffffffff0000L;
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
     * Resolves the shadow {@link AssetLocator} for a sprite body locator.
     * <p>
     * Table miss → naive same-sequence shadow; {@code -1} → no shadow ({@code null});
     * otherwise absolute shadow sequence.
     */
    public AssetLocator getShadowLocator(AssetLocator spriteLocator) {

        Integer mapped = this.shadowLookupTable.get(spriteLocator.toHash() & 0xffffffff0000L);

        if (mapped != null && mapped == -1) {
            return null;
        }

        int shadowSeq = mapped != null ? mapped : spriteLocator.sequenceIndex();

        return new AssetLocator(
            spriteLocator.fileIndex(),
            AssetType.Shadow.value,  // note: this mixes AssetType with legacy EImageLinkType; we need to decouple from legacy classes
            shadowSeq,
            spriteLocator.spriteIndex()
        );
    }


    /**
     * Returns the paired shadow texture for the body frame identified by {@code locator},
     * or {@code null} when that frame has no shadow.
     * <p>
     * Cache keys use the resolved shadow {@link AssetLocator}. Pixel data is still loaded
     * temporarily via {@link SettlerImage#getShadow()} until shadow-section DAT reads exist.
     */
    public Texture getSpriteShadow(AssetLocator locator) {

        AssetLocator shadowLocator = this.getShadowLocator(locator);
        if (shadowLocator == null) {
            return null;
        }

        long shadowHash = shadowLocator.toHash();

        if (this.textureList.containsKey(shadowHash)) {
            return this.textureList.get(shadowHash);
        }

        // Temporary pixel bridge: body SettlerImage still carries the paired shadow.
        Image image = ImageProvider.getInstance()
            .getSettlerSequence(locator.fileIndex(), locator.sequenceIndex())
            .getImageSafe(locator.spriteIndex(), null);

        SingleImage shadowImage = null;
        if (image instanceof SettlerImage settlerImage) {
            shadowImage = settlerImage.getShadow();
        }

        Texture shadow = this.createAndCacheTexture(shadowHash, shadowImage);
        if (shadow == null) {
            this.textureList.put(shadowHash, null);
        }

        return shadow;
    }


    private Texture createAndCacheTexture(long locatorHash, Image image) {

        // todo: replace this method with AssetFile.decode_image
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