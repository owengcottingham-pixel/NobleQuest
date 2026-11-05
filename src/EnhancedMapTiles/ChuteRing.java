package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.ForestChunk;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.Point;

import java.awt.image.BufferedImage;

// A glowing ring for the chute
public class ChuteRing extends EnhancedMapTile {
    private final BufferedImage dim, lit;
    private float boost = 27f;
    private int flash = 0;
    private boolean passed = false;

    public ChuteRing(Point location) {
        super(location.x, location.y,
                new FrameBuilder(new BufferedImage(48, 16, BufferedImage.TYPE_INT_ARGB))
                        .withScale(3)
                        .withBounds(0, 2, 48, 12)
                        .build(),
                TileType.PASSABLE);
        SpriteSheet sheet = new SpriteSheet(ImageLoader.load("ChuteRing.png"), 48, 16);
        dim = sheet.getSprite(0, 0);
        lit = sheet.getSprite(0, 1);
        initialize();
    }

    public ChuteRing boost(float boost) {
        this.boost = boost;
        return this;
    }

    @Override
    public void update(Player player) {
        if (intersects(player)) {
            player.bounce(boost);
            if (flash < 8) {
                ForestChunk.burst(map, getBounds().getX1() + 72, getBounds().getY1() + 20,
                        new int[] { ForestChunk.SPARK, ForestChunk.SEED }, passed ? 2 : 6, 4f);
            }
            flash = 24;
            passed = true;
        }
        if (flash > 0) flash--;
        super.update(player);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        int x = Math.round(getCalibratedXLocation());
        int y = Math.round(getCalibratedYLocation());
        // lit while you're passing, and blinks now and then afterwards so you can see the way
        boolean on = flash > 0 ? (flash > 12 || flash % 4 < 2) : passed;
        graphicsHandler.drawImage(on ? lit : dim, x, y, 144, 48);
    }
}
