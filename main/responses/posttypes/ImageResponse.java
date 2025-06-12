package responses.posttypes;

import java.text.NumberFormat;
import java.util.Locale;

import client.Reddit5J;
import data.OnlineImage;
import masecla.reddit4j.objects.RedditComment;
import responses.keywords.KeywordAnalyser;

public class ImageResponse {
	static NumberFormat germanFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
	
	/**
	 * Executes a response to an image.
	 * 
	 * 
	 * @param parent parent thing to comment on
	 * @param image to analyse
	 * @param extra Strings to append behind the actual response
	 */
	public static void execute(RedditComment parent, OnlineImage image, boolean isLink) {
		String reply = "";
		if(image != null)
			reply = countPixels(parent, image, isLink);
		reply += "  \n" + KeywordAnalyser.build(parent);
		
		if(reply.length() > 0)
			Reddit5J.comment(parent, reply);
	}

	static String countPixels(RedditComment parent, OnlineImage image, boolean isLink) {
		StringBuilder sb = new StringBuilder();
		sb.append(isLink ? "Das oben verlinkte Bild besteht aus " : "Das obrige Bild besteht aus ")
		.append(germanFormat.format((long) image.width() * (long)image.height()))
		.append(" (")
		.append(image.width())
		.append("x")
		.append(image.height())
		.append(") Pixeln.");
		return sb.toString();
	}
}
