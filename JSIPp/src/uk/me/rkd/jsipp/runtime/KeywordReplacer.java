package uk.me.rkd.jsipp.runtime;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeywordReplacer {
	private static final Pattern keywordPattern = Pattern.compile("\\[(.+?):?\\]");
	private static final String wholeLinePattern = "\\[last_.+?\\].*?\r\n";

	
	public static String replaceKeywords(String text,
			Map<String, String> variables,
			boolean mustMatchAll) throws IllegalStateException {
		Matcher m = keywordPattern.matcher(text);
		Matcher innerKeywordMatch;
		String result = text;
		int position = 0;
		while (m.find(position)) {
			String keywordBlock = m.group(0); // e.g. [last_Via:]
			String keyword = m.group(1); // e.g. last_Via
			String replacement = variables.get(keyword);
			position = m.end();
			if (replacement != null) {
				result = result.replace(keywordBlock, replacement);
			} else if (keyword.startsWith("last_")) {
				result = result.replaceFirst(wholeLinePattern, "\r\n");
			} else if (mustMatchAll) {
				throw new IllegalStateException();
			}
		}
		return result;
	}
	
	private KeywordReplacer() {
		// TODO Auto-generated constructor stub
	}

}
