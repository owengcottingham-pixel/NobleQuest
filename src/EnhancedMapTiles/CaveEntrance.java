package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.Point;

// Walking into the cave completes the level and advances to the next one.
public class CaveEntrance extends EnhancedMapTile {

    public CaveEntrance(Point location) {
        super(location.x, location.y,
                new FrameBuilder(ImageLoader.load("Cave.png"))
                        .withScale(3)
                        .withBounds(27, 30, 15, 32)
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