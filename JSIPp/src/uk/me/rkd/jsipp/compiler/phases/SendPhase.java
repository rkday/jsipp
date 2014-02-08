package uk.me.rkd.jsipp.compiler.phases;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

public class SendPhase extends CallPhase {
	public final String message;
	public static final Pattern initialSpaces = Pattern.compile("^\\s*");

	public SendPhase(Node xmlnode) {
		String raw_message = xmlnode.getFirstChild().getTextContent();
		this.message = this.stripWhitespace(raw_message);
	}

	/**
	 * @param message
	 *            - the text node parsed out of the XML file
	 * @return the SIP message stripped of formatting whitespace
	 */
	public static String stripWhitespace(String message) {
		String[] lines = message.split("\r?\n", 0);
		StringBuilder sb = new StringBuilder();
		boolean firstNonEmptyLine = false;
		boolean seenNonEmptyLine = false;
		int spaces = 0;
		Pattern someSpaces = null;
		for (String line : lines) {
			// Skip over empty lines at the start
			if (!seenNonEmptyLine) {
				boolean isLineBlank = (line.trim().isEmpty());
				if (!isLineBlank) {
					firstNonEmptyLine = true;
					seenNonEmptyLine = true;
				}
			}

			// If the first line is indented, save off the indentation...
			if (firstNonEmptyLine) {
				Matcher m = initialSpaces.matcher(line);
				if (m.find()) {
					spaces = m.group().length();
					someSpaces = Pattern.compile("^\\s{0," + Integer.toString(spaces) + "}");
				}
				firstNonEmptyLine = false;
			}

			// ...and strip off at most that much whitespace from subsequent lines.
			if (seenNonEmptyLine) {
				if (someSpaces != null) {
					sb.append(someSpaces.matcher(line).replaceFirst(""));
				} else {
					sb.append(line);
				}
				sb.append("\r\n");
			}
		}

		// Remove trailing whitespace (to match the XML format) but then add
		// CRLF CRLF to end the message if there's no body
		String stringSoFar = sb.toString().trim();
		if (!stringSoFar.contains("\r\n\r\n")) {
			stringSoFar += "\r\n\r\n";
		}
		return stringSoFar;
	}
}
