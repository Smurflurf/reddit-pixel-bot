import java.io.IOException;

import org.jsoup.HttpStatusException;

import client.ClientHandler;
import client.Credentials;
import masecla.reddit4j.exceptions.AuthenticationException;
import masecla.reddit4j.objects.RedditComment;
import responses.ResponseHandler;

public class Main {

	public static void main(String[] args) {
		if(args.length > 0) {
			Credentials.USERNAME.set(args[0]);
			Credentials.PASSWORD.set(args[1]);
			Credentials.CLIENT_ID.set(args[2]);
			Credentials.CLIENT_SECRET.set(args[3]);
			Credentials.GEMINI_API_KEY.set(args[4]);
		}

		System.out.println("running silently...");
		
		while(true) {
			try {
				mainLoop();
				Thread.sleep((int)(5_000));
			} catch(InterruptedException | AuthenticationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				if(e instanceof HttpStatusException 
						&& (((HttpStatusException) e).getStatusCode() == 500)
						|| (((HttpStatusException) e).getStatusCode() == 429))
					continue;

				System.err.println(e.getMessage());
				try {Thread.sleep(20_000);} catch (InterruptedException e1) {}
			}
		}
	}

	public static void mainLoop() throws IOException, InterruptedException, AuthenticationException  {
		var messages = ClientHandler.getUnreadMentionRequest().submit();
		for(RedditComment comment : messages) {
			ResponseHandler.handle(comment);
		}
	}
}
