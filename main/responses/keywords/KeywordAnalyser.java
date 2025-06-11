package responses.keywords;

import java.util.ArrayList;
import java.util.List;

import masecla.reddit4j.objects.RedditComment;
import responses.keywords.KeywordLoader.ResponseMapping;

public class KeywordAnalyser {
	public static String build(RedditComment comment) {
		StringBuilder reply = new StringBuilder();
		String body = comment.getBody().toLowerCase();
		List<ResponseMapping> validReplies = new ArrayList<ResponseMapping>();
		
		for(ResponseMapping mapping : KeywordLoader.getMappings()) {
			if(mapping.appearsIn(body)) {
				validReplies.add(mapping);
			}
		}
		
		for(ResponseMapping validReply : validReplies) {
			reply.append(validReply.randomResponse())
			.append("  \n");
		}
		
		return reply.toString();
	}
}
