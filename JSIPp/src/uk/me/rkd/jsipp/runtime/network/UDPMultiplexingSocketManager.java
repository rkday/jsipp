package uk.me.rkd.jsipp.runtime.network;

import java.io.IOException;

import uk.me.rkd.jsipp.runtime.Call;
import uk.me.rkd.jsipp.runtime.parsers.SIPpMessageParser;
import uk.me.rkd.jsipp.runtime.parsers.StreamMessageParser;

public class UDPMultiplexingSocketManager extends MultiplexingSocketManager {

	public UDPMultiplexingSocketManager(String defaultHost, int defaultPort, int numChannels) throws IOException {
		super(defaultHost, defaultPort, new UDPHandler(), numChannels);
	}

	@Override
	public SIPpMessageParser createParser(Call call) {
		return new StreamMessageParser(new SocketListener(this), 4096);
	}
}
