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
	public int read(SelectableChannel chan, ByteBuffer buf) throws IOException {
		((DatagramChannel) chan).receive(buf);
		return buf.position();
	}

	@Override
	public void connect(SelectableChannel chan, SocketAddress addr) throws IOException {
		((DatagramChannel) chan).connect(addr);
	}

	@Override
	public void close(SelectableChannel chan) throws IOException {
		((DatagramChannel) chan).close();
	}

	@Override
	public SocketAddress getLocalAddress(SelectableChannel chan) throws IOException {
		return ((DatagramChannel) chan).getLocalAddress();
	}

	@Override
	public SocketAddress getRemoteAddress(SelectableChannel chan) throws IOException {
		return ((DatagramChannel) chan).getRemoteAddress();
	}

	@Override
	public SelectableChannel newChan() throws IOException {
		return DatagramChannel.open();
	}

	@Override
	public SelectableChannel newListener(SocketAddress bindAddr) throws IOException {
		DatagramChannel chan = DatagramChannel.open();
		chan.bind(bindAddr);
		return chan;
	}
}
