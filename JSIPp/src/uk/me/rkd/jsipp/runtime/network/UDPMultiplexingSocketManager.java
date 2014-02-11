package uk.me.rkd.jsipp.runtime.network;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import uk.me.rkd.jsipp.runtime.Call;
import uk.me.rkd.jsipp.runtime.parsers.DatagramMessageParser;
import uk.me.rkd.jsipp.runtime.parsers.SIPpMessageParser;

public class UDPMultiplexingSocketManager extends SocketManager {

	public UDPMultiplexingSocketManager(String defaultHost, int defaultPort, int numChannels) throws IOException {
		super(defaultHost, defaultPort, new UDPHandler(), numChannels);
	}

	@Override
	public SIPpMessageParser createParser(SelectableChannel chan, Call call) {
		return new DatagramMessageParser(new SocketListener(chan, this));
	}
}
