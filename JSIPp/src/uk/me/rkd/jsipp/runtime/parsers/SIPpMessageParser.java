package uk.me.rkd.jsipp.runtime.parsers;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.MessageParser;
import gov.nist.javax.sip.parser.SIPMessageListener;
import gov.nist.javax.sip.parser.StringMsgParser;

import java.io.IOException;
import java.text.ParseException;

import org.apache.log4j.Logger;

public abstract class SIPpMessageParser {

	protected static Logger logger = Logger.getLogger(StreamMessageParser.class);

	public abstract void addBytes(byte[] bytes) throws IOException, ParseException;

	protected MessageParser smp = null;

	/**
	 * The message listener that is registered with this parser. (The message listener has methods that can process
	 * correct and erroneous messages.)
	 */
	protected SIPMessageListener sipMessageListener;

	public SIPpMessageParser(SIPMessageListener mhandler) {
		super();
		this.sipMessageListener = mhandler;
		this.smp = new StringMsgParser();
	}

	public void processSIPMessage(SIPMessage message) {
		try {
			sipMessageListener.processMessage(message);
		} catch (Exception e) {
			logger.error("Can't process message", e);
		}
	}

	/**
	 * Add a class that implements a SIPMessageListener interface whose methods get called * on successful parse and
	 * error conditons.
	 * 
	 * @param mlistener
	 *            a SIPMessageListener implementation that can react to correct and incorrect pars.
	 */
	public void setMessageListener(SIPMessageListener mlistener) {
		sipMessageListener = mlistener;
	}

	public static int getBodyLength(String message) {
		String[] parts = message.split("\r\n\r\n", 2);
		if (parts.length == 1) {
			return 0;
		}
		System.out.println(parts[1]);
		return parts[1].length();
	}
}