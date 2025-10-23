package org.example.gamesimulation;

import org.example.gamemap.SettlersMap;


public class SettlersGame implements Runnable {

    public SettlersMap gameMap;


    public SettlersGame(SettlersMap gameMap) {

        this.gameMap = gameMap;

        /*
        game architecture

        game app starts
        - creates game map instance
        - game app is the render thread
        - creates game simulation thread
        - game simulation operates on game map
        - game map is game state
        - render thread (the app) reads the game map and renders accordingly

        rendering happens in layers
        - render terrain
        - render static objects
        - render non static non player objects (trees, materials on ground, rocks, waves, mine signs etc)
        - render all player objects
        - render ui

        ui is modelled as a class that holds a value for current type of
        menu to render and a class that models that type of menu
        each menu class holds state of currently visible elements
        */

        // start new game thread with simulation
        // apply state changes to game map
        // run user tasks from the ui
        // generate ai tasks

        return;
    }


    @Override
    public void run() {

        while (this.gameMap.gameOver == false) {

            try {
                System.out.printf("calculating game state\n");
                Thread.sleep(1000);
            }

            catch (InterruptedException exception) {
                System.out.printf("keyboard interrupt detected\n");
                break;
            }
        }

        return;
    }
}