/*******************************************************************************
 * Copyright (c) 2015 - 2017
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
package jsettlers.main.swing;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.Timer;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import go.graphics.area.Area;
import go.graphics.region.Region;
import go.graphics.sound.SoundPlayer;
import go.graphics.swing.AreaContainer;
import go.graphics.swing.sound.SwingSoundPlayer;
import go.graphics.swing.contextcreator.EBackendType;

import jsettlers.common.CommitInfo;
import jsettlers.common.menu.IJoinPhaseMultiplayerGameConnector;
import jsettlers.common.menu.IMapInterfaceConnector;
import jsettlers.common.menu.IMultiplayerConnector;
import jsettlers.common.menu.IStartedGame;
import jsettlers.common.menu.IStartingGame;
import jsettlers.graphics.map.ETextDrawPosition;
import jsettlers.graphics.map.MapContent;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.main.swing.settings.SettingsManager;
import jsettlers.main.swing.menu.joinpanel.JoinGamePanel;
import jsettlers.main.swing.menu.mainmenu.MainMenuPanel;
import jsettlers.main.swing.menu.startinggamemenu.StartingGamePanel;
import jsettlers.main.swing.menu.statspanel.EndgameStatsPanel;
import jsettlers.main.swing.originalmenu.OriginalCampaignMenu;
import jsettlers.main.swing.originalmenu.OriginalMainMenu;


/**
 * @author codingberlin
 */
public class JSettlersFrame extends JFrame {

	private static final long serialVersionUID = 2607082717493797224L;

    private final MainMenuPanel mainPanel;

	private final EndgameStatsPanel endgameStatsPanel = new EndgameStatsPanel(this);
	private final StartingGamePanel startingGamePanel = new StartingGamePanel(this);
	private final JoinGamePanel joinGamePanel = new JoinGamePanel(this);
	private final SwingSoundPlayer soundPlayer = new SwingSoundPlayer(SettingsManager.getInstance());

	private Timer redrawTimer;
	private boolean fullScreen = false;
	private AreaContainer areaContainer;


	public JSettlersFrame() throws HeadlessException {

		this.setTitle("JSettlers - Version: " + CommitInfo.COMMIT_HASH_SHORT);
		this.setIconImage(JSettlersSwingUtil.APP_ICON);

		SettingsManager settingsManager = SettingsManager.getInstance();

		this.mainPanel = new MainMenuPanel(this);

        // jsettlers look and feel menu
		// showMainMenu();
        // setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		// setPreferredSize(new Dimension(1200, 800));
        // pack();
		// setLocationRelativeTo(null);

        // settlers 3 original menu
        this.showOriginalMainMenu();
        this.pack();
        this.setMinimumSize(this.getPreferredSize());  // note: setMinimumSize() needs to be called after pack()
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

		this.fullScreen = settingsManager.getFullScreenMode();
		this.updateFullScreenMode();

		KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

		keyboardFocusManager.addKeyEventDispatcher(
            (event) -> {

                if (event.getID() == KeyEvent.KEY_PRESSED && event.isAltDown() && event.getKeyCode() == KeyEvent.VK_ENTER) {
                    this.toggleFullScreenMode();
                    return true;  // consume this key event.
                }

                return false;
            }
        );

        return;
	}

	private void toggleFullScreenMode() {
		fullScreen = !fullScreen;
		SettingsManager.getInstance().setFullScreenMode(fullScreen);
		updateFullScreenMode();
	}

	private void updateFullScreenMode() {
		if(areaContainer != null) areaContainer.removeSurface();
		dispose();

		setResizable(!fullScreen);
		setUndecorated(fullScreen);

		pack();
		setVisible(true);

		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
		graphicsDevice.setFullScreenWindow(fullScreen ? this : null);
		if(areaContainer != null) areaContainer.notifyResize();
	}

	private void abortRedrawTimerIfPresent() {
		if (redrawTimer != null) {
			redrawTimer.stop();
			redrawTimer = null;
		}
	}

	public void showMainMenu() {
		setNewContentPane(mainPanel);
	}


    public void showOriginalMainMenu() {

        OriginalMainMenu mainMenu = new OriginalMainMenu(this);

        this.setNewContentPane(mainMenu);

        return;
    }


    public void showOriginalCampaignMenu() {

        OriginalCampaignMenu campaignMenu = new OriginalCampaignMenu(this);
        KeyboardFocusManager keyManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

        keyManager.addKeyEventDispatcher(campaignMenu.campaignMenuKeyListener);
        this.setNewContentPane(campaignMenu);

        return;
    }


