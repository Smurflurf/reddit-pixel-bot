package data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import masecla.reddit4j.objects.RedditPost;

@Data
public class RedditVideo {	
	public static RedditVideo of(RedditPost post) {
		String json = post.getGson().toJson(post.getMedia());
		json = json.replaceFirst("\"reddit_video\": ", "");
		json = json.substring(1, json.length() -1);
		return post.getGson().fromJson(json, RedditVideo.class);
	}
	
	public double getFramerate() {
		 try {
		        URL url = URI.create(dashUrl).toURL();
		        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		        connection.setRequestMethod("GET");

		        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
		        if (connection.getResponseCode() != 200) {
		            return -1;
		        }

		        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
		            String inputLine;
		            StringBuilder content = new StringBuilder();
		            
		            while ((inputLine = in.readLine()) != null) {
		                content.append(inputLine);
		            }
		            
		            Pattern pattern = Pattern.compile("frameRate=\"([^\"]+)\"");
		            Matcher matcher = pattern.matcher(content.toString());

		            if (matcher.find()) {
			            String[] stroengs = matcher.group(1).toString().split("/");
			            if(stroengs.length == 1)
			            	return Double.parseDouble(stroengs[0]);
			            else 
			            	return Double.parseDouble(stroengs[0]) / Double.parseDouble(stroengs[1]);
		            }
		        }
		    } catch (IOException e) {
		    	return -1;
		    }
		return -1;
	}
	
	@SerializedName("bitrate_kbps")
    private double bitrateKbps; // Neu hinzugefügt

    @SerializedName("fallback_url")
    private String fallbackUrl;

    @SerializedName("has_audio")
    private boolean hasAudio;

    @SerializedName("hls_url")
    private String hlsUrl;

    @SerializedName("dash_url")
    private String dashUrl;

    @SerializedName("duration")
    private double duration;

    @SerializedName("width")
    private double width;

    @SerializedName("height")
    private double height;

    @SerializedName("is_gif")
    private boolean isGif;
    
    @SerializedName("transcoding_status")
    private String transcodingStatus;
}