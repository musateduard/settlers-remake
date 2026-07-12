package org.example.mainwindow;

import go.graphics.swing.opengl.LWJGLDrawContext;
import org.example.gamemap.SettlersMap;
import org.example.gamesimulation.SettlersGame;


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

        Application application = new Application();
        GuiRenderer userInterface = new GuiRenderer(application.window, application.renderer);
        Camera camera = new Camera(application.eventManager);

        // create map instance
        SettlersMap gameMap = new SettlersMap();

        // start game thread
        SettlersGame gameSimulation = new SettlersGame(gameMap);
        Thread gameThread = new Thread(gameSimulation, "GameSimulationThread");
        LWJGLDrawContext context = new LWJGLDrawContext(application.renderer.capabilities, true, 1.00f);

        // gameThread.start();

        // start rendering
        // this.renderBuildingFrame();
        // this.renderGameFrame();
        // this.renderTestFrame();

        long lastFrameTime = System.nanoTime();

        while (application.shouldClose() == false) {

            /*
            - calculate frame time
            - get all input
            - calculate camera position
            - calculate game state
            - render game based on last_state and current_state

            note:

            when using separate game thread simulation you need a state buffer
            this means all state objects are being calculated inside the game thread
            and the render thread keeps a copy of all game objects for rendering
            when the game thread finishes running a new state it swaps its state buffer
            with the renderer's buffer

            this way the render thread always has a state ready to render and
            the game thread doesn't need to lock the state list

            todo: implement this method using just the map terrain first, then add other types of objects to simulation/rendering
            */

            // calculate frame duration
            long currentFrameTime = System.nanoTime();
            long frameDuration = currentFrameTime - lastFrameTime;
            lastFrameTime = currentFrameTime;

            // handle input
            InputSystem.handleInput(application);

            // run game simulation
            // dispatch game events

            RenderingSystem.drawFrame(frameDuration, application, userInterface, camera);

            continue;
        }

        gameMap.gameOver = true;
        gameSimulation.running = false;
        gameThread.join();

        System.out.printf("closing game\n");

        userInterface.cleanup();
        application.cleanup();

        return;
    }
}