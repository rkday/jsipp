package uk.me.rkd.jsipp.runtime.network;

import java.io.IOException;

import uk.me.rkd.jsipp.runtime.Call;
import uk.me.rkd.jsipp.runtime.parsers.DatagramMessageParser;
import uk.me.rkd.jsipp.runtime.parsers.SIPpMessageParser;

public class UDPMultiSocketManager extends MultiSocketManager {

	public UDPMultiSocketManager(String defaultHost, int defaultPort) throws IOException {
		super(defaultHost, defaultPort, new UDPHandler());
		// TODO Auto-generated constructor stub
	}

	@Override
	public SIPpMessageParser createParser(Call call) {
		return new DatagramMessageParser(new PerSocketListener(call));
	}

}
