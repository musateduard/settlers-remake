package org.example.events;


public record CursorEvent(
    long window,
    double xpos,
    double ypos
) implements InputEvent {}
