package client;
import java.io.IOException;

import masecla.reddit4j.client.Reddit4J;
import masecla.reddit4j.client.UserAgentBuilder;
import masecla.reddit4j.exceptions.AuthenticationException;
import masecla.reddit4j.requests.RedditUserCommentListingEndpointRequest;

public class ClientHandler {
	private static Reddit4J client = getConnectedClient();


	public static RedditUserCommentListingEndpointRequest getUnreadMentionRequest() {
		return new RedditUserCommentListingEndpointRequest("/message/unread", client);
	}

	public static RedditUserCommentListingEndpointRequest getAllMentionRequest() {
		return new RedditUserCommentListingEndpointRequest("/message/inbox", client);
	}

	public static Reddit4J getClient() {
		return client;
	}

	private static Reddit4J getConnectedClient() {
		Reddit4J client = Reddit4J
				.rateLimited()
				.setClientId(Credentials.CLIENT_ID.get())
				.setClientSecret(Credentials.CLIENT_SECRET.get())
				.setUsername(Credentials.USERNAME.get())
				.setPassword(Credentials.PASSWORD.get())
				.setUserAgent(
						new UserAgentBuilder()
						.appname(Credentials.APPNAME.get())
						.author(Credentials.AUTHOR.get())
						.version(Credentials.VERSION.get())
						);

		try {
			Reddit5J.connect(client);
		} catch (IOException | InterruptedException | ReflectiveOperationException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (AuthenticationException e) {
			e.printStackTrace();
			System.err.println("Username or Password (or Client ID/SECRET) is wrong. Try running the jar as \n"
					+ ">> java -jar pixel-zaehl-boter.jar USERNAME PASSWORD CLIENT_ID CLIENT_SECRET <<\n"
					+ "or set them in client.Credentials.java");
			System.exit(0);
		}

		return client;
	}
}
