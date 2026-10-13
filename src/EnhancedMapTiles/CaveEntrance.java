package EnhancedMapTiles;

import Builders.FrameBuilder;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.Point;

import java.awt.image.BufferedImage;

// An invisible zone. Walking into it wins the level.
// location = top-left tile of the zone, width/height are in tiles.
public class CaveEntrance extends EnhancedMapTile {

    public CaveEntrance(Point location, int widthInTiles, int heightInTiles) {
        super(location.x, location.y,
                new FrameBuilder(new BufferedImage(16 * widthInTiles, 16 * heightInTiles, BufferedImage.TYPE_INT_ARGB))
                        .withScale(3)
                        .build(),
                TileType.PASSABLE);
    }

    @Override
    public void update(Player player) {
        super.update(player);
        if (intersects(player)) {
            player.completeLevel();
        }
    }
}