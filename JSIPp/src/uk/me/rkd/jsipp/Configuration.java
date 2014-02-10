package uk.me.rkd.jsipp;

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
	private String listenIP;

	/**
	 * @return the listenIP
	 */
	public String getListenIP() {
		return listenIP;
	}

	/**
	 * @return the listenPort
	 */
	public int getListenPort() {
		return listenPort;
	}

	private int listenPort;

	public static Configuration createFromOptions(CommandLine cmd) {
		String host = null;
		int port = 5060;

		if (cmd.getArgs().length > 0) {
			String[] hostport = cmd.getArgs()[0].split(":", 2);
			host = hostport[0];

			if (hostport.length == 2) {
				port = Integer.parseInt(hostport[1]);
			}
		}
		INSTANCE.remoteHost = host;
		INSTANCE.remotePort = port;
		INSTANCE.scenarioFile = cmd.getOptionValue("sf");
		INSTANCE.transport = cmd.getOptionValue("t", "un");
		INSTANCE.rate = Double.parseDouble(cmd.getOptionValue("r", "1"));
		INSTANCE.listenIP = cmd.getOptionValue("i", "0.0.0.0");
		INSTANCE.listenPort = Integer.parseInt(cmd.getOptionValue("p", "5060"));

		return INSTANCE;
	}

	public static Options createOptions() {
		Options opts = new Options();
		Option help = new Option("h", "display help text");
		Option scenarioFile = OptionBuilder.withArgName("file").hasArg().withDescription("The XML file defining the SIPp scenario").create("sf");
		Option rate = OptionBuilder.withArgName("rate").hasArg().withDescription("The number of new calls to be created per second (default 1)").create("r");
		Option transport = OptionBuilder.withArgName("transport").hasArg().withDescription("tn: one TCP socket per call\nun: one UDP socket per call\nt1: all calls multiplexed on one TCP socket\nu1: all calls multiplexed on one UDP socket").create("t");
		Option listen_ip = OptionBuilder.withArgName("listen_ip").hasArg().withDescription("For UAS mode, the IP address to listen on").create("i");
		Option listen_port = OptionBuilder.withArgName("listen_port").hasArg().withDescription("For UAS mode, the port to listen on (default 5060)").create("p");

		opts.addOption(help);
		opts.addOption(scenarioFile);
		opts.addOption(rate);
		opts.addOption(transport);
		opts.addOption(listen_ip);
		opts.addOption(listen_port);
		return opts;
	}

	public String getTransport() {
		return transport;
	}

}
