package org.example.events;


public record MouseEvent(
    long window,
    int button,
    int action,
    int mods
) implements InputEvent {}
