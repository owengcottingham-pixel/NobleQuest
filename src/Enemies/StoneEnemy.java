package Enemies;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Player;
import Utils.Direction;
import Utils.Point;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class StoneEnemy extends Goblin {
    private static final int FIREBALL_CHARGE_FRAMES = 30;
    private static final int FIREBALL_COOLDOWN_FRAMES = 90;
    private static final int FIREBALL_RANGE_TILES = 15;

    private int fireballChargeTimer;
    private int fireballCooldownTimer;

    public StoneEnemy(Point location, Direction facingDirection) {
        super(location, facingDirection,
              new SpriteSheet(ImageLoader.loadSubImage("stoneEnemy.png", 45, 112, 357, 413), 357, 413));
        attackRange = 0;
    }

    @Override
    protected boolean isFlying() {
        return true;
    }

    @Override
    protected boolean updateSpecialAttack(Player player) {
        if (fireballChargeTimer > 0) {
            fireballChargeTimer--;
            if (fireballChargeTimer == 0) {
                launchFireball();
                fireballCooldownTimer = FIREBALL_COOLDOWN_FRAMES;
            }
            return true;
        }

        if (fireballCooldownTimer > 0) {
            fireballCooldownTimer--;
            return false;
        }

        if (playerIsInFrontWithinRange(player)) {
            fireballChargeTimer = FIREBALL_CHARGE_FRAMES;
            return true;
        }

        return false;
    }

    protected boolean playerIsInFrontWithinRange(Player player) {
        float enemyCenterX = getX() + getWidth() / 2f;
        float playerCenterX = player.getX() + player.getWidth() / 2f;
        float distance = playerCenterX - enemyCenterX;
        float range = FIREBALL_RANGE_TILES * map.getTileset().getScaledSpriteWidth();
        float fireballPathY = getY() + 36;
        boolean playerIsOnFireballPath = player.getBounds().getY1() <= fireballPathY
                && player.getBounds().getY2() >= fireballPathY;

        return playerIsOnFireballPath && (facingDirection == Direction.RIGHT
                ? distance > 0 && distance <= range
                : distance < 0 && distance >= -range);
    }

    protected void launchFireball() {
        float fireballWidth = 171 * StoneFireball.SPRITE_SCALE;
        float fireballHeight = 176 * StoneFireball.SPRITE_SCALE;
        float handCenterX = facingDirection == Direction.RIGHT
                ? getX() + getWidth() - 11
                : getX() + 11;
        float handCenterY = getY() + 36;
        Point fireballLocation = new Point(
                handCenterX - fireballWidth / 2,
                handCenterY - fireballHeight / 2
        );
        int maxDistance = FIREBALL_RANGE_TILES * map.getTileset().getScaledSpriteWidth();
        map.addEnemy(new StoneFireball(fireballLocation, facingDirection, maxDistance));
    }

    @Override
    protected void updateSpecialAttackAnimation() {
        currentAnimationName = facingDirection == Direction.RIGHT
                ? "HOLDING_FIREBALL_RIGHT"
                : "HOLDING_FIREBALL_LEFT";
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        BufferedImage stonePose = spriteSheet.getImage();
        BufferedImage castingPose = ImageLoader.loadSubImage("stoneEnemy.png", 590, 119, 357, 413);

        Frame[] walkRight = {new FrameBuilder(stonePose, 20)
                .withScale(0.2f)
                .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                .build()};
        Frame[] walkLeft = {new FrameBuilder(stonePose, 20).withScale(0.2f).build()};
        Frame[] castingRight = {new FrameBuilder(castingPose)
                .withScale(0.2f)
                .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                .build()};
        Frame[] castingLeft = {new FrameBuilder(castingPose).withScale(0.2f).build()};

        return new HashMap<String, Frame[]>() {{
            put("WALK_RIGHT", walkRight);
            put("WALK_LEFT", walkLeft);
            put("HURT_RIGHT", walkRight);
            put("HURT_LEFT", walkLeft);
            put("WINDUP_RIGHT", walkRight);
            put("WINDUP_LEFT", walkLeft);
            put("STRIKE_RIGHT", castingRight);
            put("STRIKE_LEFT", castingLeft);
            put("HOLDING_FIREBALL_RIGHT", castingRight);
            put("HOLDING_FIREBALL_LEFT", castingLeft);
        }};
    }
}
