package uk.me.rkd.jsipp.runtime.network;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;

public class UDPHandler extends NetworkProtocolHandler {

	public UDPHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void write(SelectableChannel chan, ByteBuffer buf) throws IOException {
		((DatagramChannel) chan).write(buf);
	}

	@Override
	public void read(SelectableChannel chan, ByteBuffer buf) throws IOException {
		((DatagramChannel) chan).read(buf);
	}

	@Override
	public void connect(SelectableChannel chan, SocketAddress addr) throws IOException {
		((DatagramChannel) chan).connect(addr);
	}

	@Override
	public SelectableChannel newChan() throws IOException {
		return DatagramChannel.open();
	}
}
