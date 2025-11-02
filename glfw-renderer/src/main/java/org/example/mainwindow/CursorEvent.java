package org.example.mainwindow;


public record CursorEvent(
    long window,
    double xpos,
    double ypos
) {}