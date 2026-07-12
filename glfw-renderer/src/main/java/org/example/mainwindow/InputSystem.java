package org.example.mainwindow;

import java.util.ArrayDeque;
import java.util.Queue;
import org.lwjgl.glfw.GLFW;


public class InputSystem {

    public Queue<ResizeEvent> queue = new ArrayDeque<>();
    private static InputSystem instance = null;


    private InputSystem() {
        return;
    }


    public static InputSystem getInstance() {

        if (InputSystem.instance == null) {
            InputSystem.instance = new InputSystem();
        }

        return InputSystem.instance;
    }


    public static void addResizeEvent(long windowId, int newWidth, int newHeight) {

        ResizeEvent event = new ResizeEvent(windowId, newWidth, newHeight);
        InputSystem.getInstance().queue.add(event);

        return;
    }


    public static void handleInput(Application application) {

        InputSystem input = InputSystem.getInstance();

        // gather input events
        GLFW.glfwPollEvents();

        // handle input queue
        while (input.queue.isEmpty() == false) {

            ResizeEvent event = input.queue.remove();
            application.resizeWindow(event);

            continue;
        }

        return;
    }
}