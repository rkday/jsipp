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

	/**
	 * @return the rate_increase
	 */
	public double getRateIncrease() {
		return rateIncrease;
	}

	/**
	 * @return the rate_increase_period
	 */
	public long getRateIncreasePeriod() {
		return rateIncreasePeriod;
	}

	/**
	 * @return the rate_max
	 */
	public double getRateMax() {
		return rateMax;
	}

	private double rateIncrease;
	private long rateIncreasePeriod;
	private double rateMax;
    private boolean rtpSink;

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
		INSTANCE.rateIncrease = Double.parseDouble(cmd.getOptionValue("rate_increase", "0"));
		INSTANCE.rateIncreasePeriod = Integer.parseInt(cmd.getOptionValue("rate_increase_period", "60"));
		INSTANCE.rateMax = Double.parseDouble(cmd.getOptionValue("rate_max", "100"));

		INSTANCE.listenIP = cmd.getOptionValue("i", "0.0.0.0");
		INSTANCE.listenPort = Integer.parseInt(cmd.getOptionValue("p", "5060"));

		INSTANCE.rtpSink = cmd.hasOption("rtp_sink");

		return INSTANCE;
	}

	public boolean isRtpSink() {
        return rtpSink;
    }

    public static Options createOptions() {
		Options opts = new Options();
		Option help = new Option("h", "display help text");
		Option scenarioFile = OptionBuilder.withArgName("file").hasArg().withDescription("The XML file defining the SIPp scenario").create("sf");
		Option rate = OptionBuilder.withArgName("rate").hasArg().withDescription("The number of new calls to be created per second (default 1)").create("r");
		Option transport = OptionBuilder.withArgName("transport").hasArg().withDescription("tn: one TCP socket per call\nun: one UDP socket per call\nt1: all calls multiplexed on one TCP socket\nu1: all calls multiplexed on one UDP socket").create("t");
		Option listen_ip = OptionBuilder.withArgName("listen_ip").hasArg().withDescription("For UAS mode, the IP address to listen on").create("i");
		Option listen_port = OptionBuilder.withArgName("listen_port").hasArg().withDescription("For UAS mode, the port to listen on (default 5060)").create("p");
		Option rate_increase = OptionBuilder.withArgName("rate_increase").hasArg().withDescription("If rate should ramp up periodically, specify the number of calls/second it should increase by").create("rate_increase");
		Option rate_increase_period = OptionBuilder.withArgName("rate_increase_period").hasArg().withDescription("If rate should ramp up periodically, specify the number of seconds between each step up").create("rate_increase_period");
		Option rate_max = OptionBuilder.withArgName("rate_max").hasArg().withDescription("If rate should ramp up periodically, specify the maximum number of calls/second").create("rate_max");
        Option rtp_sink = OptionBuilder.withArgName("rtp_sink").withDescription("Open a socket to receive RTP for each call, and calculate jitter/packet loss stats").create("rtp_sink");

		opts.addOption(help);
		opts.addOption(scenarioFile);
		opts.addOption(rate);
		opts.addOption(rate_increase);
		opts.addOption(rate_increase_period);
		opts.addOption(rate_max);

		opts.addOption(transport);
		opts.addOption(listen_ip);
		opts.addOption(listen_port);
		
		opts.addOption(rtp_sink);
		return opts;
	}

	public String getTransport() {
		return transport;
	}

}
