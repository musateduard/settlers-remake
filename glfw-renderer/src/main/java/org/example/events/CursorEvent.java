package org.example.events;


public record CursorEvent(
    long window,
    double offsetX,
    double offsetY
) implements InputEvent {}
