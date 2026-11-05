package EnhancedMapTiles;

import Engine.Config;
import Engine.GraphicsHandler;
import GameObject.Rectangle;
import Utils.ImageUtils;
import Utils.Point;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class LavaBossPlatform extends StationaryPlatform {
    private static final float PLATFORM_SCALE = 0.238f;
    private static final int PLATFORM_IMAGE_X = 661;
    private static final int PLATFORM_IMAGE_Y = 1073;
    private static final int PLATFORM_IMAGE_WIDTH = 808;
    private static final int PLATFORM_IMAGE_HEIGHT = 226;
    private static final int PLATFORM_SURFACE_HEIGHT = 24;
    private static final int OUTLINE_SIZE = 5;
    private static final int OUTLINE_PADDING = (int) Math.ceil(OUTLINE_SIZE / PLATFORM_SCALE);
    private static final BufferedImage PLATFORM_IMAGE = loadPlatformImage();
    private static final BufferedImage OUTLINED_PLATFORM_IMAGE = createOutlinedImage(PLATFORM_IMAGE);

    public LavaBossPlatform(Point location) {
        super(
            PLATFORM_IMAGE,
            location,
            PLATFORM_SCALE,
            new Rectangle(0, 0, PLATFORM_IMAGE_WIDTH, PLATFORM_SURFACE_HEIGHT),
            1
        );
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        int outlineX = Math.round(OUTLINE_PADDING * PLATFORM_SCALE);
        graphicsHandler.drawImage(
            OUTLINED_PLATFORM_IMAGE,
            Math.round(getCalibratedXLocation()) - outlineX,
            Math.round(getCalibratedYLocation()) - outlineX,
            getWidth() + outlineX * 2,
            getHeight() + outlineX * 2
        );
    }

    private static BufferedImage loadPlatformImage() {
        try {
            BufferedImage sourceImage = ImageIO.read(new File(Config.RESOURCES_PATH + "Lavabossplatform.png"));
            if (sourceImage == null) {
                throw new IOException("The lava boss platform image could not be decoded.");
            }
            BufferedImage platformImage = sourceImage.getSubimage(
                PLATFORM_IMAGE_X,
                PLATFORM_IMAGE_Y,
                PLATFORM_IMAGE_WIDTH,
                PLATFORM_IMAGE_HEIGHT
            );
            return ImageUtils.transformColorToTransparency(platformImage, Config.TRANSPARENT_COLOR);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load the lava boss platform image.", e);
        }
    }

    private static BufferedImage createOutlinedImage(BufferedImage image) {
        BufferedImage silhouette = new BufferedImage(
            image.getWidth(),
            image.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D silhouetteGraphics = silhouette.createGraphics();
        silhouetteGraphics.drawImage(image, 0, 0, null);
        silhouetteGraphics.setComposite(AlphaComposite.SrcIn);
        silhouetteGraphics.setColor(Color.WHITE);
        silhouetteGraphics.fillRect(0, 0, silhouette.getWidth(), silhouette.getHeight());
        silhouetteGraphics.dispose();

        BufferedImage outlinedImage = new BufferedImage(
            image.getWidth() + OUTLINE_PADDING * 2,
            image.getHeight() + OUTLINE_PADDING * 2,
            BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D outlineGraphics = outlinedImage.createGraphics();
        for (int offsetY = -OUTLINE_PADDING; offsetY <= OUTLINE_PADDING; offsetY++) {
            for (int offsetX = -OUTLINE_PADDING; offsetX <= OUTLINE_PADDING; offsetX++) {
                if (offsetX * offsetX + offsetY * offsetY <= OUTLINE_PADDING * OUTLINE_PADDING) {
                    outlineGraphics.drawImage(
                        silhouette,
                        OUTLINE_PADDING + offsetX,
                        OUTLINE_PADDING + offsetY,
                        null
                    );
                }
            }
        }
        outlineGraphics.drawImage(image, OUTLINE_PADDING, OUTLINE_PADDING, null);
        outlineGraphics.dispose();
        return outlinedImage;
    }
}
