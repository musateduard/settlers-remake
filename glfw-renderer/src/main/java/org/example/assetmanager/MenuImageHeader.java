package org.example.assetmanager;


/**
 * Menu / GUI image header (width, height, offsets; no magic).
 */
public record MenuImageHeader(
    short width,
    short height,
    short offsetX,
    short offsetY,
    int headerStartOffset,
    int dataStartOffset
) {}