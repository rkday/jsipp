package uk.me.rkd.jsipp.runtime;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import uk.me.rkd.jsipp.compiler.phases.CallPhase;
import uk.me.rkd.jsipp.compiler.phases.RecvPhase;
import uk.me.rkd.jsipp.compiler.phases.SendPhase;
import uk.me.rkd.jsipp.runtime.network.SocketManager;

public class Call implements TimerTask {

	private final int callNumber;
	private final String callId;

	/**
	 * @return the callId
	 */
	public String getCallId() {
		return callId;
	}

	private int phaseIndex = 0;
	private Map<String, String> callVariables = new HashMap<String, String>();
	private List<CallPhase> phases;
	private SocketManager sm;
	private long timeoutEnds;
	private Timeout currentTimeout;

	// unfinished
	private class variablesList {

		String get(String name) {
			// look in per-call variables
			// look in global variables
			// for last_, look in last received message
			return "";
		}

	}

	public void registerSocket() throws IOException {
		this.sm.add(this);
	}

	public Call(int callNum, List<CallPhase> phases, SocketManager sm) {
		// TODO Auto-generated constructor stub
		this.callNumber = callNum;
		this.callId = Integer.toString(callNum);
		this.callVariables.put("call_num", Integer.toString(callNum));
		this.callVariables.put("call_id", this.callId);
		this.phases = phases;
		this.sm = sm;
		System.out.println("Call " + Integer.toString(callNum) + " created");
	}

	public void end() {
		try {
			this.sm.remove(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void run(Timeout timeout) {
		// System.out.println("Call " + Integer.toString(this.callNumber) + " woken up");
		if (this.phaseIndex >= this.phases.size()) {
			System.out.println("Call " + Integer.toString(this.getNumber()) + " terminating");
			this.end();
		} else {
			CallPhase currentPhase = this.phases.get(this.phaseIndex);
			this.currentTimeout = timeout;
			if (currentPhase instanceof RecvPhase) {
				if (this.timeoutEnds == -1) {
					this.timeoutEnds = ((RecvPhase) currentPhase).timeout;
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
					String message = KeywordReplacer.replaceKeywords(((SendPhase) currentPhase).message,
					                                                 this.callVariables, false);
					this.sm.send(this.callNumber, message);
				} catch (IOException | IllegalStateException e) {
					// TODO Auto-generated catch block
					System.out.println("Send failed");
					e.printStackTrace();
					this.end();
				}
				this.phaseIndex += 1;
				this.run(timeout);
			}
		}
	}

	public int getNumber() {
		return callNumber;
	}

	public synchronized void process_incoming(SIPMessage message) {
		CallPhase phase = this.phases.get(this.phaseIndex);
		if (phase instanceof RecvPhase) {
			String expected = ((RecvPhase) phase).expected;
			if (((message instanceof SIPRequest) && ((SIPRequest) message).getMethod().equals(expected))
			        || ((message instanceof SIPResponse) && ((SIPResponse) message).getStatusCode() == Integer.parseInt(expected))) {
				System.out.println("Call " + Integer.toString(getNumber()) + " received " + expected);
				this.phaseIndex += 1;
				this.timeoutEnds = -1;
				if (this.currentTimeout != null) {
					this.run(this.currentTimeout);
				}
			} else {
				System.out.println("Expected " + expected);
				this.end();
			}
		}
	}
}
