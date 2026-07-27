package org.example.assetmanager;

import java.util.List;


/**
 * Sprite-like section (settler, color-sprite/torso, or shadow): index + sequences.
 */
public record SpriteSection(
    SectionHeader header,
    List<SequenceHeader> sequenceList
) {}