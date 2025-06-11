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

public class KeywordLoader {
	private static String keywordFileName = "keywordmappings.json";
	private static List<ResponseMapping> responses = loadResponses();
	private static Set<String> keywords = collectKeywords();
	
	
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
		
		public String randomResponse() {
			return responses.get(random.nextInt(responses.size()));
		}
		
		public boolean appearsIn(String commentBody) {
			for(String key : keywords)
				if(commentBody.contains(key))
					return true;
			return false;
		}
	}
}
