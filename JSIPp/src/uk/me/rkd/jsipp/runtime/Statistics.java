package uk.me.rkd.jsipp.runtime;

import org.zeromq.ZMQ;

public class Statistics {

	public static Statistics INSTANCE = new Statistics();
	private ZMQ.Socket publisher;
	ZMQ.Context context;
	public String scenarioDesc = "";

	public enum StatType {
		CALL_SUCCESS, CALL_FAILED, MSG_RECVD, MSG_SENT, UNEXPECTED_MSG_RECVD, PAUSE_FINISHED, RECV_TIMED_OUT
	}

	private Statistics() {
		context = ZMQ.context(1);

		publisher = context.socket(ZMQ.PUB);
		publisher.bind("tcp://*:5556");
		new ReplyThread().start();
	}

	private class ReplyThread extends Thread {
		public void run() {
			ZMQ.Socket rep = context.socket(ZMQ.REP);
			rep.bind("tcp://*:5557");

			while (true) {
				String message = rep.recvStr();
				rep.send(scenarioDesc);
			}
		}
	}

	public void report(StatType statname, String... values) {
		StringBuilder out = new StringBuilder(statname.toString());
		for (String value : values) {
			out.append(":");
			out.append(value);
		}
		System.out.println(out.toString());
		publisher.send("SIPP-" + out.toString());
	}
}
