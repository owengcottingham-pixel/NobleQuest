package EnhancedMapTiles;

import Engine.Config;
import GameObject.Rectangle;
import Utils.ImageUtils;
import Utils.Point;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class LavaBossPlatform extends StationaryPlatform {
    private static final float PLATFORM_SCALE = 0.1f;
    private static final int PLATFORM_SURFACE_HEIGHT = 16;
    private static final int[][] PLATFORM_IMAGE_BOUNDS = {
        {60, 705, 495, 450},
        {690, 705, 525, 525},
        {1320, 705, 540, 450}
    };
    private static final int PLATFORM_IMAGE_WIDTH =
        PLATFORM_IMAGE_BOUNDS[0][2] + PLATFORM_IMAGE_BOUNDS[1][2] + PLATFORM_IMAGE_BOUNDS[2][2];
    private static final BufferedImage PLATFORM_IMAGE = loadPlatformImage();

    public LavaBossPlatform(Point location) {
        super(
            PLATFORM_IMAGE,
            location,
            PLATFORM_SCALE,
            new Rectangle(0, 0, PLATFORM_IMAGE_WIDTH, PLATFORM_SURFACE_HEIGHT),
            1
        );
    }

    public boolean containsVisiblePixel(float worldX, float worldY) {
        float localX = worldX - getX();
        float localY = worldY - getY();
        if (localX < 0 || localY < 0 || localX >= getWidth() || localY >= getHeight()) {
            return false;
        }

        int imageX = (int) (localX / getWidth() * PLATFORM_IMAGE.getWidth());
        int imageY = (int) (localY / getHeight() * PLATFORM_IMAGE.getHeight());
        return (PLATFORM_IMAGE.getRGB(imageX, imageY) >>> 24) != 0;
    }

    private static BufferedImage loadPlatformImage() {
        try {
            BufferedImage sourceImage = ImageIO.read(new File(Config.RESOURCES_PATH + "RockPlatforms.PNG"));
            if (sourceImage == null) {
                throw new IOException("The rock platforms image could not be decoded.");
            }
            BufferedImage platformImage = new BufferedImage(
                PLATFORM_IMAGE_WIDTH,
                PLATFORM_IMAGE_BOUNDS[1][3],
                BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D graphics = platformImage.createGraphics();
            int x = 0;
            for (int index = 0; index < PLATFORM_IMAGE_BOUNDS.length; index++) {
                int[] bounds = PLATFORM_IMAGE_BOUNDS[index];
                BufferedImage piece = sourceImage.getSubimage(
                    bounds[0],
                    bounds[1],
                    bounds[2],
                    bounds[3]
                );
                graphics.drawImage(
                    ImageUtils.transformColorToTransparency(piece, Config.TRANSPARENT_COLOR),
                    x,
                    0,
                    null
                );
                x += bounds[2];
            }
            graphics.dispose();
            return platformImage;
        } catch (IOException e) {
            throw new RuntimeException("Unable to load the rock platforms image.", e);
        }
    }
}
