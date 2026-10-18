package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import GameObject.Rectangle;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.AirGroundState;
import Utils.Direction;
import Utils.Point;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// This class is for a horizontal moving platform
// the platform will move back and forth between its start location and end location
// if the player is standing on top of it, the player will be moved the same amount as the platform is moving (so the platform will not slide out from under the player)
public class HorizontalMovingPlatform extends EnhancedMapTile {
    private Point startLocation;
    private Point endLocation;
    private float movementSpeed = 2f;
    private Direction startDirection;
    private Direction direction;

    public HorizontalMovingPlatform(BufferedImage image, Point startLocation, Point endLocation, TileType tileType, float scale, Rectangle bounds, Direction startDirection) {
        super(startLocation.x, startLocation.y, new FrameBuilder(image).withBounds(bounds).withScale(scale).build(), tileType);
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.startDirection = startDirection;
        this.initialize();
        this.setIsUpdateOffScreen(true);
    }

    public HorizontalMovingPlatform(BufferedImage image, Point startLocation, Point endLocation, TileType tileType, float scale, Rectangle bounds, Direction startDirection, int lengthMultiplier) {
        this(createRepeatedImage(image, lengthMultiplier), startLocation, endLocation, tileType, scale,
                new Rectangle(bounds.getX1(), bounds.getY1(), bounds.getWidth() * lengthMultiplier, bounds.getHeight()), startDirection);
    }

    private static BufferedImage createRepeatedImage(BufferedImage image, int lengthMultiplier) {
        if (lengthMultiplier < 1) {
            throw new IllegalArgumentException("lengthMultiplier must be at least 1");
        }

        BufferedImage repeatedImage = new BufferedImage(image.getWidth() * lengthMultiplier, image.getHeight(), image.getType());
        Graphics2D graphics = repeatedImage.createGraphics();
        for (int index = 0; index < lengthMultiplier; index++) {
            graphics.drawImage(image, index * image.getWidth(), 0, null);
        }
        graphics.dispose();
        return repeatedImage;
    }

    @Override
    public void initialize() {
        super.initialize();
        direction = startDirection;
    }

    @Override
    public void update(Player player) {
        float leftBound = Math.min(startLocation.x, endLocation.x);
        float rightBound = Math.max(startLocation.x, endLocation.x);

        // move platform left or right based on its current direction
        float moveAmountX = direction == Direction.RIGHT ? movementSpeed : -movementSpeed;
        float nextX = getX1() + moveAmountX;

        // Treat both locations as the platform's left edge, regardless of their order.
        if (direction == Direction.RIGHT && nextX >= rightBound) {
            moveAmountX = rightBound - getX1();
            direction = Direction.LEFT;
        } else if (direction == Direction.LEFT && nextX <= leftBound) {
            moveAmountX = leftBound - getX1();
            direction = Direction.RIGHT;
        }
        moveX(moveAmountX);

        // if tile type is NOT PASSABLE, if the platform is moving and hits into the player (x axis), it will push the player
        if (tileType == TileType.NOT_PASSABLE) {
            if (intersects(player) && moveAmountX >= 0 && player.getBounds().getX1() <= getBounds().getX2()) {
                player.moveXHandleCollision(getBounds().getX2() - player.getBounds().getX1());
            } else if (intersects(player) && moveAmountX <= 0 && player.getBounds().getX2() >= getBounds().getX1()) {
                player.moveXHandleCollision(getBounds().getX1() - player.getBounds().getX2());
            }
        }

        // if player is on standing on top of platform, move player by the amount the platform is moving
        // this will cause the player to "ride" with the moving platform
        // without this code, the platform would slide right out from under the player
        if (touching(player) && (player.getBounds().getY2() + 1) == getBounds().getY1() && player.getAirGroundState() == AirGroundState.GROUND) {
            player.moveXHandleCollision(moveAmountX);
        }

        super.update(player);
    }

    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
    }

}
