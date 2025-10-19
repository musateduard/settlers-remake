/*******************************************************************************
 * Copyright (c) 2015 - 2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import java.util.function.Consumer;

import jsettlers.ai.highlevel.AiExecutor;
import jsettlers.common.CommitInfo;
import jsettlers.common.CommonConstants;
import jsettlers.common.logging.MultiplexingOutputStream;
import jsettlers.common.map.IGraphicsGrid;
import jsettlers.common.menu.EGameError;
import jsettlers.common.menu.EProgressState;
import jsettlers.common.menu.IMapInterfaceConnector;
import jsettlers.common.menu.IStartedGame;
import jsettlers.common.menu.IStartingGame;
import jsettlers.common.menu.IStartingGameListener;
import jsettlers.common.player.IInGamePlayer;
import jsettlers.common.resources.ResourceManager;
import jsettlers.common.statistics.IGameTimeProvider;
import jsettlers.input.GuiInterface;
import jsettlers.input.IGameStoppable;
import jsettlers.input.PlayerState;
import jsettlers.logic.buildings.Building;
import jsettlers.logic.buildings.trading.HarborBuilding;
import jsettlers.logic.buildings.trading.MarketBuilding;
import jsettlers.logic.constants.MatchConstants;
import jsettlers.logic.map.grid.MainGrid;
import jsettlers.logic.map.loading.IGameCreator;
import jsettlers.logic.map.loading.IGameCreator.MainGridWithUiSettings;
import jsettlers.logic.map.loading.MapLoadException;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.movable.MovableManager;
import jsettlers.logic.player.InitialGameState;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.logic.timer.RescheduleTimer;
import jsettlers.main.replay.ReplayUtils;
import jsettlers.network.client.OfflineNetworkConnector;
import jsettlers.network.client.interfaces.INetworkConnector;

/**
 * This class can start a Thread that loads and sets up a game and wait's for its termination.
 *
 * @author Andreas Eberle
 */
public class JSettlersGame {

	private static final SimpleDateFormat LOG_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
	private final Object stopMutex = new Object();

	private final IGameCreator mapCreator;
	private final INetworkConnector networkConnector;
	private final boolean multiplayer;
	private final DataInputStream replayFileInputStream;

	private final GameRunner gameRunner;
	private final InitialGameState initialGameState;

	private boolean started = false;
	private boolean stopped = false;
	private boolean shutdownFinished;

	private PrintStream systemErrorStream;
	private PrintStream systemOutStream;


