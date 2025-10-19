package org.example.glfwrenderer;


public class GameLauncherGLFW {

    public static void main(String[] args) throws Exception {

        MainWindowGLFW settlersGame = new MainWindowGLFW();

        settlersGame.start();
        settlersGame.cleanup();

        return;
    }
}