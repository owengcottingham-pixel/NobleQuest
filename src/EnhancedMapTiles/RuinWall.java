package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Level.EnhancedMapTile;
import Level.MapEntityStatus;
import Level.Player;
import Level.TileType;
import Utils.Point;


public class RuinWall extends EnhancedMapTile {
    private float rumble = 0;

    public RuinWall(Point location) {
        super(location.x, location.y,
                new FrameBuilder(ImageLoader.load("RuinWall.png")).withScale(3).build(),
                TileType.PASSABLE);
        initialize();
        setIsUpdateOffScreen(true);
    }

    // 0..1: how hard it's shaking
    public void rumble(float amount) {
        rumble = amount;
    }

    public void burst() {
        mapEntityStatus = MapEntityStatus.REMOVED;
    }

    @Override
    public void update(Player player) {
        if (mapEntityStatus == MapEntityStatus.REMOVED) return;
        super.update(player);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (mapEntityStatus == MapEntityStatus.REMOVED) return;
        if (rumble > 0) {
            float x = getX();
            setX(x + (float) (Math.random() * 2 - 1) * (1 + 4 * rumble));
            super.draw(graphicsHandler);
            setX(x);
        } else {
            super.draw(graphicsHandler);
        }
    }
}
