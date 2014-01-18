/**
 * 
 */
package uk.me.rkd.jsipp.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import uk.me.rkd.jsipp.compiler.phases.CallPhase;
import uk.me.rkd.jsipp.compiler.phases.RecvPhase;
import uk.me.rkd.jsipp.compiler.phases.SendPhase;

/**
 * @author robertday
 * 
 */
public class Scenario {
	private final List<CallPhase> actions;

	private Scenario(List<CallPhase> a) {
		this.actions = a;
	}

	/**
	 * @return The list of call phases (SendPhase, RecvPhase, PausePhase) calls
	 *         made using this scenario should go through.
	 */
	public List<CallPhase> phases() {
		return Collections.unmodifiableList(this.actions);
	}

	/**
	 * @param doc An XML DOM document containing a SIPp scenario definition.
	 * @return A Scenario object based on the XML file.
	 */
	public static Scenario fromXMLFile(Document doc) {
		Element scenario = doc.getDocumentElement();
		List<CallPhase> actions = new ArrayList<CallPhase>();
		for (Node m = scenario.getFirstChild(); m != null; m = m.getNextSibling()) {
			if (m.getNodeName() == "#text") {
				// ignore whitespace elements
			} else if (m.getNodeName() == "recv") {
				actions.add(new RecvPhase(m));
			} else if (m.getNodeName() == "send") {
				actions.add(new SendPhase(m));
			}
		}
		return new Scenario(actions);
	}
}
