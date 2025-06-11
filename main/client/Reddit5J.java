package client;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import masecla.reddit4j.client.Reddit4J;
import masecla.reddit4j.exceptions.AuthenticationException;
import masecla.reddit4j.objects.RedditComment;
import masecla.reddit4j.objects.RedditData;
import masecla.reddit4j.objects.RedditListing;
import masecla.reddit4j.objects.RedditPost;

/**
 * Eine erweiterte Version von Reddit4J, die das Anfordern von OAuth2-Scopes
 * während der Authentifizierung ermöglicht, ohne die Originalklasse zu verändern.
 * Dies wird durch Vererbung und Java Reflection erreicht.
 */
public class Reddit5J extends Reddit4J {
	/**
	 * Marks a comment as read
	 * @param comment
	 * @return true if executed without errors.
	 */
	public static boolean markAsRead(RedditComment comment) {
		Connection conn = ClientHandler.getClient().useEndpoint("/api/read_message").method(Method.POST);
	    conn.data("id", comment.getName());
	    try {
			conn.execute();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	    return true;
	}

	/**
	 * Comments comment below parent.
	 * Marks the comment as read, if answered without errors.
	 * @param parent
	 * @param comment
	 * @return
	 */
	public static boolean comment(RedditComment parent, String comment) {
		Connection connection = ClientHandler.getClient().useEndpoint("/api/comment")
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
	                System.err.println("Error commenting: " + comment);
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
	 * Returns the parent post a comment was published below.
	 * @param comment to get parent post
	 * @return parent post of comment
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static RedditPost getParentPost(RedditComment comment) throws IOException, InterruptedException {
		RedditComment copy = comment;
		while(copy.getParentId().contains("t1")) {
			String fullName = copy.getParentId();
			copy = Reddit5J.getCommentByName(ClientHandler.getClient(), fullName).get();
		}
		return ClientHandler.getClient().getPost(copy.getParentId()).get();
	}
	
	/**
	 * Connects the client to reddit and requests the in Credentials.java set OAuth2-Scopes.
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws AuthenticationException
	 * @throws ReflectiveOperationException 
	 */
	public static void connect(Reddit4J client) throws IOException, InterruptedException, AuthenticationException, ReflectiveOperationException {
		String userAgent = (String) getPrivateField(client, "userAgent");
		String username = (String) getPrivateField(client, "username");
		String password = (String) getPrivateField(client, "password");
		String clientId = (String) getPrivateField(client, "clientId");
		String clientSecret = (String) getPrivateField(client, "clientSecret");

		if (userAgent == null) {
			throw new NullPointerException("User Agent was not set!");
		}

		Connection conn = Jsoup.connect(Reddit4J.BASE_URL() + "/api/v1/access_token")
				.ignoreContentType(true)
				.ignoreHttpErrors(true)
				.method(Connection.Method.POST)
				.userAgent(userAgent);

		conn.data("grant_type", "password");
		conn.data("username", username).data("password", password);
		conn.data("scope", Credentials.SCOPE.get());

		String combination = clientId + ":" + clientSecret;
		combination = Base64.getEncoder().encodeToString(combination.getBytes());
		conn.header("Authorization", "Basic " + combination);

		Response response = client.getHttpClient().execute(conn);
		if (response.statusCode() == 401) {
			throw new AuthenticationException("Unauthorized! Invalid clientId or clientSecret!");
		}

		JsonObject object = JsonParser.parseString(response.body()).getAsJsonObject();

		if (object.keySet().contains("error")) {
			throw new AuthenticationException(object.get("error").getAsString());
		}

		setPrivateField(client, "token", object.get("access_token").getAsString());
		long expiration = object.get("expires_in").getAsInt() + Instant.now().getEpochSecond();
		setPrivateField(client, "expirationDate", expiration);
	}
	
	public static Optional<RedditComment> getCommentByName(Reddit4J client, String fullname) throws IOException, InterruptedException {
	    if (!fullname.startsWith("t1_")) {
	        fullname = "t1_" + fullname;
	    }

	    Connection connection = client.useEndpoint("/api/info").data("id", fullname);
	    Response response = connection.execute();

	    TypeToken<?> ttComment = TypeToken.getParameterized(RedditData.class, RedditComment.class);
	    TypeToken<?> ttListing = TypeToken.getParameterized(RedditListing.class, ttComment.getType());
	    TypeToken<?> ttData = TypeToken.getParameterized(RedditData.class, ttListing.getType());

	    Gson gson = new Gson();
	    RedditData<RedditListing<RedditData<RedditComment>>> fromJson = gson.fromJson(response.body(), ttData.getType());

	    return fromJson.getData().getChildren().stream()
	            .findFirst()
	            .map(RedditData::getData);
	}

	/**
	 * Helper method to get a private value of the superclass.
	 */
	private static Object getPrivateField(Reddit4J client, String fieldName) throws NoSuchFieldException, IllegalAccessException {
		Field field = Reddit4J.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(client);
	}

	/**
	 * Helper method to set a private value of the superclass.
	 */
	private static void setPrivateField(Reddit4J client, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
		Field field = Reddit4J.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(client, value);
	}
}