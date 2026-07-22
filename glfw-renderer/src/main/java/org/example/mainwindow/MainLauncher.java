package org.example.mainwindow;

import java.io.File;
import go.graphics.swing.opengl.LWJGLDrawContext;
import go.graphics.swing.sound.SwingSoundPlayer;
import jsettlers.common.ai.EPlayerType;
import jsettlers.common.menu.IStartingGame;
import jsettlers.common.player.ECivilisation;
import jsettlers.common.resources.ResourceManager;
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

        final Application application = new Application();
        final UserInterface userInterface = new UserInterface(application.window, application.renderer);
        final Camera camera = new Camera();

        // create map instance
        // SettlersMap newGameMap = new SettlersMap();  // deprecated

        ResourceManager.setProvider(new SwingResourceProvider());
        SettingsManager.setup();
        SettingsManager.getInstance().setSettlersFolder(new File("C:\\games\\Settlers 3 Ultimate"));
        ImageProvider.setLookupPath(new File("C:\\games\\Settlers 3 Ultimate\\GFX"), "745006780412758287");
        SoundManager.setLookupPath(new File("C:\\games\\Settlers 3 Ultimate\\SND"));

        final byte playerId = 0;
        final long randomSeed = System.currentTimeMillis();

        final PlayerSetting[] playerSettings = {
            new PlayerSetting(true, EPlayerType.HUMAN, ECivilisation.ROMAN, (byte) 0),
            new PlayerSetting(true, EPlayerType.AI_HARD, ECivilisation.ASIAN, (byte) 1)
        };

        final File file = new File("C:\\games\\Settlers 3 Ultimate\\Map\\User\\384-2-Brueckenkopf.map");
        final MapLoader selectedMap = MapLoader.getLoaderForListedMap(new DirectoryMapLister.ListedMapFile(file));
        final InitialGameState initialGameState = new InitialGameState(playerId, playerSettings, randomSeed, EMapStartResources.MEDIUM_GOODS);

        // this is the original jsettlers initialization sequence
        final SwingSoundPlayer soundPlayer = new SwingSoundPlayer(SettingsManager.getInstance());
        final JSettlersGame game = new JSettlersGame(selectedMap, initialGameState);
        final IStartingGame startingGame = game.start();
        final GLFWStartingGameListener startingListener = new GLFWStartingGameListener(soundPlayer);
        startingGame.setListener(startingListener);

        while (startingGame.isStartupFinished() == false) {
            Thread.sleep(100);
        }

        final MapContent mapContent = startingListener.getMap();
        final LandscapeTexture landscape = new LandscapeTexture(
            application.canvas,
            mapContent.map.getWidth(),
            mapContent.map.getHeight()
        );

        final LandscapeEventBus landscapeEventBus = new LandscapeEventBus();
        final LandscapeMeshUpdater landscapeMeshUpdater = new LandscapeMeshUpdater(landscape, mapContent.map);
        mapContent.map.setBackgroundListener(landscapeEventBus);
        camera.offsetX = -mapContent.mapContext.getScreen().getScreenCenterX();
        camera.offsetY = -mapContent.mapContext.getScreen().getScreenCenterY();

        final GLFWGOEventConverter eventConverter = new GLFWGOEventConverter(mapContent);
        final LWJGLDrawContext context = new LWJGLDrawContext(application.renderer.capabilities, true, 1.00f);
        context.updateProjectionMatrix(application.canvas.width, application.canvas.height);

        // start game thread
        // SettlersGame gameSimulation = new SettlersGame(newGameMap);
        // Thread gameThread = new Thread(gameSimulation, "GameSimulationThread");
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
            InputSystem.handleInput(application, userInterface, camera, eventConverter, mapContent);

            // run game simulation
            // dispatch game events

            RenderingSystem.drawFrame(
                frameDuration,
                application,
                userInterface,
                camera,
                landscape,
                landscapeEventBus,
                landscapeMeshUpdater,
                context,
                mapContent
            );

            continue;
        }

        // newGameMap.gameOver = true;
        // gameSimulation.running = false;
        // gameThread.join();

        System.out.printf("closing game\n");

        userInterface.cleanup();
        application.cleanup();

        return;
    }
}