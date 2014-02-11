package uk.me.rkd.jsipp.runtime.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import uk.me.rkd.jsipp.runtime.Call;
import uk.me.rkd.jsipp.runtime.parsers.SIPpMessageParser;

public abstract class SocketManager {

	Selector selector;
	SelectorThread readerThread;
	SocketAddress defaultTarget;
	NetworkProtocolHandler nethandler;
	Map<Integer, SelectableChannel> callNumToSocket;
	Map<String, Call> callIdToCall;
	private List<SelectableChannel> channels;

	public SocketManager(String defaultHost, int defaultPort, NetworkProtocolHandler nethandler, int numSockets)
	                                                                                                            throws IOException {
		if (defaultHost != null) {
			this.defaultTarget = new InetSocketAddress(defaultHost, defaultPort);
		}
		this.selector = Selector.open();
		this.readerThread = new SelectorThread();
		this.nethandler = nethandler;
		this.callIdToCall = new HashMap<String, Call>();
		this.callNumToSocket = new HashMap<Integer, SelectableChannel>();
		this.channels = new ArrayList<SelectableChannel>();
		createSockets(numSockets);
	}

	private void createSockets(int numSockets) throws IOException {
		System.out.println("In createSockets");
		for (int i = 0; i < numSockets; i++) {
			System.out.println("Creating socket");
			SelectableChannel chan = nethandler.newChan();
			nethandler.connect(chan, this.defaultTarget);
			chan.configureBlocking(false);
			this.channels.add(chan);
			this.readerThread.newCallQueue.add(new CallAndChan(null, chan));
			selector.wakeup();
		}
	}

	public void stop() throws IOException {
		this.readerThread.interrupt();
		this.selector.wakeup();
	}

	public SocketAddress getdest(Integer callNumber) throws IOException {
		SelectableChannel chan = this.callNumToSocket.get(callNumber);
		return nethandler.getRemoteAddress(chan);
	}

	public SocketAddress getaddr(Integer callNumber) throws IOException {
		SelectableChannel chan = this.callNumToSocket.get(callNumber);
		return nethandler.getLocalAddress(chan);
	}

	public void setdest(Integer callNumber, String host, int port) throws IOException {
		// TODO Auto-generated method stub
	}

	public void send(Integer callNumber, String message) throws IOException {
		SelectableChannel chan = this.callNumToSocket.get(callNumber);
		ByteBuffer buf = ByteBuffer.wrap(message.getBytes());
		nethandler.write(chan, buf);
	}

	public void add(Call call) throws IOException {
		int idx = call.getNumber() % this.channels.size();
		SelectableChannel chan = this.channels.get(idx);
		this.callNumToSocket.put(call.getNumber(), chan);
		this.callIdToCall.put(call.getCallId(), call);
	}

	public void remove(Call call) throws IOException {
		this.callNumToSocket.remove(call.getNumber());
		this.callIdToCall.remove(call.getCallId());
	}

	class CallAndChan {
		public Call call;
		public SelectableChannel chan;

		public CallAndChan(Call call, SelectableChannel chan) {
			this.call = call;
			this.chan = chan;
		}
	}

	public void start() {
		this.readerThread.start();
	}

	public void setListener(SocketAddress bindAddr) throws IOException {
		SelectableChannel listener = this.nethandler.newListener(bindAddr);
		listener.configureBlocking(false);
		if ((listener.validOps() & SelectionKey.OP_ACCEPT) != 0) {
			listener.register(this.selector, SelectionKey.OP_ACCEPT, null);
		} else {
			SIPpMessageParser parser = createParser(listener, null);
			listener.register(this.selector, SelectionKey.OP_READ, parser);
		}
		System.out.println("Created and bound network listener");
	}

	protected class SelectorThread extends Thread {
		public Queue<CallAndChan> newCallQueue = new ConcurrentLinkedQueue<CallAndChan>();

		public void run() {
			System.out.println("Selector thread started...");
			int available = 0;
			while (!this.isInterrupted() && SocketManager.this.selector.isOpen()) {
				try {
					available = SocketManager.this.selector.select();
				} catch (IOException e1) {
					System.out.println("Selector is closed, terminating thread");
					break;
				}
				if (available > 0) {
					Iterator<SelectionKey> keyIterator = SocketManager.this.selector.selectedKeys().iterator();
					while (keyIterator.hasNext()) {
						SelectionKey key = keyIterator.next();
						if (key.isReadable()) {
							readData(key);
						} else if (key.isAcceptable()) {
							acceptCxn(key);
						}
						keyIterator.remove();
					}
				}

				while (!this.newCallQueue.isEmpty()) {
					CallAndChan cnas = this.newCallQueue.poll();
					SIPpMessageParser parser = createParser(cnas.chan, cnas.call);
					try {
						cnas.chan.register(SocketManager.this.selector, SelectionKey.OP_READ, parser);
						System.out.println("Registered channel...");
					} catch (ClosedChannelException e) {
						e.printStackTrace();
						// Nothing to worry about - if the channel is closed, we
						// won't create the key, so no cleanup is needed,
						// and we won't ever need to handle any calls coming in
						// from it.
					}
				}
			}
		}

		private void acceptCxn(SelectionKey key) {
			ServerSocketChannel chan = (ServerSocketChannel) key.channel();
			try {
				SelectableChannel newChan = chan.accept();
				newChan.configureBlocking(false);
				SIPpMessageParser parser = createParser(newChan, null);
				newChan.register(selector, SelectionKey.OP_READ, parser);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void readData(SelectionKey key) {
			SelectableChannel chan = key.channel();
			SIPpMessageParser parser = (SIPpMessageParser) key.attachment();
			ByteBuffer dst = ByteBuffer.allocate(2048);
			try {
				System.out.println("Reading from channel...");
				int result = nethandler.read(chan, dst);
				if (result == -1) {
					System.out.println("Closing channel...");
					nethandler.close(chan);
					key.cancel();
				} else {
					System.out.println("Read from channel...");
					parser.addBytes(dst.array());
					System.out.println("Parsed...");
				}
			} catch (IOException | ParseException e) {
				e.printStackTrace();
				// if the channel isn't actually readable, just
				// skip it
			}
		}
	}

	protected abstract SIPpMessageParser createParser(SelectableChannel chan, Call call);

}
