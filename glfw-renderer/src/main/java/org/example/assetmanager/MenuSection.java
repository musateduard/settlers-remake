package org.example.assetmanager;

import java.util.List;


/**
 * Menu / GUI section: index + flat image headers.
 */
public record MenuSection(
    SectionHeader header,
    List<MenuImageHeader> imageList
) {}