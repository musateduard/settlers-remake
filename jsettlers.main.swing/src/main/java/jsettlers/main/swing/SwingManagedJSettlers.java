/*******************************************************************************
 * Copyright (c) 2015 - 2018
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.main.swing;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jsettlers.common.CommonConstants;
import jsettlers.common.menu.IMapInterfaceConnector;
import jsettlers.common.menu.IStartedGame;
import jsettlers.common.menu.IStartingGame;
import jsettlers.common.resources.ResourceManager;
import jsettlers.graphics.localization.AbstractLabels;
import jsettlers.graphics.localization.Labels;
import jsettlers.logic.constants.MatchConstants;
import jsettlers.logic.map.loading.MapLoadException;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.loading.list.DirectoryMapLister;
import jsettlers.logic.map.loading.newmap.MapFileHeader;
import jsettlers.logic.movable.civilian.BuildingWorkerMovable;
import jsettlers.logic.movable.other.AttackableHumanMovable;
import jsettlers.logic.player.InitialGameState;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.main.JSettlersGame;
import jsettlers.main.ReplayStartInformation;
import jsettlers.main.replay.ReplayUtils;
import jsettlers.main.swing.foldertree.SelectSettlersFolderDialog;
import jsettlers.main.swing.lookandfeel.JSettlersLookAndFeel;
import jsettlers.main.swing.lookandfeel.JSettlersLookAndFeelExecption;
import jsettlers.main.swing.resources.SwingResourceLoader;
import jsettlers.main.swing.resources.SwingResourceLoader.ResourceSetupException;
import jsettlers.main.swing.resources.SwingResourceProvider;
import jsettlers.main.swing.settings.SettingsManager;
import jsettlers.network.client.OfflineNetworkConnector;


/**
 * @author codingberlin
 * @author Andreas Eberle
 */
public class SwingManagedJSettlers {

	static {
		CommonConstants.USE_SAVEGAME_COMPRESSION = true;
	}


	public static void main(String[] args) throws IOException, MapLoadException, JSettlersLookAndFeelExecption {

  		// removes screen flickering
		System.setProperty("sun.awt.noerasebackground", "true");

		SwingManagedJSettlers.setupResources(true, args);

		JSettlersFrame settlersFrame = SwingManagedJSettlers.createJSettlersFrame();
        SwingManagedJSettlers.handleStartOptions(settlersFrame);

        return;
	}


	public static void setupResources(boolean interactive, String... args) throws IOException {

        ResourceManager.setProvider(new SwingResourceProvider());
		SettingsManager.setup(args);

		SwingManagedJSettlers.loadCommandLineSettings();
        SwingManagedJSettlers.setupResourceManagers(interactive);

        return;
	}


	private static void loadCommandLineSettings() {

		CommonConstants.CONTROL_ALL = SettingsManager.getInstance().isControllAll();
		CommonConstants.ACTIVATE_ALL_PLAYERS = SettingsManager.getInstance().isActivateAllPlayers();
		CommonConstants.ENABLE_CONSOLE_LOGGING = SettingsManager.getInstance().useConsoleOutput();
		CommonConstants.DISABLE_ORIGINAL_MAPS = SettingsManager.getInstance().areOriginalMapsDisabled();
		AbstractLabels.setPreferredLocale(SettingsManager.getInstance().getLocale());

        return;
	}


	/**
	 * Sets up the {@link ResourceManager} by using a configuration file. <br>
	 * First it is checked, if the given argsMap contains a "configFile" parameter. If so, the path specified for this parameter is used to get the file. <br>
	 * If the parameter is not given, the defaultConfigFile is used.
	 */
	private static void setupResourceManagers(boolean interactive) {

		boolean wasSuccessful = false;
		boolean firstRun = true;

		while (!wasSuccessful) {

			try {
				SwingResourceLoader.setup();
				wasSuccessful = true;
			}

            catch (ResourceSetupException exception) {

                // ask the user if we should continue...
				if (interactive) {
					askUserToSetResources(firstRun);
				}

                else {
					throw new RuntimeException(exception);
				}
			}

			firstRun = false;
		}

        return;
	}


