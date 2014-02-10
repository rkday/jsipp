package uk.me.rkd.jsipp.runtime.network;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;

public abstract class NetworkProtocolHandler {

	public NetworkProtocolHandler() {
	}

	public abstract void write(SelectableChannel chan, ByteBuffer buf) throws IOException;

	public abstract int read(SelectableChannel chan, ByteBuffer buf) throws IOException;

	public abstract void connect(SelectableChannel chan, SocketAddress addr) throws IOException;

	public abstract void close(SelectableChannel chan) throws IOException;

	public abstract SelectableChannel newChan() throws IOException;

	public abstract SelectableChannel newListener(SocketAddress bindAddr) throws IOException;

	public abstract SocketAddress getLocalAddress(SelectableChannel chan) throws IOException;

	public abstract SocketAddress getRemoteAddress(SelectableChannel chan) throws IOException;
}
