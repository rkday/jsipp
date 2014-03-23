package uk.me.rkd.jsipp.runtime.network;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.SIPMessageListener;

import java.nio.channels.SelectableChannel;
import java.text.ParseException;

import uk.me.rkd.jsipp.runtime.Call;
import uk.me.rkd.jsipp.runtime.CallOpeningTask;

class SocketListener implements SIPMessageListener {

	/**
	 * 
	 */
	private SocketManager multiplexingSocketManager;
	private SelectableChannel chan;

	public SocketListener(SelectableChannel chan, SocketManager multiplexingSocketManager) {
		super();
		this.multiplexingSocketManager = multiplexingSocketManager;
		this.chan = chan;
	}

	@Override
	public void handleException(ParseException e, SIPMessage msg, Class c, String s, String s2) throws ParseException {
		e.printStackTrace();
	}

	@Override
	public void processMessage(SIPMessage msg) throws Exception {
		String callId = msg.getCallId().getCallId();
		Call call = this.multiplexingSocketManager.callIdToCall.get(callId);
		if (call == null) {
			System.out.println("No call found withj Call-ID " + callId + ", creating new one");
			call = CallOpeningTask.getInstance().newUAS(callId);
			this.multiplexingSocketManager.callIdToCall.put(callId, call);
			this.multiplexingSocketManager.callNumToSocket.put(call.getNumber(), this.chan);
		}
		call.process_incoming(msg);
	}

	@Override
	public void sendSingleCLRF() throws Exception {
		// TODO Auto-generated method stub

	}
}