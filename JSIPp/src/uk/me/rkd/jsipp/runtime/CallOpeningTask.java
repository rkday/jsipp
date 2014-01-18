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
	
	public CallOpeningTask(Scenario scenario, SocketManager socketManager) {
		this.scenario = scenario;
		this.start = System.currentTimeMillis();
		this.socketManager = socketManager;
		// TODO Auto-generated constructor stub
	}
	
	public synchronized void stop() {
		this.handle.cancel();
	}
	
	@Override
	public synchronized void run(Timeout timeout) throws Exception {
		long runtime = System.currentTimeMillis() - this.start;
		long expectedCalls = runtime * 2;
		long callstoStart = expectedCalls - this.callNum;
		this.handle = timeout;
		timeout.timer().newTimeout(this, 50, TimeUnit.MILLISECONDS);
		for (int i = 0; i < callstoStart; i++) {
			Call call = new Call(this.callNum, this.scenario.phases(), this.socketManager);
			call.registerSocket();
			timeout.timer().newTimeout(call, 10, TimeUnit.MILLISECONDS);
			this.callNum += 1;
		}
	}

}
