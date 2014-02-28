package uk.me.rkd.jsipp.compiler;

import java.util.HashMap;
import java.util.Map;

public class SimpleVariableTable implements VariableTable {
	protected Map<String, String> vars = new HashMap<String, String>();
	private static SimpleVariableTable global = new SimpleVariableTable();

	public static SimpleVariableTable global() {
		return global;
	}

	@Override
	public String get(String name) {
		return vars.get(name);
	}

	@Override
	public void putKeyword(String name, String value) {
		vars.put(name, value);
	}

	@Override
	public void putVariable(String name, String value) {
		vars.put("$" + name, value);

	}

	@Override
	public void removeKeyword(String name) {
		vars.remove(name);
	}

	@Override
	public void removeVariable(String name) {
		vars.remove("$" + name);
	}

}
