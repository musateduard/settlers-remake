package org.example.glfwrenderer;

import java.io.IOException;
import jsettlers.logic.map.loading.MapLoadException;


public class GLFWGameLauncher {

    public static void main(String[] args) throws Exception {

        DemoGLFWFrame settlersGame = new DemoGLFWFrame();

        settlersGame.start();
        settlersGame.cleanup();

        return;
    }
}