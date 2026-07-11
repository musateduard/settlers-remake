package org.example.mainwindow;

import imgui.ImGui;
import imgui.ImFont;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiStyleVar;
import imgui.glfw.ImGuiImplGlfw;
import imgui.gl3.ImGuiImplGl3;
import imgui.internal.ImGuiContext;
import org.lwjgl.glfw.GLFW;

import static imgui.flag.ImGuiWindowFlags.NoMove;
import static imgui.flag.ImGuiWindowFlags.NoResize;
import static imgui.flag.ImGuiWindowFlags.NoCollapse;
import static imgui.flag.ImGuiWindowFlags.NoTitleBar;
import static imgui.flag.ImGuiWindowFlags.NoBringToFrontOnFocus;
import static org.lwjgl.system.MemoryUtil.NULL;


public class GuiRenderer {

    public final double idealAspectRatio;
    public final ImGuiImplGlfw glfwBackend;
    public final ImGuiImplGl3 openglBackend;
    public final ImFont menuFont;


    public GuiRenderer(Window window, Renderer renderer) {

        this.idealAspectRatio = 800.00f / 600.00f;

        if (window.windowId == NULL) {
            throw new RuntimeException("cannot initialize gui renderer before creating glfw window");
        }

        // create imgui context
        ImGui.createContext();

        // disable ini file
        ImGuiIO input = ImGui.getIO();

        input.setIniFilename(null);
        input.setLogFilename(null);

        // load font files
        this.menuFont = input.getFonts().addFontFromFileTTF("D:\\fonts\\ms-sans-serif-1.ttf", 12.00f);

        // init all backends
        this.glfwBackend = new ImGuiImplGlfw();
        this.openglBackend = new ImGuiImplGl3();

        this.glfwBackend.init(window.windowId, false);
        this.openglBackend.init(renderer.glslVersion);

        return;
    }


