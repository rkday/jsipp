package uk.me.rkd.jsipp.runtime.network;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

public class TCPHandler extends NetworkProtocolHandler {

	public TCPHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void write(SelectableChannel chan, ByteBuffer buf) throws IOException {
		((SocketChannel) chan).write(buf);
	}

	@Override
	public void read(SelectableChannel chan, ByteBuffer buf) throws IOException {
		((SocketChannel) chan).read(buf);
	}

	@Override
	public void connect(SelectableChannel chan, SocketAddress addr) throws IOException {
		((SocketChannel) chan).connect(addr);
	}

	@Override
	public SocketAddress getLocalAddress(SelectableChannel chan) throws IOException {
		return ((SocketChannel) chan).getLocalAddress();
	}

	@Override
	public SocketAddress getRemoteAddress(SelectableChannel chan) throws IOException {
		return ((SocketChannel) chan).getRemoteAddress();
	}

	@Override
	public SelectableChannel newChan() throws IOException {
		return SocketChannel.open();
	}
}
