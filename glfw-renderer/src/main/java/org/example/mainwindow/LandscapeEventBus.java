package org.example.mainwindow;

import java.util.Queue;
import java.util.ArrayDeque;
import java.util.concurrent.locks.ReentrantLock;
import jsettlers.common.map.IGraphicsBackgroundListener;


/**
 * Queues background / fog-of-war change notifications from the game thread.
 * The render thread swaps the queue out under {@link #lock} and applies events.
 */
public class LandscapeEventBus implements IGraphicsBackgroundListener {

    public sealed interface LandscapeEvent permits BackgroundLineChanged, FogOfWarEnabledChanged {}

    public record BackgroundLineChanged(
        int x,
        int y,
        int length
    ) implements LandscapeEvent {}


    public record FogOfWarEnabledChanged(
        boolean enabled
    ) implements LandscapeEvent {}


    public final ReentrantLock lock = new ReentrantLock();
    public Queue<LandscapeEvent> queue = new ArrayDeque<>();


    @Override
    public void backgroundLineChangedAt(int x, int y, int length) {

        this.lock.lock();

        try {
            this.queue.add(new BackgroundLineChanged(x, y, length));
        }

        finally {
            this.lock.unlock();
        }

        return;
    }


    @Override
    public void fogOfWarEnabledStatusChanged(boolean enabled) {

        this.lock.lock();

        try {
            this.queue.add(new FogOfWarEnabledChanged(enabled));
        }

        finally {
            this.lock.unlock();
        }

        return;
    }
}