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
				addToValid(validReplies, mapping);
			}
		}

		for(ResponseMapping validReply : validReplies) {
			reply.append(validReply.randomResponse())
			.append("  \n");
		}

		return reply.toString();
	}

	private static void addToValid(List<ResponseMapping> validReplies, ResponseMapping newReply) {
		List<ResponseMapping> remove = new ArrayList<ResponseMapping>();
		
		for(ResponseMapping mapping : validReplies) {
			ResponseMapping stronger = mapping.getStronger(newReply);
			if(stronger != null)
				if(stronger == mapping) {
					remove.clear();
					newReply = null;
					break;
				} else {
					remove.add(mapping);
				}

		}

		for(ResponseMapping mapping : remove)
			validReplies.remove(mapping);
		
		if(newReply != null)
			validReplies.add(newReply);
	}
}
