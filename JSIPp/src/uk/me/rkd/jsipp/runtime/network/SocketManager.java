package uk.me.rkd.jsipp.runtime.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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

	public SocketManager(String defaultHost, int defaultPort) throws IOException {
		this.defaultTarget = new InetSocketAddress(defaultHost, defaultPort);
		this.selector = Selector.open();
		this.readerThread = new SelectorThread();
		this.readerThread.start();
	}

	class CallAndChan {
		public Call call;
		public SelectableChannel chan;

		public CallAndChan(Call call, SelectableChannel chan) {
			this.call = call;
			this.chan = chan;
		}
	}

	protected class SelectorThread extends Thread {
		public Queue<SelectionKey> deadKeyQueue = new ConcurrentLinkedQueue<SelectionKey>();
		public Queue<CallAndChan> newCallQueue = new ConcurrentLinkedQueue<CallAndChan>();

		public void run() {
			int available = 0;
			while (!this.isInterrupted() && SocketManager.this.selector.isOpen()) {
				System.out.println("Selector loop...");
				try {
					available = SocketManager.this.selector.select(50);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					System.out.println("Selector is closed, terminating thread");
					break;
				}
				if (available > 0) {
					Iterator<SelectionKey> keyIterator = SocketManager.this.selector.selectedKeys().iterator();
					while (keyIterator.hasNext()) {
						SelectionKey key = keyIterator.next();
						if (key.isReadable()) {
							SelectableChannel chan = key.channel();
							SIPpMessageParser parser = (SIPpMessageParser) key.attachment();
							ByteBuffer dst = ByteBuffer.allocate(2048);
							try {
								System.out.println("Reading from channel...");
								read(chan, dst);
								parser.addBytes(dst.array());
							} catch (IOException | ParseException e) {
								e.printStackTrace();
								// if the channel isn't actually readable, just
								// skip it
							}
						}
						keyIterator.remove();
					}
				}

				while (!this.newCallQueue.isEmpty()) {
					CallAndChan cnas = this.newCallQueue.poll();
					SelectionKey key;
					SIPpMessageParser parser = createParser(cnas.call);
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

	public abstract void send(Integer callNumber, String message) throws IOException;

	public abstract void add(Call call) throws IOException;

	public abstract void remove(Call call) throws IOException;

	public abstract void stop() throws IOException;

	protected abstract void write(SelectableChannel chan, ByteBuffer buf) throws IOException;

	protected abstract void read(SelectableChannel chan, ByteBuffer buf) throws IOException;

	protected abstract void connect(SelectableChannel chan, SocketAddress addr) throws IOException;

	protected abstract SelectableChannel newChan() throws IOException;

	protected abstract SIPpMessageParser createParser(Call call);
}
