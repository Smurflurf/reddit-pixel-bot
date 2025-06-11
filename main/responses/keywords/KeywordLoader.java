package responses.keywords;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.Data;

/**
 * Loads the keyword-reply mappings, contains the Map class and useful methods for interacting with it.
 */
public class KeywordLoader {
	private static String keywordFileName = "keywordmappings.json";
	private static List<ResponseMapping> responses = loadResponses();
	private static Set<String> keywords = collectKeywords();
	
	public static ResponseMapping getMapping(int id) {
		for(ResponseMapping mapping : responses) {
			if(mapping.getId() == id)
				return mapping;
		}
		return null;
	}
	
	public static List<ResponseMapping> getMappings() {
		return responses;
	}
	
	public static Set<String> getKeywords() {
		return keywords;
	}
	
	private static Set<String> collectKeywords() {
		HashSet<String> strings = new HashSet<String>();
		for(ResponseMapping mapping : responses)
			for(String key : mapping.getKeywords())
				strings.add(key);
		return strings;
	}
	
	private static List<ResponseMapping> loadResponses() {
		try (InputStream inputStream = KeywordLoader.class.getResourceAsStream(keywordFileName)) {
			Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
			return new Gson().fromJson(reader, new TypeToken<List<ResponseMapping>>() {}.getType());

		} catch (Exception e) {
			throw new RuntimeException("Error loading keywordmappings.json\n", e);
		}
	}

	@Data
	class ResponseMapping {
		private static SplittableRandom random = new SplittableRandom();
		private List<String> keywords;
		private List<String> responses;
		private int id;
		private int priority;
		private List<Integer> similarTo;
		
		/**
		 * If other appears in {@link #similarTo}, the ResponseMapping with smaller (meaning higher) priority gets returned.
		 * @param other 
		 * @return stronger ResponseMapping or null if they are not similar.
		 */
		public ResponseMapping getStronger(ResponseMapping other) {
			if(other.getSimilarTo().contains(id)) {
				if(other.getPriority() > priority)
					return this;
				else if(other.getPriority() <= priority)
					return other;
			}
			
			return null;
		}
		
		public String randomResponse() {
			return responses.get(random.nextInt(responses.size()));
		}
		
		public boolean appearsIn(String commentBody) {
			for(String key : keywords) {
				if(commentBody.contains(key))
					return true;
			}
			return false;
		}
	}
}
