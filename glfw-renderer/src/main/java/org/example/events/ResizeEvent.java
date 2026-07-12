package org.example.events;


public record ResizeEvent(
    long windowId,
    int width,
    int height
) implements InputEvent {}
