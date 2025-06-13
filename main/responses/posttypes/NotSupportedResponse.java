package responses.posttypes;

import java.util.SplittableRandom;

import client.ClientHandler;
import masecla.reddit4j.objects.RedditComment;
import responses.keywords.KeywordAnalyser;

public class NotSupportedResponse {
	public static void execute(RedditComment parent, NotSupported notSupported) {
		StringBuilder sb = new StringBuilder()
				.append("Du konntest deine Aufgabe als Pixel-Zähl-Roboter bei diesen spezifischen Beitrag aus einem bestimmten Grund nicht erfüllen. ")
				.append("Der spezifische Grund, welcher nur als Hintergrundwissen dient und nicht so wichtig ist, trotzdem aber irgendwie genannt sein sollte, ist: \"")
				.append(notSupported.getReason())
				.append("\". Folgender Satz ist die Antwort, die an den Nutzer übergeben werden soll: \"")
				.append(notSupported.getAnswer())
				.append("\". Bitte erweitere die Antwort so, dass sie vorherigen Kontext beinhaltet, falls er existiert. ")
				.append("Die Antwort soll flüssig, präzise und prägnant sein. ")
				.append("Sie soll möglichst wenig, aber ausdrücklich nur Text beinhalten und der originalen Antwort treu sein. ")
				.append("Halte dich bitte also generell kurz, schweife nicht ab und bleib den Vorgaben treu. ")
				.append("Sie soll die schon bestehenden Informationen adressieren. ")
				.append("Antworte bündig, nicht formell und atomar. ")				
				.append("Schreibe immer aus erster Person aber referenziere dich nicht selbst. Nutze 'Ich'. ")
				.append("Schreibe nicht fabriziert aber auch nicht zu persönlich. ")
				.append("Schreibe nur die Antwort, ohne Namennennungen oder Pings. ")
				.append("Schreibe NUR AUF DEUTSCH, jedes einzelne Wort muss zwingend deutsch sein. Worte wie 'sorry', 'post' oder 'cool' kennst du nicht mehr. ");
		
		if(notSupported == NotSupported.UNKNOWN_ERROR
				|| notSupported == NotSupported.TEXT_POST)
				sb.append("Die Antwort soll ein klein wenig lustig sein, aber nicht zu sehr aussschweifen.")
				;
		
		String reply = "  \n" + KeywordAnalyser.build(parent, sb.toString());
		
		ClientHandler.getClient().comment(parent, reply);
	}
	
	public enum NotSupported {
		LINKED_VIDEO(
				"Externe, nur verlinkte Videos können noch nicht analysiert werden. Da Videos von jeder Website anders gehostet werden ist es nicht einheitlich genug für mich.",
				"Externe Videos von außerhalb kann ich nicht analysieren, sie haben kein einheitliches Format wie auf Reddit hochgeladene Videos."
				),
		TEXT_POST(
				"Es existiert noch kein Code um Links aus Text zu bestimmen.",
				"Text-Pfosten kann ich noch nicht analysieren, auch wenn sie Bilder und/oder Video enthalten.",
				"Der ganze Text im Pfosten verwirrt meinen Geist.",
				"Buchstaben ... überall Buchstaben.",
				"Ich hab garnix gemacht, gar nix. Ich hab nur eine Bier getrunken. Eine Bier."
				),
		UNKNOWN_ERROR(
				"Es kam zu irgendeinem unbekannten Fehler.",
				"Schwarze Magie hält mich von der Analyse dieses Pfostens ab.",
				"Mythische Runen verschleiern den Inhalt des Pfostens.",
				"Ich wurde eindeutig verflucht.",
				"Dunkle Mächte erheben sich, ich bin abgelenkt.",
				"Ich muss nur noch kurz die Welt retten.",
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
				"Ich hab garnix gemacht, gar nix. Ich hab nur eine Bier getrunken. Eine Bier."
				);
		
		final String reason;
		final String[] answers;
		final static SplittableRandom random = new SplittableRandom();
		
		private NotSupported(String reason, String ... answers) {
			this.reason = reason;
			this.answers = answers;
		}
		
		public String getReason() {
			return reason;
		}
		public String getAnswer() {
			return answers[random.nextInt(answers.length)];
		}
	}
}
