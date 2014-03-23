package uk.me.rkd.jsipp.runtime;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

public class Scheduler {

	private HashedWheelTimer timer;

	public Scheduler(long milliseconds) {
		this.timer = new HashedWheelTimer(milliseconds, TimeUnit.MILLISECONDS);
		this.timer.start();
	}

	public Timer getTimer() {
		return this.timer;
	}

	public void stop() {
		this.timer.stop();
	}

	public Timeout add(TimerTask task, long when) {
		return this.timer.newTimeout(task, when, TimeUnit.MILLISECONDS);
	}

}
