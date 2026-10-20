package EnhancedMapTiles;

import GameObject.Rectangle;
import Level.Player;
import Level.TileType;
import Utils.AirGroundState;
import Utils.Direction;
import Utils.Point;

import java.awt.image.BufferedImage;

public class HorizontalMovingBouncyPlatform extends HorizontalMovingPlatform {
    private static final float DEFAULT_BOUNCE_FORCE = 23f;

    private float bounceForce = DEFAULT_BOUNCE_FORCE;

    public HorizontalMovingBouncyPlatform(
        BufferedImage image,
        Point startLocation,
        Point endLocation,
        float scale,
        Rectangle bounds,
        Direction startDirection,
        int lengthMultiplier
    ) {
        super(
            image,
            startLocation,
            endLocation,
            TileType.JUMP_THROUGH_PLATFORM,
            scale,
            bounds,
            startDirection,
            lengthMultiplier
        );
    }

    public void setBounceForce(float bounceForce) {
        this.bounceForce = bounceForce;
    }

    @Override
    public void update(Player player) {
        super.update(player);

        boolean playerOnTop = touching(player)
            && Math.abs((player.getBounds().getY2() + 1) - getBounds().getY1()) <= 2
            && player.getAirGroundState() == AirGroundState.GROUND;

        if (playerOnTop) {
            player.bounce(bounceForce);
        }
    }
}
