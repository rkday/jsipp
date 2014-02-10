package uk.me.rkd.jsipp.runtime.network;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import uk.me.rkd.jsipp.runtime.Call;
import uk.me.rkd.jsipp.runtime.parsers.SIPpMessageParser;
import uk.me.rkd.jsipp.runtime.parsers.StreamMessageParser;

public class TCPMultiplexingSocketManager extends MultiplexingSocketManager {

	public TCPMultiplexingSocketManager(String defaultHost, int defaultPort, int numSockets) throws IOException {
		super(defaultHost, defaultPort, new TCPHandler(), numSockets);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected SIPpMessageParser createParser(SelectableChannel chan, Call call) {
		return new StreamMessageParser(new SocketListener(chan, this), 4096);
	}

}
