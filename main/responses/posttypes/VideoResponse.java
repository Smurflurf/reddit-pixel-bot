package responses.posttypes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import client.Reddit5J;
import data.RedditVideo;
import masecla.reddit4j.objects.RedditComment;
import responses.keywords.KeywordAnalyser;

public class VideoResponse {
	static NumberFormat germanFormat = NumberFormat.getNumberInstance(Locale.GERMANY);

	public static void execute(RedditComment parent, RedditVideo video) {
		String reply = lookAtBandwidth(parent, video);
		reply += "  \n" + KeywordAnalyser.build(parent);
		Reddit5J.comment(parent, reply);
	}
	
	static String lookAtBandwidth(RedditComment parent, RedditVideo video) {
		StringBuilder sb = new StringBuilder();
		sb.append("Das obrige Video hat ")
		.append(germanFormat.format((long) video.getWidth() * (long)video.getHeight()))
		.append(" (")
		.append((int)video.getWidth())
		.append("x")
		.append((int)video.getHeight())
		.append(") Pixel und eine Bitrate von ")
		.append((int)video.getBitrateKbps())
		.append("kbps.");

		double fps = video.getFramerate();
		if(fps > 0) {
			sb.append("\nBei ")
			.append(fps)
			.append(" bps sind das ")
			.append(
					germanFormat.format(
							BigDecimal.valueOf(video.getWidth())
							.multiply(BigDecimal.valueOf(video.getHeight()))
							.multiply(BigDecimal.valueOf(fps))
							.longValue()
							)
					)
			.append(" Pixel.");
		}
		return sb.toString();
	}
}
