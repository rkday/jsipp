/**
 * 
 */
package uk.me.rkd.jsipp.compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import uk.me.rkd.jsipp.compiler.phases.CallPhase;
import uk.me.rkd.jsipp.compiler.phases.RecvPhase;
import uk.me.rkd.jsipp.compiler.phases.SendPhase;

/**
 * @author robertday
 * 
 */
public class Scenario {
	private final List<CallPhase> actions;
	private boolean uac = false;

	private Scenario(List<CallPhase> a) {
		this.actions = a;
		for (CallPhase action : actions) {
			if (action instanceof SendPhase) {
				this.uac = true;
				break;
			} else if (action instanceof RecvPhase) {
				this.uac = false;
				break;
			}
		}
	}

	public boolean isUac() {
		return this.uac;
	}

	public boolean isUas() {
		return !this.uac;
	}

	/**
	 * @return The list of call phases (SendPhase, RecvPhase, PausePhase) calls made using this scenario should go
	 *         through.
	 */
	public List<CallPhase> phases() {
		return Collections.unmodifiableList(this.actions);
	}

	public static Scenario fromXMLFilename(String filename) throws ParserConfigurationException, SAXException,
	        IOException {
		File fXmlFile = new File(filename);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setValidating(true);
		dbFactory.setNamespaceAware(true);
		dbFactory.setFeature("http://xml.org/sax/features/namespaces", false);
		dbFactory.setFeature("http://xml.org/sax/features/validation", false);
		dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		dbFactory.setIgnoringElementContentWhitespace(true);
		dbFactory.setIgnoringComments(true);
		dbFactory.setCoalescing(true);
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		return Scenario.fromXMLDocument(doc);
	}

	/**
	 * @param doc
	 *            An XML DOM document containing a SIPp scenario definition.
	 * @return A Scenario object based on the XML file.
	 */
	public static Scenario fromXMLDocument(Document doc) {
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
