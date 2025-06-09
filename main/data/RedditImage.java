package data;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.imageio.ImageIO;

import masecla.reddit4j.objects.RedditPost;

public class RedditImage {
	String url;
	BufferedImage image;

	public int width() {
		return image.getWidth();
	}

	public int height() {
		return image.getHeight();
	}

	public RedditImage(RedditPost post) {
		this.url = post.getUrl();
		try (InputStream inputStream = URI.create(url).toURL().openStream()) {
			BufferedImage image = ImageIO.read(inputStream);
			if (image != null) {
				this.image = image;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
