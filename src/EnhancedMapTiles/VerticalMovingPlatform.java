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

public class VerticalMovingPlatform extends EnhancedMapTile {
    private Point startLocation;
    private Point endLocation;
    private float movementSpeed = 2f;
    private Direction startDirection;
    private Direction direction;

    public VerticalMovingPlatform(BufferedImage image, Point startLocation, Point endLocation, TileType tileType, float scale, Rectangle bounds, Direction startDirection) {
        super(startLocation.x, startLocation.y, new FrameBuilder(image).withBounds(bounds).withScale(scale).build(), tileType);
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.startDirection = startDirection;
        this.initialize();
    }

    public VerticalMovingPlatform(BufferedImage image, Point startLocation, Point endLocation, TileType tileType, float scale, Rectangle bounds, Direction startDirection, int lengthMultiplier) {
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
        float topBound = startLocation.y;
        float bottomBound = endLocation.y;

        boolean playerIsRiding = touching(player) && Math.abs((player.getBounds().getY2() + 1) - getBounds().getY1()) <= 2 && player.getAirGroundState() == AirGroundState.GROUND;


        // move platform left or right based on its current direction
        float moveAmountY = 0;
        if (direction == Direction.DOWN) {
            moveAmountY += movementSpeed;
        } else if (direction == Direction.UP) {
            moveAmountY -= movementSpeed;
        }

        moveY(moveAmountY);

        // if platform reaches the start or end location, it turns around
        // platform may end up going a bit past the start or end location depending on movement speed
        // this calculates the difference and pushes the platform back a bit so it ends up right on the start or end location
        if (getY1() >= bottomBound) {
            moveY(bottomBound - getY1());
            direction = Direction.UP;
        } else if (getY1() <= topBound) {
            moveY(topBound - getY1());
            direction = Direction.DOWN;
        }

        if (playerIsRiding) {
            float playerTargetY2 = getBounds().getY1() - 1;
            player.moveYHandleCollision(playerTargetY2 - player.getBounds().getY2());
        }

        super.update(player);
    }

    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
    }
}
