package uk.me.rkd.jsipp.runtime.network;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;

import uk.me.rkd.jsipp.runtime.Call;
import uk.me.rkd.jsipp.runtime.parsers.DatagramMessageParser;
import uk.me.rkd.jsipp.runtime.parsers.SIPpMessageParser;

public class UDPMultiSocketManager extends MultiSocketManager {

	public UDPMultiSocketManager(String defaultHost, int defaultPort) throws IOException {
		super(defaultHost, defaultPort);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void write(SelectableChannel chan, ByteBuffer buf) throws IOException {
		((DatagramChannel) chan).write(buf);
	}

	@Override
	protected void read(SelectableChannel chan, ByteBuffer buf) throws IOException {
		((DatagramChannel) chan).read(buf);
	}

	@Override
	protected void connect(SelectableChannel chan, SocketAddress addr) throws IOException {
		((DatagramChannel) chan).connect(addr);
	}

	@Override
	protected SelectableChannel newChan() throws IOException {
		return DatagramChannel.open();
	}

	@Override
	protected SIPpMessageParser createParser(Call call) {
		return new DatagramMessageParser(new PerSocketListener(call));
	}

}
