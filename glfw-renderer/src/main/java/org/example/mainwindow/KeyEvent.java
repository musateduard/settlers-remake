package org.example.mainwindow;


public record KeyEvent(
    long windowId,
    int key,
    int scanCode,
    int action,
    int modifier
) {}