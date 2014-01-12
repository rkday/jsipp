package uk.me.rkd.jsipp.compiler.phases;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class RecvPhase extends CallPhase {
	RecvType type;
	String expected;
	boolean optional = false;
	
	public RecvPhase(Node xmlnode) {
		NamedNodeMap attr = xmlnode.getAttributes();
		Node expected_response = attr.getNamedItem("response");
		Node expected_request = attr.getNamedItem("request");
		Node regex_param = attr.getNamedItem("regexp");
		boolean regex = false;
		Node optional_param = attr.getNamedItem("optional");

		if (regex_param != null &&
				regex_param.getTextContent().equalsIgnoreCase("true")) {
			regex = true;
		}

		if (optional_param != null &&
				optional_param.getTextContent().equalsIgnoreCase("true")) {
			this.optional = true;
		}
		
		if (expected_response != null) {
			expected = expected_response.getTextContent();
			if (regex) {
				type = RecvType.RESPONSEREGEX;
			} else {
				type = RecvType.RESPONSE;
			}
		} else if (expected_request != null) {
			expected = expected_request.getTextContent();
			if (regex) {
				type = RecvType.REQUESTREGEX;
			} else {
				type = RecvType.REQUEST;
			}
		}
		System.out.println("Created RecvCallAction: type " + this.type +
				", optional " + this.optional +
				", expected " + this.expected);
	}

}
