package uk.me.rkd.jsipp.runtime;

import java.util.concurrent.TimeUnit;

import uk.me.rkd.jsipp.compiler.Scenario;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

public class CallOpeningTask implements TimerTask {

	private final Scenario scenario;
	private final SocketManager socketManager;
	private int callNum = 0;
	private Timeout handle;
	private long start;
	private double rate;
	
	public CallOpeningTask(Scenario scenario, SocketManager socketManager, double rate) {
		this.scenario = scenario;
		this.start = System.currentTimeMillis();
		this.socketManager = socketManager;
		this.rate = rate;
		// TODO Auto-generated constructor stub
	}
	
	public synchronized void stop() {
		this.handle.cancel();
	}
	
	@Override
	public synchronized void run(Timeout timeout) throws Exception {
		long runtime = System.currentTimeMillis() - this.start;
		double msPerCall = (this.rate / 1000.0);
		long expectedCalls = (long) (runtime * msPerCall);
		long callstoStart = expectedCalls - this.callNum;
		this.handle = timeout;
		timeout.timer().newTimeout(this, (long) msPerCall, TimeUnit.MILLISECONDS);
		for (int i = 0; i < callstoStart; i++) {
			Call call = new Call(this.callNum, this.scenario.phases(), this.socketManager);
			call.registerSocket();
			timeout.timer().newTimeout(call, 10, TimeUnit.MILLISECONDS);
			this.callNum += 1;
		}
	}

}
