package uk.me.rkd.jsipp.runtime.network;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import uk.me.rkd.jsipp.runtime.Call;
import uk.me.rkd.jsipp.runtime.parsers.SIPpMessageParser;
import uk.me.rkd.jsipp.runtime.parsers.StreamMessageParser;

public class TCPMultiSocketManager extends MultiSocketManager {

	public TCPMultiSocketManager(String defaultHost, int defaultPort) throws IOException {
		super(defaultHost, defaultPort, new TCPHandler());
	}

	@Override
	protected SIPpMessageParser createParser(SelectableChannel chan, Call call) {
		return new StreamMessageParser(new PerSocketListener(chan, call), 4096);
	}

}
