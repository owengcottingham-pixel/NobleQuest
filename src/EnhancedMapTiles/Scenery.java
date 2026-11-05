package EnhancedMapTiles;

import Builders.FrameBuilder;
import Level.EnhancedMapTile;
import Level.TileType;
import Utils.Point;

import java.awt.image.BufferedImage;

// Background scenery
public class Scenery extends EnhancedMapTile {
    public Scenery(BufferedImage image, Point location, float scale) {
        super(location.x, location.y,
                new FrameBuilder(image).withScale(scale).withBounds(0, 0, 1, 1).build(),
                TileType.PASSABLE);
    }
}
