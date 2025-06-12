package data;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;

import javax.imageio.ImageIO;

import masecla.reddit4j.objects.RedditPost;

public class OnlineImage {
	String url;
	BufferedImage image;
	HttpURLConnection connection = null;
	
	public int width() {
		return image.getWidth();
	}

	public int height() {
		return image.getHeight();
	}

	public OnlineImage(RedditPost post) throws MalformedURLException, IOException {
		url = post.getUrl();
		connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
		
		try (InputStream inputStream = connection.getInputStream()) {
			BufferedImage image = ImageIO.read(inputStream);
			if (image != null) {
				this.image = image;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