    public void renderGuiStack(Window window, Renderer renderer) {

        // init new glfw frame
        this.glfwBackend.newFrame();

        // init new opengl frame
        this.openglBackend.newFrame();

        // get imgui state structures
        ImGuiIO input = ImGui.getIO();
        ImGuiStyle style = ImGui.getStyle();
        ImGuiContext context = ImGui.getCurrentContext();

        // scale input based on canvas size
        int screenWidth = (int) input.getDisplaySizeX();
        int screenHeight = (int) input.getDisplaySizeY();
        float currentAspectRatio = (float) screenWidth / screenHeight;
        boolean isWideScreen = currentAspectRatio >= this.idealAspectRatio;

        // note: getMousePos returns incorrect values
        // note: getCursorPosition returns accurate cursor position using glfw

        int mouseX = window.getCursorPosition().x;
        int mouseY = window.getCursorPosition().y;

        int canvasWidth =         isWideScreen ? (int) (screenHeight * this.idealAspectRatio) : screenWidth;
        int canvasHeight =        isWideScreen ? screenHeight                                 : (int) (screenWidth / this.idealAspectRatio);
        int canvasX =             isWideScreen ? (screenWidth - canvasWidth) / 2              : 0;
        int canvasY =             isWideScreen ? 0                                            : (screenHeight - canvasHeight) / 2;
        float canvasCursorScale = isWideScreen ? (screenHeight / 600.00f)                     : (screenWidth / 800.00f);

        float canvasCursorX = (mouseX - canvasX) / canvasCursorScale;
        float canvasCursorY = (mouseY - canvasY) / canvasCursorScale;

        /*
        note:

        cursor only has problems when moving cursor fast and pressing lmb
        possible solution with AddMousePosEvent() or AddMouseButtonEvent()
        maybe check if mouse is pressed and dispatch mouse button event?
        imgui window is still mapped to top left corner of screen
        this causes input to sometimes be dispatched to the "real" window instead of the one scaled to the framebuffer

        fix:
        in order to prevent this behavior you need to disable imgui from processing inputs itself
        ImGui_ImplGlfw_InitForOpenGL(window.window_id, false);

        this means you need to add input events to the imgui backend manually
        input.AddMouseButtonEvent(ImGuiMouseButton_Left, true);
        input.AddMouseButtonEvent(ImGuiMouseButton_Left, false);
        */

        // when rendering to canvas frame buffer
        input.setDisplaySize(800.00f, 600.00f);
        input.setDisplayFramebufferScale(1.00f, 1.00f);
        input.addMousePosEvent(canvasCursorX, canvasCursorY);

        if (GLFW.glfwGetMouseButton(window.windowId, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
            input.addMouseButtonEvent(ImGuiMouseButton.Left, true);
        }

        else {
            input.addMouseButtonEvent(ImGuiMouseButton.Left, false);
        }

        // when rendering to main buffer
        // input.setDisplaySize(800, 600);
        // input.setDisplayFramebufferScale(canvasCursorScale, canvasCursorScale);

        // init new imgui frame
        ImGui.newFrame();

        // set window transparency
        style.setColor(ImGuiCol.WindowBg, 0, 0, 0, 50);
        style.setColor(ImGuiCol.Button,        144, 128, 56, 255);
        style.setColor(ImGuiCol.ButtonActive,  132, 117, 49, 255);
        style.setColor(ImGuiCol.ButtonHovered, 138, 123, 52, 255);

        // set text color
        style.setColor(ImGuiCol.Text, 0, 12, 64, 255);

        // set window size and position
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(800, 600, ImGuiCond.Always);

        // draw window
        ImGui.pushFont(this.menuFont);  // set window font
        // ImGui.begin("main menu window", NoBackground | NoDecoration);  // for release
        ImGui.begin("main menu window", NoCollapse | NoTitleBar | NoResize | NoMove | NoBringToFrontOnFocus);  // for debug

        // draw buttons
        ImGui.pushStyleVar(ImGuiStyleVar.ButtonTextAlign, 0.50f, 0.49f);

        ImGui.setCursorPos(80, 20); boolean tutorial = ImGui.button("Tutorial", 172, 32);
        ImGui.setCursorPos(80, 60); boolean campaign = ImGui.button("Campaign", 172, 32);
        ImGui.setCursorPos(80, 100); boolean missionCdCampaign = ImGui.button("Mission CD Campaign", 172, 32);
        ImGui.setCursorPos(80, 140); boolean amazonCampaign = ImGui.button("Amazon Campaign", 172, 32);
        ImGui.setCursorPos(80, 180); boolean campaignNormal = ImGui.button("Campaign: Normal", 172, 32);
        ImGui.setCursorPos(80, 220); boolean singlePlayerScenario = ImGui.button("Single Player: Scenario", 172, 32);
        ImGui.setCursorPos(80, 260); boolean multiplayerLan = ImGui.button("Multi-player Game: LAN", 172, 32);
        ImGui.setCursorPos(80, 300); boolean multiplayerInternet = ImGui.button("Multi-player Game: Internet", 172, 32);
        ImGui.setCursorPos(80, 340); boolean loadGame = ImGui.button("Load Game", 172, 32);
        ImGui.setCursorPos(80, 400); boolean onlineHelp = ImGui.button("Online Help", 172, 32);
        ImGui.setCursorPos(80, 440); boolean tipsTricks = ImGui.button("Tips & Tricks", 172, 32);
        ImGui.setCursorPos(80, 480); boolean credits = ImGui.button("Credits", 172, 32);
        ImGui.setCursorPos(80, 540); boolean exitGame = ImGui.button("Exit Game", 172, 32);

        ImGui.popStyleVar();

        // set button actions
        if (tutorial) {
            System.out.printf("tutorial clicked\n");
        }

        if (campaign) {
            System.out.printf("campaign clicked\n");
        }

        // draw text labels
        ImGui.setCursorPos(34, 577); ImGui.textColored(248, 220, 0, 255, "Version 1.60");

        ImGui.end();
        ImGui.popFont();  // pop font

        // draw debug info
        ImGui.begin("debug info");
        ImGui.text("window pos %d %d".formatted((int) ImGui.getWindowPos().x, (int) ImGui.getWindowPos().y));
        ImGui.text("window width %d".formatted(screenWidth));
        ImGui.text("window height %d".formatted(screenHeight));
        ImGui.text("cursor x %d".formatted(mouseX));
        ImGui.text("cursor y %d".formatted(mouseY));
        ImGui.text("canvas cursor x %d".formatted((int) canvasCursorX));
        ImGui.text("canvas cursor y %d".formatted((int) canvasCursorY));
        ImGui.text("LMB down %s".formatted(input.getMouseDown(ImGuiMouseButton.Left)));
        // ImGui.text("input queue size %d", event_queue_size);
        ImGui.end();

        ImGui.render();
        this.openglBackend.renderDrawData(ImGui.getDrawData());

        return;
    }


    public void cleanup() {

        this.openglBackend.shutdown();
        this.glfwBackend.shutdown();

        ImGui.destroyContext();

        return;
    }
}