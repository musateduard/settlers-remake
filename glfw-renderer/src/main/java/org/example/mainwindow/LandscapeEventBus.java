package org.example.mainwindow;

import java.util.Queue;
import java.util.ArrayDeque;
import jsettlers.common.map.IGraphicsBackgroundListener;


/**
 * Queues background / fog-of-war change notifications from the game thread.
 * The render thread swaps the queue out under {@link #lock} and applies events.
 */
public class LandscapeEventBus implements IGraphicsBackgroundListener {

    public Queue<LandscapeEvent> queue = new ArrayDeque<>();


    public Queue<LandscapeEvent> drainEventQueue() {

        synchronized (this) {

            Queue<LandscapeEvent> events = this.queue;
            this.queue = new ArrayDeque<>();

            return events;
        }
    }


    @Override
    public void backgroundLineChangedAt(int x, int y, int length) {

        synchronized (this) {
            this.queue.add(new BackgroundLineChanged(x, y, length));
        }

        return;
    }


    @Override
    public void fogOfWarEnabledStatusChanged(boolean enabled) {

        synchronized (this) {
            this.queue.add(new FogOfWarEnabledChanged(enabled));
        }

        return;
    }
}