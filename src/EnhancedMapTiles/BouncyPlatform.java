package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import GameObject.Rectangle;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.AirGroundState;
import Utils.Point;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class BouncyPlatform extends EnhancedMapTile {
    private float bounceForce = 23f;

    public BouncyPlatform(BufferedImage image, Point location, float scale, Rectangle bounds) {
        super(location.x, location.y, new FrameBuilder(image).withBounds(bounds).withScale(scale).build(), TileType.JUMP_THROUGH_PLATFORM);
    }

    public void setBounceForce(float bounceForce) {
        this.bounceForce = bounceForce;
    }

    public BouncyPlatform(BufferedImage image, Point location, float scale, Rectangle bounds, int lengthMultiplier) {
    this(createRepeatedImage(image, lengthMultiplier), location, scale,
            new Rectangle(bounds.getX1(), bounds.getY1(), bounds.getWidth() * lengthMultiplier, bounds.getHeight()));
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
    public void update(Player player) {
        boolean playerOnTop = touching(player)
                && Math.abs((player.getBounds().getY2() + 1) - getBounds().getY1()) <= 2
                && player.getAirGroundState() == AirGroundState.GROUND;

        if (playerOnTop) {
            player.bounce(bounceForce);
        }

        super.update(player);
    }

    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
    }
}