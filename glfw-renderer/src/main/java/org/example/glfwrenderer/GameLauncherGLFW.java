package org.example.glfwrenderer;


public class GameLauncherGLFW {

    public static void main(String[] args) throws Exception {

        DemoGLFWFrame settlersGame = new DemoGLFWFrame();

        settlersGame.start();
        settlersGame.cleanup();

        return;
    }
}