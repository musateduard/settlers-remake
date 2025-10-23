package org.example.gamesimulation;

import jsettlers.network.client.task.packets.TaskPacket;
import jsettlers.network.synchronic.timer.ITaskExecutor;


public class TaskExecutorGLFW implements ITaskExecutor {

    public TaskExecutorGLFW() {
        return;
    }


    @Override
    public void executeTask(TaskPacket task) {
        System.out.printf("executing task object: %s\n", task);
        return;
    }
}