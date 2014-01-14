package uk.me.rkd.jsipp.runtime;

import java.util.concurrent.TimeUnit;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;


public class Scheduler {

	private HashedWheelTimer timer;
	
	public Scheduler(long milliseconds) {
		this.timer = new HashedWheelTimer(milliseconds, TimeUnit.MILLISECONDS);
		// TODO Auto-generated constructor stub
	}
	
	public void stop() {
		this.timer.stop();
	}
	
	public Timeout add(TimerTask task, long when) {
		return this.timer.newTimeout(task, when, TimeUnit.MILLISECONDS);
	}

}
