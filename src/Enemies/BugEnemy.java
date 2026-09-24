package Enemies;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.MapEntity;
import Level.Player;
import Utils.AirGroundState;
import Utils.Direction;
import Utils.Point;

import java.util.HashMap;

// This class is for the black bug enemy
// enemy behaves like a Mario goomba -- walks forward until it hits a solid map tile, and then turns around
// if it ends up in the air from walking off a cliff, it will fall down until it hits the ground again, and then will continue walking
public class BugEnemy extends Enemy {
    private float gravity = .5f;
    private float movementSpeed = .5f;
    protected int hurtTimer = 0;
    protected int hurtDuration = 45;        // frames of red, ~0.75s at 60fps
    protected int knockbackTimer = 0;
    protected int knockbackDuration = 14;
    protected float knockbackSpeed = 4f;
    protected Direction knockbackDirection = Direction.LEFT;
    private Direction startFacingDirection;
    private Direction facingDirection;
    private AirGroundState airGroundState;
   

    public BugEnemy(Point location, Direction facingDirection) {
        super(location.x, location.y, 
            new SpriteSheet(ImageLoader.load("Goblin.png"), 104, 80), "WALK_RIGHT");
            
        this.startFacingDirection = facingDirection;
        this.initialize();
    }

    @Override
    public void touchedPlayer(Player player) {
        if (hurtTimer > 0) {
            return;                 // already reeling, ignore repeat contact
        }
        hurtTimer = hurtDuration;
        knockbackTimer = knockbackDuration;
        knockbackDirection = (getX() < player.getX()) ? Direction.LEFT : Direction.RIGHT;
    }

    @Override
    public void initialize() {
        super.initialize();
        facingDirection = startFacingDirection;
        if (facingDirection == Direction.RIGHT) {
            currentAnimationName = "WALK_RIGHT";
        } else if (facingDirection == Direction.LEFT) {
            currentAnimationName = "WALK_LEFT";
        }
        airGroundState = AirGroundState.GROUND;
    }

    
    @Override
    public void update(Player player) {
        float moveAmountX = 0;
        float moveAmountY = 0;

        moveAmountY += gravity;

        if (hurtTimer > 0) {
            hurtTimer--;
        }

        if (knockbackTimer > 0) {
            knockbackTimer--;
            moveAmountX += knockbackDirection == Direction.RIGHT ? knockbackSpeed : -knockbackSpeed;
        } else if (airGroundState == AirGroundState.GROUND) {
            moveAmountX += facingDirection == Direction.RIGHT ? movementSpeed : -movementSpeed;
        }

        moveYHandleCollision(moveAmountY);
        moveXHandleCollision(moveAmountX);

        if (hurtTimer > 0) {
            currentAnimationName = facingDirection == Direction.RIGHT ? "HURT_RIGHT" : "HURT_LEFT";
        } else {
            currentAnimationName = facingDirection == Direction.RIGHT ? "WALK_RIGHT" : "WALK_LEFT";
        }

        super.update(player);
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction,  MapEntity entityCollidedWith) {
        // if bug has collided into something while walking forward,
        // it turns around (changes facing direction)
        if (knockbackTimer > 0) {
            return;
        }

        if (hasCollided) {
            if (direction == Direction.RIGHT) {
                facingDirection = Direction.LEFT;
                currentAnimationName = "WALK_LEFT";
            } else {
                facingDirection = Direction.RIGHT;
                currentAnimationName = "WALK_RIGHT";
            }
        }
    }

    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        // if bug is colliding with the ground, change its air ground state to GROUND
        // if it is not colliding with the ground, it means that it's currently in the air, so its air ground state is changed to AIR
        if (direction == Direction.DOWN) {
            if (hasCollided) {
                airGroundState = AirGroundState.GROUND;
            } else {
                airGroundState = AirGroundState.AIR;
            }
        }
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{

            put("WALK_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0), 20)
                    .withScale(1).withBounds(35, 10, 38, 66).build(),
                new FrameBuilder(spriteSheet.getSprite(0, 1), 20)
                    .withScale(1).withBounds(35, 10, 38, 66).build()
            });

            put("WALK_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0), 20)
                    .withScale(1).withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .withBounds(31, 10, 38, 66).build(),
                new FrameBuilder(spriteSheet.getSprite(0, 1), 20)
                    .withScale(1).withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .withBounds(31, 10, 38, 66).build()
            });

            put("HURT_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(1, 0), 20)
                    .withScale(1).withBounds(35, 10, 38, 66).build(),
                new FrameBuilder(spriteSheet.getSprite(1, 1), 20)
                    .withScale(1).withBounds(35, 10, 38, 66).build()
        
            });

            put("HURT_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(1, 0), 20)
                    .withScale(1).withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .withBounds(31, 10, 38, 66).build(),
                new FrameBuilder(spriteSheet.getSprite(1, 1), 20)
                    .withScale(1).withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .withBounds(31, 10, 38, 66).build()
            });
        }};
    }
}
