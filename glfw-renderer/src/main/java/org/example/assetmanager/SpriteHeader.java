package org.example.assetmanager;


/**
 * Displaced sprite / shadow / color-sprite image header (magic 0x0c).
 */
public record SpriteHeader(
    int magicNumber,
    short width,
    short height,
    short offsetX,
    short offsetY,
    int headerStartOffset,
    int dataStartOffset
) {}