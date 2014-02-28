package uk.me.rkd.jsipp.runtime;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.me.rkd.jsipp.compiler.SimpleVariableTable;
import uk.me.rkd.jsipp.compiler.VariableTable;

public class KeywordReplacerTest {
	@Test
	public void testSingleMatch() throws Exception {
		String sample = "1 2 3 [call_id] 5";
		VariableTable variables = new SimpleVariableTable();
		variables.putKeyword("call_id", "4");
		String result = KeywordReplacer.replaceKeywords(sample, variables, false);
		assertEquals(result, "1 2 3 4 5");
	}

	@Test
	public void testNoMatch() throws Exception {
		String sample = "1 2 3 [other] 5";
		VariableTable variables = new SimpleVariableTable();
		variables.putKeyword("call_id", "4");
		String result = KeywordReplacer.replaceKeywords(sample, variables, false);
		assertEquals(result, "1 2 3 [other] 5");
	}

	@Test(expected = Exception.class)
	public void testNoMatchException() throws Exception {
		String sample = "1 2 3 [other] 5";
		VariableTable variables = new SimpleVariableTable();
		variables.putKeyword("call_id", "4");
		String result = KeywordReplacer.replaceKeywords(sample, variables, true);
	}

	@Test
	public void testMissingLast() {
		String sample = "1 2 3 [last_Via:] 5\r\n";
		VariableTable variables = new SimpleVariableTable();
		String result = KeywordReplacer.replaceKeywords(sample, variables, true);
		assertEquals("1 2 3 \r\n", result);
	}

	@Test
	public void testAddition() throws Exception {
		String sample = "1 2 3 [call_id+4] 5";
		VariableTable variables = new SimpleVariableTable();
		variables.putKeyword("call_id", "4");
		String result = KeywordReplacer.replaceKeywords(sample, variables, false);
		assertEquals(result, "1 2 3 8 5");
	}

	@Test
	public void testEscape() throws Exception {
		String sample = "1 2 3 \\u005Bcall_id] 5";
		VariableTable variables = new SimpleVariableTable();
		variables.putKeyword("call_id", "4");
		String result = KeywordReplacer.replaceKeywords(sample, variables, false);
		assertEquals("1 2 3 [call_id] 5", result);
	}
}
