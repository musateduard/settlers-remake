package org.example.mainwindow;


public record BackgroundLineChanged(
    int offsetX,
    int offsetY,
    int length
) implements LandscapeEvent {}
