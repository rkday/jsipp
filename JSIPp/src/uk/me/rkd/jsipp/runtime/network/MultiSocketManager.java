package uk.me.rkd.jsipp.runtime.network;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.SIPMessageListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import uk.me.rkd.jsipp.runtime.Call;

public abstract class MultiSocketManager extends SocketManager {

	Map<Integer, SelectableChannel> callNumToSocket;

	class PerSocketListener implements SIPMessageListener {

		private Call call;

		public PerSocketListener(Call call) {
			super();
			this.call = call;
		}

		@Override
		public void handleException(ParseException arg0, SIPMessage arg1, Class arg2, String arg3, String arg4)
		        throws ParseException {
			arg0.printStackTrace();
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

	public MultiSocketManager(String defaultHost, int defaultPort) throws IOException {
		super(defaultHost, defaultPort);
		this.callNumToSocket = new HashMap<Integer, SelectableChannel>();
	}

	@Override
	public void stop() throws IOException {
		this.readerThread.interrupt();
		this.selector.close();
	}

	@Override
	public void setdest(Integer callNumber, String host, int port) throws IOException {
		// TODO Auto-generated method stub
		SelectableChannel chan = this.callNumToSocket.get(callNumber);
		connect(chan, new InetSocketAddress(host, port));
	}

	@Override
	public void send(Integer callNumber, String message) throws IOException {
		SelectableChannel chan = this.callNumToSocket.get(callNumber);
		if (chan == null) {
			// We may (rarely) try and send a message before the selector thread
			// has created the channel. In this case, sleep for 200ms to let it
			// catch up, and log and fail if it's still null.
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			}
			chan = this.callNumToSocket.get(callNumber);
		}
		if (chan == null) {
			System.out.println("Attempted to send message for a call with no associated socket");
			return;
		}
		ByteBuffer buf = ByteBuffer.wrap(message.getBytes());
		write(chan, buf);
	}

	@Override
	public void add(Call call) throws IOException {
		SelectableChannel chan = newChan();
		connect(chan, this.defaultTarget);
		chan.configureBlocking(false);
		this.readerThread.newCallQueue.add(new CallAndChan(call, chan));
		this.callNumToSocket.put(call.getNumber(), chan);
		selector.wakeup();
	}

	@Override
	public void remove(Call call) throws IOException {
		SelectableChannel chan = this.callNumToSocket.get(call.getNumber());
		this.callNumToSocket.remove(call.getNumber());
		try {
			chan.close();
			// System.out.println("Channel closed");
		} catch (IOException e) {
			System.out.println("IOException");
			// TODO Auto-generated catch block
		}
		this.readerThread.deadKeyQueue.add(chan.keyFor(selector));
		selector.wakeup();
		// System.out.println("Socket removed");
	}

}
