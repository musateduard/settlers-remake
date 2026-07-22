package org.example.mainwindow;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import jsettlers.common.map.IGraphicsBackgroundListener;


/**
 * Queues background / fog-of-war change notifications from the game thread.
 * The render thread drains and applies them while holding {@link #lock}.
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
    private final ArrayList<LandscapeEvent> queue = new ArrayList<>();


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


    /**
     * Moves all pending events into {@code out} and clears the queue.
     * Caller must already hold {@link #lock}.
     */
    public void drainTo(List<LandscapeEvent> out) {

        if (this.lock.isHeldByCurrentThread() == false) {
            throw new IllegalStateException("drainTo requires the event-bus lock");
        }

        out.addAll(this.queue);
        this.queue.clear();

        return;
    }
}