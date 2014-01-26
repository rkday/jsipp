package uk.me.rkd.jsipp.runtime;

import static org.junit.Assert.*;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import uk.me.rkd.jsipp.Configuration;

public class ConfigurationTest {

	@Test
	public void test() throws ParseException {
		CommandLineParser parser = new BasicParser();
		String[] args = {"-r", "22", "-sf", "example.xml", "example.com:5080"};
		CommandLine cmd = parser.parse( Configuration.createOptions(), args);
		Configuration cfg = Configuration.createFromOptions(cmd);
		assertEquals(cfg.getRemotePort(), 5080);
		assertEquals(cfg.getScenarioFile(), "example.xml");
		assertEquals(cfg.getRate(), 22, 0.5);
		assertEquals(cfg.getRemoteHost(), "example.com");
	}

	@Test
	public void testDefaults() throws ParseException {
		CommandLineParser parser = new BasicParser();
		String[] args = {"-sf", "example.xml", "example.com"};
		CommandLine cmd = parser.parse( Configuration.createOptions(), args);
		Configuration cfg = Configuration.createFromOptions(cmd);
		assertEquals(cfg.getRemotePort(), 5060);
		assertEquals(cfg.getScenarioFile(), "example.xml");
		assertEquals(cfg.getRate(), 1, 0.5);
		assertEquals(cfg.getRemoteHost(), "example.com");
	}
	
}
