package org.example.events;


public record KeyEvent(
    long windowId,
    int key,
    int scanCode,
    int action,
    int modifier
) implements InputEvent {}
