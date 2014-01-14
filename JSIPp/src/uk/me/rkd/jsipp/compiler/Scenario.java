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
import org.w3c.dom.NodeList;

import uk.me.rkd.jsipp.compiler.phases.CallPhase;
import uk.me.rkd.jsipp.compiler.phases.RecvPhase;
import uk.me.rkd.jsipp.compiler.phases.SendPhase;
import uk.me.rkd.jsipp.runtime.Call;

/**
 * @author robertday
 *
 */
public class Scenario {
	private final List<CallPhase> actions;
	private Scenario(List<CallPhase> a) {
		this.actions = a;
	}
	
	public List<CallPhase> phases() {
		return Collections.unmodifiableList(this.actions);
	}
	/**
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
