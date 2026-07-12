package org.example.mainwindow;

import java.util.ArrayDeque;
import java.util.Queue;
import org.lwjgl.glfw.GLFW;
import org.example.events.CursorEvent;
import org.example.events.InputEvent;
import org.example.events.KeyEvent;
import org.example.events.MouseEvent;
import org.example.events.ResizeEvent;


public class InputSystem {

    private static InputSystem instance = null;
    public Queue<InputEvent> queue = new ArrayDeque<>();


    private InputSystem() {
        return;
    }


    public static InputSystem getInstance() {

        if (InputSystem.instance == null) {
            InputSystem.instance = new InputSystem();
        }

        return InputSystem.instance;
    }


    public static void addKeyEvent(long windowId, int key, int scanCode, int action, int modifiers) {

        KeyEvent event = new KeyEvent(windowId, key, scanCode, action, modifiers);
        InputSystem.getInstance().queue.add(event);

        return;
    }


    public static void addMouseButtonEvent(long windowId, int button, int action, int modifiers) {

        MouseEvent event = new MouseEvent(windowId, button, action, modifiers);
        InputSystem.getInstance().queue.add(event);

        return;
    }


    public static void addCursorEvent(long windowId, double offsetX, double offsetY) {

        CursorEvent event = new CursorEvent(windowId, offsetX, offsetY);
        InputSystem.getInstance().queue.add(event);

        return;
    }


    public static void addResizeEvent(long windowId, int newWidth, int newHeight) {

        ResizeEvent event = new ResizeEvent(windowId, newWidth, newHeight);
        InputSystem.getInstance().queue.add(event);

        return;
    }


    public static void handleInput(Application application, Camera camera) {

        InputSystem input = InputSystem.getInstance();

        // gather input events
        GLFW.glfwPollEvents();

        // handle input queue
        while (input.queue.isEmpty() == false) {

            InputEvent event = input.queue.remove();

            switch (event) {
                case ResizeEvent resize -> application.resizeWindow(resize);
                case KeyEvent key -> camera.handleKeyEvent(key);
                case MouseEvent mouseButton -> camera.handleMouseButtonEvent(mouseButton);
                case CursorEvent mouseMove -> camera.handleCursorEvent(mouseMove);
                default -> throw new RuntimeException("unhandled event type");
            }

            continue;
        }

        return;
    }
}