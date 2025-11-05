package org.example.mainwindow;


public record ResizeEvent(
    long windowId,
    int width,
    int height
) {}