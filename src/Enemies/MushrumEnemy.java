package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
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

public class MushrumEnemy extends Enemy {
    private static final float GRAVITY = 0.5f;
    private static final float MAX_FALL_SPEED = 6f;
    private static final float WALK_SPEED = 0.75f;
    private static final float STOMP_BOUNCE_FORCE = 12f;
    private static final int CONTACT_DAMAGE = 1;
    private static final int SMUSH_ANIMATION_FRAMES = 12;
    private static final int RESPAWN_FRAMES = 120;
    private static final int SPRITE_SIZE = 127;
    private static final int SPRITE_SCALE = 1;
    private static final int BOUNDS_X = 28;
    private static final int BOUNDS_Y = 59;
    private static final int BOUNDS_WIDTH = 72;
    private static final int BOUNDS_HEIGHT = 45;
    private static final int SMUSHED_DRAW_OFFSET_Y = 9;

    private final Point startLocation;
    private final Point endLocation;
    private final Direction startDirection;

    private Direction direction;
    private AirGroundState airGroundState = AirGroundState.AIR;
    private float verticalMomentum;
    private State state = State.WALKING;
    private int smushTimer;

    private enum State {
        WALKING,
        SMUSHED
    }

    public MushrumEnemy(Point startLocation, Point endLocation, Direction direction) {
        super(
            startLocation.x,
            startLocation.y,
            new SpriteSheet(ImageLoader.load("mushrumEnemy.png"), SPRITE_SIZE, SPRITE_SIZE),
            "WALK_RIGHT"
        );
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.startDirection = direction;
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        direction = startDirection;
        state = State.WALKING;
        airGroundState = AirGroundState.AIR;
        verticalMomentum = 0;
        smushTimer = 0;
        setWalkingAnimation();
    }

    @Override
    public void update(Player player) {
        if (state == State.SMUSHED) {
            updateSmushedState();
            super.update(player);
            return;
        }

        if (isPlayerStomping(player)) {
            state = State.SMUSHED;
            smushTimer = RESPAWN_FRAMES;
            setCurrentAnimationName(direction == Direction.RIGHT ? "SMUSH_RIGHT" : "SMUSH_LEFT");
            player.bounce(STOMP_BOUNCE_FORCE);
            super.update(player);
            return;
        }

        moveAlongPatrolRoute();
        moveWithGravity();
        setWalkingAnimation();
        super.update(player);
    }

    private void updateSmushedState() {
        smushTimer--;
        if (smushTimer == RESPAWN_FRAMES - SMUSH_ANIMATION_FRAMES) {
            setCurrentAnimationName(direction == Direction.RIGHT ? "FLAT_RIGHT" : "FLAT_LEFT");
        }
        if (smushTimer <= 0) {
            state = State.WALKING;
            setWalkingAnimation();
        }
    }

    private boolean isPlayerStomping(Player player) {
        return player.getAirGroundState() == AirGroundState.AIR
            && player.getLastAmountMovedY() > 0
            && intersects(player)
            && player.getBounds().getY2() <= getBounds().getY1() + 14;
    }

    private void moveAlongPatrolRoute() {
        float leftBound = Math.min(startLocation.x, endLocation.x);
        float rightBound = Math.max(startLocation.x, endLocation.x);
        float moveAmountX = direction == Direction.RIGHT ? WALK_SPEED : -WALK_SPEED;
        float nextX = getX() + moveAmountX;

        if (direction == Direction.RIGHT && nextX >= rightBound) {
            moveAmountX = rightBound - getX();
            direction = Direction.LEFT;
        } else if (direction == Direction.LEFT && nextX <= leftBound) {
            moveAmountX = leftBound - getX();
            direction = Direction.RIGHT;
        }

        moveXHandleCollision(moveAmountX);
    }

    private void moveWithGravity() {
        moveYHandleCollision(airGroundState == AirGroundState.GROUND ? 0 : GRAVITY + verticalMomentum);
        if (airGroundState == AirGroundState.AIR) {
            verticalMomentum = Math.min(verticalMomentum + GRAVITY, MAX_FALL_SPEED);
        }
    }

    private void setWalkingAnimation() {
        setCurrentAnimationName(direction == Direction.RIGHT ? "WALK_RIGHT" : "WALK_LEFT");
    }

    @Override
    public void touchedPlayer(Player player) {
        if (state == State.WALKING) {
            player.damage(CONTACT_DAMAGE);
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (state == State.SMUSHED) {
            graphicsHandler.drawImage(
                currentFrame.getImage(),
                Math.round(getCalibratedXLocation()),
                Math.round(getCalibratedYLocation()) + SMUSHED_DRAW_OFFSET_Y,
                getWidth(),
                getHeight(),
                currentFrame.getImageEffect()
            );
            return;
        }

        super.draw(graphicsHandler);
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction collisionDirection, MapEntity entityCollidedWith) {
        if (hasCollided) {
            direction = collisionDirection == Direction.RIGHT ? Direction.LEFT : Direction.RIGHT;
            setWalkingAnimation();
        }
    }

    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction collisionDirection, MapEntity entityCollidedWith) {
        if (collisionDirection == Direction.DOWN) {
            if (hasCollided) {
                verticalMomentum = 0;
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
                buildFrame(spriteSheet, 0, 0, false, 12),
                buildFrame(spriteSheet, 0, 1, false, 12)
            });
            put("WALK_LEFT", new Frame[] {
                buildFrame(spriteSheet, 0, 0, true, 12),
                buildFrame(spriteSheet, 0, 1, true, 12)
            });
            put("SMUSH_RIGHT", new Frame[] {
                buildFrame(spriteSheet, 2, 0, false, 6),
                buildFrame(spriteSheet, 2, 1, false, 6)
            });
            put("SMUSH_LEFT", new Frame[] {
                buildFrame(spriteSheet, 2, 0, true, 6),
                buildFrame(spriteSheet, 2, 1, true, 6)
            });
            put("FLAT_RIGHT", new Frame[] {
                buildFrame(spriteSheet, 2, 1, false, 0)
            });
            put("FLAT_LEFT", new Frame[] {
                buildFrame(spriteSheet, 2, 1, true, 0)
            });
        }};
    }

    private static Frame buildFrame(SpriteSheet spriteSheet, int row, int column, boolean flip, int delay) {
        FrameBuilder frameBuilder = new FrameBuilder(spriteSheet.getSprite(row, column), delay)
            .withScale(SPRITE_SCALE)
            .withBounds(BOUNDS_X, BOUNDS_Y, BOUNDS_WIDTH, BOUNDS_HEIGHT);
        if (flip) {
            frameBuilder.withImageEffect(ImageEffect.FLIP_HORIZONTAL);
        }
        return frameBuilder.build();
    }
}
