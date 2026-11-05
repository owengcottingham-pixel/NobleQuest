package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.ForestChunk;
import Enemies.GourdProjectile;
import Enemies.RollingBoulder;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.Effects;
import Utils.Point;

import java.awt.image.BufferedImage;


public class BoulderTrigger extends EnhancedMapTile {
    private static final int RUMBLE_TIME = 45;

    private final Point spawn;
    private RuinWall wall;
    private int timer = -1;
    private boolean done = false;
    private RollingBoulder boulder;

    public BoulderTrigger(Point location, int widthInTiles, int heightInTiles, Point spawn) {
        super(location.x, location.y,
                new FrameBuilder(new BufferedImage(16 * widthInTiles, 16 * heightInTiles, BufferedImage.TYPE_INT_ARGB))
                        .withScale(3)
                        .build(),
                TileType.PASSABLE);
        this.spawn = spawn;
        setIsUpdateOffScreen(true);
    }

    // the wall the boulder bursts out of (it shakes during the rumble and explodes when it comes out)
    public BoulderTrigger wall(RuinWall wall) {
        this.wall = wall;
        return this;
    }

    public RollingBoulder getBoulder() {
        return boulder;
    }

    public boolean isDone() {
        return done;
    }

    @Override
    public void update(Player player) {
        super.update(player);
        if (done) return;

        if (timer < 0 && intersects(player)) {
            timer = RUMBLE_TIME;
            Effects.shake(2.5f, RUMBLE_TIME);      // ...uh oh
        }
        if (timer > 0) {
            if (wall != null) wall.rumble(1 - timer / (float) RUMBLE_TIME);
            if (timer % 5 == 0) {
                // pebbles shaken loose from the wall the boulder is about to come out of
                float x = spawn.x + (float) Math.random() * 140;
                ForestChunk.burst(map, x, spawn.y - 10, new int[] { ForestChunk.STONE, ForestChunk.LEAF }, 2, 1.2f);
            }
        }
        if (timer > 0 && --timer == 0) {
            done = true;
            boulder = new RollingBoulder(spawn);
            map.addEnemy(boulder);
            if (wall != null) wall.burst();
            Effects.shake(11, 26);
            Effects.banner(Effects.BannerStyle.RUN, "RUN!", null, 90);
            float cx = spawn.x + 72, ground = spawn.y + 144;
            GourdProjectile.puff(map, cx - 50, ground, -2f);
            GourdProjectile.puff(map, cx + 50, ground, 2f);
            ForestChunk.burst(map, cx, spawn.y + 50,
                    new int[] { ForestChunk.STONE, ForestChunk.STONE, ForestChunk.ROCK, ForestChunk.MOSS_ROCK }, 24, 7f);
        }
    }
}
