
package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class StoryIntroScreen extends Screen {

    private final ScreenCoordinator screenCoordinator;

    private static final int IMAGE_COUNT = 5;

    // ==========================================
    // STORY TEXT - EDIT HERE
    // ==========================================
    private static final String[] STORY_TEXT = {
        "All your life, you have been told tales of a place beyond the mountains.",
        "A village inhabited by elders who possess infinite wisdom.",
        "Many have attempted the journey, but none have returned.",
        "Even as a child, you had your doubts. But now you must know.",
        "And so, the journey begins... a Noble Quest."
    };

    private BufferedImage[] images;
    private int currentImage;

    // Prevent holding E from skipping pages
    private boolean eWasPressed;

    public StoryIntroScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {

        images = new BufferedImage[IMAGE_COUNT];

        images[0] = ImageLoader.load("StoryIntro1.png");
        images[1] = ImageLoader.load("StoryIntro2.png");
        images[2] = ImageLoader.load("StoryIntro3.png");
        images[3] = ImageLoader.load("StoryIntro4.png");
        images[4] = ImageLoader.load("StoryIntro5.png");

        currentImage = 0;
        eWasPressed = false;
    }

    @Override
    public void update() {

        // Detect E key
        boolean ePressed = Keyboard.isKeyDown(Key.E);

        // Only advance on a new press
        if (ePressed && !eWasPressed) {

            currentImage++;

            // After image 5, start Level 1
            if (currentImage >= IMAGE_COUNT) {
                screenCoordinator.setGameState(GameState.LEVEL);
                return;
            }
        }

        eWasPressed = ePressed;
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        int width = ScreenManager.getScreenWidth();
        int height = ScreenManager.getScreenHeight();

        // Draw black background
        graphicsHandler.drawFilledRectangle(
            0, 0, width, height, Color.BLACK
        );

        // Draw current story image
        if (images[currentImage] != null) {
            graphicsHandler.drawImage(
                images[currentImage],
                0,
                0,
                width,
                height
            );
        }

        // Draw story text at bottom
        drawStoryText(
            graphicsHandler,
            STORY_TEXT[currentImage],
            width,
            height
        );

        // Draw E - NEXT PAGE at top
        drawNextPageText(
            graphicsHandler,
            width
        );
    }

    private void drawNextPageText(
        GraphicsHandler graphicsHandler,
        int width
    ) {

        Font font = new Font(
            "Arial",
            Font.BOLD,
            22
        );

        String message = "E - NEXT PAGE";

        FontMetrics metrics = getFontMetrics(font);

        int textX =
            (width - metrics.stringWidth(message)) / 2;

        graphicsHandler.drawString(
            message,
            textX,
            40,
            font,
            Color.WHITE
        );
    }

    private void drawStoryText(
        GraphicsHandler graphicsHandler,
        String text,
        int width,
        int height
    ) {

        // Story text font
        int fontSize = Math.max(
            16,
            (int) (height * 0.04)
        );

        Font font = new Font(
            "Serif",
            Font.BOLD,
            fontSize
        );

        FontMetrics metrics = getFontMetrics(font);

        // Maximum text width
        int maxTextWidth = width - 100;

        ArrayList<String> lines = new ArrayList<>();

        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();

        // Automatically wrap long sentences
        for (String word : words) {

            String testLine = line.length() == 0
                ? word
                : line.toString() + " " + word;

            if (metrics.stringWidth(testLine) > maxTextWidth
                    && line.length() > 0) {

                lines.add(line.toString());
                line = new StringBuilder(word);

            } else {
                line = new StringBuilder(testLine);
            }
        }

        if (line.length() > 0) {
            lines.add(line.toString());
        }

        int lineHeight = metrics.getHeight();
        int totalHeight = lines.size() * lineHeight;

        // Story text position near bottom
        // Smaller 40 = lower text
        int textY = height - 40
            - totalHeight
            + metrics.getAscent();

        // Draw centered text
        for (String storyLine : lines) {

            int textX =
                (width - metrics.stringWidth(storyLine)) / 2;

            graphicsHandler.drawString(
                storyLine,
                textX,
                textY,
                font,
                Color.WHITE
            );

            textY += lineHeight;
        }
    }

    // Helper method for measuring text width
    private FontMetrics getFontMetrics(Font font) {

        BufferedImage tempImage = new BufferedImage(
            1,
            1,
            BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g = tempImage.createGraphics();

        g.setFont(font);

        FontMetrics metrics = g.getFontMetrics();

        g.dispose();

        return metrics;
    }
}
