package org.example.assetmanager;


/**
 * Common section index block: magic, size, and absolute offsets into the DAT.
 */
public record SectionHeader(
    int magicNumber,
    short headerSize,
    short offsetCount,
    int[] offsetList,
    int headerStartOffset
) {}