package uk.me.rkd.jsipp.compiler.phases;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class RecvPhase extends CallPhase {

	public static enum RecvType {
		REQUEST, RESPONSE,
	}

	public final RecvType recvType;
	public final boolean regexp;
	public final boolean optional;
	public final int timeout;

	public RecvPhase(Node xmlnode, int idx) {
		this.idx = idx;
		NamedNodeMap attr = xmlnode.getAttributes();
		Node expected_response = attr.getNamedItem("response");
		Node expected_request = attr.getNamedItem("request");
		Node regex_param = attr.getNamedItem("regexp");
		Node optional_param = attr.getNamedItem("optional");
		Node timeout_param = attr.getNamedItem("timeout");

		if (timeout_param != null) {
			this.timeout = new Integer(regex_param.getTextContent());
		} else {
			this.timeout = 5000;
		}

		if (regex_param != null && regex_param.getTextContent().equalsIgnoreCase("true")) {
			this.regexp = true;
		} else {
			this.regexp = false;
		}

		if (optional_param != null && optional_param.getTextContent().equalsIgnoreCase("true")) {
			this.optional = true;
		} else {
			this.optional = false;
		}

		if (expected_response != null) {
			this.expected = expected_response.getTextContent();
			this.recvType = RecvType.RESPONSE;
		} else if (expected_request != null) {
			this.expected = expected_request.getTextContent();
			this.recvType = RecvType.REQUEST;
		} else {
			this.expected = "";
			this.recvType = null;
			throw new NullPointerException();
		}
	}

	@Override
	public boolean expected(SIPMessage msg) {
		if (msg instanceof SIPRequest) {
			return ((SIPRequest) msg).getMethod().equals(expected);
		} else {
			// message instanceof SIPResponse
			return ((SIPResponse) msg).getStatusCode() == Integer.parseInt(expected);
		}
	}

	@Override
	public boolean isOptional() {
		return this.optional;
	}

	@Override
	public String forZMQ() {
		return "IN:" + this.expected;
	}

}
