package org.example.assetmanager;

import java.util.List;


/**
 * Sprite sequence header (magic 0x1402) with per-frame sprite headers.
 */
public record SequenceHeader(
    int magicNumber,
    int unknown1,
    byte unknown2,
    byte spriteCount,
    List<SpriteHeader> spriteList,
    int headerStartOffset
) {}