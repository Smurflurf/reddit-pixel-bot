package responses.keywords;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import client.Credentials;
import masecla.reddit4j.objects.RedditComment;
import responses.keywords.KeywordLoader.ResponseMapping;

public class KeywordAnalyser {
	public static final String model = "gemma-3-27b-it";
	public static SplittableRandom random = new SplittableRandom();

	/**
	 * Generates a reply to comment.
	 * @param comment that will be replied to 
	 * @param reasonForError Strings that will be added to the prompt, if present, prompt will be written to describe the Error
	 * @return A valid comment, or, if no keywords got triggered, nothing.
	 */
	public static String build(RedditComment comment, String ... reasonForError) {
		String reply = "";
		String body = comment.getBody().toLowerCase().replace(Credentials.USERNAME.get().toLowerCase(), "(my_username)");
		List<ResponseMapping> validReplies = new ArrayList<ResponseMapping>();
		List<String> buildingBlocks = new ArrayList<String>();

		for(ResponseMapping mapping : KeywordLoader.getMappings()) {
			if(mapping.appearsIn(body)) {
				addToValid(validReplies, mapping);
			}
		}

		for(ResponseMapping validReply : validReplies) {
			buildingBlocks.add(validReply.randomResponse());
		}

		if(buildingBlocks.size() > 1) {
			reply = magicGlue(comment, buildingBlocks);
		} else if (buildingBlocks.size() == 1) {
			reply = buildingBlocks.getFirst();
		}

		if(reasonForError != null) {
			reply = errorResponse(comment, reply, reasonForError);
		}

		if(!reply.isEmpty() && reasonForError == null) {
			reply = checkAnswerContextBased(comment, reply);
			//			reply.replace(" Känguru", "[Känguru](https://die-kaenguru-chroniken.fandom.com/wiki/Schnapspralinen)");
		}

		return reply;
	}

	/**
	 * Gives an error Response
	 * @param comment
	 * @param reply
	 * @param reasonForError
	 * @return
	 */
	private static String errorResponse(RedditComment comment, String reply, String ... reasonForError) {
		StringBuffer knownKeywords = new StringBuffer();
		if(!reply.isEmpty()) {
			knownKeywords.
			append("\" Hast du schon mit: \"")
			.append(reply)
			.append("\" geantwortet und alle Keywords die von mir identifiziert wurden korrekt in diese Antwort eingebunden. ")
			.append("Deine folgende Antwort wird direkt hinter die eben genannte geschrieben. ")
			.append("Sieh deine folgende Antwort also als Erweiterung und baue einen Übergang für flüssiges Lesen ein. ")
			.append("Nutze Übergänge wie 'aber', 'jedoch', sofern sie dem Übergang behilflich sind.")
			;
		} else {
			knownKeywords
			.append("\" Sollst du nun antworten. ");
		}
		
		StringBuilder prompt = new StringBuilder()
				.append("Du bist ein reddit bot namens pixel-zaehl-boter. ")
				.append("Du sollst auf den Kommentar eines Nutzers reagieren. ")
				.append("Auf seinen Kommentar \"")
				.append(comment.getBody())
				.append(knownKeywords);


		for(String req : reasonForError)
			prompt.append(req).append(" ");

		return askGemma(prompt.toString());
	}
	
