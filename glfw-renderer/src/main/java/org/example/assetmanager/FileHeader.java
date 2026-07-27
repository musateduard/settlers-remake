package org.example.assetmanager;


/**
 * Fixed 0x54-byte DAT file header (little-endian).
 */
public record FileHeader(
    int magicNumber,
    int unknown1,
    int unknown2,
    int headerSize,
    int unknown3,
    int unknown4,
    int unknown5,
    int unknown6,
    int redChannelBits,
    int greenChannelBits,
    int blueChannelBits,
    int alphaChannelBits,
    int fileSize,
    int textSectionOffset,
    int landscapeSectionOffset,
    int menuSectionOffset,
    int spriteSectionOffset,
    int colorSpriteSectionOffset,
    int shadowSectionOffset,
    int animationSectionOffset,
    int paletteSectionOffset
) {}