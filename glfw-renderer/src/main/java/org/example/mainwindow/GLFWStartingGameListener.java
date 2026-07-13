package org.example.mainwindow;

import go.graphics.swing.sound.SwingSoundPlayer;
import jsettlers.common.menu.EGameError;
import jsettlers.common.menu.EProgressState;
import jsettlers.common.menu.IMapInterfaceConnector;
import jsettlers.common.menu.IStartedGame;
import jsettlers.common.menu.IStartingGameListener;
import jsettlers.graphics.map.ETextDrawPosition;
import jsettlers.graphics.map.MapContent;
import jsettlers.graphics.map.draw.ImageProvider;


public class GLFWStartingGameListener implements IStartingGameListener {

    public SwingSoundPlayer soundPlayer;
    public MapContent map;


    public GLFWStartingGameListener(SwingSoundPlayer sound) {
        this.soundPlayer = sound;
        return;
    }


    public MapContent getMap() {
        return this.map;
    }


    @Override
    public void startProgressChanged(EProgressState state, float progress) {
        return;
    }


    @Override
    public IMapInterfaceConnector preLoadFinished(IStartedGame game) {
        this.map = new MapContent(game, this.soundPlayer, ETextDrawPosition.DESKTOP);
        game.setGameExitListener((IStartedGame exit) -> { System.out.println("exiting game"); });
        return this.map.getInterfaceConnector();
    }


    @Override
    public void startFailed(EGameError errorType, Exception exception) {
        System.err.println("failed to start game");
        return;
    }


    @Override
    public void startFinished() {
        return;
    }


    @Override
    public void startingLoadingGame() {
        ImageProvider.getInstance().startPreloading();
        return;
    }


    @Override
    public void waitForPreloading() {
        ImageProvider.getInstance().waitForPreloadingFinish();
        return;
    }
}