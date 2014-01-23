package uk.me.rkd.jsipp;

import java.util.List;

import org.apache.commons.cli.CommandLine;

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
	
	static Configuration createFromOptions(CommandLine cmd) {
		String[] hostport = cmd.getArgs()[0].split(":", 2);
		String host = hostport[0];
		int port = 5060;
		if (hostport.length == 2) {
			port = Integer.parseInt(hostport[1]);
		}
		INSTANCE.remoteHost = host;
		INSTANCE.remotePort = port;
		INSTANCE.scenarioFile = cmd.getOptionValue("sf");
		INSTANCE.rate = Double.parseDouble(cmd.getOptionValue("r", "1"));
		return INSTANCE;
	}
	
	static Configuration createFromDefaults() {
		INSTANCE.remoteHost = "localhost";
		INSTANCE.remotePort = 5060;
		INSTANCE.rate = 2000;
		INSTANCE.scenarioFile = "resources/message.xml";
		return INSTANCE;
	}
}