	public void showStartingGamePanel(IStartingGame startingGame) {
		startingGamePanel.setStartingGame(startingGame);
		setNewContentPane(startingGamePanel);
	}


	private void setNewContentPane(Container newContent) {

        this.abortRedrawTimerIfPresent();
		this.setContentPane(newContent);
		this.revalidate();
		this.repaint();

        return;
	}


	public void exit() {
		soundPlayer.close();
		abortRedrawTimerIfPresent();
		System.exit(0);
	}

	public SoundPlayer getSoundPlayer() {
		return soundPlayer;
	}


    /**
     * this method is called from {@link JSettlersFrame#showStartedGame} after creating a new game. it receives
     * a {@link MapContent} instance which is passed to a new {@link Region} object. the Region is then passed
     * to an {@link Area} object which is then passed to a new {@link AreaContainer} instance. the AreaContainer
     * is the object that will eventually be added to the main JFrame.<br>
     *
     * note: AreaContainer is a child class of JPanel
     *
     * @see MapContent
     * @see Region
     * @see Area
     * @see AreaContainer
     *
     * @param content {@link MapContent} object used for creating new AreaContainer instance that is added to the {@link JSettlersFrame}
     */
	public void setContent(MapContent content) {

        Area area = new Area();
		Region region = new Region(500, 500);

        region.setContent(content);
		area.set(region);

        EBackendType backend = SettingsManager.getInstance().getBackend();
        boolean debugFlag = SettingsManager.getInstance().isGraphicsDebug();
        float uiScale = SettingsManager.getInstance().getGuiScale();
		int fpsLimit = SettingsManager.getInstance().getFpsLimit();
        int delay = (int) (1000.0f / fpsLimit);

        this.areaContainer = new AreaContainer(area, backend, debugFlag, uiScale);

		if (fpsLimit != 0) {
			this.redrawTimer = new Timer(delay, (event) -> region.requestRedraw());
			this.redrawTimer.setInitialDelay(0);
			this.redrawTimer.start();
		}

		SwingUtilities.invokeLater(
            () -> {
                this.setContentPane(this.areaContainer);
                this.areaContainer.updateFPSLimit(fpsLimit);
                this.revalidate();
                this.repaint();
		    }
        );
	}


	public void showNewSinglePlayerGameMenu(MapLoader mapLoader) {
		joinGamePanel.setSinglePlayerMap(mapLoader);
		setNewContentPane(joinGamePanel);
	}

	public void showNewMultiPlayerGameMenu(MapLoader mapLoader, IMultiplayerConnector connector) {
		joinGamePanel.setNewMultiPlayerMap(mapLoader, connector);
		setNewContentPane(joinGamePanel);
	}

	public void showJoinMultiplayerMenu(IJoinPhaseMultiplayerGameConnector joinPhaseMultiplayerGameConnector, MapLoader mapLoader, String playerUUID) {
		joinGamePanel.setJoinMultiPlayerMap(joinPhaseMultiplayerGameConnector, mapLoader, playerUUID);
		setNewContentPane(joinGamePanel);
	}


    /**
     * this method draws the end game statistics menu at the end of a game.
     *
     * @param game IStartedGame interface passed to setGame() method.
     */
	public void showEndgameStatistics(IStartedGame game) {

        if(this.areaContainer != null) {

			this.areaContainer.disposeAll();
			this.areaContainer = null;
		}

		this.endgameStatsPanel.setGame(game);
		this.setNewContentPane(this.endgameStatsPanel);

        return;
	}


    /**
     * this method is called when starting a new game. it creates a new instance of {@link MapContent}
     * which it passes down to {@link JSettlersFrame#setContent}. then it adds an exit game listener to the startedGame
     * argument, and it returns an {@link IMapInterfaceConnector} to its caller.
     *
     * @param startedGame IStartedGame interface used for creating new MapContent instance and receiving an exit game listener
     *
     * @return IMapInterfaceConnector corresponding to MapContent instance
     */
	public IMapInterfaceConnector showStartedGame(IStartedGame startedGame) {

		MapContent content = new MapContent(startedGame, soundPlayer, ETextDrawPosition.DESKTOP);

		SwingUtilities.invokeLater(() -> this.setContent(content));
		startedGame.setGameExitListener((exitGame) -> SwingUtilities.invokeLater(() -> this.showEndgameStatistics(exitGame)));

        IMapInterfaceConnector connector = content.getInterfaceConnector();

        return connector;
	}
}