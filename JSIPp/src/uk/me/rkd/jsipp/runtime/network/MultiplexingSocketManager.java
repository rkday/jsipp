package uk.me.rkd.jsipp.runtime.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.me.rkd.jsipp.runtime.Call;

public abstract class MultiplexingSocketManager extends SocketManager {

	Map<Integer, SelectableChannel> callNumToSocket;
	Map<String, Call> callIdToCall;
	private List<SelectableChannel> channels;

	public MultiplexingSocketManager(String defaultHost, int defaultPort, NetworkProtocolHandler nethandler,
	                                 int numSockets) throws IOException {
		super(defaultHost, defaultPort, nethandler);
		this.selector = Selector.open();
		this.callIdToCall = new HashMap<String, Call>();
		this.callNumToSocket = new HashMap<Integer, SelectableChannel>();
		this.channels = new ArrayList<SelectableChannel>();
		createSockets(numSockets);
	}

	private void createSockets(int numSockets) throws IOException {
		for (int i = 0; i < numSockets; i++) {
			SelectableChannel chan = nethandler.newChan();
			nethandler.connect(chan, this.defaultTarget);
			chan.configureBlocking(false);
			this.channels.add(chan);
			this.readerThread.newCallQueue.add(new CallAndChan(null, chan));
		}
	}

	@Override
	public void stop() throws IOException {
		this.readerThread.interrupt();
		this.selector.wakeup();
	}

	@Override
	public void setdest(Integer callNumber, String host, int port) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void send(Integer callNumber, String message) throws IOException {
		SelectableChannel chan = this.callNumToSocket.get(callNumber);
		ByteBuffer buf = ByteBuffer.wrap(message.getBytes());
		nethandler.write(chan, buf);
	}

	@Override
	public void add(Call call) throws IOException {
		int idx = call.getNumber() % this.channels.size();
		SelectableChannel chan = this.channels.get(idx);
		this.callNumToSocket.put(call.getNumber(), chan);
		this.callIdToCall.put(call.getCallId(), call);
	}

	@Override
	public void remove(Call call) throws IOException {
		this.callNumToSocket.remove(call.getNumber());
		this.callIdToCall.remove(call.getCallId());
	}

}
