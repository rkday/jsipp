package uk.me.rkd.jsipp.runtime.network;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.SIPMessageListener;

import java.nio.channels.SelectableChannel;
import java.text.ParseException;

import uk.me.rkd.jsipp.runtime.Call;
import uk.me.rkd.jsipp.runtime.CallOpeningTask;

class PerSocketListener implements SIPMessageListener {

	private Call call;
	private SelectableChannel chan;

	public PerSocketListener(SelectableChannel chan, Call call) {
		super();
		this.call = call;
		this.chan = chan;
	}

	@Override
	public void handleException(ParseException arg0, SIPMessage arg1, Class arg2, String arg3, String arg4)
	        throws ParseException {
		arg0.printStackTrace();
	}

	@Override
	public void processMessage(SIPMessage msg) throws Exception {
		System.out.println("Processing message..");
		if (this.call == null) {
			System.out.println("Creating new call..");
			String callId = msg.getCallId().getCallId();
			Call call = CallOpeningTask.getInstance().newUAS(callId);
		}
		this.call.process_incoming(msg);
	}

	@Override
	public void sendSingleCLRF() throws Exception {
		// TODO Auto-generated method stub

	}
}