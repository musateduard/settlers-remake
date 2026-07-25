package org.example.mainwindow;


/**
 * Identifies an image asset inside a DAT file.
 * Layout when packed to a 64-bit key (unsigned fields):
 * <pre>
 *   fileIndex      1 byte  (bits 40–47)
 *   sectionIndex   1 byte  (bits 32–39)  — asset type
 *   sequenceIndex  2 bytes (bits 16–31)
 *   spriteIndex    2 bytes (bits  0–15)
 * </pre>
 */
public record AssetLocator(
    int fileIndex,
    int sectionIndex,
    int sequenceIndex,
    int spriteIndex
) {

    /**
     * Packs this locator into an unsigned 64-bit cache key.
     */
    public long toHash() {

        long hash = (this.fileIndex & 0xFFL) << 40 |
            (this.sectionIndex & 0xFFL) << 32 |
            (this.sequenceIndex & 0xFFFFL) << 16 |
            (this.spriteIndex & 0xFFFFL);

        return hash;
    }
}