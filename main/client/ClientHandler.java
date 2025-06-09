package client;
import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import masecla.reddit4j.client.Reddit4J;
import masecla.reddit4j.client.UserAgentBuilder;
import masecla.reddit4j.exceptions.AuthenticationException;
import masecla.reddit4j.objects.RedditComment;
import masecla.reddit4j.requests.RedditUserCommentListingEndpointRequest;

public class ClientHandler {
	private static Reddit4J client = getConnectedClient();

	public static boolean comment(RedditComment parent, String comment) {
		Connection connection = getClient().useEndpoint("/api/comment")
                .method(Connection.Method.POST)
                .data("thing_id", parent.getName())
                .data("text", comment)
                .data("api_type", "json");
		
		try {
			Response response = connection.execute();
			
			String responseBody = response.body();
			JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
	        if (jsonResponse.has("json") && jsonResponse.getAsJsonObject("json").has("errors")) {
	            JsonArray errors = jsonResponse.getAsJsonObject("json").getAsJsonArray("errors");
	            if (errors.size() > 0) {
	                System.err.println("Error commenting: " + errors.get(0).getAsString());
	                return false;
	            }
	        }
	        
	        return markAsRead(parent);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Marks a comment as read
	 * @param comment
	 * @return
	 */
	public static boolean markAsRead(RedditComment comment) {
		Connection conn = client.useEndpoint("/api/read_message").method(Method.POST);
	    conn.data("id", comment.getName());
	    try {
			conn.execute();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	    return true;
	}
	
	public static RedditUserCommentListingEndpointRequest getUnreadMentionRequest() {
		return new RedditUserCommentListingEndpointRequest("/message/unread", client);
	}
	
	public static RedditUserCommentListingEndpointRequest getAllMentionRequest() {
		return new RedditUserCommentListingEndpointRequest("/message/inbox", client);
	}
	
	public static Reddit4J getClient() {
		return client;
	}
	
	

	public static Reddit4J getConnectedClient() {
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
