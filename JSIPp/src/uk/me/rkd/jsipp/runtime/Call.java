package uk.me.rkd.jsipp.runtime;

import gov.nist.javax.sip.message.SIPMessage;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import uk.me.rkd.jsipp.compiler.SimpleVariableTable;
import uk.me.rkd.jsipp.compiler.phases.CallPhase;
import uk.me.rkd.jsipp.compiler.phases.RecvPhase;
import uk.me.rkd.jsipp.compiler.phases.SendPhase;
import uk.me.rkd.jsipp.runtime.Statistics.StatType;
import uk.me.rkd.jsipp.runtime.network.SocketManager;
import uk.me.rkd.jsipp.runtime.parsers.SIPpMessageParser;

public class Call implements TimerTask {

	private final int callNumber;
	private final String callId;

	/**
	 * @return the callId
	 */
	public String getCallId() {
		return callId;
	}

	final int NO_TIMEOUT = -1;
	private int phaseIndex = 0;
	private List<CallPhase> phases;
	private SocketManager sm;
	private long timeoutEnds = NO_TIMEOUT;
	private Timer timer;
	private SIPMessage lastMessage;
	private CallVariables variables;

	public class CallVariables extends SimpleVariableTable {

		@Override
		public String get(String name) {
			try {
				if (name.equals("local_port")) {
					return Integer.toString(getLocalAddress().getPort());
				} else if (name.equals("remote_port")) {
					return Integer.toString(getRemoteAddress().getPort());
				} else if (name.equals("local_ip")) {
					System.out.println(getLocalAddress().getAddress().getHostAddress());
					return getLocalAddress().getAddress().getHostAddress();
				} else if (name.equals("remote_ip")) {
					return getRemoteAddress().getAddress().getHostAddress();
				} else if (name.equals("local_ip_type")) {
					return (getLocalAddress().getAddress() instanceof Inet6Address) ? "6" : "4";
				} else if (name.equals("media_ip")) {
					return "0.0.0.0";
				} else if (name.equals("media_port")) {
					return "0";
				} else if (name.equals("media_ip_type")) {
					return "4";
				}
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}

			if (vars.containsKey(name)) {
				return vars.get(name);
			}

			String global = SimpleVariableTable.global().get(name);

			if (global != null) {
				return global;
			}

			if (name.startsWith("last_") && lastMessage != null) {
				String headerName = name.replace("last_", "");
				return lastMessage.getHeaderAsFormattedString(headerName).trim();
			}
			return null;
		}

	}

	public void registerSocket() throws IOException {
		this.sm.add(this);
	}

	public Call(int callNum, String callId, List<CallPhase> phases, SocketManager sm) {
		this.variables = new CallVariables();
		this.callNumber = callNum;
		this.callId = callId;
		this.variables.putKeyword("call_number", Integer.toString(callNum));
		this.variables.putKeyword("call_id", this.callId);
		this.phases = phases;
		this.sm = sm;
		System.out.println("Call " + Integer.toString(callNum) + " created");
	}

	private void success() {
		System.out.println("Call " + Integer.toString(this.getNumber()) + " terminating");
		Statistics.INSTANCE.report(StatType.CALL_SUCCESS, this.callId);
		end();
	}

	private void fail() {
		Statistics.INSTANCE.report(StatType.CALL_FAILED, this.callId);
		end();
	}

	private void end() {
		try {
			this.sm.remove(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean hasCompleted() {
		return this.phaseIndex >= this.phases.size();
	}

	private void reschedule(long when) {
		if (this.timer != null) {
			this.timer.newTimeout(this, when, TimeUnit.MILLISECONDS);
		}
	}

	public synchronized void run(Timeout timeout) {
		System.out.println(String.format("Call %d, phase %d", this.getNumber(), this.phaseIndex));
		if (hasCompleted()) {
			this.success();
		} else {
			CallPhase currentPhase = getCurrentPhase();
			this.timer = timeout.timer();

			// If we're waiting to receive, check for timeout
			if (currentPhase instanceof RecvPhase) {
				if (this.timeoutEnds == NO_TIMEOUT) {
					this.timeoutEnds = ((RecvPhase) currentPhase).timeout + System.currentTimeMillis();
				}
				long untilTimeout = this.timeoutEnds - System.currentTimeMillis();
				if (untilTimeout < 0) {
					Statistics.INSTANCE.report(StatType.RECV_TIMED_OUT, Integer.toString(currentPhase.idx));
					System.out.println("Call timed out");
					this.fail();
				} else {
					// We haven't timed out yet - reschedule ourselves to run when we will time out
					reschedule(untilTimeout);
				}
			} else if (currentPhase instanceof SendPhase) {
				// We're sending - just send and move on
				send();
				nextPhase();
				this.run(timeout);
			}
		}
	}

	private void send() {
		SendPhase currentPhase = (SendPhase) getCurrentPhase();
		System.out.println("Sending");
		this.variables.putKeyword("branch", "z9hG4bK" + UUID.randomUUID().toString());
		try {
			// Do a first pass of keyword replacement, so we can calculate the body length
			String message = KeywordReplacer.replaceKeywords(currentPhase.message, this.variables, false);
			int len = SIPpMessageParser.getBodyLength(message);
			this.variables.putKeyword("len", Integer.toString(len));

			// Do a second pass now that we know the value of [len]
			message = KeywordReplacer.replaceKeywords(currentPhase.message, this.variables, false);
			this.sm.send(this.callNumber, message);
			Statistics.INSTANCE.report(StatType.MSG_SENT, Integer.toString(currentPhase.idx));
		} catch (Exception e) {
			System.out.println("Send failed");
			e.printStackTrace();
			this.fail();
		}
	}

	public int getNumber() {
		return callNumber;
	}

	public synchronized void process_incoming(SIPMessage message) {
		this.timeoutEnds = NO_TIMEOUT;
		this.lastMessage = message;

		CallPhase phase = getCurrentPhase();
		if (phase.expected(message)) {
			Statistics.INSTANCE.report(StatType.MSG_RECVD, Integer.toString(phase.idx));
			System.out.println("Call " + Integer.toString(getNumber()) + " received " + phase.expected);
			nextPhase();
			reschedule(0);
		} else {
			// No match - check if this was optional
			if (phase.isOptional()) {
				nextPhase();
				process_incoming(message);
				return;
			}
			Statistics.INSTANCE.report(StatType.UNEXPECTED_MSG_RECVD, Integer.toString(phase.idx));
			System.out.println("Expected " + phase.expected);
			this.fail();
		}
	}

	private CallPhase getCurrentPhase() {
		return this.phases.get(this.phaseIndex);
	}

	private void nextPhase() {
		this.phaseIndex += 1;
	}

	InetSocketAddress getRemoteAddress() throws IOException {
		return (InetSocketAddress) this.sm.getdest(callNumber);
	}

	InetSocketAddress getLocalAddress() throws IOException {
		return (InetSocketAddress) this.sm.getaddr(callNumber);
	}
}
