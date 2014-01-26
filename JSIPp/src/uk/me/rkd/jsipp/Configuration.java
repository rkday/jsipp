package uk.me.rkd.jsipp;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public enum Configuration {
	INSTANCE;
	
	private String remoteHost;
	private int remotePort;
	/**
	 * @return the remoteHost
	 */
	public String getRemoteHost() {
		return remoteHost;
	}

	/**
	 * @return the remotePort
	 */
	public int getRemotePort() {
		return remotePort;
	}

	/**
	 * @return the scenarioFile
	 */
	public String getScenarioFile() {
		return scenarioFile;
	}

	/**
	 * @return the rate
	 */
	public double getRate() {
		return rate;
	}

	private String scenarioFile;
	private double rate;
	private String transport;
	
	public static Configuration createFromOptions(CommandLine cmd) {
		String[] hostport = cmd.getArgs()[0].split(":", 2);
		String host = hostport[0];
		int port = 5060;
		
		if (hostport.length == 2) {
			port = Integer.parseInt(hostport[1]);
		}
		INSTANCE.remoteHost = host;
		INSTANCE.remotePort = port;
		INSTANCE.scenarioFile = cmd.getOptionValue("sf");
		if (cmd.getOptionValue("t","un").equals("tn")) {
			INSTANCE.transport = "tcp";
		} else {
			INSTANCE.transport = "udp";
		}
		INSTANCE.rate = Double.parseDouble(cmd.getOptionValue("r", "1"));
		return INSTANCE;
	}
	
	public static Options createOptions() {
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
		Option transport = OptionBuilder.withArgName( "transport" )
				.hasArg()
                .withDescription(  "tn: one TCP socket per call\nun: one UDP socket per call" )
                .create( "t" );
		opts.addOption(help);
		opts.addOption(scenarioFile);
		opts.addOption(rate);
		opts.addOption(transport);
		return opts;
	}

	public String getTransport() {
		return transport;
	}

}
