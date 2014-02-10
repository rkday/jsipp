package uk.me.rkd.jsipp.compiler.phases;

import gov.nist.javax.sip.message.SIPMessage;

public abstract class CallPhase {
	public String expected;

	public abstract boolean expected(SIPMessage msg);

	public abstract boolean isOptional();

}
