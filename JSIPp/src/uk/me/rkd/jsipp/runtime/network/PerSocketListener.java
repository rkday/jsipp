package uk.me.rkd.jsipp.runtime.network;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.SIPMessageListener;

import java.text.ParseException;

import uk.me.rkd.jsipp.runtime.Call;

class PerSocketListener implements SIPMessageListener {

	private Call call;

	public PerSocketListener(Call call) {
		super();
		this.call = call;
	}

	@Override
	public void handleException(ParseException arg0, SIPMessage arg1, Class arg2, String arg3, String arg4)
	        throws ParseException {
		arg0.printStackTrace();
	}

	@Override
	public void processMessage(SIPMessage arg0) throws Exception {
		call.process_incoming(arg0);
	}

	@Override
	public void sendSingleCLRF() throws Exception {
		// TODO Auto-generated method stub

	}
}