package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.ForestChunk;
import Enemies.GourdProjectile;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Rectangle;
import Level.EnhancedMapTile;
import Level.MapEntityStatus;
import Level.Player;
import Level.TileType;
import Utils.Direction;
import Utils.Effects;
import Utils.Point;

// 2 tiles tall, solid across the chase path.
// You can jump, dash, or attack through it
public class BrambleWall extends EnhancedMapTile {
    private int hits = 0;
    private int wobble = 0;
    private boolean wasAttacking = false;

    public BrambleWall(Point location) {
        super(location.x, location.y,
                new FrameBuilder(ImageLoader.load("BrambleWall.png")).withScale(3).withBounds(1, 2, 14, 30).build(),
                TileType.NOT_PASSABLE);
        initialize();
    }

    @Override
    public void update(Player player) {
        if (mapEntityStatus == MapEntityStatus.REMOVED) return;
        super.update(player);

        // dash into it: it bursts. (it's solid, so check a few px out from its sides)
        Rectangle b = getBounds();
        Rectangle reach = new Rectangle(b.getX1() - 14, b.getY1(), b.getWidth() + 28, b.getHeight());
        if (player.getPlayerState() == Player.PlayerState.DASHING && reach.intersects(player.getBounds())) {
            smash();
            return;
        }

        // two sword hits also do it (one hit per swing)
        boolean attacking = player.isAttacking();
        if (attacking && !wasAttacking && inSwordRange(player)) {
            hits++;
            wobble = 10;
            ForestChunk.burst(map, b.getX1() + b.getWidth() / 2f, b.getY1() + 30, new int[] { ForestChunk.THORN }, 4, 3f);
            if (hits >= 2) {
                smash();
            }
        }
        wasAttacking = attacking;
        if (wobble > 0) wobble--;
    }

    private boolean inSwordRange(Player player) {
        Rectangle me = getBounds(), p = player.getBounds();
        float dx = (me.getX1() + me.getWidth() / 2f) - (p.getX1() + p.getWidth() / 2f);
        if (p.getY2() < me.getY1() || p.getY1() > me.getY2()) return false;
        return player.getFacingDirection() == Direction.RIGHT ? (dx > 0 && dx < 90) : (dx < 0 && dx > -90);
    }

    public void smash() {
        if (mapEntityStatus == MapEntityStatus.REMOVED) return;
        mapEntityStatus = MapEntityStatus.REMOVED;
        tileType = TileType.PASSABLE;     // stop blocking right away, not just from the next frame
        Rectangle b = getBounds();
        float cx = b.getX1() + b.getWidth() / 2f;
        ForestChunk.burst(map, cx, b.getY1() + b.getHeight() / 2f,
                new int[] { ForestChunk.THORN, ForestChunk.THORN, ForestChunk.LEAF }, 16, 6f);
        GourdProjectile.puff(map, cx - 20, b.getY2() + 1, -1.2f);
        GourdProjectile.puff(map, cx + 20, b.getY2() + 1, 1.2f);
        Effects.shake(4, 10);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (mapEntityStatus == MapEntityStatus.REMOVED) return;
        if (wobble > 0) {
            float x = getX();
            setX(x + ((wobble / 2) % 2 == 0 ? -3 : 3));
            super.draw(graphicsHandler);
            setX(x);
        } else {
            super.draw(graphicsHandler);
        }
    }
}
