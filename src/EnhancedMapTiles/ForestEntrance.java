package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.ForestChunk;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.Effects;
import Utils.Point;

import java.awt.image.BufferedImage;

// An invisible zone at the top of the chute; screen announces where you are
public class ForestEntrance extends EnhancedMapTile {
    private final String title, subtitle;
    private boolean done = false;

    public ForestEntrance(Point location, int widthInTiles, int heightInTiles, String title, String subtitle) {
        super(location.x, location.y,
                new FrameBuilder(new BufferedImage(16 * widthInTiles, 16 * heightInTiles, BufferedImage.TYPE_INT_ARGB))
                        .withScale(3)
                        .build(),
                TileType.PASSABLE);
        this.title = title;
        this.subtitle = subtitle;
    }

    @Override
    public void update(Player player) {
        super.update(player);
        if (done || !intersects(player)) return;
        done = true;
        float cx = player.getBounds().getX1() + player.getBounds().getWidth() / 2f;
        float cy = player.getBounds().getY1() + 20;
        ForestChunk.burst(map, cx, cy, new int[] { ForestChunk.LEAF, ForestChunk.LEAF, ForestChunk.BARK, ForestChunk.WISP }, 26, 7f);
        Effects.shake(7, 16);
        Effects.banner(Effects.BannerStyle.TOAST, title, subtitle, 170);
    }
}
