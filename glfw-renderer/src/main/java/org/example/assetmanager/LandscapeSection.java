package org.example.assetmanager;

import java.util.List;


/**
 * Landscape section: index + flat landscape image headers.
 */
public record LandscapeSection(
    SectionHeader header,
    List<LandscapeImageHeader> imageList
) {}