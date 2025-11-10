package org.example.mainwindow;

import imgui.ImGui;
import imgui.ImFont;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiStyleVar;
import imgui.glfw.ImGuiImplGlfw;
import imgui.gl3.ImGuiImplGl3;

import static imgui.flag.ImGuiCol.Text;
import static imgui.flag.ImGuiCol.Button;
import static imgui.flag.ImGuiCol.WindowBg;
import static imgui.flag.ImGuiCol.ButtonActive;
import static imgui.flag.ImGuiCol.ButtonHovered;
import static imgui.flag.ImGuiWindowFlags.NoMove;
import static imgui.flag.ImGuiWindowFlags.NoResize;
import static imgui.flag.ImGuiWindowFlags.NoCollapse;
import static imgui.flag.ImGuiWindowFlags.NoTitleBar;
import static imgui.flag.ImGuiWindowFlags.NoBringToFrontOnFocus;
import static org.lwjgl.system.MemoryUtil.NULL;
import static imgui.flag.ImGuiCond.Always;


public class GuiRenderer {

    public final double idealAspectRatio;
    public final ImGuiImplGlfw imguiGlfw;
    public final ImGuiImplGl3 imguiOpengl;
    public final ImFont menuFont;


    public GuiRenderer(Window window, Renderer renderer) {

        this.idealAspectRatio = 4.00f / 3.00f;

        if (window.windowId == NULL) {
            throw new RuntimeException("cannot initialize hui renderer before creating glfw window");
        }

        // create imgui context
        ImGui.createContext();

        // disable ini file
        ImGuiIO imguiSettings = ImGui.getIO();

        imguiSettings.setIniFilename(null);
        imguiSettings.setLogFilename(null);

        // load font files
        this.menuFont = ImGui.getIO().getFonts().addFontFromFileTTF("D:\\fonts\\ms-sans-serif-1.ttf", 12.00f);

        // init all backends
        this.imguiGlfw = new ImGuiImplGlfw();
        this.imguiOpengl = new ImGuiImplGl3();

        this.imguiGlfw.init(window.windowId, true);
        this.imguiOpengl.init(renderer.glslVersion);

        return;
    }


    public void renderGuiStack(int width, int height, Window window, Renderer renderer) {

        this.imguiGlfw.newFrame();
        this.imguiOpengl.newFrame();
        ImGui.newFrame();

        // set window transparency
        ImGuiStyle style = ImGui.getStyle();

        style.setColor(WindowBg, 0, 0, 0, 50);
        style.setColor(Button,        144, 128, 56, 255);
        style.setColor(ButtonActive,  132, 117, 49, 255);
        style.setColor(ButtonHovered, 138, 123, 52, 255);

        // set text color
        style.setColor(Text, 0, 12, 64, 255);

        // set window size and position
        final double currentAspectRatio = (float) width / height;
        ImGui.setNextWindowPos(0, window.height - 600, Always);
        ImGui.setNextWindowSize(800, 600, Always);

        /*
        note:

        gui size should be fixed to 800 x 600 and instead drawn to an internal frame buffer object
        the frame buffer object then gets drawn to the screen with the correct position and aspect ratio
        */

        /*
        // calculate based on height
        if (currentAspectRatio >= this.idealAspectRatio) {

            int menuWidth = (int) (height * this.idealAspectRatio);
            int menuHeight = height;
            int menuX = (width - menuWidth) / 2;
            int menuY = 0;

            ImGui.setNextWindowPos(menuX, menuY, ImGuiCond.Always);
            ImGui.setNextWindowSize(menuWidth, menuHeight, ImGuiCond.Always);
        }

        // calculate based on width
        else {

            int menuWidth = width;
            int menuHeight = (int) (width / this.idealAspectRatio);
            int menuX = 0;
            int menuY = (height - menuHeight) / 2;

            ImGui.setNextWindowPos(menuX, menuY, ImGuiCond.Always);
            ImGui.setNextWindowSize(menuWidth, menuHeight, ImGuiCond.Always);
        }
        */

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
        ImGui.text("window width %d".formatted(window.width));
        ImGui.text("window height %d".formatted(window.height));
        ImGui.end();

        ImGui.render();
        this.imguiOpengl.renderDrawData(ImGui.getDrawData());

        return;
    }


    public void cleanup() {

        this.imguiOpengl.shutdown();
        this.imguiGlfw.shutdown();

        ImGui.destroyContext();

        return;
    }
}