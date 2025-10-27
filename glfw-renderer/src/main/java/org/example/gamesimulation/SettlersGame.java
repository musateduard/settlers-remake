package org.example.gamesimulation;

import org.example.gamemap.SettlersMap;


public class SettlersGame implements Runnable {

    public static final int LOCKSTEP_DURATION_MS = 100;
    public static final int TIME_SLICE_MS = 50;

    public SettlersMap gameMap;
    public boolean running;
    public long accumulatedTime;
    public long currentLockstep;
    public long lastTime;
    public long currentTime;
    public long nextTime;
    public boolean inputReady;


    public SettlersGame(SettlersMap gameMap) {

        /*
        todo: implement deterministic lockstep simulation
        todo: implement state buffer so that game thread can write to a state and render thread can read from a state

        note: state in this context means the map and all entities in the game world
        note: swap buffers when new state is available
        note: should we still use objects for game state or just use entities?
        */

        this.gameMap = gameMap;
        this.running = true;
        this.accumulatedTime = 0;
        this.currentLockstep = 0;
        this.currentTime = 0;
        this.lastTime = 0;
        this.nextTime = 0;
        this.inputReady = false;

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


    public void executeTasks() {

        System.out.printf("executing tasks\n");

        /*
        for item in task_queue:

            if item.scheduled_lockstep <= current_lockstep:
                item.execute()

            else:
                continue
        */

        return;
    }


    @Override
    public void run() {

        while (this.running) {

            this.currentTime = System.currentTimeMillis();
            this.currentLockstep = this.accumulatedTime / SettlersGame.LOCKSTEP_DURATION_MS;

            // reschedule loop in the future and execute tasks now
            if (this.nextTime <= this.currentTime) {
                this.nextTime = this.currentTime + SettlersGame.TIME_SLICE_MS;
            }

            // wait until next loop is scheduled and then execute tasks
            else {
                try {
                    Thread.sleep(this.nextTime - this.currentTime);
                }

                catch (InterruptedException exception) {
                    // do nothing yet
                }
            }

            this.executeTasks();

            this.accumulatedTime += SettlersGame.TIME_SLICE_MS;
            continue;
        }

        return;
    }
}