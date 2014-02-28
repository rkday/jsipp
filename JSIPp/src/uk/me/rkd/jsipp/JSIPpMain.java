package uk.me.rkd.jsipp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.xml.sax.SAXException;

import uk.me.rkd.jsipp.compiler.Scenario;
import uk.me.rkd.jsipp.compiler.SimpleVariableTable;
import uk.me.rkd.jsipp.compiler.VariableTable;
import uk.me.rkd.jsipp.runtime.CallOpeningTask;
import uk.me.rkd.jsipp.runtime.Scheduler;
import uk.me.rkd.jsipp.runtime.network.SocketManager;
import uk.me.rkd.jsipp.runtime.network.TCPMultiplexingSocketManager;
import uk.me.rkd.jsipp.runtime.network.UDPMultiplexingSocketManager;

public class JSIPpMain {

	public JSIPpMain() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String argv[]) throws ParserConfigurationException, SAXException, IOException,
	        InterruptedException, ParseException {
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(Configuration.createOptions(), argv);
		if (cmd.hasOption("h")) {
			new HelpFormatter().printHelp("sipp.jar [OPTIONS] remotehost[:port]", Configuration.createOptions());
			return;
		}
		Configuration cfg = Configuration.createFromOptions(cmd);
		Scenario scenario = Scenario.fromXMLFilename(cfg.getScenarioFile());
		if (scenario.isUac() && (cmd.getArgList().size() != 1)) {
			new HelpFormatter().printHelp("sipp.jar [OPTIONS] remotehost[:port]", Configuration.createOptions());
			return;
		}
		Scheduler sched = new Scheduler(50);
		VariableTable globalVariables = SimpleVariableTable.global();
		globalVariables.putKeyword("service", "sipp");
		globalVariables.putKeyword("pid", UUID.randomUUID().toString());
		SocketManager sm;

		if (cfg.getTransport().equals("un")) {
			sm = new UDPMultiplexingSocketManager(cfg.getRemoteHost(), cfg.getRemotePort(), 4096);
			globalVariables.putKeyword("transport", "UDP");
		} else if (cfg.getTransport().equals("tn")) {
			sm = new TCPMultiplexingSocketManager(cfg.getRemoteHost(), cfg.getRemotePort(), 4096);
			globalVariables.putKeyword("transport", "TCP");
		} else if (cfg.getTransport().equals("t1")) {
			sm = new TCPMultiplexingSocketManager(cfg.getRemoteHost(), cfg.getRemotePort(), 1);
			globalVariables.putKeyword("transport", "TCP");
		} else if (cfg.getTransport().equals("u1")) {
			sm = new UDPMultiplexingSocketManager(cfg.getRemoteHost(), cfg.getRemotePort(), 1);
			globalVariables.putKeyword("transport", "UDP");
		} else {
			sm = new UDPMultiplexingSocketManager(cfg.getRemoteHost(), cfg.getRemotePort(), 4096);
			globalVariables.putKeyword("transport", "UDP");
		}

		if (scenario.isUas()) {
			sm = new TCPMultiplexingSocketManager(cfg.getRemoteHost(), cfg.getRemotePort(), 0);
			InetSocketAddress bindAddr = new InetSocketAddress(cfg.getListenIP(), cfg.getListenPort());
			sm.setListener(bindAddr);
		}

		CallOpeningTask opentask = CallOpeningTask.getInstance(scenario, sm, cfg.getRate(), sched.getTimer());
		sm.start();

		if (scenario.isUac()) {
			sched.add(opentask, 10);
		}

		Thread.sleep(5 * 60 * 1000);

		opentask.stop();

		// Wait for a second so all the calls finish
		Thread.sleep(1000);

		sm.stop();
		sched.stop();
		System.out.println("fin");
	}
}