	private JSettlersGame(
        IGameCreator mapCreator,
        INetworkConnector networkConnector,
        InitialGameState initialGameState,
        boolean controlAll,
        boolean multiplayer,
        DataInputStream replayFileInputStream) {

		this.configureLogging(mapCreator);

		this.initialGameState = initialGameState;

        System.out.printf("OS version: %s %s %s\n", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"));
        System.out.printf("Java version: %s %s\n", System.getProperty("java.vendor"), System.getProperty("java.version"));
        System.out.printf("JSettlers version: %s %s\n", CommitInfo.COMMIT_HASH_SHORT, CommitInfo.BUILD_TIME);
        System.out.printf("JSettlersGame(): initialGameState: %s multiplayer: %s mapCreator: %s\n", initialGameState, multiplayer, mapCreator);

		if (mapCreator == null) {
			throw new IllegalArgumentException("No mapCreator provided (mapCreator == null).");
		}

		this.mapCreator = mapCreator;
		this.networkConnector = networkConnector;
		this.multiplayer = multiplayer;
		this.replayFileInputStream = replayFileInputStream;

		MatchConstants.ENABLE_ALL_PLAYER_FOG_OF_WAR = controlAll;
		MatchConstants.ENABLE_ALL_PLAYER_SELECTION = controlAll;
		MatchConstants.ENABLE_FOG_OF_WAR_DISABLING = controlAll;
		MatchConstants.ENABLE_DEBUG_COLORS = controlAll;

		this.gameRunner = new GameRunner();
        return;
	}


	/**
	 * @param mapCreator
	 * @param networkConnector
	 */
	public JSettlersGame(IGameCreator mapCreator, INetworkConnector networkConnector, InitialGameState initialGameState) {
		this(mapCreator, networkConnector, initialGameState, CommonConstants.CONTROL_ALL, true, null);
        return;
	}


	/**
	 * Creates a new {@link JSettlersGame} object with an {@link OfflineNetworkConnector}.
	 *
	 * @param mapCreator
	 */
	public JSettlersGame(IGameCreator mapCreator, InitialGameState initialGameState) {
		this(mapCreator, new OfflineNetworkConnector(), initialGameState, CommonConstants.CONTROL_ALL, false, null);
        return;
	}


	public static JSettlersGame loadFromReplayFile(
        ReplayUtils.IReplayStreamProvider loadableReplayFile,
        INetworkConnector networkConnector,
        ReplayStartInformation replayStartInformation) throws MapLoadException {

		try {
			DataInputStream replayFileInputStream = new DataInputStream(loadableReplayFile.openStream());
			replayStartInformation.deserialize(replayFileInputStream);

			MapLoader mapCreator = loadableReplayFile.getMap(replayStartInformation);
			return new JSettlersGame(mapCreator, networkConnector, replayStartInformation.getReplayableGameState(), true, false, replayFileInputStream);
		}

        catch (IOException exception) {
			throw new MapLoadException("Could not deserialize " + loadableReplayFile, exception);
		}
	}


	/**
	 * Starts the game in a new thread. Returns immediately.
	 *
	 * @return {@link IStartedGame}
	 */
	public synchronized IStartingGame start() {

		if (!this.started) {
			this.started = true;
			new Thread(null, this.gameRunner, "GameThread", 16 * 1024 * 1024).start();
		}

		return this.gameRunner;
	}


	public void stop() {

		synchronized (this.stopMutex) {
			this.stopped = true;
			this.stopMutex.notifyAll();
		}

        return;
	}


	protected OutputStream createReplayWriteStream() throws IOException {
		final String replayFilename = getLogFile(this.mapCreator, "_replay.log");
		return ResourceManager.writeUserFile(replayFilename);
	}


	public class GameRunner implements Runnable, IStartingGame, IStartedGame, IGameStoppable {

		private IStartingGameListener startingGameListener;
		private MainGrid mainGrid;
		private GameTimeProvider gameTimeProvider;
		private EProgressState progressState;
		private float progress;
		private Consumer<IStartedGame> exitListener;
		private boolean gameRunning;
		private AiExecutor aiExecutor;


		@Override
		public void run() {

			try {
				if (this.startingGameListener != null) {
					this.startingGameListener.startingLoadingGame();
				}

				this.updateProgressListener(EProgressState.LOADING, 0.1f);

                JSettlersGame.clearState();
				MatchConstants.init(networkConnector.getGameClock(), initialGameState.getRandomSeed());

				try {
					MatchConstants.clock().setReplayLogStream(createReplayFileStream());
				}

                catch (IOException exception) {
					// TODO: log that we do not have write access to resources.
					System.out.println("Cannot write jsettlers.integration.replay file.");
				}

				this.updateProgressListener(EProgressState.LOADING_MAP, 0.3f);

				MainGridWithUiSettings gridWithUiState = mapCreator.loadMainGrid(initialGameState.getPlayerSettings(), initialGameState.getStartResources());
				PlayerState playerState = gridWithUiState.getPlayerState(initialGameState.getPlayerId());
				this.mainGrid = gridWithUiState.getMainGrid();

				RescheduleTimer.schedule(MatchConstants.clock()); // schedule timer

				this.updateProgressListener(EProgressState.LOADING_IMAGES, 0.7f);
				this.gameTimeProvider = new GameTimeProvider(MatchConstants.clock());

				this.mainGrid.initForPlayer(initialGameState.getPlayerId(), playerState.getFogOfWar());
				this.mainGrid.startThreads();

                this.waitForStartingGameListener();
                this.startingGameListener.waitForPreloading();

                this.updateProgressListener(EProgressState.WAITING_FOR_OTHER_PLAYERS, 0.98f);

				if (replayFileInputStream != null) {
					MatchConstants.clock().loadReplayLogFromStream(replayFileInputStream);
				}

				networkConnector.setStartFinished(true);
				this.waitForAllPlayersStartFinished(networkConnector);

				final IMapInterfaceConnector connector = startingGameListener.preLoadFinished(this);

                GuiInterface guiInterface = new GuiInterface(
                    connector, MatchConstants.clock(),
                    networkConnector.getTaskScheduler(),
                    this.mainGrid.getGuiInputGrid(), this,
                    initialGameState.getPlayerId(), multiplayer
                );

                /*
                note:

                This is required after the GuiInterface instantiation so that
				ConstructionMarksThread has it's mapArea variable initialized via the EActionType.SCREEN_CHANGE event.
                */
				connector.loadUIState(playerState.getUiState());

				this.aiExecutor = new AiExecutor(initialGameState.getPlayerSettings(), this.mainGrid, networkConnector.getTaskScheduler());
				networkConnector.getGameClock().schedule(this.aiExecutor, (short) 1000);

				MatchConstants.clock().startExecution(); // WARNING: GAME CLOCK IS STARTED!
				// NO CONFIGURATION AFTER THIS POINT! =================================

                this.gameRunning = true;
                this.startingGameListener.startFinished();

				synchronized (stopMutex) {

					while (!stopped) {

						try {
							stopMutex.wait();
						}

                        catch (InterruptedException exception) {
                            // do nothing
						}
					}
				}

				networkConnector.shutdown();
                this.mainGrid.stopThreads();
				connector.shutdown();
				guiInterface.stop();
				JSettlersGame.clearState();

				System.setErr(systemErrorStream);
				System.setOut(systemOutStream);

			}

            catch (MapLoadException exception) {
				exception.printStackTrace();
				reportFail(EGameError.MAPLOADING_ERROR, exception);
			}

            catch (Exception exception) {
				exception.printStackTrace();
				reportFail(EGameError.UNKNOWN_ERROR, exception);
			}

            finally {
				shutdownFinished = true;
				if (this.exitListener != null) {
					this.exitListener.accept(this);
				}
			}

            return;
		}


		public AiExecutor getAiExecutor() {
			return this.aiExecutor;
		}


		private DataOutputStream createReplayFileStream() throws IOException {

			DataOutputStream replayFileStream = new DataOutputStream(createReplayWriteStream());

			ReplayStartInformation replayInfo = new ReplayStartInformation(mapCreator.getMapName(), mapCreator.getMapId(), initialGameState);
			replayInfo.serialize(replayFileStream);
			replayFileStream.flush();

			return replayFileStream;
		}


		/**
		 * Waits until the {@link #startingGameListener} has been set.
		 */
		private void waitForStartingGameListener() {

			while (this.startingGameListener == null) {

				try {
					Thread.sleep(5L);
				}

                catch (InterruptedException exception) {
                    // do nothing
				}
			}

            return;
		}


		private void waitForAllPlayersStartFinished(INetworkConnector networkConnector) {

			while (!networkConnector.haveAllPlayersStartFinished()) {

				try {
					Thread.sleep(5L);
				}

                catch (InterruptedException exception) {
                    // do nothing
				}
			}

            return;
		}


		private void updateProgressListener(EProgressState progressState, float progress) {

			this.progressState = progressState;
			this.progress = progress;

			if (this.startingGameListener != null) {
				this.startingGameListener.startProgressChanged(progressState, progress);
			}

            return;
		}


		private void reportFail(EGameError gameError, Exception exception) {

			if (this.startingGameListener != null) {
                this.startingGameListener.startFailed(gameError, exception);
            }

            return;
		}


		// METHODS of IStartingGame
		// ====================================================
		@Override
		public void setListener(IStartingGameListener startingGameListener) {

			this.startingGameListener = startingGameListener;
			if (startingGameListener != null) {
                startingGameListener.startProgressChanged(progressState, progress);
            }

            return;
		}


		// METHODS of IStartedGame
		// ======================================================
		@Override
		public IGraphicsGrid getMap() {
			return this.mainGrid.getGraphicsGrid();
		}


		@Override
		public IGameTimeProvider getGameTimeProvider() {
			return this.gameTimeProvider;
		}


		@Override
		public IInGamePlayer getInGamePlayer() {
			return this.mainGrid.getPartitionsGrid().getPlayer(initialGameState.getPlayerId());
		}


		@Override
		public IInGamePlayer[] getAllInGamePlayers() {
			return this.mainGrid.getPartitionsGrid().getPlayers();
		}


		@Override
		public boolean isShutdownFinished() {
			return shutdownFinished;
		}


		@Override
		public boolean isMultiplayerGame() {
			return multiplayer;
		}


		@Override
		public void stopGame() {
			stop();
            return;
		}


		@Override
		public void setGameExitListener(Consumer<IStartedGame> exitListener) {
			this.exitListener = exitListener;
            return;
		}


		@Override
		public boolean isStartupFinished() {
			return this.gameRunning;
		}


		public MainGrid getMainGrid() {
			return this.mainGrid;
		}
	}


	private void configureLogging(final IGameCreator mapcreator) {

		try {
			this.systemErrorStream = System.err;
			this.systemOutStream = System.out;

			OutputStream logStream;
			OutputStream errStream;
			OutputStream logFileStream = ResourceManager.writeUserFile(getLogFile(mapcreator, "_out.log"));

			if (CommonConstants.ENABLE_CONSOLE_LOGGING) {
				logStream = new MultiplexingOutputStream(System.out, logFileStream);
				errStream = new MultiplexingOutputStream(System.err, logFileStream);
			}

            else {
				logStream = logFileStream;
				errStream = logFileStream;
			}

			System.setOut(new PrintStream(logStream));
			System.setErr(new PrintStream(errStream));
		}

        catch (IOException ex) {
			throw new RuntimeException("Error setting up logging.", ex);
		}

        return;
	}


	private static String getLogFile(IGameCreator mapcreator, String suffix) {

		final String dateAndMap = getLogDateFormatter().format(new Date()) + "_" + mapcreator.getMapName().replace(" ", "_");
		final String logFolder = "logs/" + dateAndMap + "/";

		return logFolder + dateAndMap + suffix;
	}


	private static DateFormat getLogDateFormatter() {
		return JSettlersGame.LOG_DATE_FORMATTER;
	}


	public static void clearState() {

		RescheduleTimer.stopAndClear();
		MovableManager.resetState();
		Building.clearState();
		MatchConstants.clearState();

        return;
	}
}