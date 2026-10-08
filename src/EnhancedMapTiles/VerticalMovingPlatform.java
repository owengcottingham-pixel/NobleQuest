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
    private float startOffset = 0;
    private Direction startDirection;
    private Direction direction;

    public VerticalMovingPlatform(BufferedImage image, Point startLocation, Point endLocation, TileType tileType, float scale, Rectangle bounds, Direction startDirection) {
        super(startLocation.x, startLocation.y, new FrameBuilder(image).withBounds(bounds).withScale(scale).build(), tileType);
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.startDirection = startDirection;
        this.initialize();
        this.setIsUpdateOffScreen(true);
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

    public void setStartOffset(float tiles) {
        this.startOffset = tiles * 48;
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        direction = startDirection;
        moveY(startOffset);

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
            float snapAmount = playerTargetY2 - player.getBounds().getY2();

            if (moveAmountY > 0) {
                snapAmount += 1;
            }
            player.moveYHandleCollision(snapAmount);
        }

                // catch a falling player whose feet slipped just inside the platform this frame
        float playerBottom = player.getBounds().getY2();
        float platformTop = getBounds().getY1();
        boolean overlapsX = player.getBounds().getX2() > getBounds().getX1()
                && player.getBounds().getX1() < getBounds().getX2();

        if (!playerIsRiding && overlapsX
                && player.getLastAmountMovedY() >= 0      // falling, not jumping up through it
                && playerBottom >= platformTop - 1
                && playerBottom <= platformTop + 12) {    // only if just barely inside
            player.moveYHandleCollision((platformTop - 1) - playerBottom); // pop up onto the top
            player.moveYHandleCollision(1);                                // tap down so you "land"
        }

        super.update(player);
    }

    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
    }
}
