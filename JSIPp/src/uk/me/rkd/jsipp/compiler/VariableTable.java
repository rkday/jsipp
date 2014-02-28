package uk.me.rkd.jsipp.compiler;


public interface VariableTable {
	public String get(String name);

	public void putKeyword(String name, String value);

	public void putVariable(String name, String value);

	public void removeKeyword(String name);

	public void removeVariable(String name);
}
