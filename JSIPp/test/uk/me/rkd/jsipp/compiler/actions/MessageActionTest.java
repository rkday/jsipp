package uk.me.rkd.jsipp.compiler.actions;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.zip.DataFormatException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MessageActionTest {
	static private Node parseXMLSnippet(String s) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new InputSource(new StringReader(s)));
		return doc.getFirstChild();
	}
	
	@Test
	public void testRegexpActionCreation() throws ParserConfigurationException, SAXException, IOException, DataFormatException {
		Node action = parseXMLSnippet("<action><ereg /></action>");
		assertTrue(MessageAction.fromActionNode(action) instanceof RegexpAction);
	}

}
