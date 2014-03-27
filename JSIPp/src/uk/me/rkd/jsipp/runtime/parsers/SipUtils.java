package uk.me.rkd.jsipp.runtime.parsers;

public class SipUtils {

	public static String methodOrStatusCode(String firstLine) {
		String[] parts = firstLine.split(" ");
		if (parts[0].equals("SIP/2.0")) {
			return parts[1]; 
		} else {
			return parts[0];
		}
	}

}
