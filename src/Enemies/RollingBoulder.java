package Enemies;

import Builders.FrameBuilder;
import EnhancedMapTiles.BrambleWall;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.Rectangle;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.EnhancedMapTile;
import Level.MapEntity;
import Level.MapEntityStatus;
import Level.Player;
import Utils.Direction;
import Utils.Effects;
import Utils.Point;

import java.util.ArrayList;
import java.util.HashMap;


public class RollingBoulder extends Enemy {
    private float speed = 0;
    private float velocityY = 0;
    private boolean onGround = false;
    private float topSpeed = 3.4f;          // speed: a tiny bit than the knight's run (3.5)
    private float downhillBonus = 1.6f;     // extra speed it can build rolling down a slope

    public RollingBoulder(Point location) {
        super(location.x, location.y, new SpriteSheet(ImageLoader.load("RollingBoulder.png"), 48, 48), "ROLL");
        initialize();
        setIsUpdateOffScreen(true);   // keeps rolling even if you get way ahead of it
    }

    public RollingBoulder topSpeed(float topSpeed) {
        this.topSpeed = topSpeed;
        return this;
    }

    public boolean isRolling() {
        return mapEntityStatus != MapEntityStatus.REMOVED;
    }

    @Override
    public void update(Player player) {
        if (mapEntityStatus == MapEntityStatus.REMOVED) return;
        Rectangle b = getBounds();

        // smash bramble walls just ahead (before moving, since they're solid)
        Rectangle ahead = new Rectangle(b.getX1(), b.getY1(), (int) (b.getWidth() + speed + 8), b.getHeight());
        for (EnhancedMapTile tile : new ArrayList<>(map.getEnhancedMapTiles())) {
            if (tile instanceof BrambleWall && tile.getMapEntityStatus() != MapEntityStatus.REMOVED
                    && tile.getBounds().intersects(ahead)) {
                ((BrambleWall) tile).smash();
            }
        }


        int tile = map.getTileset().getScaledSpriteWidth();
        float gap = player.getBounds().getX1() - b.getX2();
        float target = gap < -20 ? topSpeed
                : Math.max(2.8f, Math.min(7f, topSpeed + (gap / tile - 4.5f) * 0.6f));
        if (speed < target) speed = Math.min(target, speed + 0.1f);
        else speed = Math.max(target, speed - 0.15f);
        float yBefore = getY();
        boolean wasInAir = !onGround;
        float fallSpeed = velocityY;
        velocityY = Math.min(velocityY + 0.6f, 12f);
        moveYHandleCollision(velocityY);
        float moved = moveXHandleCollision(speed);
        float dy = getY() - yBefore;

        if (onGround) {
            if (dy < -0.5f) {
                speed *= 0.985f;                                                            // uphill: slows a little
            } else if (dy > 0.5f) {
                speed = Math.min(Math.max(target, topSpeed) + downhillBonus, speed + 0.2f);   // downhill: picks up speed
            }
            if (moved < speed * 0.5f) {
                velocityY = -9f;      // bumped into a step: hop it
                onGround = false;
            }
        }
        if (wasInAir && onGround && fallSpeed > 6) {
            Effects.shake(7, 14);     // THOOM
            GourdProjectile.puff(map, b.getX1() + 20, b.getY2() + 1, -1.5f);
            GourdProjectile.puff(map, b.getX2() - 20, b.getY2() + 1, 1.5f);
        }

        // the ground rumbles the closer it gets
        float dist = Math.abs((b.getX1() + b.getWidth() / 2f) - (player.getBounds().getX1() + player.getBounds().getWidth() / 2f));
        if (dist < 720) {
            Effects.shake(1f + 3f * (1 - dist / 720f), 2);
        }
        // leaves and thorns torn up as it goes
        if (onGround && speed > 2 && Math.random() < 0.12) {
            ForestChunk.burst(map, b.getX1() + 30, b.getY2() - 4, new int[] { ForestChunk.LEAF, ForestChunk.STONE }, 1, 2.5f);
        }

        // rolled off into a chasm
        if (getBounds().getY1() > map.getEndBoundY()) {
            mapEntityStatus = MapEntityStatus.REMOVED;
            Effects.shake(12, 34);
            return;
        }

        currentAnimationName = speed > topSpeed + 0.5f ? "ROLL_FAST" : "ROLL";
        super.update(player);
    }

    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (direction == Direction.DOWN) {
            if (hasCollided) {
                velocityY = 0;
                onGround = true;
            } else {
                onGround = false;
            }
        } else if (hasCollided) {
            velocityY = 0;
        }
    }

    @Override
    public void touchedPlayer(Player player) {
        if (player.isHurtInvincible() || player.getHealth() <= 0) return;
        // only the front of it is dangerous -- once it's past you (or you jumped clean over it)
        // you can't get hurt by bumping into its back
        Rectangle b = getBounds(), p = player.getBounds();
        if (p.getX1() + p.getWidth() / 2f < b.getX1() + b.getWidth() / 2f - 6) return;
        player.damage(3);
        player.bounce(11f);      // flattened... and flung
        Effects.shake(10, 20);
        ForestChunk.burst(map, p.getX1() + p.getWidth() / 2f, p.getY2() - 20,
                new int[] { ForestChunk.ROCK, ForestChunk.MOSS_ROCK, ForestChunk.LEAF }, 10, 5f);
    }

    @Override
    public boolean hitByBolt(Player player) {
        return true;   // bolts just bounce off
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet s) {
        HashMap<String, Frame[]> a = new HashMap<>();
        a.put("ROLL", frames(s, 6));
        a.put("ROLL_FAST", frames(s, 3));
        return a;
    }

    private Frame[] frames(SpriteSheet s, int delay) {
        Frame[] f = new Frame[4];
        for (int i = 0; i < 4; i++) {
            f[i] = new FrameBuilder(s.getSprite(0, i), delay).withScale(3).withBounds(4, 4, 40, 42).build();
        }
        return f;
    }
}
