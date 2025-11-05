package org.example.mainwindow;

import imgui.ImGui;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.system.MemoryUtil;


public class Window {

    public int width;
    public int height;
    public final long windowId;
    public final EventManager eventManager;


    public Window(EventManager eventManager) {

        this.width = 800;
        this.height = 600;
        this.eventManager = eventManager;

        // init glfw
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW.");
        }

        // set window hints
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);

        // create window
        this.windowId = GLFW.glfwCreateWindow(this.width, this.height, "demo window", MemoryUtil.NULL, MemoryUtil.NULL);

        if (this.windowId == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window.");
        }

        // make context current
        GLFW.glfwMakeContextCurrent(this.windowId);

        // enable vsync
        GLFW.glfwSwapInterval(1);

        // register keyboard handler
        GLFW.glfwSetKeyCallback(this.windowId, this::keyCallback);
        GLFW.glfwSetCursorPosCallback(this.windowId, this::cursorCallback);
        GLFW.glfwSetMouseButtonCallback(this.windowId, this::mouseCallback);
        GLFW.glfwSetWindowSizeCallback(this.windowId, this::resizeCallback);

        // make window visible
        GLFW.glfwShowWindow(this.windowId);

        return;
    }


    public void keyCallback(long windowId, int key, int scanCode, int action, int modifier) {

        KeyEvent event = new KeyEvent(windowId, key, scanCode, action, modifier);
        this.eventManager.dispatchKeyEvent(event);

        return;
    }


    public void mouseCallback(long window, int button, int action, int mods) {

        if (ImGui.getIO().getWantCaptureMouse()) {
            return;
        }

        MouseEvent event = new MouseEvent(window, button, action, mods);
        this.eventManager.dispatchMouseEvent(event);

        return;
    }


    public void cursorCallback(long window, double xpos, double ypos) {

        if (ImGui.getIO().getWantCaptureMouse()) {
            return;
        }

        CursorEvent event = new CursorEvent(window, xpos, ypos);
        this.eventManager.dispatchCursorEvent(event);

        return;
    }


    public void resizeCallback(long windowId, int width, int height) {

        this.width = width;
        this.height = height;

        ResizeEvent event = new ResizeEvent(windowId, width, height);
        this.eventManager.dispatchResizeEvent(event);

        return;
    }


    public boolean shouldClose() {
        boolean shouldClose = GLFW.glfwWindowShouldClose(this.windowId);
        return shouldClose;
    }


    public void cleanup() {

        Callbacks.glfwFreeCallbacks(this.windowId);
        GLFW.glfwDestroyWindow(this.windowId);
        GLFW.glfwTerminate();

        return;
    }


    public void swapBuffers() {
        GLFW.glfwSwapBuffers(this.windowId);
        return;
    }


    public void pollEvents() {
        GLFW.glfwPollEvents();
        return;
    }
}