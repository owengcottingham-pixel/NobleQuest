package Enemies;

import Level.MapEntity;
import Level.Player;
import Utils.Direction;
import Utils.Point;

public class VerticalStoneEnemy extends StoneEnemy {
    private static final float VERTICAL_SPEED = 1f;

    private final float upperBoundY;
    private final float lowerBoundY;
    private float verticalDirection = -1f;

    public VerticalStoneEnemy(Point location, Direction facingDirection, float upperBoundY, float lowerBoundY) {
        super(location, facingDirection);
        this.upperBoundY = upperBoundY;
        this.lowerBoundY = lowerBoundY;
        this.movementSpeed = 0;
    }

    @Override
    protected boolean shouldFacePlayerForMelee() {
        return false;
    }

    @Override
    public void update(Player player) {
        super.update(player);

        float nextY = getY() + verticalDirection * VERTICAL_SPEED;
        if (nextY <= upperBoundY) {
            nextY = upperBoundY;
            verticalDirection = 1f;
        } else if (nextY >= lowerBoundY) {
            nextY = lowerBoundY;
            verticalDirection = -1f;
        }
        setY(nextY);
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
    }
}
