package uk.me.rkd.jsipp.runtime;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public abstract class SocketManager {
	
	public abstract void setdest(Integer callNumber, String host, int port) throws IOException;
	public abstract void send(Integer callNumber, String message) throws IOException;
	public abstract void add(Call call) throws IOException;
	public abstract void remove(Call call) throws IOException;
	public abstract void stop() throws IOException;


}
