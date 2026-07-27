package org.example.assetmanager;


/**
 * Palette section header and absolute palette data offsets.
 */
public record PaletteSectionHeader(
    int magicNumber,
    short headerSize,
    short offsetCount,
    int paletteSize,
    int[] paletteOffsetList,
    int headerStartOffset
) {}