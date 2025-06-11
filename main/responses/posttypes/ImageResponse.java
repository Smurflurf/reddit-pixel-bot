package responses.posttypes;

import java.text.NumberFormat;
import java.util.Locale;

import client.Reddit5J;
import data.RedditImage;
import masecla.reddit4j.objects.RedditComment;
import responses.keywords.KeywordAnalyser;

public class ImageResponse {
	static NumberFormat germanFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
	
	public static void execute(RedditComment parent, RedditImage image) {
		String reply = countPixels(parent, image);
		reply += "  \n" + KeywordAnalyser.build(parent);
		Reddit5J.comment(parent, reply);
	}

	static String countPixels(RedditComment parent, RedditImage image) {
		StringBuilder sb = new StringBuilder();
		sb.append("Das obrige Bild besteht aus ")
		.append(germanFormat.format((long) image.width() * (long)image.height()))
		.append(" (")
		.append(image.width())
		.append("x")
		.append(image.height())
		.append(") Pixeln.");
		return sb.toString();
	}
}
