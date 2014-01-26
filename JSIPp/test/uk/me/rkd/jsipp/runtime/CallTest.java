package uk.me.rkd.jsipp.runtime;

import static org.junit.Assert.*;
import io.netty.util.Timeout;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import uk.me.rkd.jsipp.compiler.Scenario;
import static org.mockito.Mockito.*;

public class CallTest {

	   String resp = "SIP/2.0 200 OK\r\n" +
	   "Via: SIP/2.0/TCP client.atlanta.example.com:5060;branch=z9hG4bK74bf9\r\n" +
	   " ;received=192.0.2.101\r\n" +
	   "From: Alice <sip:alice@atlanta.example.com>;tag=9fxced76sl\r\n" +
	   "To: Bob <sip:bob@biloxi.example.com>;tag=8321234356\r\n" +
	   "Call-ID: 3848276298220188511@atlanta.example.com\r\n" +
	   "CSeq: 1 INVITE\r\n" +
	   "Contact: <sip:bob@client.biloxi.example.com;transport=tcp>\r\n" +
	   "Contact: <sip:bob2@client.biloxi.example.com;transport=tcp>\r\n" +
	   "Content-Type: application/sdp\r\n" +
	   "Content-Length: 147\r\n" +
	   "\r\n" +
	   "v=0\r\n" +
	   "o=bob 2890844527 2890844527 IN IP4 client.biloxi.example.com\r\n" +
	   "s=-\r\n" +
	   "c=IN IP4 192.0.2.201\r\n" +
	   "t=0 0\r\n" +
	   "m=audio 3456 RTP/AVP 0\r\n" +
	   "a=rtpmap:0 PCMU/8000\r\n";
		String message_req = 
				"MESSAGE sip:carol@chicago.com SIP/2.0\r\n" +
				"Via: SIP/2.0/UDP pc33.atlanta.com;branch=z9hG4bKhjhs8ass877\r\n" +
		        "Max-Forwards: 70\r\n" +
		        "To: <sip:carol@chicago.com>\r\n" +
		        "From: Alice <sip:alice@atlanta.com>;tag=1928301774\r\n" +
		        "Call-ID: a84b4c76e66710\r\n" +
		        "CSeq: 63104 OPTIONS\r\n" +
		        "Contact: <sip:alice@pc33.atlanta.com>\r\n" +
		        "Accept: application/sdp\r\n" +
		        "Content-Length: 0\r\n" +
		        "\r\n";
		String req = 
				"OPTIONS sip:carol@chicago.com SIP/2.0\r\n" +
				"Via: SIP/2.0/UDP pc33.atlanta.com;branch=z9hG4bKhjhs8ass877\r\n" +
		        "Max-Forwards: 70\r\n" +
		        "To: <sip:carol@chicago.com>\r\n" +
		        "From: Alice <sip:alice@atlanta.com>;tag=1928301774\r\n" +
		        "Call-ID: a84b4c76e66710\r\n" +
		        "CSeq: 63104 OPTIONS\r\n" +
		        "Contact: <sip:alice@pc33.atlanta.com>\r\n" +
		        "Accept: application/sdp\r\n" +
		        "Content-Length: 0\r\n" +
		        "\r\n";
	
	@Test
	public void test() throws ParserConfigurationException, SAXException, IOException, InterruptedException {
		SocketManager sm = mock(SocketManager.class);
		Scenario s = Scenario.fromXMLFilename("resources/message-uas.xml");
		Scheduler sched = new Scheduler(1);
		Call c = new Call(1, s.phases(), sm);
		c.registerSocket();
		sched.add(c, 0);
		c.process_incoming(message_req);
		Thread.sleep(50);
		verify(sm).send(eq(1), anyString());
		Thread.sleep(50);
		verify(sm).remove(c);
	}
	
	@Test
	public void testBadInput() throws ParserConfigurationException, SAXException, IOException {
		SocketManager sm = mock(SocketManager.class);
		Scenario s = Scenario.fromXMLFilename("resources/message-uas.xml");
		Call c = new Call(2, s.phases(), sm);
		c.registerSocket();
		c.process_incoming(req);
		verify(sm).remove(c);
	}
	
	@Test
	public void testUAC() throws ParserConfigurationException, SAXException, IOException, InterruptedException {
		SocketManager sm = mock(SocketManager.class);
		Scheduler sched = new Scheduler(1);
		Scenario s = Scenario.fromXMLFilename("resources/message.xml");
		Call c = new Call(3, s.phases(), sm);
		sched.add(c, 0);
		Thread.sleep(50);
		verify(sm).send(eq(3), anyString());
		c.process_incoming(resp);
		verify(sm).remove(c);
	}

}