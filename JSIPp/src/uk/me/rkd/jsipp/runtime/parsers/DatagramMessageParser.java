package uk.me.rkd.jsipp.runtime.parsers;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.SIPMessageListener;

import java.io.IOException;
import java.text.ParseException;

public class DatagramMessageParser extends SIPpMessageParser {

	public DatagramMessageParser(SIPMessageListener mhandler) {
		super(mhandler);
	}

	@Override
	public void addBytes(byte[] bytes) throws IOException, ParseException {
		SIPMessage msg = this.smp.parseSIPMessage(bytes, true, false, this.sipMessageListener);
		try {
			this.sipMessageListener.processMessage(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
