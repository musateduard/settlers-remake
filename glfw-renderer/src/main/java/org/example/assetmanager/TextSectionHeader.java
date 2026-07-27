package org.example.assetmanager;

import java.util.List;


/**
 * Text section header only. String bodies are not decoded.
 */
public record TextSectionHeader(
    int magicNumber,
    int headerSize,
    short stringCount,
    short languageCount,
    List<String> stringList,
    int headerStartOffset
) {}