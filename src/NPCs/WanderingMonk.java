
package NPCs;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.SpriteSheet;
import Level.NPC;
import Level.Player;
import Utils.Point;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class WanderingMonk extends NPC {

    private static final float SCALE = 1.5f;
    private static final int LIGHT_UP_TIME = 14 + 8 + 10 + 16;
    private static final int SMOKE_TIME = 60;

    private static String savedMap = null;
    private static float savedX, savedY;

    private final float homeX, homeY;

    private boolean lit = false;
    private boolean lightingUp = false;
    private boolean checkedSave = false;
    private int lightTimer = 0;

    // Transformation variables
    private boolean hasTalked = false;
    private boolean transforming = false;
    private boolean transformed = false;
    private int smokeTimer = 0;

    private BufferedImage smokeImage;
    private BufferedImage lanternImage;

    public WanderingMonk(Point location, String message) {

        super(
            location.x,
            location.y,
            new SpriteSheet(
                ImageLoader.load("WanderingMonk.png"),
                56,
                72
            ),
            "IDLE"
        );

        homeX = location.x;
        homeY = location.y;

        // Load transformation images
        smokeImage = ImageLoader.load("SmokePuff.png");
        lanternImage = ImageLoader.load("Lantern.png");

        isInteractable = true;
        talkedToTime = 400;

        textbox.setText(message);
        textbox.setFillColor(new Color(30, 24, 49, 225));
        textbox.setBorderColor(new Color(214, 170, 92));
        textbox.setBorderThickness(3);
        textbox.setTextColor(new Color(251, 230, 160));
        textbox.setFont(new Font("Serif", Font.BOLD, 16));
        textbox.setHPadding(14);
        textbox.setVPadding(10);
        textbox.setGap(4);

        textboxOffsetX = -90;
        textboxOffsetY = -105;
    }

    // Checkpoint methods
    public static boolean hasCheckpoint(String mapName) {
        return mapName.equals(savedMap);
    }

    public static Point getCheckpoint() {
        return new Point(savedX, savedY);
    }

    public static void clearCheckpoint() {
        savedMap = null;
    }

    private boolean isSavedHere() {
        return map.getClass().getSimpleName().equals(savedMap)
            && savedX == spawnX()
            && savedY == spawnY();
    }

    private float spawnX() {
        return homeX + 32 * SCALE - 56;
    }

    private float spawnY() {
        return homeY + 71 * SCALE - 82;
    }

    public boolean isLit() {
        return lit;
    }

    @Override
    public void update(Player player) {

        if (!checkedSave) {
            checkedSave = true;
            lit = isSavedHere();
        }

        // Lantern stays after transformation
        if (transformed) {
            return;
        }

        // Smoke countdown
        if (transforming) {

            smokeTimer--;

            if (smokeTimer <= 0) {
                transforming = false;
                transformed = true;
            }

            return;
        }

        // Normal NPC behavior
        super.update(player);

        // Remember when dialogue starts
        if (talkedTo) {
            hasTalked = true;
        }

        // When dialogue closes, transform
        if (hasTalked && !talkedTo) {

            hasTalked = false;
            transforming = true;
            smokeTimer = SMOKE_TIME;
            isInteractable = false;

            return;
        }

        // Checkpoint activation
        if (!lit && !lightingUp && intersects(player)) {

            lightingUp = true;
            lightTimer = LIGHT_UP_TIME;

            savedMap = map.getClass().getSimpleName();
            savedX = spawnX();
            savedY = spawnY();
        }

        if (lightingUp) {

            currentAnimationName = "LIGHT_UP";
            lightTimer--;

            if (lightTimer <= 0) {
                lightingUp = false;
                lit = true;
            }

        } else if (talkedTo) {

            currentAnimationName = lit ? "TALK_LIT" : "TALK";

        } else {

            currentAnimationName = lit ? "IDLE_LIT" : "IDLE";
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        Graphics2D g = graphicsHandler.getGraphics();

        // Use camera-calibrated coordinates
        int x = (int) getCalibratedXLocation();
        int y = (int) getCalibratedYLocation();

        // Draw lantern after transformation
        if (transformed) {

            if (lanternImage != null) {

                g.drawImage(
                    lanternImage,
                    x + 25,
                    y + 80,
                    (int) (20 * SCALE),
                    (int) (25 * SCALE),
                    null
                );
            }

            return;
        }

        // Draw smoke while transforming
        if (transforming) {

            if (smokeImage != null) {

                g.drawImage(
                    smokeImage,
                    x,
                    y,
                    (int) (56 * SCALE),
                    (int) (72 * SCALE),
                    null
                );
            }

            return;
        }

        // Draw monk normally
        super.draw(graphicsHandler);
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(
        SpriteSheet spriteSheet
    ) {

        HashMap<String, Frame[]> animations = new HashMap<>();

        animations.put(
            "IDLE",
            row(spriteSheet, 0, new int[] {14, 14, 14, 14})
        );

        animations.put(
            "TALK",
            row(spriteSheet, 1, new int[] {8, 8, 8, 8})
        );

        animations.put(
            "LIGHT_UP",
            row(spriteSheet, 2, new int[] {14, 8, 10, 0})
        );

        animations.put(
            "IDLE_LIT",
            row(spriteSheet, 3, new int[] {12, 12, 12, 12})
        );

        animations.put(
            "TALK_LIT",
            row(spriteSheet, 4, new int[] {8, 8, 8, 8})
        );

        return animations;
    }

    private Frame[] row(
        SpriteSheet sheet,
        int row,
        int[] delays
    ) {

        Frame[] frames = new Frame[delays.length];

        for (int i = 0; i < delays.length; i++) {

            frames[i] = new FrameBuilder(
                sheet.getSprite(row, i),
                delays[i]
            )
            .withScale(SCALE)
            .withBounds(18, 8, 28, 62)
            .build();
        }

        return frames;
    }
}
