package uk.me.rkd.jsipp.runtime.network;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.SIPMessageListener;

import java.text.ParseException;

import uk.me.rkd.jsipp.runtime.Call;

class SocketListener implements SIPMessageListener {

	/**
	 * 
	 */
    private MultiplexingSocketManager multiplexingSocketManager;

	public SocketListener(MultiplexingSocketManager multiplexingSocketManager) {
		super();
		this.multiplexingSocketManager = multiplexingSocketManager;
	}

	@Override
	public void handleException(ParseException e, SIPMessage msg, Class c, String s, String s2)
	        throws ParseException {
		e.printStackTrace();
	}

	@Override
	public void processMessage(SIPMessage msg) throws Exception {
		System.out.println(msg.getCallId().getCallId());
		Call call = this.multiplexingSocketManager.callIdToCall.get(msg.getCallId().getCallId());
		call.process_incoming(msg);
	}

	@Override
	public void sendSingleCLRF() throws Exception {
		// TODO Auto-generated method stub

	}
}