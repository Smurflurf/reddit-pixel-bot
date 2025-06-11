package client;

public enum Credentials {
	CLIENT_ID("xxx"),
	CLIENT_SECRET("xxx"),
	USERNAME("xxx"),
	PASSWORD("xxx"),
	
	GEMINI_API_KEY("xxx"),
	
	SCOPE("identity edit history mysubreddits privatemessages read submit vote"),
	
	APPNAME("pixel-zaehl-boter"),
	AUTHOR("Simon"),
	VERSION("2.0");
	
	
	String string;
	private Credentials(String string) {
		this.string = string;
	}
	public String get() {
		return string;
	}
	public void set(String string) {
		this.string = string;
	}
}
