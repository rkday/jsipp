package uk.me.rkd.jsipp.runtime.network;

import java.io.IOException;

import uk.me.rkd.jsipp.runtime.Call;
import uk.me.rkd.jsipp.runtime.parsers.SIPpMessageParser;
import uk.me.rkd.jsipp.runtime.parsers.StreamMessageParser;

public class TCPMultiSocketManager extends MultiSocketManager {

	public TCPMultiSocketManager(String defaultHost, int defaultPort) throws IOException {
		super(defaultHost, defaultPort, new TCPHandler());
	}

	@Override
	protected SIPpMessageParser createParser(Call call) {
		return new StreamMessageParser(new PerSocketListener(call), 4096);
	}

}
