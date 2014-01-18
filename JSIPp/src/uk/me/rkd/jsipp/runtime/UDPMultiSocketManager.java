package uk.me.rkd.jsipp.runtime;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UDPMultiSocketManager extends SocketManager {

	Selector selector;
	Map<Integer, SelectionKey> callNumToSocket;
	private SocketAddress defaultTarget;
	private Thread readerThread;
	Queue<SelectionKey> deadKeyQueue;
	Queue<CallAndChan> newCallQueue;
	
	private class CallAndChan {
		public Call call;
		public DatagramChannel chan;
		
		public CallAndChan(Call call, DatagramChannel chan) {
			this.call = call;
			this.chan = chan;
		}
	}
	
	public UDPMultiSocketManager(String defaultHost, int defaultPort) throws IOException {
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
			while (!this.isInterrupted() && UDPMultiSocketManager.this.selector.isOpen()) {
				try {
					available = UDPMultiSocketManager.this.selector.select();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					System.out.println("Selector is closed, terminating thread");
					break;
				}
				if (available > 0) {
					int processed = 0;
					Iterator<SelectionKey> keyIterator = UDPMultiSocketManager.this.selector.selectedKeys().iterator();
					while (keyIterator.hasNext()) {
						processed += 1;
						SelectionKey key = keyIterator.next();
						if (key.isReadable()) {
							DatagramChannel chan = (DatagramChannel) key.channel();
							Call attachedCall = (Call) key.attachment();
							ByteBuffer dst = ByteBuffer.allocate(1400);
							try {
								chan.read(dst);
								String message = new String(dst.array());
								attachedCall.process_incoming(message);
							} catch (IOException e) {
								// if the channel isn't actually readable, just skip it
							}
						}
						keyIterator.remove();
					}
				}
				
				while (!UDPMultiSocketManager.this.deadKeyQueue.isEmpty()) {
					SelectionKey key = UDPMultiSocketManager.this.deadKeyQueue.poll();
					key.cancel();
				}
				
				while (!UDPMultiSocketManager.this.newCallQueue.isEmpty()) {
					CallAndChan cnas = UDPMultiSocketManager.this.newCallQueue.poll();					
					SelectionKey key;
					try {
						key = cnas.chan.register(UDPMultiSocketManager.this.selector, (SelectionKey.OP_READ), cnas.call);
						UDPMultiSocketManager.this.callNumToSocket.put(new Integer(cnas.call.getNumber()), key);
					} catch (ClosedChannelException e) {
						// Nothing to worry about - if the channel is closed, we won't create the key, so no cleanup is needed,
						// and we won't ever need to handle any calls coming in from it.
					}
				}
			}
		}
	}
	
	@Override
	public void setdest(Integer callNumber, String host, int port) throws IOException {
		// TODO Auto-generated method stub
		DatagramChannel chan = (DatagramChannel)this.callNumToSocket.get(callNumber).channel();
		chan.connect(new InetSocketAddress(host, port));
	}

	@Override
	public void send(Integer callNumber, String message) throws IOException {
		DatagramChannel chan = (DatagramChannel)this.callNumToSocket.get(callNumber).channel();
		ByteBuffer buf = ByteBuffer.wrap(message.getBytes());
		chan.write(buf);
		// TODO Auto-generated method stub

	}

	@Override
	public void add(Call call) throws IOException {
		DatagramChannel chan = DatagramChannel.open();
		chan.connect(this.defaultTarget);
		chan.configureBlocking(false);
		this.newCallQueue.add(new CallAndChan(call, chan));
		selector.wakeup();
	}
	
	@Override
	public void remove(Call call) throws IOException {
		SelectionKey key = this.callNumToSocket.get(call.getNumber());
		this.callNumToSocket.remove(call.getNumber());
		DatagramChannel chan = (DatagramChannel)key.channel();
		try {
			chan.close();
			System.out.println("Channel closed");
		} catch (IOException e) {
			System.out.println("IOException");
			// TODO Auto-generated catch block
		}	
		this.deadKeyQueue.add(key);
		selector.wakeup();
		System.out.println("Socket removed");
	}

}
