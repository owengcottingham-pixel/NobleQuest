package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import GameObject.Rectangle;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.AirGroundState;
import Utils.Point;
import java.util.ArrayList;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class SuperBouncyPlatform extends EnhancedMapTile {
    private float bounceForce = 27f;
    private ArrayList<SuperBouncyPlatform> chutePlatforms = new ArrayList<>();
    private boolean chuteSpawned = false;
    private boolean boostWhilePassing = false;

    public SuperBouncyPlatform(BufferedImage image, Point location, float scale, Rectangle bounds) {
        super(location.x, location.y, new FrameBuilder(image).withBounds(bounds).withScale(scale).build(), TileType.JUMP_THROUGH_PLATFORM);
    }

    public void setBounceForce(float bounceForce) {
        this.bounceForce = bounceForce;
    }

    public SuperBouncyPlatform(BufferedImage image, Point location, float scale, Rectangle bounds, int lengthMultiplier) {
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

    public void addChutePlatform(SuperBouncyPlatform platform) {
        platform.boostWhilePassing = true;
        chutePlatforms.add(platform);
    }

    @Override
    public void update(Player player) {
        boolean playerOnTop = touching(player)
                && Math.abs((player.getBounds().getY2() + 1) - getBounds().getY1()) <= 2
                && player.getAirGroundState() == AirGroundState.GROUND;

        boolean flyingThrough = boostWhilePassing && intersects(player);

        if (playerOnTop || flyingThrough) {
            player.bounce(bounceForce);

            if (!chuteSpawned) {
                for (SuperBouncyPlatform platform : chutePlatforms) {
                map.addEnhancedMapTile(platform);
            }
            chuteSpawned = true;
        }
}

        super.update(player);
    }

    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
    }
}
