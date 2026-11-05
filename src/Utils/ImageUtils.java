package Utils;

import java.awt.*;
import java.awt.image.*;

// This class has some useful image methods that are used when loading in images to the game
public class ImageUtils {
	// changes desired color to be transparent (the chosen color will not be seen in game when drawn)
	public static BufferedImage transformColorToTransparency(BufferedImage image, Color transparentColor) {
		// (works on the whole pixel array at once -- same result as going pixel by pixel with
		// drawRect, which took several milliseconds for even a small sprite sheet)
		int width = image.getWidth(), height = image.getHeight();
		int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
		int transparentColorIndex = transparentColor.getRGB();

		// if a pixel is the transparent color it becomes fully transparent, otherwise fully opaque
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = pixels[i] == transparentColorIndex ? 0 : pixels[i] | 0xFF000000;
		}
		BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		newImage.getRaster().setDataElements(0, 0, width, height, pixels);
		return newImage;
	}

	// resizes an image
	public static BufferedImage resizeImage(BufferedImage image, int newWidth, int newHeight) {
		BufferedImage resized = new BufferedImage(newWidth, newHeight, image.getType());
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(image, 0, 0, newWidth, newHeight, 0, 0, image.getWidth(),
				image.getHeight(), null);
		g.dispose();
		return resized;
	}

	public static BufferedImage createSolidImage(Color color) {
		BufferedImage nothing = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		nothing = ImageUtils.transformColorToTransparency(nothing, color);
		return nothing;
	}

	public static BufferedImage createSolidImage(Color color, int width, int height) {
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		image = ImageUtils.transformColorToTransparency(image, color);
		return resizeImage(image, width, height);
	}
}
