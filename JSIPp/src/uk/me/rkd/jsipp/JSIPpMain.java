package uk.me.rkd.jsipp;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import uk.me.rkd.jsipp.compiler.Scenario;
import uk.me.rkd.jsipp.runtime.CallOpeningTask;
import uk.me.rkd.jsipp.runtime.Scheduler;
import uk.me.rkd.jsipp.runtime.SocketManager;
import uk.me.rkd.jsipp.runtime.UDPMultiSocketManager;

public class JSIPpMain {

	public JSIPpMain() {
		// TODO Auto-generated constructor stub
	}
	
	private static Options createOptions() {
		Options opts = new Options();
		Option help = new Option("h", "display help text");
		Option scenarioFile = OptionBuilder.withArgName( "file" )
				.hasArg()
                .withDescription(  "The XML file defining the SIPp scenario" )
                .create( "sf" );
		Option rate = OptionBuilder.withArgName( "rate" )
				.hasArg()
                .withDescription(  "The number of new calls to be created per second (default 1)" )
                .create( "r" );
		opts.addOption(help);
		opts.addOption(scenarioFile);
		opts.addOption(rate);
		return opts;
	}

	public static void main (String argv[]) throws ParserConfigurationException, SAXException, IOException, InterruptedException, ParseException {
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse( createOptions(), argv);
		if (cmd.hasOption("h") || (cmd.getArgList().size() != 1)) {
			new HelpFormatter().printHelp("sipp.jar [OPTIONS] remotehost[:port]", createOptions());
			return;
		}
		Configuration cfg = Configuration.createFromOptions(cmd);
		Scenario scenario = Scenario.fromXMLFilename(cfg.getScenarioFile());
		Scheduler sched = new Scheduler(50);
		SocketManager sm = new UDPMultiSocketManager(cfg.getRemoteHost(), cfg.getRemotePort());
		CallOpeningTask opentask = new CallOpeningTask(scenario, sm, cfg.getRate());

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
