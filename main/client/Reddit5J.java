package client;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import masecla.reddit4j.client.Reddit4J;
import masecla.reddit4j.exceptions.AuthenticationException;
import masecla.reddit4j.objects.RedditComment;
import masecla.reddit4j.objects.RedditData;
import masecla.reddit4j.objects.RedditListing;
import masecla.reddit4j.objects.RedditThing;

/**
 * Eine erweiterte Version von Reddit4J, die das Anfordern von OAuth2-Scopes
 * während der Authentifizierung ermöglicht, ohne die Originalklasse zu verändern.
 * Dies wird durch Vererbung und Java Reflection erreicht.
 */
public class Reddit5J extends Reddit4J {
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
	
	public static RedditThing getThingByID(String id) {
		return null;
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