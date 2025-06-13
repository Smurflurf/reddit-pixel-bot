package responses;

import java.io.IOException;

import client.ClientHandler;
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

//		ClientHandler.getClient().markCommentAsRead(comment);
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
		RedditPost post = ClientHandler.getClient().getParentPost(comment);

		switch(post.getPostHint()) {
		case(null):				// text post
			NotSupportedResponse.execute(comment, NotSupported.TEXT_POST);
			break;
		case("hosted:video"): 	// reddit hosted video from a normal post
			VideoResponse.execute(comment, RedditVideo.of(post), false);
			break;
		case("rich:video"): 	// external video, linked
			NotSupportedResponse.execute(comment, NotSupported.LINKED_VIDEO);
			break;
		case("image"):			// all forms of images
			ImageResponse.execute(comment, new OnlineImage(post), false);
			break;
		case("link"):			// crosspost video, not completely sure tho
			try { 
				VideoResponse.execute(comment, 
						RedditVideo.of(
								ClientHandler.getClient().getPost(post.getCrosspostParent()).get()
								)
						, false);
				break;
			} catch (Exception e) {
				System.err.println("Link Type was no cross-video. " + post.getId() + " " + post.getSubreddit());
			}
		default:				// something unsupported is used : generic unknown response
			NotSupportedResponse.execute(comment, NotSupported.UNKNOWN_ERROR);
			break;
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
			ClientHandler.getClient().comment(parent, reply);
		}
	}
}
