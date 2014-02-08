package uk.me.rkd.jsipp.runtime;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import uk.me.rkd.jsipp.compiler.phases.CallPhase;
import uk.me.rkd.jsipp.compiler.phases.RecvPhase;
import uk.me.rkd.jsipp.compiler.phases.SendPhase;
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

	private int phaseIndex = 0;
	private List<CallPhase> phases;
	private SocketManager sm;
	private long timeoutEnds;
	private Timeout currentTimeout;
	private SIPMessage lastMessage;
	private variablesList variables = new variablesList();
	private Map<String, String> globalVariables;

	public class variablesList {

		private Map<String, String> callVariables = new HashMap<String, String>();

		void put(String k, String v) {
			callVariables.put(k, v);
		}

		void remove(String k) {
			callVariables.remove(k);
		}

		String get(String name) {
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

			if (callVariables.containsKey(name)) {
				return callVariables.get(name);
			}

			if (globalVariables.containsKey(name)) {
				return globalVariables.get(name);
			}

			if (name.equals("service")) {
				return "sipp";
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

	public Call(int callNum, List<CallPhase> phases, SocketManager sm, Map<String, String> globalVariables) {
		this.callNumber = callNum;
		this.callId = Integer.toString(callNum);
		this.variables.put("call_number", Integer.toString(callNum));
		this.variables.put("call_id", this.callId);
		this.phases = phases;
		this.sm = sm;
		this.globalVariables = globalVariables;
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
		System.out.println("Call " + Integer.toString(this.getNumber()) + ", phase "
		        + Integer.toString(this.phaseIndex));
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
				send((SendPhase) currentPhase);
				this.phaseIndex += 1;
				this.run(timeout);
			}
		}
	}

	public void send(SendPhase currentPhase) {
		System.out.println("Sending");
		this.variables.put("branch", "z9hG4bK" + UUID.randomUUID().toString());
		try {
			// Do a first pass of keyword replacement, so we fill in the right things in the body and can calculate its
			// length
			String message = KeywordReplacer.replaceKeywords(currentPhase.message, this.variables, false);
			int len = SIPpMessageParser.getBodyLength(message);
			this.variables.put("len", Integer.toString(len));

			// Do a second pass now that we know the value of [len]
			message = KeywordReplacer.replaceKeywords(currentPhase.message, this.variables, false);
			this.sm.send(this.callNumber, message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Send failed");
			e.printStackTrace();
			this.end();
		}
	}

	public int getNumber() {
		return callNumber;
	}

	public synchronized void process_incoming(SIPMessage message) {
		this.lastMessage = message;

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

	InetSocketAddress getRemoteAddress() throws IOException {
		return (InetSocketAddress) this.sm.getdest(callNumber);
	}

	InetSocketAddress getLocalAddress() throws IOException {
		return (InetSocketAddress) this.sm.getaddr(callNumber);
	}
}
