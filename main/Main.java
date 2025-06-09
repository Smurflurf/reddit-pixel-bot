import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import client.ClientHandler;
import client.Credentials;
import client.Reddit5J;
import data.RedditImage;
import data.RedditVideo;
import masecla.reddit4j.exceptions.AuthenticationException;
import masecla.reddit4j.objects.RedditComment;
import masecla.reddit4j.objects.RedditPost;

public class Main extends ClientHandler {
	static NumberFormat germanFormat = NumberFormat.getNumberInstance(Locale.GERMANY);

	public static void main(String[] args) {
		System.out.println("Run the jar as \n"
				+ ">> java -jar pixel-zaehl-boter.jar USERNAME PASSWORD CLIENT_ID CLIENT_SECRET<<\n"
				+ "or set them in client.Credentials.java");
		
		if(args.length > 0) {
			Credentials.USERNAME.set(args[0]);
			Credentials.PASSWORD.set(args[1]);
			Credentials.CLIENT_ID.set(args[2]);
			Credentials.CLIENT_SECRET.set(args[3]);
		}
		
		while(true) {
			try {
				mainLoop();
				Thread.sleep((int)(3000));
			} catch(IOException | InterruptedException | AuthenticationException e) {
				e.printStackTrace();
			}
		}
	}

	public static void mainLoop() throws IOException, InterruptedException, AuthenticationException {
		var messages = getUnreadMentionRequest().submit();

		for(RedditComment comment : messages) {
			if(comment.getBody().contains(getClient().getUsername()))
				reactToMentioned(comment);
			else 
				reactToNotMentioned(comment);
		}
	}

	static void reactToMentioned(RedditComment comment) throws IOException, InterruptedException, AuthenticationException {
		RedditComment copy = comment;
		while(copy.getParentId().contains("t1")) {
			String fullName = copy.getParentId();
			copy = Reddit5J.getCommentByName(getClient(), fullName).get();
		}

		RedditPost post = getClient().getPost(copy.getParentId()).get();
		//		System.out.println(post.getTitle() + " : PT");

		if(post.getUrl().contains("i.redd.it")) {	// is image
			countPixels(comment, new RedditImage(post));
		} else if(post.getMedia() != null) {	// is video
			lookAtBandwidth(comment, RedditVideo.of(post));
		}
	}

	static void countPixels(RedditComment parent, RedditImage image) {
		StringBuilder sb = new StringBuilder();
		sb.append("Das obrige Bild besteht aus ")
		.append(germanFormat.format((long) image.width() * (long)image.height()))
		.append(" (")
		.append(image.width())
		.append("x")
		.append(image.height())
		.append(") Pixeln.");

		//		System.out.println(sb.toString());
		comment(parent, sb.toString());
	}

	static void lookAtBandwidth(RedditComment parent, RedditVideo video) {
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

		//		System.out.println(sb.toString());
		comment(parent, sb.toString());
	}

	static void reactToNotMentioned(RedditComment parent){
		String reply = "";
		String body = parent.getBody().toLowerCase();
		boolean rep = false;
		
		if(body.contains("schnapspraline")) {
			reply += "Sehr gerne.  \n";
			rep = true;
		} 

		if (body.contains("prapsschnaline")) {
			reply += "Das überlasse ich lieber dem [Känguru](https://die-kaenguru-chroniken.fandom.com/wiki/Schnapspralinen).  \n";
			rep = true;
		} 

		if (body.contains("guter")){
			reply += "Ich danke.  \n";
			rep = true;
		}
		
		if(!rep){
			return;
		}

		comment(parent, reply);
	}
}
