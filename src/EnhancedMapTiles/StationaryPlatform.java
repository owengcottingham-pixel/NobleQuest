package EnhancedMapTiles;

import Builders.FrameBuilder;
import GameObject.Rectangle;
import Level.EnhancedMapTile;
import Level.TileType;
import Utils.Point;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class StationaryPlatform extends EnhancedMapTile {

    public StationaryPlatform(BufferedImage image, Point location, float scale, Rectangle bounds, int lengthMultiplier) {
        super(
            location.x,
            location.y,
            new FrameBuilder(createRepeatedImage(image, lengthMultiplier))
                .withBounds(new Rectangle(bounds.getX1(), bounds.getY1(), bounds.getWidth() * lengthMultiplier, bounds.getHeight()))
                .withScale(scale)
                .build(),
            TileType.JUMP_THROUGH_PLATFORM
        );
        initialize();
    }

    private static BufferedImage createRepeatedImage(BufferedImage image, int lengthMultiplier) {
        if (lengthMultiplier < 1) {
            throw new IllegalArgumentException("lengthMultiplier must be at least 1");
        }

        BufferedImage repeatedImage = new BufferedImage(image.getWidth() * lengthMultiplier, image.getHeight(), image.getType());
        Graphics2D graphics = repeatedImage.createGraphics();
        for (int index = 0; index < lengthMultiplier; index++) {
            graphics.drawImage(image, index * image.getWidth(), 0, null);
        }
        graphics.dispose();
        return repeatedImage;
    }

}
