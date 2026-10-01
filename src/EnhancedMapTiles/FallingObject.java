package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Rectangle;
import Level.EnhancedMapTile;
import Level.MapEntity;
import Level.MapEntityStatus;
import Level.Player;
import Level.TileType;
import Utils.Direction;

public class FallingObject extends EnhancedMapTile {

    private float velocityY = 0;

    private final float gravity = 0.35f;
    private final float terminalVelocity = 8.0f;

    private enum State {
        WAITING,
        FALLING,
        DISAPPEARING
    }

    private State state = State.WAITING;

    public FallingObject(float x, float y) {
        super(
            x,
            y,
            new FrameBuilder(ImageLoader.load("GoldBox.png"))
                .withBounds(new Rectangle(1, 1, 14, 14))
                .withScale(3)
                .build(),
            TileType.NOT_PASSABLE
        );

        initialize();

        setIsUpdateOffScreen(true);
    }

    @Override
    public void update(Player player) {

        // Once the object disappears, stop updating it.
        if (state == State.DISAPPEARING) {
            return;
        }

        /*
         * WAITING:
         * Wait until the player walks underneath the object.
         */
        if (state == State.WAITING) {

            boolean horizontallyUnder =
                player.getBounds().getX2() >= getBounds().getX1() &&
                player.getBounds().getX1() <= getBounds().getX2();

            boolean playerIsBelow =
                player.getBounds().getY1() >= getBounds().getY2();

            if (horizontallyUnder && playerIsBelow) {
                state = State.FALLING;
            }
        }

        /*
         * FALLING:
         * Apply gravity and move the object downward.
         */
        if (state == State.FALLING) {

            // Apply gravity.
            velocityY += gravity;

            // Prevent the object from falling too quickly.
            if (velocityY > terminalVelocity) {
                velocityY = terminalVelocity;
            }

            // Move the object downward FIRST.
            moveYHandleCollision(velocityY);

            /*
             * Check for the player AFTER moving.
             *
             * This is important because checking before movement
             * can cause the object to miss the player.
             */
            if (getBounds().intersects(player)) {

                // Kill the player using the game's existing
                // player damage/death system.
                player.hurtPlayer(this);

                // Remove the object's collision/hitbox.
                state = State.DISAPPEARING;
                setMapEntityStatus(MapEntityStatus.REMOVED);

                return;
            }
        }

        super.update(player);
    }

    @Override
    public void onEndCollisionCheckY(
        boolean hasCollided,
        Direction direction,
        MapEntity entityCollidedWith) {

        /*
         * If the falling object hits the ground,
         * remove it from the map.
         */
        if (
            state == State.FALLING &&
            hasCollided &&
            direction == Direction.DOWN
        ) {

            velocityY = 0;

            state = State.DISAPPEARING;

            // Completely remove the object's collision/hitbox.
            setMapEntityStatus(MapEntityStatus.REMOVED);
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        // Do not draw the object after it disappears.
        if (state == State.DISAPPEARING) {
            return;
        }

        super.draw(graphicsHandler);
    }
}