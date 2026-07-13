package org.example.mainwindow;

import java.io.File;
import go.graphics.swing.opengl.LWJGLDrawContext;
import go.graphics.swing.sound.SwingSoundPlayer;
import jsettlers.common.ai.EPlayerType;
import jsettlers.common.menu.IStartingGame;
import jsettlers.common.player.ECivilisation;
import jsettlers.common.resources.ResourceManager;
import jsettlers.graphics.map.ETextDrawPosition;
import jsettlers.graphics.map.MapContent;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.graphics.sound.SoundManager;
import jsettlers.logic.map.loading.EMapStartResources;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.loading.list.DirectoryMapLister;
import jsettlers.logic.player.InitialGameState;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.main.JSettlersGame;
import jsettlers.main.swing.resources.SwingResourceProvider;
import jsettlers.main.swing.settings.SettingsManager;
import org.example.gamemap.SettlersMap;
import org.example.gamesimulation.JSettlersGameGLFW;
import org.example.gamesimulation.SettlersGame;
import org.example.gamesimulation.TaskExecutorGLFW;


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
        UserInterface userInterface = new UserInterface(application.window, application.renderer);
        Camera camera = new Camera();

        // create map instance
        SettlersMap newGameMap = new SettlersMap();  // deprecated

        ResourceManager.setProvider(new SwingResourceProvider());
        SettingsManager.setup();
        SettingsManager.getInstance().setSettlersFolder(new File("C:\\games\\Settlers 3 Ultimate"));
        ImageProvider.setLookupPath(new File("C:\\games\\Settlers 3 Ultimate\\GFX"), "745006780412758287");
        SoundManager.setLookupPath(new File("C:\\games\\Settlers 3 Ultimate\\SND"));

        byte playerId = 0;
        long randomSeed = System.currentTimeMillis();

        PlayerSetting[] playerSettings = {
            new PlayerSetting(true, EPlayerType.AI_HARD, ECivilisation.ROMAN, (byte) 0),
            new PlayerSetting(true, EPlayerType.AI_HARD, ECivilisation.ASIAN, (byte) 1)
        };

        File file = new File("C:\\games\\Settlers 3 Ultimate\\Map\\User\\384-2-Brueckenkopf.map");
        MapLoader selectedMap = MapLoader.getLoaderForListedMap(new DirectoryMapLister.ListedMapFile(file));
        InitialGameState initialGameState = new InitialGameState(playerId, playerSettings, randomSeed, EMapStartResources.MEDIUM_GOODS);

        /*
        note: this is the modified glfw initialization that is able to start the game

        JSettlersGameGLFW offlineGame = new JSettlersGameGLFW(selectedMap, initialGameState);
        TaskExecutorGLFW taskExecutor = new TaskExecutorGLFW();

        offlineGame.networkConnector.getGameClock().setTaskExecutor(taskExecutor);

        JSettlersGameGLFW.GameRunner runner = (JSettlersGameGLFW.GameRunner) offlineGame.start();
        SwingSoundPlayer soundPlayer = new SwingSoundPlayer(SettingsManager.getInstance());
        ImageProvider.getInstance().startPreloading();

        // note: MapContent can only be instantiated after GameRunner.mainGrid is properly loaded
        while (runner.getMainGrid() == null) {
            Thread.sleep(100);
        }

        while (runner.isStartupFinished() == false) {
            Thread.sleep(100);
        }

        ImageProvider.getInstance().waitForPreloadingFinish();
        MapContent mapContent = new MapContent(runner, soundPlayer, ETextDrawPosition.DESKTOP);
        */

        // this is the original jsettlers initialization sequence
        SwingSoundPlayer soundPlayer = new SwingSoundPlayer(SettingsManager.getInstance());
        JSettlersGame game = new JSettlersGame(selectedMap, initialGameState);
        IStartingGame startingGame = game.start();
        GLFWStartingGameListener startingListener = new GLFWStartingGameListener(soundPlayer);
        startingGame.setListener(startingListener);

        while (startingGame.isStartupFinished() == false) {
            Thread.sleep(100);
        }

        MapContent mapContent = startingListener.getMap();

        LWJGLDrawContext context = new LWJGLDrawContext(application.renderer.capabilities, true, 1.00f);
        context.updateProjectionMatrix(application.renderer.canvas.width, application.renderer.canvas.height);

        // start game thread
        SettlersGame gameSimulation = new SettlersGame(newGameMap);
        Thread gameThread = new Thread(gameSimulation, "GameSimulationThread");

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
            InputSystem.handleInput(application, camera);

            // run game simulation
            // dispatch game events

            RenderingSystem.drawFrame(frameDuration, application, userInterface, camera, newGameMap, context, mapContent);

            continue;
        }

        newGameMap.gameOver = true;
        gameSimulation.running = false;
        gameThread.join();

        System.out.printf("closing game\n");

        userInterface.cleanup();
        application.cleanup();

        return;
    }
}