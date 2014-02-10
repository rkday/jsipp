package uk.me.rkd.jsipp.runtime;

import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import uk.me.rkd.jsipp.compiler.Scenario;
import uk.me.rkd.jsipp.runtime.network.SocketManager;

public class CallOpeningTask implements TimerTask {

	private final Scenario scenario;
	private final SocketManager socketManager;
	private int callNum = 0;
	private Timer handle;
	private long start;
	private double rate;
	private boolean finished = false;
	private Map<String, String> globalVariables;
	private static CallOpeningTask INSTANCE;

	public static CallOpeningTask getInstance(Scenario scenario, SocketManager socketManager, double rate, Timer timer,
	                                          Map<String, String> globalVariables) {
		if (INSTANCE == null) {
			INSTANCE = new CallOpeningTask(scenario, socketManager, rate, timer, globalVariables);
		}
		return INSTANCE;
	}

	public static CallOpeningTask getInstance() {
		return INSTANCE;
	}

	private CallOpeningTask(Scenario scenario, SocketManager socketManager, double rate, Timer timer,
	                        Map<String, String> globalVariables) {
		this.scenario = scenario;
		this.start = System.currentTimeMillis();
		this.socketManager = socketManager;
		this.rate = rate;
		this.globalVariables = globalVariables;
		this.handle = timer;
		// TODO Auto-generated constructor stub
	}

	public synchronized void stop() {
		this.finished = true;
	}

	@Override
	public synchronized void run(Timeout timeout) {
		if (finished) {
			return;
		}
		try {
			long runtime = System.currentTimeMillis() - this.start;
			double callsPerMs = (this.rate / 1000.0);
			double msPerCall = 1 / callsPerMs;
			long expectedCalls = (long) (runtime * callsPerMs);
			long callstoStart = expectedCalls - this.callNum;
			timeout.timer().newTimeout(this, (long) msPerCall, TimeUnit.MILLISECONDS);
			for (int i = 0; i < callstoStart; i++) {
				Call call = new Call(this.callNum, Integer.toString(this.callNum), this.scenario.phases(),
				        this.socketManager, this.globalVariables);
				call.registerSocket();
				timeout.timer().newTimeout(call, 10, TimeUnit.MILLISECONDS);
				this.callNum += 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized Call newUAS(String callId) {
		Call call = new Call(this.callNum, callId, this.scenario.phases(), this.socketManager, this.globalVariables);
		this.handle.newTimeout(call, 10, TimeUnit.MILLISECONDS);
		this.callNum += 1;
		return call;
	}
}
