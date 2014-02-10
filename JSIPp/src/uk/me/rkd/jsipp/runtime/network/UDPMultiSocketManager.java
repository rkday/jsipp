package uk.me.rkd.jsipp.runtime.network;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import uk.me.rkd.jsipp.runtime.Call;
import uk.me.rkd.jsipp.runtime.parsers.SIPpMessageParser;
import uk.me.rkd.jsipp.runtime.parsers.StreamMessageParser;

public class UDPMultiSocketManager extends MultiSocketManager {

	public UDPMultiSocketManager(String defaultHost, int defaultPort) throws IOException {
		super(defaultHost, defaultPort, new UDPHandler());
		// TODO Auto-generated constructor stub
	}

	@Override
	public SIPpMessageParser createParser(SelectableChannel chan, Call call) {
		return new StreamMessageParser(new PerSocketListener(chan, call), 4060);
	}

}
