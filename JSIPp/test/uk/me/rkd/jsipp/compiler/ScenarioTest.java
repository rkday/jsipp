package uk.me.rkd.jsipp.compiler;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

public class ScenarioTest {

	@Test
	public void testMainlineUAS() throws ParserConfigurationException, SAXException, IOException {
		String xml = "<scenario><recv request='INVITE' /><send>SIP/2.0 200 OK</send></scenario>";
		Scenario.fromXMLDocument(XMLHelper.parseXML(xml));
	}

	@Test
	public void testMainlineUAC() throws ParserConfigurationException, SAXException, IOException {
		String xml = "<scenario><send>MESSAGE sip:example.com SIP/2.0</send><recv response='200' /></scenario>";
		Scenario.fromXMLDocument(XMLHelper.parseXML(xml));
	}

	@Test
	public void testWhitespace() throws ParserConfigurationException, SAXException, IOException {
		String xml = "<scenario><send>MESSAGE sip:example.com SIP/2.0</send><recv response='200' /></scenario>";
		String wsxml = "<scenario>      \n\n<send>\n   \nMESSAGE sip:example.com SIP/2.0</send>\n\n  <recv response='200' /></scenario>";
		Scenario scenario = Scenario.fromXMLDocument(XMLHelper.parseXML(wsxml));
		assertTrue(scenario.isUac());
	}

	@Test
	public void testRegexpRequest() throws ParserConfigurationException, SAXException, IOException {
		String xml = "<scenario><recv request='INVITE|NOTIFY' regexp='true'/><send>SIP/2.0 200 OK</send></scenario>";
		Scenario scenario = Scenario.fromXMLDocument(XMLHelper.parseXML(xml));
		assertTrue(scenario.isUas());
	}

	@Test
	public void testRegexpResponse() throws ParserConfigurationException, SAXException, IOException {
		String xml = "<scenario><send>MESSAGE sip:example.com SIP/2.0</send><recv response='200|204' regexp='true'/></scenario>";
		Scenario.fromXMLDocument(XMLHelper.parseXML(xml));
	}

}
