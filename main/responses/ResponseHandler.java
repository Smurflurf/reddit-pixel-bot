package responses;

import java.io.IOException;

import client.ClientHandler;
import client.Reddit5J;
import data.RedditImage;
import data.RedditVideo;
import masecla.reddit4j.exceptions.AuthenticationException;
import masecla.reddit4j.objects.RedditComment;
import masecla.reddit4j.objects.RedditPost;
import responses.keywords.KeywordAnalyser;
import responses.posttypes.ImageResponse;
import responses.posttypes.VideoResponse;

public class ResponseHandler {
	
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
	 * Currently just understands videos (VideoResponse.java) and images (ImageResponse.java)
	 * @param comment
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws AuthenticationException
	 */
	static void reactToMentioned(RedditComment comment) throws IOException, InterruptedException, AuthenticationException {
		RedditPost post = Reddit5J.getParentPost(comment);
		
		if(post.getUrl().contains("i.redd.it")) {	// is image
			ImageResponse.execute(comment, new RedditImage(post));
		} else if(post.getMedia() != null) {	// is video
			VideoResponse.execute(comment, RedditVideo.of(post));
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
			Reddit5J.comment(parent, reply);
		}
	}
}
