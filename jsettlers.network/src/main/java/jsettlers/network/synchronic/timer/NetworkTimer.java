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
package jsettlers.network.synchronic.timer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import jsettlers.network.NetworkConstants;
import jsettlers.network.client.INetworkClientClock;
import jsettlers.network.client.task.packets.SyncTasksPacket;
import jsettlers.network.client.task.packets.TaskPacket;


/**
 * This is a basic game timer. All synchronous actions must be based on this clock. The {@link NetworkTimer} also triggers the execution of synchronous tasks in the network game.
 *
 * @author Andreas Eberle
 *
 */
public final class NetworkTimer extends TimerTask implements INetworkClientClock {

	public static final short TIME_SLICE = 50;
	private static final Comparator<SyncTasksPacket> tasksByTimeComparator = Comparator.comparingInt(SyncTasksPacket::getLockstepNumber);

	private final Timer timer;
	private final Object lockstepLock = new Object();

	private final List<ScheduledTimerable> timerables = new ArrayList<>();
	private final List<ScheduledTimerable> newTimerables = new LinkedList<>();
	private final List<INetworkTimerable> timerablesToBeRemoved = new LinkedList<>();

	private final LinkedList<SyncTasksPacket> tasks = new LinkedList<>();

	private int time = 0;
	private int maxAllowedLockstep = -1;

	private boolean isPausing;
	private int pauseTime;
	private float speedFactor = 1.0f;
	private float progress = 0.0f;

	private boolean scheduled = false;

	private ITaskExecutor taskExecutor;
	private DataOutputStream replayLogStream;


	public NetworkTimer() {
		this.timer = new Timer("NetworkTimer");
        return;
	}


	public NetworkTimer(boolean disableLockstepWaiting) {

        this();

		if (disableLockstepWaiting) {
			this.maxAllowedLockstep = Integer.MAX_VALUE;
		}

        return;
	}


    @Override
    public synchronized void startExecution() {

        if (this.scheduled == false) {
            this.scheduled = true;
            this.timer.schedule(this, 0, NetworkTimer.TIME_SLICE);
        }

        return;
    }


    @Override
    public void stopExecution() {

        this.setPausing(true);
        this.timer.cancel();
        this.closeReplayLogStreamIfNeeded();

        return;
    }


    @Override
    public void run() {

        if (this.isPausing == true) {
            return;
        }

        // this is used for synchronizing the network clients
        if (this.pauseTime <= 0) {

            this.progress += this.speedFactor;

            while (this.progress >= 1) {
                this.executeRun();
                this.progress--;
            }
        }

        else {
            this.pauseTime -= NetworkTimer.TIME_SLICE;
        }

        return;
    }


    private synchronized void executeRun() {

        try {

            this.time += NetworkTimer.TIME_SLICE;
            final int lockstep = this.time / NetworkConstants.Client.LOCKSTEP_PERIOD;

            // check if the lockstep is allowed
            synchronized (this.lockstepLock) {
                while (lockstep > this.maxAllowedLockstep) {
                    System.out.println("WAITING for lockstep!");
                    this.lockstepLock.wait();
                }
            }

            SyncTasksPacket tasksPacket;
            synchronized (this.tasks) {
                tasksPacket = this.tasks.peekFirst();
            }

            while (tasksPacket != null && tasksPacket.getLockstepNumber() <= lockstep) {

                assert tasksPacket.getLockstepNumber() == lockstep : "FOUND TasksPacket FOR older lockstep!";

                System.out.printf("Executing SyncTaskPacket %s in %s\n", tasksPacket, this.getLockstepText(lockstep));

                try {
                    this.executeTasksPacket(tasksPacket);
                }

                catch (Throwable exception) {
                    System.err.println("Error during execution of scheduled task:");
                    exception.printStackTrace();
                }

                // remove the executed tasksPacket and retrieve the next one to check it.
                synchronized (this.tasks) {
                    this.tasks.pollFirst();
                    tasksPacket = this.tasks.peekFirst();
                }
            }

            this.addNewTimerables();
            this.handleRemovedTimerables();

            for (ScheduledTimerable curr : this.timerables) {
                curr.checkExecution(NetworkTimer.TIME_SLICE);
            }
        }

        catch (Throwable exception) {
            System.err.println("WARNING: Networking Timer caught Throwable!!!");
            exception.printStackTrace();
        }

        return;
    }


    private void executeTasksPacket(SyncTasksPacket tasksPacket) {

        if (this.taskExecutor != null) {
            for (TaskPacket currTask : tasksPacket.getTasks()) {
                this.taskExecutor.executeTask(currTask);
            }
        }

        else {
            System.err.println("couldn't execute task, due to missing taskExecutor!");
        }

        return;
    }


	private void addNewTimerables() {
		synchronized (newTimerables) {
			timerables.addAll(newTimerables);
			newTimerables.clear();
		}
	}

	private void handleRemovedTimerables() {
		synchronized (timerablesToBeRemoved) {
			for (INetworkTimerable currToBeRemoved : timerablesToBeRemoved) {
				for (Iterator<ScheduledTimerable> iter = timerables.iterator(); iter.hasNext();) {
					if (iter.next().getTimerable() == currToBeRemoved) {
						iter.remove();
						break;
					}
				}
				System.err.println("tried to remove a object from timer that's not registered!");
			}
			timerablesToBeRemoved.clear();
		}
	}

