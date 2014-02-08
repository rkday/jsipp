package uk.me.rkd.jsipp.runtime.network;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;

public abstract class NetworkProtocolHandler {

	public NetworkProtocolHandler() {
		// TODO Auto-generated constructor stub
	}

	public abstract void write(SelectableChannel chan, ByteBuffer buf) throws IOException;

	public abstract void read(SelectableChannel chan, ByteBuffer buf) throws IOException;

	public abstract void connect(SelectableChannel chan, SocketAddress addr) throws IOException;

	public abstract SelectableChannel newChan() throws IOException;
}
