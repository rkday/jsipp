package uk.me.rkd.jsipp.compiler.phases;

import org.w3c.dom.Node;

public class SendPhase extends CallPhase {
	public final String message;
	
	public SendPhase(Node xmlnode) {
		this.message = xmlnode.getFirstChild().getTextContent();
	}

}
