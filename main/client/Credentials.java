package client;

public enum Credentials {
	CLIENT_ID("xxx"),
	CLIENT_SECRET("xxx"),
	USERNAME("xxx"),
	PASSWORD("xxx"),
	
	SCOPE("identity edit history mysubreddits privatemessages read submit vote"),
	
	APPNAME("pixel-zaehl-boter"),
	AUTHOR("Simon"),
	VERSION("1.0");
	
	
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
	
	/**
	 * Call and use this link to grant posting access without password.
	 * Not used here.
	 * @deprecated
	 */
	public static void createAuthUrl() {
		System.out.println(
				"https://www.reddit.com/api/v1/authorize?"
				+ "client_id=" + CLIENT_ID.get() + "&"
				+ "response_type=" + "code" + "&"
				+ "state=" + System.currentTimeMillis() + ""+ System.currentTimeMillis() + (int)(Math.random()*100000) + "&"
				+ "redirect_uri=" + "https://www.reddit.com/user/pixel-zaehl-boter/" + "&"
				+ "duration=" + "permanent" + "&"
				+ "scope=" + SCOPE.get() + "");
	}
}
