package org.example.mainwindow;

import java.util.Stack;
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
        Renderer renderer = new Renderer(window, eventManager);
        GuiRenderer guiRenderer = new GuiRenderer(window, renderer);
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

            // activate frame buffer
            // note: frame buffer needs to be active for the entire render pipeline (this means both scene and ui)
            renderer.activateFrameBuffer();

            // clear screen
            renderer.clearScreen();

            // render game scene
            renderer.renderGameScene(frameTimeDeltaNs, window, camera, gameMap);

            // render ui stack
            // note: ui stack has variable size and position depending on game state: main menu or in game
            guiRenderer.renderGuiStack(800, 600, window, renderer);

            renderer.activateMainBuffer(window.width, window.height);

            // swap buffers
            window.swapBuffers();

            continue;
        }

        gameMap.gameOver = true;
        gameSimulation.running = false;
        gameThread.join();

        System.out.printf("closing game\n");

        guiRenderer.cleanup();
        renderer.cleanup();
        window.cleanup();

        return;
    }
}