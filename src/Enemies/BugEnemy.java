package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.MapEntity;
import Level.MapEntityStatus;
import Level.Player;
import Utils.AirGroundState;
import Utils.Direction;
import Utils.Point;

import java.awt.Color;
import java.util.HashMap;

// This class is for the black bug enemy
// enemy behaves like a Mario goomba -- walks forward until it hits a solid map tile, and then turns around
// if it ends up in the air from walking off a cliff, it will fall down until it hits the ground again, and then will continue walking
public class BugEnemy extends Enemy {
    protected float gravity = .5f;
    protected float momentumY = 0;
    protected float momentumYIncrease = .5f;
    protected float terminalVelocityY = 6f;
    protected float movementSpeed = .5f;

    protected int maxHealth = 3;
    protected int health = maxHealth;
    protected int swordReach = 85;
    protected int attackDamage = 1;

    protected int hurtTimer = 0;
    protected int hurtDuration = 45;        // frames of red, ~0.75s at 60fps
    protected int knockbackTimer = 0;
    protected int knockbackDuration = 14;
    protected float knockbackSpeed = 4f;
    protected Direction knockbackDirection = Direction.LEFT;
    protected Direction startFacingDirection;
    private Direction facingDirection;
    private AirGroundState airGroundState;
    protected int attackRange = 80;          // horizontal px before he commits
    protected int attackHeightRange = 60;    // ignores the knight on a far ledge
    protected int windupFrames = 20;         // axe held up
    protected int strikeFrames = 18;         // axe out front
    protected int attackCooldown = 70;
    protected int attackTimer = 0;
    protected int attackCooldownTimer = 0;
   

    public BugEnemy(Point location, Direction facingDirection) {
        this(location, facingDirection, new SpriteSheet(ImageLoader.load("Goblin.png"), 104, 80));
    }

    protected BugEnemy(Point location, Direction facingDirection, SpriteSheet spriteSheet) {
        super(location.x, location.y, spriteSheet, "WALK_RIGHT");
        this.startFacingDirection = facingDirection;
        this.initialize();
    }

    @Override
    public void touchedPlayer(Player player) {
       
    }

    private boolean isInSwordRange(Player player) {
        float dx = getX() - player.getX();
        if (Math.abs(getY() - player.getY()) > 60) {
            return false;
        }
        return player.getFacingDirection() == Direction.RIGHT
            ? (dx > -20 && dx < swordReach)
            : (dx < 20 && dx > -swordReach);
    }

    protected void takeHit(Player player) {
        health--;
        hurtTimer = hurtDuration;
        knockbackTimer = knockbackDuration;
        knockbackDirection = (getX() < player.getX()) ? Direction.LEFT : Direction.RIGHT;
        attackTimer = 0;

        if (health <= 0) {
            player.addCoins(5);
            mapEntityStatus = MapEntityStatus.REMOVED;
        }
    }

    private boolean isPlayerInRange(Player player) {
        float dx = Math.abs(player.getX() - getX());
        float dy = Math.abs(player.getY() - getY());
        return dx <= attackRange && dy <= attackHeightRange;
    }

    protected void onStrike(Player player) {
        if (isPlayerInRange(player)) {
            player.damage(attackDamage);
        }
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

        moveAmountY += gravity + momentumY;

        if (hurtTimer > 0) {
            hurtTimer--;
        }
        if (player.getPlayerState() == Player.PlayerState.ATTACKING && hurtTimer == 0 && isInSwordRange(player)) {
            takeHit(player);
        }
        if (attackCooldownTimer > 0) {
            attackCooldownTimer--;
        }

        if (attackTimer == 0 && attackCooldownTimer == 0
                && hurtTimer == 0 && knockbackTimer == 0
                && airGroundState == AirGroundState.GROUND
                && isPlayerInRange(player)) {
            facingDirection = (player.getX() > getX()) ? Direction.RIGHT : Direction.LEFT;
            attackTimer = windupFrames + strikeFrames;
        }

        if (knockbackTimer > 0) {
            knockbackTimer--;
            attackTimer = 0;                       // taking a hit cancels the swing
            moveAmountX += knockbackDirection == Direction.RIGHT ? knockbackSpeed : -knockbackSpeed;
        } else if (attackTimer > 0) {
            attackTimer--;                          // planted while swinging
            if (attackTimer == strikeFrames) {
                onStrike(player);                   // the frame the axe comes forward
            }
        
            if (attackTimer == 0) {
                attackCooldownTimer = attackCooldown;
            }
        } else if (airGroundState == AirGroundState.GROUND) {
            moveAmountX += facingDirection == Direction.RIGHT ? movementSpeed : -movementSpeed;
        }

        moveYHandleCollision(moveAmountY);

        // build up fall speed while airborne so it drops at a believable rate
        if (airGroundState == AirGroundState.AIR) {
            momentumY = Math.min(momentumY + momentumYIncrease, terminalVelocityY);
        }

        moveXHandleCollision(moveAmountX);

        boolean right = facingDirection == Direction.RIGHT;
        if (hurtTimer > 0) {
            currentAnimationName = right ? "HURT_RIGHT" : "HURT_LEFT";
        } else if (attackTimer > strikeFrames) {
            currentAnimationName = right ? "WINDUP_RIGHT" : "WINDUP_LEFT";
        } else if (attackTimer > 0) {
            currentAnimationName = right ? "STRIKE_RIGHT" : "STRIKE_LEFT";
        } else {
            currentAnimationName = right ? "WALK_RIGHT" : "WALK_LEFT";
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
                momentumY = 0;
                airGroundState = AirGroundState.GROUND;
            } else {
                airGroundState = AirGroundState.AIR;
            }
        }
    }

        @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);

        if (health >= maxHealth || health <= 0) {
            return;                      // only show it once he's been hit
        }

        int x = Math.round(getCalibratedXLocation()) + 35;
        int y = Math.round(getCalibratedYLocation());
        int width = 38;
        int height = 5;

        int fillWidth = Math.round(width * ((float) health / maxHealth));

        graphicsHandler.drawFilledRectangle(x, y, width, height, new Color(30, 30, 30));
        if (fillWidth > 0) {
            graphicsHandler.drawFilledRectangle(x, y, fillWidth, height, new Color(200, 45, 45));
        }
        graphicsHandler.drawRectangle(x, y, width, height, Color.black, 1);
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

            put("WINDUP_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(2, 0))
                .withScale(1).withBounds(35, 10, 38, 66).build()
            });

            put("WINDUP_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(2, 0))
                    .withScale(1).withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .withBounds(31, 10, 38, 66).build()
            });

            put("STRIKE_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(2, 1))
                    .withScale(1).withBounds(35, 10, 38, 66).build()
            });

            put("STRIKE_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(2, 1))
                    .withScale(1).withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .withBounds(31, 10, 38, 66).build()
            });

        }};
    }
}