	/**
	 * Checks if the generated reply is valid for comment.
	 * In case that the reply does not fit the comment, it is assumed that something bad got commented and a bad response is chosen.
	 * @param comment parent comment
	 * @param reply already generated reply
	 * @return reply or if it does not match the comments tone, a negative reply
	 */
	private static String checkAnswerContextBased(RedditComment comment, String reply) {
		try {
			StringBuilder prompt = new StringBuilder()
					.append("Du bist ein Kommentarprüfer und hast nur die Macht mit 'true' oder 'false' zu antworten. ")
					.append("Sind sowohl Kommentar als auch Antwort nett und positiv, dann antwortest du nur 'true'. ")
					.append("Sind sowohl Kommentar als auch Antwort böse und negativ, dann antwortest du nur 'false'. ")
					.append("Versteht die Antwort den Kommentar falsch, dann antwortest du 'false'. ")
					.append("Ist eine Antwort nur ein klein wenig positiv wird sie als 'true' gewertet. ")
					.append("Wird im Kommentar etwas angeboten (z.B. Schnapspralinen), dann ist das positiv. ")
					.append("Wird gesagt dass der Bot etwas schlecht macht, ist das negativ, antwortet er aber auch negativ dann ist das 'true'. ")
					.append("Der Kommentar ist: '")
					.append(comment.getBody())
					.append("' und geantwortet wurde '")
					.append(reply)
					;
			String answer = askGemma(prompt.toString());
			if(answer.trim().equals("true")) {
				return reply;
			} else {
				return KeywordLoader.getMapping(-1).randomResponse();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return reply;
		}
	}

	/**
	 * Glues different building blocks together, using {@link #model}.
	 * @param comment parent comment
	 * @param buildingBlocks for the answer
	 * @return an answer composed of buildingBlocks, arranged by {@link #model}
	 */
	private static String magicGlue(RedditComment comment, List<String> buildingBlocks) {
		StringBuilder backup = new StringBuilder();
		StringBuilder blockList = new StringBuilder();

		while(buildingBlocks.size() > 0) {
			int rand = random.nextInt(buildingBlocks.size());
			backup.append(buildingBlocks.get(rand) + "  \n");
			blockList.append("\""+ buildingBlocks.get(rand) +"\"" + (buildingBlocks.size() == 1 ? "," : "."));
			buildingBlocks.remove(rand);
		}

		StringBuilder prompt = new StringBuilder()
				.append("Du bist ein reddit Bot namens pixel-zaehl-boter und garantiert kein Mensch. ")
				.append("Du sollst auf den Kommentar eines Nutzers reagieren. ")
				.append("Auf seinen Kommentar \"")
				.append(comment.getBody())
				.append("\" antwortest du mit folgenden Bausteinen: ")
				.append(blockList.toString())

				.append(" Deine Aufgabe ist es, auf den Bausteinen aufbauend eine flüssige und prägnante Antwort zu erstellen. ")
				.append("Verändere die Bausteine so, dass sie zu einem oder zwei flüssig zusammenhängenden, kurzen Satz fusioniert werden. ")
				.append("Die Satzteile sollen ineinander übergehen und flüssig lesbar sein. ")
				.append("Antworte nur mit Text und schreibe möglichst wenig neu dazu. ")
				.append("Die Bausteine sollen einen gemeinsamen Kontext ergeben und diesem als Grundbaustein dienen. ")
				.append("Lege wert darauf die Bausteine in einen zusammenhängenden aber kurzen Satz zu fusionieren. ")
				.append("Antworte sehr kurz, bündig, nicht formell und atomar. ")
				.append("Schreibe immer aus erster Person aber referenziere dich nicht selbst. ")
				.append("Schreibe nicht fabriziert sondern eher wie ein echter Redditor. ")
				;

		try {
			return askGemma(prompt.toString());
		} catch (Exception e) {
			return backup.toString();
		}
	}

	/**
	 * Asks {@link #model} a prompt
	 * @param prompt String to ask
	 * @return the models answer to the prompt
	 */
	private static String askGemma(String prompt) {
		Client client = 
				Client
				.builder()
				.apiKey(Credentials.GEMINI_API_KEY.get())
				.build();
		GenerateContentResponse response =
				client
				.models
				.generateContent(model, prompt.toString(), null);
		return response.text();
	}

	/**
	 * Adds newReply to validReplies in case it is not similar to any other already valid replies.
	 * If replies are similar, the ones with the lower priority get removed.
	 * @param validReplies List of valid already checked replies
	 * @param newReply a new reply to put into the list
	 */
	private static void addToValid(List<ResponseMapping> validReplies, ResponseMapping newReply) {
		List<ResponseMapping> remove = new ArrayList<ResponseMapping>();
		
		for(ResponseMapping mapping : validReplies) {
			ResponseMapping stronger = mapping.getStronger(newReply);
			if(stronger != null) {
				if(stronger == mapping) {
					remove.clear();
					newReply = null;
					break;
				} else {
					remove.add(mapping);
				}
				System.out.println(stronger);
			}
		}
		
		for(ResponseMapping mapping : remove)
			validReplies.remove(mapping);

		if(newReply != null)
			validReplies.add(newReply);
	}
}
