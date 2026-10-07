package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Rectangle;
import Level.EnhancedMapTile;
import Level.MapEntityStatus;
import Level.Player;
import Level.TileType;
import Utils.AirGroundState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class DisappearingPlatform extends EnhancedMapTile {

    private static final int TOTAL_TIME = 120;
    private static final int DECAY_START = 60;

    private int standingTimer = 0;
    private State state = State.NORMAL;

    private enum State {
        NORMAL,
        DECAYING,
        GONE
    }

    public DisappearingPlatform(
        float x,
        float y,
        float scale,
        Rectangle bounds
    ) {
        super(
            x,
            y,
            new FrameBuilder(ImageLoader.load("GreenPlatform.png"))
                .withBounds(bounds)
                .withScale(scale)
                .build(),
            TileType.JUMP_THROUGH_PLATFORM
        );

        initialize();
        setIsUpdateOffScreen(true);
    }

    public DisappearingPlatform(float x, float y, float scale, Rectangle bounds, int lengthMultiplier) {
        super(
            x,
            y,
            new FrameBuilder(createRepeatedImage(ImageLoader.load("GreenPlatform.png"), lengthMultiplier))
                .withBounds(new Rectangle(bounds.getX1(), bounds.getY1(), bounds.getWidth() * lengthMultiplier, bounds.getHeight()))
                .withScale(scale)
                .build(),
            TileType.JUMP_THROUGH_PLATFORM
        );

        initialize();
        setIsUpdateOffScreen(true);
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
        if (state == State.GONE) {
            return;
        }

        boolean playerStandingOnPlatform =
            touching(player)
            && Math.abs(
                (player.getBounds().getY2() + 1)
                - getBounds().getY1()
            ) <= 2
            && player.getAirGroundState() == AirGroundState.GROUND;

        if (playerStandingOnPlatform) {
            standingTimer++;

            if (standingTimer >= DECAY_START) {
                state = State.DECAYING;
            }

            if (standingTimer >= TOTAL_TIME) {
                state = State.GONE;
                setMapEntityStatus(MapEntityStatus.REMOVED);
                return;
            }
        } else {
            standingTimer = 0;
            state = State.NORMAL;
        }

        super.update(player);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (state == State.GONE) {
            return;
        }

        if (state == State.NORMAL) {
            super.draw(graphicsHandler);
            return;
        }

        Graphics2D g = graphicsHandler.getGraphics();

        if (g == null) {
            super.draw(graphicsHandler);
            return;
        }

        float progress =
            Math.min(
                1.0f,
                (standingTimer - DECAY_START)
                / (float) (TOTAL_TIME - DECAY_START)
            );

        int shake;

        if (standingTimer % 4 < 2) {
            shake = -1;
        } else {
            shake = 1;
        }

        int drawX = Math.round(getX()) + shake;
        int drawY = Math.round(getY());

        java.awt.Composite oldComposite = g.getComposite();

        float alpha =
            Math.max(
                0.30f,
                0.70f - progress * 0.40f
            );

        g.setComposite(
            java.awt.AlphaComposite.getInstance(
                java.awt.AlphaComposite.SRC_OVER,
                alpha
            )
        );

        graphicsHandler.drawImage(
            currentFrame.getImage(),
            drawX,
            drawY,
            getWidth(),
            getHeight(),
            currentFrame.getImageEffect()
        );

        g.setComposite(oldComposite);

        Color oldColor = g.getColor();
        java.awt.Stroke oldStroke = g.getStroke();

        g.setColor(new Color(45, 25, 15));
        g.setStroke(new BasicStroke(2));

        int x1 =
            Math.round(
                getX() + getWidth() * 0.30f
            );

        int x2 =
            Math.round(
                getX() + getWidth() * 0.62f
            );

        int yTop =
            Math.round(
                getY() + getHeight() * 0.15f
            );

        int yMiddle =
            Math.round(
                getY() + getHeight() * 0.50f
            );

        int yBottom =
            Math.round(
                getY() + getHeight() * 0.85f
            );

        g.drawLine(
            x1,
            yTop,
            x1 - 5,
            yMiddle
        );

        g.drawLine(
            x1 - 5,
            yMiddle,
            x1 + 2,
            yBottom
        );

        g.drawLine(
            x2,
            yTop,
            x2 + 5,
            yMiddle
        );

        g.drawLine(
            x2 + 5,
            yMiddle,
            x2 - 1,
            yBottom
        );

        g.setColor(oldColor);
        g.setStroke(oldStroke);
    }
}