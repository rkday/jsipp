package uk.me.rkd.jsipp.runtime.network;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
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
import uk.me.rkd.jsipp.runtime.RTPPacket;
import uk.me.rkd.jsipp.runtime.RTPSession;
import uk.me.rkd.jsipp.runtime.parsers.SIPpMessageParser;

public class RTPSocketManager {

	Selector selector;
	SelectorThread readerThread;
	NetworkProtocolHandler nethandler;
	Map<Integer, SelectableChannel> callNumToSocket;
	Map<String, Call> callIdToCall;
	private List<DatagramChannel> channels;

	public static RTPSocketManager INSTANCE = new RTPSocketManager();
	
	private RTPSocketManager() {
		try {
			this.selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.readerThread = new SelectorThread();
		this.channels = new ArrayList<DatagramChannel>();
	}

	public int add(String id) throws IOException {
			DatagramChannel chan = DatagramChannel.open();
			chan.bind(null);
			chan.configureBlocking(false);
			this.readerThread.add(chan, id);
			selector.wakeup();
			return chan.socket().getLocalPort();
	}
	
	public void remove(String id) {
	    this.readerThread.remove(id);
	}

	public void start() {
		this.readerThread.start();
	}

	protected class SelectorThread extends Thread {
	    private Map<String, SelectionKey> keysById = new HashMap<String, SelectionKey>();
		private Queue<RTPSession> newSessionQueue = new ConcurrentLinkedQueue<RTPSession>();
        private Queue<String> idsToDelete = new ConcurrentLinkedQueue<String>();

		public void add(DatagramChannel c, String id) {
		    newSessionQueue.add(new RTPSession(id, c));
		}
		
		public void remove(String id) {
		    idsToDelete.add(id);
		}
		
		public void run() {
			System.out.println("Selector thread started...");
			int available = 0;
			while (!this.isInterrupted() && RTPSocketManager.this.selector.isOpen()) {
				try {
					available = RTPSocketManager.this.selector.select();
				} catch (IOException e1) {
					System.out.println("Selector is closed, terminating thread");
					break;
				}
				if (available > 0) {
					Iterator<SelectionKey> keyIterator = RTPSocketManager.this.selector.selectedKeys().iterator();
					while (keyIterator.hasNext()) {
						SelectionKey key = keyIterator.next();
						if (key.isReadable()) {
							readData(key);
						}
						keyIterator.remove();
					}
				}

				while (!this.newSessionQueue.isEmpty()) {
					RTPSession sess = this.newSessionQueue.poll();
					try {
						SelectionKey k = sess.getChannel().register(RTPSocketManager.this.selector, SelectionKey.OP_READ, sess);
						keysById.put(sess.getId(), k);
					} catch (ClosedChannelException e) {
						e.printStackTrace();
						// Nothing to worry about - if the channel is closed, we
						// won't create the key, so no cleanup is needed,
						// and we won't ever need to handle any calls coming in
						// from it.
					}
				}
                while (!this.idsToDelete.isEmpty()) {
                    String id = this.idsToDelete.poll();
                    try {
                        SelectionKey k = keysById.get(id);
                        k.cancel();
                        k.channel().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Nothing to worry about - if the channel is closed,
                        // no cleanup is needed,
                        // and we won't ever need to handle any calls coming in
                        // from it.
                    }
                }
			}
		}

		private void readData(SelectionKey key) {
			RTPSession sess = (RTPSession) key.attachment();
			DatagramChannel chan = (DatagramChannel)key.channel();
			ByteBuffer dst = ByteBuffer.allocate(512);
			try {
				chan.receive(dst);
				dst.limit(dst.position());
				dst.position(0);
				RTPPacket packet = RTPPacket.decode(dst);
				//System.out.println(packet.toString());
				sess.update(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