	private static void askUserToSetResources(boolean firstRun) {

		if (!firstRun) {
			JOptionPane.showMessageDialog(null, Labels.getString("settlers-folder-still-invalid"));
		}

		File selectedFolder = SelectSettlersFolderDialog.askForFolder();

		if (selectedFolder == null) {
			String noFolderSelectedMessage = Labels.getString("error-no-settlers-3-folder-selected");
			JOptionPane.showMessageDialog(null, noFolderSelectedMessage);
			System.err.println(noFolderSelectedMessage);
			System.exit(1);
		}

		System.out.println(selectedFolder);
		SettingsManager.getInstance().setSettlersFolder(selectedFolder);

        return;
	}


	private static void handleStartOptions(JSettlersFrame settlersFrame) throws MapLoadException {

		long randomSeed = SettingsManager.getInstance().getRandom().orElse(0L);

		ReplayUtils.ReplayFile loadableReplayFile = SettingsManager.getInstance().getReplayFile().map(

            (loadableReplayFileString) -> {

                File replayFile = new File(loadableReplayFileString);

                if (replayFile.exists()) {
                    System.out.println("Found loadable jsettlers.integration.replay file and loading it: " + replayFile);
                    return new ReplayUtils.ReplayFile(replayFile);
                }

                else {
                    System.err.println("Found replayFile parameter, but file can not be found!");
                    return null;
                }
            }

        ).orElse(null);

		int targetGameTime = SettingsManager.getInstance().getTargetTimeMinutes().orElse(0);

		String mapFile = SettingsManager.getInstance().getMapFile();

		if (mapFile != null || loadableReplayFile != null) {

			IStartingGame game;

			if (loadableReplayFile == null) {

				MapLoader mapLoader = MapLoader.getLoaderForListedMap(new DirectoryMapLister.ListedMapFile(new File(mapFile)));

				if (mapLoader.getFileHeader().getType() == MapFileHeader.MapType.NORMAL) {

					byte playerId = 0;
					PlayerSetting[] playerSettings = PlayerSetting.createDefaultSettings(playerId, (byte) mapLoader.getMaxPlayers());

					InitialGameState initialGameState = new InitialGameState(playerId, playerSettings, randomSeed);
					game = new JSettlersGame(mapLoader, initialGameState).start();
				}

                else {
					MapFileHeader header = mapLoader.getFileHeader();
					InitialGameState initialGameState = new InitialGameState(header.getPlayerId(), header.getPlayerSettings(), randomSeed);
					game = new JSettlersGame(mapLoader, initialGameState).start();
				}
			}

            else {
				game = JSettlersGame.loadFromReplayFile(loadableReplayFile, new OfflineNetworkConnector(), new ReplayStartInformation()).start();
			}

			settlersFrame.showStartingGamePanel(game);

			if (targetGameTime > 0) {

				while (!game.isStartupFinished()) {

					try {
						Thread.sleep(100L);
					}

                    catch (InterruptedException exception) {
                        // do nothing
					}
				}

				MatchConstants.clock().fastForwardTo(targetGameTime * 60 * 1000);
			}
		}

        return;
	}


	public static IMapInterfaceConnector showJSettlers(IStartedGame startedGame) throws JSettlersLookAndFeelExecption {
		JSettlersFrame jSettlersFrame = SwingManagedJSettlers.createJSettlersFrame();
		return jSettlersFrame.showStartedGame(startedGame);
	}


	private static JSettlersFrame createJSettlersFrame() throws JSettlersLookAndFeelExecption {
		JSettlersLookAndFeel.install();
        JSettlersFrame frame = new JSettlersFrame();
        return frame;
	}
}