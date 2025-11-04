package org.example.mainwindow;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;
import imgui.glfw.ImGuiImplGlfw;
import imgui.gl3.ImGuiImplGl3;
import imgui.ImGui;


public class Window {

    public final int width;
    public final int height;
    public final long windowId;
    public final GLCapabilities capabilities;
    public final EventManager eventManager;
    public final ImGuiImplGlfw imGuiGlfw;
    public final ImGuiImplGl3 imGuiGl3;
    public final String glslVersion;


    public Window(EventManager eventManager) {

        this.width = 800;
        this.height = 600;
        this.eventManager = eventManager;
        this.imGuiGlfw = new ImGuiImplGlfw();
        this.imGuiGl3 = new ImGuiImplGl3();
        this.glslVersion = "#version 330";

        // init glfw
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW.");
        }

        // set window hints
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
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
        GLFW.glfwSetKeyCallback(

            this.windowId,

            (long windowId, int key, int scanCode, int action, int modifier) -> {

                KeyEvent event = new KeyEvent(windowId, key, scanCode, action, modifier);
                this.eventManager.emitKeyEvent(event);

                return;
            }
        );

        GLFW.glfwSetCursorPosCallback(

            this.windowId,

            (long window, double xpos, double ypos) -> {

                CursorEvent event = new CursorEvent(window, xpos, ypos);
                this.eventManager.emitCursorEvent(event);

                return;
            }
        );

        GLFW.glfwSetMouseButtonCallback(

            this.windowId,

            (long window, int button, int action, int mods) -> {

                MouseEvent event = new MouseEvent(window, button, action, mods);
                this.eventManager.emitMouseEvent(event);

                return;
            }
        );

        // init gl capabilities for current context
        this.capabilities = GL.createCapabilities();

        // init imgui
        ImGui.createContext();

        this.imGuiGlfw.init(this.windowId, true);
        this.imGuiGl3.init(this.glslVersion);

        // make window visible
        GLFW.glfwShowWindow(this.windowId);

        return;
    }


    public boolean shouldClose() {
        boolean shouldClose = GLFW.glfwWindowShouldClose(this.windowId);
        return shouldClose;
    }


    public void cleanup() {

        this.imGuiGlfw.shutdown();
        this.imGuiGl3.shutdown();

        ImGui.destroyContext();

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