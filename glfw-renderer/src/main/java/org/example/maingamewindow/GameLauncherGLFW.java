package org.example.maingamewindow;


public class GameLauncherGLFW {

    public static void main(String[] args) throws Exception {

        MainWindowGLFW settlersGame = new MainWindowGLFW();

        settlersGame.startGame();
        settlersGame.cleanup();

        return;
    }
}