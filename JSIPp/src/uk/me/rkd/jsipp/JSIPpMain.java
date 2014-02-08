package uk.me.rkd.jsipp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.xml.sax.SAXException;

import uk.me.rkd.jsipp.compiler.Scenario;
import uk.me.rkd.jsipp.runtime.CallOpeningTask;
import uk.me.rkd.jsipp.runtime.Scheduler;
import uk.me.rkd.jsipp.runtime.network.SocketManager;
import uk.me.rkd.jsipp.runtime.network.TCPMultiSocketManager;
import uk.me.rkd.jsipp.runtime.network.TCPMultiplexingSocketManager;
import uk.me.rkd.jsipp.runtime.network.UDPMultiSocketManager;
import uk.me.rkd.jsipp.runtime.network.UDPMultiplexingSocketManager;

public class JSIPpMain {

	public JSIPpMain() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String argv[]) throws ParserConfigurationException, SAXException, IOException,
	        InterruptedException, ParseException {
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(Configuration.createOptions(), argv);
		if (cmd.hasOption("h") || (cmd.getArgList().size() != 1)) {
			new HelpFormatter().printHelp("sipp.jar [OPTIONS] remotehost[:port]", Configuration.createOptions());
			return;
		}
		Configuration cfg = Configuration.createFromOptions(cmd);
		Scenario scenario = Scenario.fromXMLFilename(cfg.getScenarioFile());
		Scheduler sched = new Scheduler(50);
		Map<String, String> globalVariables = new HashMap<String, String>();
		globalVariables.put("service", "sipp");
		globalVariables.put("pid", UUID.randomUUID().toString());
		SocketManager sm;
		if (cfg.getTransport().equals("un")) {
			sm = new UDPMultiSocketManager(cfg.getRemoteHost(), cfg.getRemotePort());
			globalVariables.put("transport", "UDP");
		} else if (cfg.getTransport().equals("tn")) {
			sm = new TCPMultiSocketManager(cfg.getRemoteHost(), cfg.getRemotePort());
			globalVariables.put("transport", "TCP");
		} else if (cfg.getTransport().equals("t1")) {
			sm = new TCPMultiplexingSocketManager(cfg.getRemoteHost(), cfg.getRemotePort(), 1);
			globalVariables.put("transport", "TCP");
		} else if (cfg.getTransport().equals("u1")) {
			sm = new UDPMultiplexingSocketManager(cfg.getRemoteHost(), cfg.getRemotePort(), 1);
			globalVariables.put("transport", "UDP");
		} else {
			sm = new UDPMultiSocketManager(cfg.getRemoteHost(), cfg.getRemotePort());
			globalVariables.put("transport", "UDP");
		}

		CallOpeningTask opentask = new CallOpeningTask(scenario, sm, cfg.getRate(), globalVariables);

		sched.add(opentask, 10);
		Thread.sleep(5 * 60 * 1000);

		opentask.stop();

		// Wait for a second so all the calls finish
		Thread.sleep(1000);

		sm.stop();
		sched.stop();
		System.out.println("fin");
	}
}
