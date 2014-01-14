package uk.me.rkd.jsipp.runtime;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import uk.me.rkd.jsipp.compiler.phases.CallPhase;
import uk.me.rkd.jsipp.compiler.phases.RecvPhase;
import uk.me.rkd.jsipp.compiler.phases.SendPhase;

public class Call implements TimerTask {

	private final int callNumber;
	private int phaseIndex = 0;
	private Map<String, String> callVariables = new HashMap<String, String>();
	private List<CallPhase> phases;
	private SocketManager sm;
	private long timeoutEnds;
	private Timeout currentTimeout;
	
	public void setSocketManager(SocketManager sm) {
		this.sm = sm;
	}
	
	public Call(int callNum, List<CallPhase> phases, SocketManager sm) throws IOException {
		// TODO Auto-generated constructor stub
		//System.out.println("Call created");
		this.callNumber = callNum;
		this.callVariables.put("call_num", Integer.toString(callNum));
		this.callVariables.put("call_id", Integer.toString(callNum));
		this.phases = phases;
		this.sm = sm;
		sm.add(this);
	}

	public void end() {
		try {
			this.sm.remove(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}
	
	public void run(Timeout timeout) {
		//System.out.println("Call " + Integer.toString(this.callNumber) + " woken up");
		if (this.phaseIndex == phases.size()) {
			System.out.println("Call terminating");
			this.end();
		} else {
			CallPhase currentPhase = this.phases.get(this.phaseIndex);
			this.currentTimeout = timeout;
			if (currentPhase instanceof RecvPhase) {
				if (this.timeoutEnds == -1) {
					this.timeoutEnds = ((RecvPhase)currentPhase).timeout;
				}
				long untilTimeout = System.currentTimeMillis() - this.timeoutEnds;
				if (untilTimeout < 0) {
					System.out.println("Call timed out");
					this.end();
				} else { 
					timeout.timer().newTimeout(this, untilTimeout, TimeUnit.MILLISECONDS);
				}
			} else if (currentPhase instanceof SendPhase) {
				try {
					String message = KeywordReplacer.replaceKeywords(((SendPhase)currentPhase).message, this.callVariables, true);
					this.sm.send(this.callNumber, message);
				} catch (IOException | IllegalStateException e) {
					// TODO Auto-generated catch block
					System.out.println("Send failed");
					this.end();
				}
				this.phaseIndex += 1;
				this.run(timeout);
			}
		}
	}


	public int getNumber() {
		// TODO Auto-generated method stub
		return callNumber;
	}


	public void process_incoming(String message) {
		// TODO Auto-generated method stub
		CallPhase phase = this.phases.get(this.phaseIndex);
		if (phase instanceof RecvPhase) {
			String expected = ((RecvPhase)phase).expected;
			String firstLine = message.substring(0, message.indexOf("\n"));
			if (firstLine.contains(expected)) {
				System.out.println("Successful recv");
				this.phaseIndex += 1;
				this.timeoutEnds = -1;
				this.run(this.currentTimeout);
			} else {
				System.out.println("Received " + firstLine + ", expected " + expected);
				this.end();
			}
		}
	}
}
