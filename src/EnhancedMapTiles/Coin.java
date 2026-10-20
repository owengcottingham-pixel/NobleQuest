package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Level.EnhancedMapTile;
import Level.MapEntityStatus;
import Level.Player;
import Level.TileType;

public class Coin extends EnhancedMapTile {
    private static final int COIN_VALUE = 10;
    private static final float SCALE = 2f;

    public Coin(float x, float y) {
        super(
            x,
            y,
            new FrameBuilder(ImageLoader.load("Coin.png")).withScale(SCALE).build(),
            TileType.PASSABLE
        );
        initialize();
    }

    @Override
    public void update(Player player) {
        if (mapEntityStatus == MapEntityStatus.REMOVED) {
            return;
        }

        if (intersects(player)) {
            player.addCoins(COIN_VALUE);
            setMapEntityStatus(MapEntityStatus.REMOVED);
            return;
        }

        super.update(player);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (mapEntityStatus != MapEntityStatus.REMOVED) {
            super.draw(graphicsHandler);
        }
    }
}
