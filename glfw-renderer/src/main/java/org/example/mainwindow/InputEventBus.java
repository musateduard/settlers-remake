package org.example.mainwindow;

import java.util.Queue;
import java.util.ArrayDeque;
import org.example.events.InputEvent;


public class InputEventBus {

    private static InputEventBus instance = null;
    public final Queue<InputEvent> queue = new ArrayDeque<>();


    private InputEventBus() {

        // note: this class is a singleton and is not meant to be instantiated
        // instead you use getInstance() to get the current running instance
        // don't call this method from threads other than main
        // you can also use glfwSetWindowUserPointer to pass an instance to glfw that you can access during the callbacks
        // use glfwGetWindowUserPointer to get the instance pointer
        // for this project a singleton is sufficient

        return;
    }


    public static InputEventBus getInstance() {

        if (InputEventBus.instance == null) {
            InputEventBus.instance = new InputEventBus();
        }

        return InputEventBus.instance;
    }
}