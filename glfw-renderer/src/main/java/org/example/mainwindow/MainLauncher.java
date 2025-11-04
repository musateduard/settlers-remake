package org.example.mainwindow;

import imgui.ImGui;
import java.util.Stack;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.example.gamemap.SettlersMap;
import org.example.gamesimulation.SettlersGame;


class UiLayer {

    public UiLayer() {

        // contains all ui elements for current overlay

        return;
    }
}


class UiStack {

    public final Stack<UiLayer> layerList;


    public UiStack() {

        // contains methods to navigate from one menu to another
        // this means destructing/constructing new ui stacks

        this.layerList = new Stack<>();

        // construct to main menu
        // add background image
        // add buttons

        return;
    }
}


class UiRenderer {

    public final ImGuiImplGlfw imGuiGlfw;
    public final ImGuiImplGl3 imGuiGl3;


    public UiRenderer(Window window, Renderer renderer) {

        // create imgui context
        ImGui.createContext();

        this.imGuiGlfw = new ImGuiImplGlfw();
        this.imGuiGl3 = new ImGuiImplGl3();

        this.imGuiGlfw.init(window.windowId, true);
        this.imGuiGl3.init(renderer.glslVersion);

        return;
    }


    public void cleanup() {

        this.imGuiGlfw.shutdown();
        this.imGuiGl3.shutdown();

        ImGui.destroyContext();

        return;
    }
}


public class MainLauncher {

    public static final long GAME_START_TIME_MS = System.currentTimeMillis();


    public static void main(String[] args) throws Exception {

        /*
        note:

        window and renderer are too loosely coupled
        in current setup renderer has hidden dependency on window constructing before itself
        this is problematic because renderer and window cannot act independently of one another
        imgui is also closely coupled with opengl renderer and cannot construct before renderer constructed

        todo: pass window as argument to renderer constructor
        todo: pass renderer as argument to gui constructor
        */

        EventManager eventManager = new EventManager();
        Window window = new Window(eventManager);
        Renderer renderer = new Renderer(window);
        UiRenderer uiRenderer = new UiRenderer(window, renderer);
        Camera camera = new Camera(eventManager);

        // create map instance
        SettlersMap gameMap = new SettlersMap();

        // start game thread
        SettlersGame gameSimulation = new SettlersGame(gameMap);
        Thread gameThread = new Thread(gameSimulation, "GameSimulationThread");

        // gameThread.start();

        // start rendering
        // this.renderBuildingFrame();
        // this.renderGameFrame();
        // this.renderTestFrame();

        long startFrameTimeNs = System.nanoTime();

        while (window.shouldClose() == false) {

            // - calculate frame time
            // - get all input
            // - calculate camera position
            // - calculate game state
            // - render game based on last_state and current_state
            //
            // note:
            //
            // when using separate game thread simulation you need a state buffer
            // this means all state objects are being calculated inside the game thread
            // and the render thread keeps a copy of all game objects for rendering
            // when the game thread finishes running a new state it swaps its state buffer
            // with the renderer's buffer
            //
            // this way the render thread always has a state ready to render and
            // the game thread doesn't need to lock the state list
            //
            // todo: implement this method using just the map terrain first, then add other types of objects to simulation/rendering

            // calculate frame time
            long endFrameTimeNs = System.nanoTime();
            long frameTimeDeltaNs = endFrameTimeNs - startFrameTimeNs;
            startFrameTimeNs = endFrameTimeNs;

            // poll events
            window.pollEvents();

            // update camera position
            camera.updateCameraPosition(frameTimeDeltaNs);
            renderer.updateViewMatrix(camera);

            // clear screen
            renderer.clearScreen();

            // render game scene
            renderer.renderMapTerrain(gameMap);
            renderer.renderStaticObjects();
            renderer.renderNonStaticObjects();
            renderer.renderTeamObjects();

            // render ui
            uiRenderer.imGuiGl3.newFrame();
            uiRenderer.imGuiGlfw.newFrame();
            ImGui.newFrame();

            ImGui.begin("title1");
            ImGui.button("button1");
            ImGui.end();

            ImGui.render();
            uiRenderer.imGuiGl3.renderDrawData(ImGui.getDrawData());
            renderer.renderGui();

            // swap buffers
            window.swapBuffers();

            continue;
        }

        gameMap.gameOver = true;
        gameSimulation.running = false;
        gameThread.join();

        System.out.printf("closing game\n");

        uiRenderer.cleanup();
        window.cleanup();

        return;
    }
}