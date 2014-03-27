package uk.me.rkd.jsipp.compiler.actions;

import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.zip.DataFormatException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import uk.me.rkd.jsipp.testutils.XMLHelper;

public class MessageActionTest {
	@Test
	public void testRegexpActionCreation() throws ParserConfigurationException, SAXException, IOException,
	        DataFormatException {
		Node action = XMLHelper.parseXMLSnippet("<action><ereg /></action>");
		assertNull(MessageAction.fromActionNode(action));
	}

	@Test(expected = DataFormatException.class)
	public void testInvalidAction() throws ParserConfigurationException, SAXException, IOException, DataFormatException {
		Node action = XMLHelper.parseXMLSnippet("<action><goldfish /></action>");
		MessageAction.fromActionNode(action);
	}
}
