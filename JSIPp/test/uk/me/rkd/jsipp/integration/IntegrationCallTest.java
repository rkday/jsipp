package uk.me.rkd.jsipp.integration;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import uk.me.rkd.jsipp.compiler.Scenario;
import uk.me.rkd.jsipp.runtime.Call;
import uk.me.rkd.jsipp.runtime.CallOpeningTask;
import uk.me.rkd.jsipp.runtime.Scheduler;
import uk.me.rkd.jsipp.runtime.network.SocketManager;
import uk.me.rkd.jsipp.runtime.network.TCPMultiplexingSocketManager;

public class IntegrationCallTest {

	@Test
	public void test() throws ParserConfigurationException, SAXException, IOException, InterruptedException {
		Scheduler sched = new Scheduler(50);

		Scenario uasScenario = Scenario.fromXMLFilename("resources/message-uas.xml");

		Scenario uacScenario = Scenario.fromXMLFilename("resources/message.xml");

		SocketManager uasSM = new TCPMultiplexingSocketManager(null, 0, 0);
		InetSocketAddress bindAddr = new InetSocketAddress("127.0.0.1", 15060);
		uasSM.setListener(bindAddr);

		SocketManager uacSM = new TCPMultiplexingSocketManager("127.0.0.1", 15060, 1);

		CallOpeningTask opentask = CallOpeningTask.getInstance(uasScenario, uasSM, 0, sched.getTimer());

		uasSM.start();
		uacSM.start();

		Call call = new Call(8, "uac1", uacScenario.phases(), uacSM);
		call.registerSocket();
		sched.getTimer().newTimeout(call, 10, TimeUnit.MILLISECONDS);
		Thread.sleep(500);
		assertTrue(call.hasCompleted());
		opentask.stop();
		CallOpeningTask.reset();
		sched.stop();
	}

}
