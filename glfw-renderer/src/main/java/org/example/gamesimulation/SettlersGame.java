package org.example.gamesimulation;

import org.example.gamemap.SettlersMap;


public class SettlersGame implements Runnable {

    static class Task {

        public Task() {
            return;
        }


        public void execute() {
            return;
        }
    }


    static class TaskPacket {

        public TaskPacket() {
            // task packet contains all tasks
            // do i need separate task packet? can i just store tasks in list and append them?
            return;
        }
    }


    public static final int LOCKSTEP_DURATION_MS = 100;
    public static final int TICK_DURATION_MS = 50;

    public boolean running;
    public boolean inputReady;
    public long accumulatedTime;
    public long nextLoopTime;
    public long currentLockstep;
    public long lastLockstep;
    public TaskPacket[] taskPacketList;

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
        this.taskPacketList = new TaskPacket[10];

        // todo: implement lockstep buffer
        // note: lockstep buffer is a circular buffer where each slot contains tasks for that specific lockstep

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

        /*
        conceptual game loop

        starts at lockstep 0
        clients compose packets for lockstep 0, 1, 2 immediately
        server receives all packets for 0, 1, 2
        server sends all inputs back to everyone for 0, 1, 2
        clients then run simulation based on all inputs for next locksteps
        while running simulation clients also compose packets for following locksteps
        as server receives packets it stores them in a ring buffer where each slot corresponds to a lockstep
        when server reaches a lockstep it reads all inputs packets for that lockstep and executes
        whenever server receives packets for a lockstep it sends all relevant packets back to all clients
        clients store packets in their input buffers and execute them whenever they reach that specific lockstep
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
        in single player a task packet is generated synthetically for the current lockstep

        in multiplayer clients issue tasks that are stored locally in a buffer
        these tasks then get sent to the server for the corresponding future lockstep

        for ex.:
        game starts at lockstep 0
        - clients issue packets for locksteps 0, 1, 2
        - server receives packets for lockstep 0, 1, 2
        - server sends back all inputs to all players as confirmation for locksteps 0, 1, 2
        - clients then start issuing tasks regularly for the corresponding future lockstep

        server and client needs a catchup mechanism for when client can't reach server
        for a period of time and needs to send certain nr of accumulated packets
        client should also compose and send packets for future lockstep in advance
        this is possible because all tasks get scheduled to a future lockstep by default
        this means that at any given lockstep n we either already know the tasks associated
        with packets n, n+1, n+2, or there are no tasks and we can send empty packets for
        those specific locksteps already

        note:

        server needs to store all future packets in a circular buffer and access them
        whenever it arrives at that specific lockstep
        we also need to make sure that server doesn't overwrite packets in the circular buffer
        that haven't been processed yet.

        for ex.:
        if server has a buffer of 10 slots and a client sends future packets worth of
        12 locksteps this means that server will write 12 slots worth of packets for 12 locksteps
        this means that after writing the first 10 packets for the next 10 future locksteps
        the packets that were written in slots 0 and 1 will be overwritten by packets scheduled
        for locksteps 11 and 12
        */

        /*
        in single player this function only retrieves all user input from gui
        and sets it as input for the scheduled lockstep
        in single player it doesn't poll any sockets for network input
        if player didn't submit any input it only generates an empty task packet which
        gets stored in the assigned lockstep in the ring buffer
        */

        /*
        if single player
            get input from gui
            generate task packet based on input

        else
            // send input
            get input from gui
            generate task packet
            send packet to server

            // receive input
            poll network for task packets
            store any task packets in relevant buffer slot
        */

        return;
    }


    /**
     * this function runs in a separate thread and is responsible for running the game simulation loop.
     * the main loop runs in 2 intervals: {@link #TICK_DURATION_MS} interval and {@link #LOCKSTEP_DURATION_MS} interval
     * the loop iterates every 50ms and every 100ms it executes a lockstep.
     * the 50ms interval is used so that the network can poll twice for every lockstep interval. this gives
     * the network the opportunity to recover network packets more frequently.
     *
     * @return {@code void}
     */
    @Override
    public void run() {

        // initialize nextLoopTime to current time so that when loop starts it will be scheduled at +50ms
        this.nextLoopTime = System.currentTimeMillis();

        while (this.running) {

            // calculate current lockstep
            this.nextLoopTime += SettlersGame.TICK_DURATION_MS;
            this.currentLockstep = this.accumulatedTime / SettlersGame.LOCKSTEP_DURATION_MS;

            System.out.printf("current lockstep: %d\n", this.currentLockstep);
            System.out.printf("next: %d\n", this.nextLoopTime);

            // execute tasks for current lockstep
            if (this.currentLockstep > this.lastLockstep) {

                System.out.printf("executing tasks for lockstep %d\n", this.currentLockstep);

                /*
                int slotIndex = this.currentLockstep % this.taskPacketList.size();
                for (Task item : this.taskPacketList[slotIndex]) {
                    item.execute();
                }
                */
            }

            // get tasks for next lockstep
            this.pollPackets();

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

            // adjust nextLoopTime if loop took longer than TICK_DURATION_MS to execute
            // this ensures that the game still runs in fixed 50ms intervals even
            // when a loop iteration took longer to complete
            else if (waitTime < -SettlersGame.TICK_DURATION_MS) {
                this.nextLoopTime = currentTime + SettlersGame.TICK_DURATION_MS;
            }

            this.accumulatedTime += SettlersGame.TICK_DURATION_MS;
            this.lastLockstep = this.currentLockstep;
            continue;
        }

        return;
    }
}