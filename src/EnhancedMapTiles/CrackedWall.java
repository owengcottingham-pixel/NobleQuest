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


public class CrackedWall extends EnhancedMapTile {
    private boolean wasAttacking = false;
    private int wobble = 0;

    public CrackedWall(Point location) {
        super(location.x, location.y, new FrameBuilder(ImageLoader.load("CrackedWall.png")).withScale(3).build(), TileType.NOT_PASSABLE);
        initialize();
    }

    @Override
    public void update(Player player) {
        if (mapEntityStatus == MapEntityStatus.REMOVED) return;
        super.update(player);
        Rectangle b = getBounds();
        Rectangle reach = new Rectangle(b.getX1() - 16, b.getY1(), b.getWidth() + 32, b.getHeight());
        if (player.getPlayerState() == Player.PlayerState.DASHING && reach.intersects(player.getBounds())) {
            smash();
            return;
        }
        boolean attacking = player.isAttacking();
        if (attacking && !wasAttacking && inSwordRange(player)) {
            wobble = 8;   // clink -- too tough for the sword
            float sx = player.getFacingDirection() == Direction.RIGHT ? b.getX1() : b.getX2();
            ForestChunk.burst(map, sx, player.getBounds().getY1() + 30, new int[] { ForestChunk.SPARK }, 4, 3f);
        }
        wasAttacking = attacking;
        if (wobble > 0) wobble--;
    }

    private boolean inSwordRange(Player player) {
        Rectangle me = getBounds(), p = player.getBounds();
        float dx = (me.getX1() + me.getWidth() / 2f) - (p.getX1() + p.getWidth() / 2f);
        if (p.getY2() < me.getY1() || p.getY1() > me.getY2()) return false;
        return player.getFacingDirection() == Direction.RIGHT ? (dx > 0 && dx < 100) : (dx < 0 && dx > -100);
    }

    public void smash() {
        if (mapEntityStatus == MapEntityStatus.REMOVED) return;
        mapEntityStatus = MapEntityStatus.REMOVED;
        tileType = TileType.PASSABLE;
        Rectangle b = getBounds();
        float cx = b.getX1() + b.getWidth() / 2f;
        ForestChunk.burst(map, cx, b.getY1() + b.getHeight() / 2f, new int[] { ForestChunk.STONE, ForestChunk.ROCK, ForestChunk.STONE }, 22, 7f);
        GourdProjectile.puff(map, cx - 20, b.getY2() + 1, -1.5f);
        GourdProjectile.puff(map, cx + 20, b.getY2() + 1, 1.5f);
        Effects.shake(8, 14);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (mapEntityStatus == MapEntityStatus.REMOVED) return;
        if (wobble > 0) {
            float x = getX();
            setX(x + (wobble % 4 < 2 ? -2 : 2));
            super.draw(graphicsHandler);
            setX(x);
        } else {
            super.draw(graphicsHandler);
        }
    }
}
