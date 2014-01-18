package uk.me.rkd.jsipp.compiler;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.me.rkd.jsipp.compiler.phases.SendPhase;

public class SendPhaseTest {

	String exampleInput = 	"     \n" +
			"   \n" +
			"  OPTIONS sip:carol@chicago.com SIP/2.0\n" +
			"  Via: SIP/2.0/UDP pc33.atlanta.com;branch=z9hG4bKhjhs8ass877\n" +
	        "  Max-Forwards: 70\n" +
	        "  To: <sip:carol@chicago.com>\n" +
	        "  From: Alice <sip:alice@atlanta.com>;tag=1928301774\n" +
	        "  Call-ID: a84b4c76e66710\n" +
	        "  CSeq: 63104 OPTIONS\n" +
	        "  Contact: <sip:alice@pc33.atlanta.com>\n" +
	        "  Accept: application/sdp, \n" +
	        "          text/plain\n" +
	        "  Contact: <sip:alice@pc34.atlanta.com>\n" +
	        "  Content-Length: 0\n" +
	        "\n";
	String exampleOutput = 
			"OPTIONS sip:carol@chicago.com SIP/2.0\r\n" +
			"Via: SIP/2.0/UDP pc33.atlanta.com;branch=z9hG4bKhjhs8ass877\r\n" +
	        "Max-Forwards: 70\r\n" +
	        "To: <sip:carol@chicago.com>\r\n" +
	        "From: Alice <sip:alice@atlanta.com>;tag=1928301774\r\n" +
	        "Call-ID: a84b4c76e66710\r\n" +
	        "CSeq: 63104 OPTIONS\r\n" +
	        "Contact: <sip:alice@pc33.atlanta.com>\r\n" +
	        "Accept: application/sdp, \r\n" +
	        "        text/plain\r\n" +
	        "Contact: <sip:alice@pc34.atlanta.com>\r\n" +
	        "Content-Length: 0\r\n" +
	        "\r\n";
	
	@Test
	public void testMessageNormalization() {
			assertEquals(exampleOutput, SendPhase.stripWhitespace(exampleInput));
	}

}
