package org.example.mainwindow;


/**
 * this class describes one vertex attribute for a {@link VertexBuffer} VAO setup.
 */
public record VertexAttribute(
    int location,
    int size,
    int type,
    boolean normalized,
    int stride,
    long pointer
) {}
