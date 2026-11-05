package Engine;

import Utils.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

// a bunch of helpful methods for loading images file into the game
public class ImageLoader {

    private static final HashMap<String, BufferedImage> cache = new HashMap<>();

    // loads an image and sets its transparent color to the one defined in the Config class
    public static BufferedImage load(String imageFileName) {
        return ImageLoader.load(imageFileName, Config.TRANSPARENT_COLOR);
    }

    // loads an image and allows the transparent color to be specified
    public static synchronized BufferedImage load(String imageFileName, Color transparentColor) {
        String key = imageFileName + "|" + transparentColor.getRGB();
        BufferedImage cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        try {
            BufferedImage initialImage = ImageIO.read(new File(Config.RESOURCES_PATH + imageFileName));
            BufferedImage image = ImageUtils.transformColorToTransparency(initialImage, transparentColor);
            cache.put(key, image);
            return image;
        } catch (IOException e) {
            System.out.println("Unable to find file " + Config.RESOURCES_PATH + imageFileName);
            throw new RuntimeException(e);
        }
    }

    // loads a piece of an image from an image file and sets its transparent color to the one defined in the Config class
    public static BufferedImage loadSubImage(String imageFileName, int x, int y, int width, int height) {
        return ImageLoader.loadSubImage(imageFileName, Config.TRANSPARENT_COLOR, x, y, width, height);
    }

    // loads a piece of an image from an image file and allows the transparent color to be specified
    public static BufferedImage loadSubImage(String imageFileName, Color transparentColor, int x, int y, int width, int height) {
        return load(imageFileName, transparentColor).getSubimage(x, y, width, height);
    }
}
