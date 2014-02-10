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
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import uk.me.rkd.jsipp.runtime.Call;
import uk.me.rkd.jsipp.runtime.parsers.SIPpMessageParser;

public abstract class SocketManager {
	Selector selector;
	SelectorThread readerThread;
	SocketAddress defaultTarget;
	NetworkProtocolHandler nethandler;

	public SocketManager(String defaultHost, int defaultPort, NetworkProtocolHandler nethandler) throws IOException {
		if (defaultHost != null) {
			this.defaultTarget = new InetSocketAddress(defaultHost, defaultPort);
		}
		this.selector = Selector.open();
		this.readerThread = new SelectorThread();
		this.nethandler = nethandler;
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

	protected class SelectorThread extends Thread {
		public Queue<SelectionKey> deadKeyQueue = new ConcurrentLinkedQueue<SelectionKey>();
		public Queue<CallAndChan> newCallQueue = new ConcurrentLinkedQueue<CallAndChan>();

		public void run() {
			System.out.println("Selector thread started...");
			int available = 0;
			while (!this.isInterrupted() && SocketManager.this.selector.isOpen()) {
				try {
					available = SocketManager.this.selector.select();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					System.out.println("Selector is closed, terminating thread");
					break;
				}
				if (available > 0) {
					System.out.println("Selectors available...");
					Iterator<SelectionKey> keyIterator = SocketManager.this.selector.selectedKeys().iterator();
					while (keyIterator.hasNext()) {
						SelectionKey key = keyIterator.next();
						if (key.isReadable()) {
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
						} else if (key.isAcceptable()) {
							System.out.println("Accepting new connection...");
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

				while (!this.deadKeyQueue.isEmpty()) {
					SelectionKey key = this.deadKeyQueue.poll();
					key.cancel();
				}
			}
		}
	}

	public abstract void setdest(Integer callNumber, String host, int port) throws IOException;

	public abstract SocketAddress getdest(Integer callNumber) throws IOException;

	public abstract SocketAddress getaddr(Integer callNumber) throws IOException;

	public abstract void send(Integer callNumber, String message) throws IOException;

	public abstract void add(Call call) throws IOException;

	public abstract void remove(Call call) throws IOException;

	public abstract void stop() throws IOException;

	protected abstract SIPpMessageParser createParser(SelectableChannel chan, Call call);

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
}
