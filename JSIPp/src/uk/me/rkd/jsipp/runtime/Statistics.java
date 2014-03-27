package uk.me.rkd.jsipp.runtime;

import java.util.concurrent.ArrayBlockingQueue;

import org.zeromq.ZMQ;

public class Statistics {

	public static Statistics INSTANCE = new Statistics();
	private ZMQ.Socket publisher;
	ZMQ.Context context;
	public String scenarioDesc = "";
	ArrayBlockingQueue<String> toPublish = new ArrayBlockingQueue<String>(1024);

	public enum StatType {
		CALL_SUCCESS, CALL_FAILURE, PHASE_SUCCESS, UNEXPECTED_MSG_RECVD, RECV_TIMED_OUT, CALL_BEGIN
	}

	private Statistics() {
		context = ZMQ.context(1);

		new ReplyThread().start();
		new PublisherThread().start();
	}

	private class ReplyThread extends Thread {
		public void run() {
			ZMQ.Socket rep = context.socket(ZMQ.REP);
			rep.bind("tcp://*:5557");

			while (true) {
				String message = rep.recvStr();
				if (message.equals("get rate")) {
					rep.send(String.format("%f", CallOpeningTask.getInstance().getRate()));
				} else if (message.startsWith("set rate ")) {
					double rate = Double.parseDouble(message.substring(9));
					CallOpeningTask.getInstance().setRate(rate);
					rep.send("OK");
				} else {
					rep.send(scenarioDesc);
				}
			}
		}
	}

	private class PublisherThread extends Thread {
		public void run() {
			publisher = context.socket(ZMQ.PUB);
			publisher.bind("tcp://*:5556");
			while (true) {
				String msg;
				try {
					msg = Statistics.this.toPublish.take();
					publisher.send(msg);
				} catch (InterruptedException e) {
					// Drop message
				}
			}
		}
	}

	public void report(StatType statname, String... values) {
		StringBuilder out = new StringBuilder(statname.toString());
		for (String value : values) {
			out.append(":");
			out.append(value);
		}
		//System.out.println(out.toString());
		try {
			toPublish.put("SIPP-" + out.toString());
		} catch (InterruptedException e) {
			// Do nothing
		}
	}
}
