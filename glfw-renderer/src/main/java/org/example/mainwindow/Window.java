package org.example.mainwindow;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_DONT_CARE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_DEBUG_CONTEXT;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.system.MemoryUtil.NULL;


public class Window {

    public int width;
    public int height;
    public final long handle;


    public Window() {

        this.width = 800;
        this.height = 600;

        GLFW.glfwSetErrorCallback(this::errorCallback);

        // init glfw
        if (GLFW.glfwInit() == false) {
            throw new RuntimeException("Failed to initialize GLFW.");
        }

        // set window hints
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);  // note: GLFW_OPENGL_DEBUG_CONTEXT needs to be conditional

        // create window
        this.handle = GLFW.glfwCreateWindow(this.width, this.height, "demo window", NULL, NULL);

        if (this.handle == 0) {
            throw new RuntimeException("failed to create glfw window");
        }

        // set minimum size
        GLFW.glfwSetWindowSizeLimits(this.handle, 800, 600, GLFW_DONT_CARE, GLFW_DONT_CARE);

        // make context current
        GLFW.glfwMakeContextCurrent(this.handle);

        // enable vsync
        GLFW.glfwSwapInterval(1);

        // register input callbacks
        GLFW.glfwSetKeyCallback(this.handle, InputSystem::addKeyEvent);
        GLFW.glfwSetCursorPosCallback(this.handle, InputSystem::addCursorEvent);
        GLFW.glfwSetMouseButtonCallback(this.handle, InputSystem::addMouseButtonEvent);
        GLFW.glfwSetWindowSizeCallback(this.handle, InputSystem::addResizeEvent);

        // make window visible
        GLFW.glfwShowWindow(this.handle);

        return;
    }


    private void errorCallback(int errorCode, long errorMessage) {
        throw new RuntimeException(GLFWErrorCallback.getDescription(errorMessage));
    }
}