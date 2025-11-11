package org.example.mainwindow;

import imgui.ImGui;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import java.nio.DoubleBuffer;
import java.awt.Point;

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
    public final long windowId;
    public final EventManager eventManager;


    public Window(EventManager eventManager) {

        this.width = 800;
        this.height = 600;
        this.eventManager = eventManager;

        GLFW.glfwSetErrorCallback(this::errorCallback);

        // init glfw
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW.");
        }

        // set window hints
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);  // note: GLFW_OPENGL_DEBUG_CONTEXT needs to be conditional

        // create window
        this.windowId = GLFW.glfwCreateWindow(this.width, this.height, "demo window", NULL, NULL);

        // set minimum size
        GLFW.glfwSetWindowSizeLimits(this.windowId, 800, 600, GLFW_DONT_CARE, GLFW_DONT_CARE);

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


    private void errorCallback(int errorCode, long errorMessage) {
        throw new RuntimeException(GLFWErrorCallback.getDescription(errorMessage));
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


    public void resizeCallback(long windowId, int newWidth, int newHeight) {

        this.width = newWidth;
        this.height = newHeight;

        ResizeEvent event = new ResizeEvent(windowId, newWidth, newHeight);
        this.eventManager.dispatchResizeEvent(event);

        return;
    }


    public Point getCursorPosition() {

        DoubleBuffer screenX = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer screenY = BufferUtils.createDoubleBuffer(1);

        GLFW.glfwGetCursorPos(this.windowId, screenX, screenY);

        Point position = new Point((int) screenX.get(0), (int) screenY.get(0));

        return position;
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