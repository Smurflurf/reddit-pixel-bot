package responses;

import java.io.IOException;

import client.ClientHandler;
import client.Reddit5J;
import data.OnlineImage;
import data.RedditVideo;
import masecla.reddit4j.exceptions.AuthenticationException;
import masecla.reddit4j.objects.RedditComment;
import masecla.reddit4j.objects.RedditPost;
import responses.keywords.KeywordAnalyser;
import responses.posttypes.ImageResponse;
import responses.posttypes.NotSupportedResponse;
import responses.posttypes.NotSupportedResponse.NotSupported;
import responses.posttypes.VideoResponse;

public class ResponseHandler extends Reddit5J {

	/**
	 * Checks how to react to a given comment and acts accordingly.
	 * The reply depends on wether the bot got mentioned and what the parent posts type (image, video, text, ...) is.
	 * TODO
	 * Schaut zurzeit nur auf den parent post, auf Kommentare eingehen wäre cool.
	 * @param comment
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws AuthenticationException
	 */
	public static void handle(RedditComment comment) throws IOException, InterruptedException, AuthenticationException {
		if(comment.getBody().contains(ClientHandler.getClient().getUsername()))
			reactToMentioned(comment);
		else 
			reactToNotMentioned(comment);

		Reddit5J.markAsRead(comment);
	}

	/**
	 * Reaction to a u/... mention.
	 * Only if mentioned, a Pixel analysis takes place.
	 * Currently understands videos (VideoResponse.java) and images (ImageResponse.java)
	 * TODO multi thing postings - also mehrere bilder und videos in einem post behandeln
	 * @param comment
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws AuthenticationException
	 */
	static void reactToMentioned(RedditComment comment) throws IOException, InterruptedException, AuthenticationException {
		RedditPost post = getParentPost(comment);

		if(post.getDomain().equals("i.redd.it")) {				// is reddit image (cross or normal)
			ImageResponse.execute(comment, new OnlineImage(post), false);
		} else if(post.getMedia() != null) {					// is reddit video
			try {												// is a reddit Video if it works
				VideoResponse.execute(comment, RedditVideo.of(post), false);
			} catch (Exception e) { 							// no reddit video if it does not work
				NotSupportedResponse.execute(comment, NotSupported.LINKED_VIDEO);
			}
		} else if (post.getDomain().equals("v.redd.it") && post.getMedia() == null) { // is cross video
			VideoResponse.execute(comment, RedditVideo.of(Reddit5J.getOriginalPost(post)), true);
		} else if(post.getUrl().contains("comments")) {			// is text post
			NotSupportedResponse.execute(comment, NotSupported.TEXT_POST);
		} else {												// is something else (link image)
			try {
				ImageResponse.execute(comment, new OnlineImage(post), true);
			} catch (Exception e) {
				NotSupportedResponse.execute(comment, NotSupported.UNKNOWN_ERROR);
			}
		}
	}

	/**
	 * Reaction to not-mentions. Might be a new comment on a post or comment the bot made.
	 * Looks for keywords using KeywordAnalyser.java
	 * @param parent
	 */
	static void reactToNotMentioned(RedditComment parent){
		String reply = KeywordAnalyser.build(parent);
		if(reply.length() > 0) {
			comment(parent, reply);
		}
	}
}
