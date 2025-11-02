package org.example.mainwindow;

public record MouseEvent(
    long window,
    int button,
    int action,
    int mods
) {}