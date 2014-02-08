package uk.me.rkd.jsipp.runtime.network;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import uk.me.rkd.jsipp.runtime.Call;
import uk.me.rkd.jsipp.runtime.parsers.SIPpMessageParser;
import uk.me.rkd.jsipp.runtime.parsers.StreamMessageParser;

public class TCPMultiSocketManager extends MultiSocketManager {

	public TCPMultiSocketManager(String defaultHost, int defaultPort) throws IOException {
		super(defaultHost, defaultPort);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void write(SelectableChannel chan, ByteBuffer buf) throws IOException {
		((SocketChannel) chan).write(buf);
		System.out.println("Message sent");
	}

	@Override
	protected void read(SelectableChannel chan, ByteBuffer buf) throws IOException {
		((SocketChannel) chan).read(buf);
	}

	@Override
	protected void connect(SelectableChannel chan, SocketAddress addr) throws IOException {
		((SocketChannel) chan).connect(addr);
	}

	@Override
	protected SelectableChannel newChan() throws IOException {
		return SocketChannel.open();
	}

	@Override
	protected SIPpMessageParser createParser(Call call) {
		return new StreamMessageParser(new PerSocketListener(call), 4096);
	}

}
