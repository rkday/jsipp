package uk.me.rkd.jsipp.compiler.phases;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import gov.nist.javax.sip.message.SIPMessage;

public class Pause extends CallPhase {

	private long duration;
	
	public Pause(Node xmlnode, int idx) {
		this.idx = idx;
		NamedNodeMap attr = xmlnode.getAttributes();
		Node duration = attr.getNamedItem("duration");
		if (duration == null) {
			this.duration = 1000;
		} else {
			this.duration = Long.parseLong(duration.getTextContent());
		}
	}
	
	@Override
	public boolean expected(SIPMessage msg) {
		return false;
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	public long getDuration() {
		return duration;
	}

	@Override
	public String forZMQ() {
		return ("PAUSE:" + Long.toString(this.duration));
	}

}
