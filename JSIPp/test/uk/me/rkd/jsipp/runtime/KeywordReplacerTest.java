package uk.me.rkd.jsipp.runtime;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

import uk.me.rkd.jsipp.runtime.Call.VariablesList;

public class KeywordReplacerTest {

	private Call c = new Call(1, "1", null, null, new HashMap<String, String>());

	@Test
	public void testSingleMatch() throws Exception {
		String sample = "1 2 3 [call_id] 5";
		VariablesList variables = c.new VariablesList();
		variables.put("call_id", "4");
		String result = KeywordReplacer.replaceKeywords(sample, variables, false);
		assertEquals(result, "1 2 3 4 5");
	}

	@Test
	public void testNoMatch() throws Exception {
		String sample = "1 2 3 [other] 5";
		Call.VariablesList variables = c.new VariablesList();
		variables.put("call_id", "4");
		String result = KeywordReplacer.replaceKeywords(sample, variables, false);
		assertEquals(result, "1 2 3 [other] 5");
	}

	@Test(expected = Exception.class)
	public void testNoMatchException() throws Exception {
		String sample = "1 2 3 [other] 5";
		Call.VariablesList variables = c.new VariablesList();
		variables.put("call_id", "4");
		String result = KeywordReplacer.replaceKeywords(sample, variables, true);
	}

	@Test
	public void testMissingLast() {
		String sample = "1 2 3 [last_Via:] 5\r\n";
		Call.VariablesList variables = c.new VariablesList();
		String result = KeywordReplacer.replaceKeywords(sample, variables, true);
		assertEquals("1 2 3 \r\n", result);
	}

	@Test
	public void testAddition() throws Exception {
		String sample = "1 2 3 [call_id+4] 5";
		Call.VariablesList variables = c.new VariablesList();
		variables.put("call_id", "4");
		String result = KeywordReplacer.replaceKeywords(sample, variables, false);
		assertEquals(result, "1 2 3 8 5");
	}

	@Test
	public void testEscape() throws Exception {
		String sample = "1 2 3 \\u005Bcall_id] 5";
		Call.VariablesList variables = c.new VariablesList();
		variables.put("call_id", "4");
		String result = KeywordReplacer.replaceKeywords(sample, variables, false);
		assertEquals("1 2 3 [call_id] 5", result);
	}
}
