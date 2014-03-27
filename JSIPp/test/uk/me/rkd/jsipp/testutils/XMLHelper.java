package uk.me.rkd.jsipp.testutils;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLHelper {

	private XMLHelper() {
		// TODO Auto-generated constructor stub
	}

	public static Node parseXMLSnippet(String s) throws ParserConfigurationException, SAXException, IOException {
		Document doc = parseXML(s);
		return doc.getFirstChild();
	}

	public static Document parseXML(String s)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new InputSource(new StringReader(s)));
		return doc;
	}

}