	/**
	 * Schedules the given {@link INetworkTimerable} with given delay. The internal delay of NetworkTimer is {@value #TIME_SLICE}, but you may choose smaller delays for the {@link INetworkTimerable}.
	 * The NetworkTimer will then call the {@link INetworkTimerable} multiple times on each internal tick in the exact rate to ensure the given delay in the long run.
	 *
	 * @param timerable
	 *            {@link INetworkTimerable} to be scheduled.
	 * @param period
	 *            delay of the given {@link INetworkTimerable}.
	 */
	@Override
	public void schedule(INetworkTimerable timerable, short period) {
		synchronized (newTimerables) {
			newTimerables.add(new ScheduledTimerable(timerable, period));
		}
	}

	/**
	 * removes an INetworkTimerable from the list of scheduled tasks.
	 *
	 * @param timerable
	 */
	@Override
	public void remove(INetworkTimerable timerable) {
		synchronized (timerablesToBeRemoved) {
			timerablesToBeRemoved.add(timerable);
		}
	}

	/**
	 * Goes 60 * 1000 milliseconds forward as fast as possible
	 */
	@Override
	public synchronized void fastForward() {
		this.setPausing(true);

		final int runs = 60 * 1000 / TIME_SLICE;
		for (int i = 0; i < runs; i++) {
			executeRun();
		}

		this.setPausing(false);
	}

	@Override
	public synchronized void fastForwardTo(int targetGameTime) {
		this.setPausing(true);

		System.out.println("Playing game forward to game time: " + targetGameTime);

		while (time < targetGameTime) {
			executeRun();
		}
	}

	// methods for pausing

	@Override
	public void setPausing(boolean pausing) {
		this.isPausing = pausing;
	}

	@Override
	public void invertPausing() {
		this.isPausing = !this.isPausing;
	}

	@Override
	public boolean isPausing() {
		return isPausing;
	}

	@Override
	public void pauseClockFor(int timeDelta) {
		this.pauseTime = timeDelta;
		System.err.println("pausing for " + timeDelta + " ms");
	}

	@Override
	public void setGameSpeed(float speedFactor) {
		this.speedFactor = speedFactor;
	}


	public float getGameSpeed() {
		return speedFactor;
	}

	@Override
	public void multiplyGameSpeed(float factor) {
		this.speedFactor *= factor;
	}

	@Override
	public void setTaskExecutor(ITaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}


    @Override
    public void scheduleSyncTasksPacket(SyncTasksPacket tasksPacket) {

        assert
            this.maxAllowedLockstep == Integer.MAX_VALUE ||
            this.maxAllowedLockstep + 1 == tasksPacket.getLockstepNumber() :
            "received unlock for wrong step! current max allowed: %d new: %d".formatted(this.maxAllowedLockstep, tasksPacket.getLockstepNumber());

        if (tasksPacket.getTasks().isEmpty() == false) {

            synchronized (this.tasks) {

                System.out.println("Scheduled SyncTasksPacket %s for %s".formatted(tasksPacket, this.getLockstepText(tasksPacket.getLockstepNumber())));

                this.tasks.addLast(tasksPacket);
                this.tasks.sort(NetworkTimer.tasksByTimeComparator);
                this.saveReplayIfNeeded(tasksPacket);
            }
        }

        this.maxAllowedLockstep = Math.max(this.maxAllowedLockstep, tasksPacket.getLockstepNumber());

        synchronized (this.lockstepLock) {
            this.lockstepLock.notifyAll();
        }

        return;
    }


	private void saveReplayIfNeeded(SyncTasksPacket tasksPacket) {
		if (replayLogStream != null) {
			try {
				tasksPacket.serialize(replayLogStream);
				replayLogStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setTime(int newTime) {
		this.time = newTime;
	}

	@Override
	public int getTime() {
		return time;
	}

	@Override
	public void setReplayLogStream(DataOutputStream replayFileStream) {
		if (this.replayLogStream != null) {
			throw new IllegalStateException("Replay log stream cannot be set twice!");
		}

		if (replayFileStream != null) {
			replayLogStream = replayFileStream;
		} else {
			closeReplayLogStreamIfNeeded();
		}
	}

	@Override
	public synchronized void saveRemainingTasks(DataOutputStream dos) throws IOException {
		for (SyncTasksPacket task : tasks) {
			task.serialize(dos);
		}
		dos.flush();
	}

	private void closeReplayLogStreamIfNeeded() {
		if (replayLogStream != null) {
			try {
				replayLogStream.flush();
				replayLogStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				replayLogStream = null;
			}
		}
	}

	@Override
	public void loadReplayLogFromStream(DataInputStream dataInputStream) {
		try {
			while (true) {
				SyncTasksPacket currPacket = new SyncTasksPacket();
				currPacket.deserialize(dataInputStream);
				scheduleSyncTasksPacket(currPacket);
			}
		} catch (IOException e1) { // something went wrong, or the stream was empty
			try {
				if (dataInputStream.read() == -1) {
					System.out.println("Successfully loaded jsettlers.integration.replay file.");
				} else {
					System.out.println("Error loading jsettlers.integration.replay file.");
					e1.printStackTrace();
				}
			} catch (IOException e2) {
				System.out.println("Error loading jsettlers.integration.replay file.");
				e1.printStackTrace();
				e2.printStackTrace();
			}
		}
	}

	private String getLockstepText(int lockstep) {
		int time = lockstep * NetworkConstants.Client.LOCKSTEP_PERIOD;
		int hours = time / (1000 * 60 * 60);
		int minutes = (time / (1000 * 60)) % 60;
		int seconds = (time / 1000) % 60;
		int millis = time % 1000;
		return String.format(Locale.ENGLISH, "lockstep: %d (game time: %dms / %02d:%02d:%02d:%03d)", lockstep, time, hours, minutes, seconds, millis);
	}
}
