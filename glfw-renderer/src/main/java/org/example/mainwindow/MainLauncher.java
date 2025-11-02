package org.example.mainwindow;

import org.example.gamemap.SettlersMap;
import org.example.gamesimulation.SettlersGame;


public class MainLauncher {

    public static final long GAME_START_TIME_MS = System.currentTimeMillis();


    public static void main(String[] args) throws Exception {

        EventManager eventManager = new EventManager();
        Window window = new Window(eventManager);
        Renderer renderer = new Renderer(window.width, window.height);
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

            // render game state
            renderer.renderMapTerrain(gameMap);
            renderer.renderStaticObjects();
            renderer.renderNonStaticObjects();
            renderer.renderTeamObjects();
            renderer.renderGui();

            // swap buffers
            window.swapBuffers();

            continue;
        }

        gameMap.gameOver = true;
        gameSimulation.running = false;
        gameThread.join();

        System.out.printf("closing game\n");

        window.cleanup();

        return;
    }
}