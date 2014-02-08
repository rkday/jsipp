package uk.me.rkd.jsipp.external;

import static org.junit.Assert.*;

import java.text.ParseException;

import javax.sip.header.ViaHeader;

import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.parser.SIPMessageListener;

import org.junit.Test;

import uk.me.rkd.jsipp.runtime.parsers.StreamMessageParser;

public class JainSIPParserTest {
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
	   "a=rtpmap:0 PCMU/8000\r\n" +
	   "SIP/2.0 404 Not Found\r\n" +
	   "Via: SIP/";
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
	   
	   
	public class TestHandler implements SIPMessageListener {

		public SIPMessage msg;
		public boolean error = false;
		public String err = "";
		
		@Override
		public void handleException(ParseException ex, SIPMessage sipMessage,
				Class headerClass, String headerText, String messageText)
				throws ParseException {
			this.err = "Parse error: " + headerText;
			this.error = true;
// TODO Auto-generated method stub
			
		}

		@Override
		public void processMessage(SIPMessage msg) throws Exception {
			this.msg = msg;
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendSingleCLRF() throws Exception {
			throw new ParseException("bad", 0);
			// TODO Auto-generated method stub
			
		}
		
	}

	@Test
	public void testRequest() throws Exception {
		TestHandler h = new TestHandler();
		StreamMessageParser p = new StreamMessageParser(h, 4096);
		p.addBytes(this.req.getBytes());
		System.out.println(h.err);
		assertFalse(h.error);
		assertNotNull(h.msg);
		assertTrue(h.msg instanceof SIPRequest);
		SIPRequest r = (SIPRequest)h.msg;
		assertEquals(r.getMethod(), "OPTIONS");
		assertEquals(r.getRequestURI().toString(), "sip:carol@chicago.com");
		SipUri u = (SipUri)r.getRequestURI();
		assertEquals(u.getUserAtHost(), "carol@chicago.com");
}
	
	@Test
	public void testResponse() throws Exception {
		TestHandler h = new TestHandler();
		StreamMessageParser p = new StreamMessageParser(h, 4096);
		p.addBytes(this.resp.getBytes());
		assertFalse(h.error);
		assertNotNull(h.msg);
		assertTrue(h.msg instanceof SIPResponse);
		SIPResponse r = (SIPResponse)h.msg;
		assertEquals(r.getStatusCode(), 200);
		assertEquals(h.msg.getCallIdHeader().getCallId(), "3848276298220188511@atlanta.example.com");
		Via v = (Via)r.getViaHeaders().getFirst();
		assertEquals(v.getBranch(), "z9hG4bK74bf9");
		assertEquals("Contact: <sip:bob@client.biloxi.example.com;transport=tcp>,<sip:bob2@client.biloxi.example.com;transport=tcp>", r.getHeaderAsFormattedString("Contact").trim());
	}

}
