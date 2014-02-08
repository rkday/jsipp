package uk.me.rkd.jsipp.runtime;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class KeywordReplacerTest {

	@Test
	public void testSingleMatch() throws Exception {
		String sample = "1 2 3 [call_id] 5";
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("call_id", "4");
		String result = KeywordReplacer.replaceKeywords(sample, variables, false);
		assertEquals(result, "1 2 3 4 5");
	}

	@Test
	public void testNoMatch() throws Exception {
		String sample = "1 2 3 [other] 5";
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("call_id", "4");
		String result = KeywordReplacer.replaceKeywords(sample, variables, false);
		assertEquals(result, "1 2 3 [other] 5");
	}

	@Test(expected = Exception.class)
	public void testNoMatchException() throws Exception {
		String sample = "1 2 3 [other] 5";
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("call_id", "4");
		String result = KeywordReplacer.replaceKeywords(sample, variables, true);
	}

	@Test
	public void testMissingLast() {
		String sample = "1 2 3 [last_Via:] 5\r\n";
		Map<String, String> variables = new HashMap<String, String>();
		String result = KeywordReplacer.replaceKeywords(sample, variables, true);
		assertEquals("1 2 3 \r\n", result);
	}

	@Test
	public void testAddition() throws Exception {
		String sample = "1 2 3 [call_id+4] 5";
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("call_id", "4");
		String result = KeywordReplacer.replaceKeywords(sample, variables, false);
		assertEquals(result, "1 2 3 8 5");
	}

}
