package io.smppgateway.server;
/**
 *
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 07/09/17.
 */
public class FormatUtils {
	public static String formatAsHex(long msgId) {
		String msgIdStr = String.format("%07x", msgId);		
		return msgIdStr.toLowerCase(); // just to be sure :)
	}
	
	public static String formatAsHexUppercase(long msgId) {		
		return formatAsHex(msgId).toUpperCase();
	}
	
	public static String formatAsDec(long msgId) {
		return String.format("%d", msgId);
	}
}
