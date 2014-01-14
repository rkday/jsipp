package uk.me.rkd.jsipp;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import uk.me.rkd.jsipp.compiler.Scenario;
import uk.me.rkd.jsipp.runtime.CallOpeningTask;
import uk.me.rkd.jsipp.runtime.Scheduler;
import uk.me.rkd.jsipp.runtime.SocketManager;
import uk.me.rkd.jsipp.runtime.UDPMultiSocketManager;

public class JSIPpMain {

	public JSIPpMain() {
		// TODO Auto-generated constructor stub
	}

	public static void main (String argv[]) throws ParserConfigurationException, SAXException, IOException, InterruptedException {
		File fXmlFile = new File("/Users/robertday/message.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setValidating(false);
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
		//doc.getDocumentElement().normalize();
		Scenario scenario = Scenario.fromXMLFile(doc);
		System.out.println("Scenario created");
		Scheduler sched = new Scheduler(50);
		System.out.println("Scheduler created");
		SocketManager sm = new UDPMultiSocketManager("localhost", 5060);
		System.out.println("Socket manager created");
		CallOpeningTask opentask = new CallOpeningTask(scenario, sm);
		sched.add(opentask, 10);
		System.out.println("Call generation task started");
		Thread.sleep(180000);
		opentask.stop();
		Thread.sleep(1000);
		sm.stop();
		sched.stop();
		System.out.println("fin");
	}

}
