package uk.me.rkd.jsipp.runtime;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.SIPMessageListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.DatagramChannel;
import java.nio.channels.spi.SelectorProvider;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class MultiSocketManager extends SocketManager {

	Selector selector;
	Map<Integer, SelectionKey> callNumToSocket;
	private SocketAddress defaultTarget;
	private Thread readerThread;
	Queue<SelectionKey> deadKeyQueue;
	Queue<CallAndChan> newCallQueue;

	private class CallAndChan {
		public Call call;
		public SelectableChannel chan;

		public CallAndChan(Call call, SelectableChannel chan) {
			this.call = call;
			this.chan = chan;
		}
	}
	
	private class PerSocketListener implements SIPMessageListener {

		private Call call;
		
		public PerSocketListener(Call call) {
			this.call = call;
		}
		
		@Override
		public void handleException(ParseException arg0, SIPMessage arg1,
				Class arg2, String arg3, String arg4) throws ParseException {
			
		}

		@Override
		public void processMessage(SIPMessage arg0) throws Exception {
			call.process_incoming(arg0);
		}

		@Override
		public void sendSingleCLRF() throws Exception {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	protected abstract void write(SelectableChannel chan, ByteBuffer buf) throws IOException;
	protected abstract void read(SelectableChannel chan, ByteBuffer buf) throws IOException;
	protected abstract void connect(SelectableChannel chan, SocketAddress addr) throws IOException;
	protected abstract SelectableChannel newChan() throws IOException;
	
	public MultiSocketManager(String defaultHost, int defaultPort)
			throws IOException {
		this.defaultTarget = new InetSocketAddress(defaultHost, defaultPort);
		this.selector = Selector.open();
		this.callNumToSocket = new HashMap<Integer, SelectionKey>();
		this.readerThread = new SelectorThread();
		this.readerThread.start();
		this.deadKeyQueue = new ConcurrentLinkedQueue<SelectionKey>();
		this.newCallQueue = new ConcurrentLinkedQueue<CallAndChan>();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void stop() throws IOException {
		this.readerThread.interrupt();
		this.selector.close();
	}

	private class SelectorThread extends Thread {
		public void run() {
			int available = 0;
			while (!this.isInterrupted()
					&& MultiSocketManager.this.selector.isOpen()) {
				try {
					available = MultiSocketManager.this.selector.select();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					System.out
							.println("Selector is closed, terminating thread");
					break;
				}
				if (available > 0) {
					Iterator<SelectionKey> keyIterator = MultiSocketManager.this.selector
							.selectedKeys().iterator();
					while (keyIterator.hasNext()) {
						SelectionKey key = keyIterator.next();
						if (key.isReadable()) {
							SelectableChannel chan = key.channel();
							StreamMessageParser parser = (StreamMessageParser) key.attachment();
							ByteBuffer dst = ByteBuffer.allocate(2048);
							try {
								read(chan, dst);
								parser.addBytes(dst.array());
							} catch (IOException e) {
								// if the channel isn't actually readable, just
								// skip it
							}
						}
						keyIterator.remove();
					}
				}

				while (!MultiSocketManager.this.newCallQueue.isEmpty()) {
					CallAndChan cnas = MultiSocketManager.this.newCallQueue
							.poll();
					SelectionKey key;
					StreamMessageParser parser = new StreamMessageParser(new PerSocketListener(cnas.call), 4096);
					try {
						key = cnas.chan.register(
								MultiSocketManager.this.selector,
								(SelectionKey.OP_READ), parser);
						MultiSocketManager.this.callNumToSocket.put(
								new Integer(cnas.call.getNumber()), key);
					} catch (ClosedChannelException e) {
						// Nothing to worry about - if the channel is closed, we
						// won't create the key, so no cleanup is needed,
						// and we won't ever need to handle any calls coming in
						// from it.
					}
				}

				while (!MultiSocketManager.this.deadKeyQueue.isEmpty()) {
					SelectionKey key = MultiSocketManager.this.deadKeyQueue.poll();
					key.cancel();
				}
			}
		}
	}

	@Override
	public void setdest(Integer callNumber, String host, int port)
			throws IOException {
		// TODO Auto-generated method stub
		SelectableChannel chan = this.callNumToSocket.get(
				callNumber).channel();
		connect(chan, new InetSocketAddress(host, port));
	}

	@Override
	public void send(Integer callNumber, String message) throws IOException {
		SelectionKey key = this.callNumToSocket.get(callNumber);
		if (key == null) {
			// We may (rarely) try and send a message before the selector thread
			// has created the channel. In this case, sleep for 200ms to let it
			// catch up, and log and fail if it's still null.
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			}
			key = this.callNumToSocket.get(callNumber);
		}
		if (key == null) {
			System.out.println("Attempted to send message for a call with no associated socket");
			return;
		}
		SelectableChannel chan = key.channel();
		ByteBuffer buf = ByteBuffer.wrap(message.getBytes());
		write(chan, buf);
		// TODO Auto-generated method stub

	}

	@Override
	public void add(Call call) throws IOException {
		SelectableChannel chan = newChan();
		connect(chan, this.defaultTarget);
		chan.configureBlocking(false);
		this.newCallQueue.add(new CallAndChan(call, chan));
		selector.wakeup();
	}

	@Override
	public void remove(Call call) throws IOException {
		SelectionKey key = this.callNumToSocket.get(call.getNumber());
		this.callNumToSocket.remove(call.getNumber());
		SelectableChannel chan = key.channel();
		try {
			chan.close();
			// System.out.println("Channel closed");
		} catch (IOException e) {
			System.out.println("IOException");
			// TODO Auto-generated catch block
		}
		this.deadKeyQueue.add(key);
		selector.wakeup();
		// System.out.println("Socket removed");
	}

}
