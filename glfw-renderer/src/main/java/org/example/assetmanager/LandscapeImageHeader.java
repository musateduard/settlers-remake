package org.example.assetmanager;


/**
 * Landscape texture header (width, height, typeSize, textureType).
 */
public record LandscapeImageHeader(
    short width,
    short height,
    byte typeSize,
    byte textureType,
    int headerStartOffset,
    int dataStartOffset
) {}