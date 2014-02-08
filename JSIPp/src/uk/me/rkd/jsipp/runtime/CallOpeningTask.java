package uk.me.rkd.jsipp.runtime;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

import uk.me.rkd.jsipp.compiler.Scenario;
import uk.me.rkd.jsipp.runtime.network.SocketManager;

public class CallOpeningTask implements TimerTask {

	private final Scenario scenario;
	private final SocketManager socketManager;
	private int callNum = 0;
	private Timeout handle;
	private long start;
	private double rate;
	private boolean finished = false;

	public CallOpeningTask(Scenario scenario, SocketManager socketManager, double rate) {
		this.scenario = scenario;
		this.start = System.currentTimeMillis();
		this.socketManager = socketManager;
		this.rate = rate;
		// TODO Auto-generated constructor stub
	}

	public synchronized void stop() {
		this.finished = true;
	}

	@Override
	public synchronized void run(Timeout timeout) throws Exception {
		if (finished) {
			return;
		}
		try {
			long runtime = System.currentTimeMillis() - this.start;
			double callsPerMs = (this.rate / 1000.0);
			double msPerCall = 1 / callsPerMs;
			long expectedCalls = (long) (runtime * callsPerMs);
			long callstoStart = expectedCalls - this.callNum;
			this.handle = timeout;
			timeout.timer().newTimeout(this, (long) msPerCall, TimeUnit.MILLISECONDS);
			for (int i = 0; i < callstoStart; i++) {
				Call call = new Call(this.callNum, this.scenario.phases(), this.socketManager);
				call.registerSocket();
				timeout.timer().newTimeout(call, 10, TimeUnit.MILLISECONDS);
				this.callNum += 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
