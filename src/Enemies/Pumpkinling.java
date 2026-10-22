package Enemies;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.Rectangle;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.MapEntity;
import Level.MapEntityStatus;
import Level.Player;
import Utils.Direction;
import Utils.Point;

import java.util.HashMap;


public class Pumpkinling extends Enemy {
    private float velocityX, velocityY;
    private float gravity = 0.5f;   // keep it at least .5 or the ground check misses every other frame
    private float terminalVelocity = 9f;
    private float walkSpeed;
    private boolean onGround = false;
    private boolean hasLanded = false;
    private Direction facing;

    public Pumpkinling(Point location, float velocityX, float velocityY, float walkSpeed) {
        super(location.x, location.y, new SpriteSheet(ImageLoader.load("GourdzillaFX.png"), 16, 16), velocityX > 0 ? "AIR_RIGHT" : "AIR_LEFT");
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.walkSpeed = walkSpeed;
        this.facing = velocityX > 0 ? Direction.RIGHT : Direction.LEFT;
        initialize();
        setIsUpdateOffScreen(true);   // keep chasing even if the camera scrolls away
    }

    public boolean isPopped() {
        return mapEntityStatus == MapEntityStatus.REMOVED;
    }

    public void pop() {
        if (isPopped()) return;
        mapEntityStatus = MapEntityStatus.REMOVED;
        GourdProjectile.puff(map, centerX(getBounds()), getBounds().getY2() + 1, 0);
    }

    @Override
    public boolean hitByBolt(Player player) {
        player.addCoins(1);
        pop();
        return true;
    }

    @Override
    public void update(Player player) {
        // one sword hit and he's done
        if (player.isAttacking() && inSwordRange(player)) {
            player.addCoins(1);
            pop();
            return;
        }

        // on the ground: waddle toward the knight
        if (onGround) {
            float dx = centerX(player.getBounds()) - centerX(getBounds());
            if (Math.abs(dx) > 8) {
                facing = dx < 0 ? Direction.LEFT : Direction.RIGHT;
                velocityX = facing == Direction.LEFT ? -walkSpeed : walkSpeed;
            } else {
                velocityX = 0;   // right under him, just wait
            }
        }

        velocityY = Math.min(velocityY + gravity, terminalVelocity);
        moveYHandleCollision(velocityY);
        moveXHandleCollision(velocityX);

        String side = facing == Direction.LEFT ? "_LEFT" : "_RIGHT";
        if (!onGround) currentAnimationName = "AIR" + side;
        else if (velocityX != 0) currentAnimationName = "WALK" + side;
        else currentAnimationName = "STAND" + side;

        super.update(player);
    }

    private float centerX(Rectangle r) {
        return r.getX1() + r.getWidth() / 2f;
    }

    private boolean inSwordRange(Player player) {
        Rectangle me = getBounds(), p = player.getBounds();
        float dx = centerX(me) - centerX(p);
        float dy = (me.getY1() + me.getHeight() / 2f) - (p.getY1() + p.getHeight() / 2f);
        if (Math.abs(dy) > 60) return false;
        return player.getFacingDirection() == Direction.RIGHT ? (dx > -24 && dx < 80) : (dx < 24 && dx > -80);
    }

    @Override
    public void touchedPlayer(Player player) {
        if (player.isHurtInvincible()) return;
        player.damage(1);
        // bonk! it bounces back off the knight
        float away = centerX(getBounds()) < centerX(player.getBounds()) ? -1 : 1;
        velocityX = away * 2.5f;
        velocityY = -5f;
        onGround = false;
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (hasCollided && !onGround) {
            velocityX = 0;   // hit a wall in mid air, just drop
        }
    }

    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (direction == Direction.DOWN) {
            if (hasCollided) {
                velocityY = 0;
                onGround = true;
                if (!hasLanded) {   // first landing after flying out of the boss
                    hasLanded = true;
                    GourdProjectile.puff(map, centerX(getBounds()), getBounds().getY2() + 1, 0);
                }
            } else {
                onGround = false;
            }
        } else if (hasCollided) {
            velocityY = 0;   // bumped its head
        }
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet s) {
        HashMap<String, Frame[]> a = new HashMap<>();
        for (String side : new String[] { "LEFT", "RIGHT" }) {
            boolean right = side.equals("RIGHT");
            a.put("WALK_" + side, new Frame[] { frame(s, 4, 8, right), frame(s, 5, 8, right) });
            a.put("STAND_" + side, new Frame[] { frame(s, 4, 0, right) });
            a.put("AIR_" + side, new Frame[] { frame(s, 5, 0, right) });
        }
        return a;
    }

    private Frame frame(SpriteSheet s, int cell, int delay, boolean right) {
        FrameBuilder b = new FrameBuilder(s.getSprite(0, cell), delay).withScale(3).withBounds(2, 4, 12, 12);
        if (right) b.withImageEffect(ImageEffect.FLIP_HORIZONTAL);   // the art faces left
        return b.build();
    }
}
