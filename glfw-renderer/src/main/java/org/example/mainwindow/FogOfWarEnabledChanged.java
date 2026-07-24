package org.example.mainwindow;


public record FogOfWarEnabledChanged(
    boolean enabled
) implements LandscapeEvent {}
