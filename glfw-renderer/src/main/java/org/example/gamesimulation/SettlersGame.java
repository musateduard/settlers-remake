package org.example.gamesimulation;

import org.example.gamemap.SettlersMap;


public class SettlersGame implements Runnable {

    public static final int LOCKSTEP_DURATION_MS = 100;
    public static final int TIME_SLICE_MS = 50;

    public boolean running;
    public boolean inputReady;
    public long accumulatedTime;
    public long nextLoopTime;
    public long currentLockstep;
    public long lastLockstep;

    public SettlersMap gameMap;


    public SettlersGame(SettlersMap gameMap) {

        /*
        todo: implement deterministic lockstep simulation
        todo: implement state buffer so that game thread can write to a state and render thread can read from a state
        todo: implement circular buffer for specific lockstep with all tasks gathered in that slot

        note: state in this context means the map and all entities in the game world
        note: swap buffers when new state is available
        note: should we still use objects for game state or just use entities?
        */

        /*
        todo: this class needs a way to receive task packets
        todo: we also need a way to generate tasks based on user input

        task packets are synchronization packets that are received for every lockstep
        they are required to signal the game that the simulation has advanced to that particular lockstep
        task packets may or may not contain tasks but they are required for advancing the simulation
        */

        this.gameMap = gameMap;
        this.running = true;
        this.inputReady = false;
        this.accumulatedTime = 0;
        this.nextLoopTime = 0;
        this.currentLockstep = 0;
        this.lastLockstep = 0;

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


    public void pollPackets() {

        /*
        poll network for task packets
        if packets are available then generate task packet and advance maxAllowedLockstep
        otherwise wait for next packet

        note:

        a task packet is needed regardless of user input in order to advance lockstep
        in networked play we also need all current player task packets to arrive before advancing the lockstep
        */

        return;
    }


    public void executeTasks() {

        System.out.printf("executing tasks for lockstep %d\n", this.currentLockstep);

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

        // initialize nextLoopTime to current time so that inside the loop it's always scheduled at +50ms
        this.nextLoopTime = System.currentTimeMillis();

        while (this.running) {

            this.nextLoopTime += SettlersGame.TIME_SLICE_MS;
            this.currentLockstep = this.accumulatedTime / SettlersGame.LOCKSTEP_DURATION_MS;

            System.out.printf("current lockstep: %d\n", this.currentLockstep);
            System.out.printf("next: %d\n", this.nextLoopTime);

            this.pollPackets();

            if (this.currentLockstep > this.lastLockstep) {
                this.executeTasks();
            }

            long currentTime = System.currentTimeMillis();
            long waitTime = this.nextLoopTime - currentTime;

            // wait until next loop is scheduled
            if (waitTime > 0) {
                try {
                    Thread.sleep(waitTime);
                }

                catch (InterruptedException exception) {
                    // do nothing yet
                }
            }

            /*
            adjust nextLoopTime if loop took longer than TIME_SLICE_MS to execute
            this ensures that the game still runs in fixed 50ms intervals even
            when a loop iteration took longer to complete
            */
            else if (waitTime < -SettlersGame.TIME_SLICE_MS) {
                this.nextLoopTime = currentTime + SettlersGame.TIME_SLICE_MS;
            }

            this.accumulatedTime += SettlersGame.TIME_SLICE_MS;
            this.lastLockstep = this.currentLockstep;
            continue;
        }

        return;
    }
}