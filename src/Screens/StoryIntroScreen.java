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

    // ORIGINAL STORY IMAGES
    private static final String[] IMAGE_FILES = {
        "StoryIntro1.png",
        "StoryIntro2.png",
        "StoryIntro3.png",
        "StoryIntro4.png",
        "StoryIntro5.png"
    };

    // ORIGINAL STORY TEXT - EDIT HERE
    private static final String[] STORY_TEXT = {
        "All your life, you have been told tales of a place beyond the mountains.",
        "A village inhabited by elders who possess infinite wisdom.",
        "Many have attempted the journey, but none have returned.",
        "Even as a child, you had your doubts. But now you must know.",
        "And so, the journey begins... a Noble Quest."
    };

    // LEVEL 2 STORY IMAGES
    private static final String[] LEVEL_2_IMAGES = {
        "level2intro1.PNG",
        "level2intro2.PNG",
        "level2intro3.PNG"
    };

    // LEVEL 2 STORY TEXT - EDIT HERE
    private static final String[] LEVEL_2_TEXT = {
        "At long last, you have reached the caves in the mountains",
        "Legends tell of fearsome beasts within.",
        "Only one way to know for sure."
    };

    private BufferedImage[] images;
    private String[] activeStoryText;

    private int currentImage;
    private boolean eWasPressed;
    private boolean finished;
    private boolean level2Story;

    // Original opening story constructor
    public StoryIntroScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
        this.level2Story = false;
    }

    // Level 2 story constructor
    public StoryIntroScreen(
            ScreenCoordinator screenCoordinator,
            boolean level2Story) {

        this.screenCoordinator = screenCoordinator;
        this.level2Story = level2Story;
    }

    @Override
    public void initialize() {

        String[] imageFiles;

        if (level2Story) {
            imageFiles = LEVEL_2_IMAGES;
            activeStoryText = LEVEL_2_TEXT;
        } else {
            imageFiles = IMAGE_FILES;
            activeStoryText = STORY_TEXT;
        }

        images = new BufferedImage[imageFiles.length];

        for (int i = 0; i < imageFiles.length; i++) {
            images[i] = ImageLoader.load(imageFiles[i]);
        }

        currentImage = 0;
        eWasPressed = Keyboard.isKeyDown(Key.E);
        finished = false;
    }

    @Override
    public void update() {

        if (finished) {
            return;
        }

        boolean ePressed = Keyboard.isKeyDown(Key.E);

        if (ePressed && !eWasPressed) {

            if (currentImage < images.length - 1) {

                currentImage++;

            } else {

                finished = true;

                // Only the original story starts a new game.
                // The Level 2 story resumes existing gameplay.
                if (!level2Story) {
                    screenCoordinator.setGameState(
                        GameState.LEVEL
                    );
                }
            }
        }

        eWasPressed = ePressed;
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        int width = ScreenManager.getScreenWidth();
        int height = ScreenManager.getScreenHeight();

        graphicsHandler.drawFilledRectangle(
            0, 0, width, height, Color.BLACK
        );

        if (images[currentImage] != null) {

            graphicsHandler.drawImage(
                images[currentImage],
                0,
                0,
                width,
                height
            );
        }

        drawStoryText(
            graphicsHandler,
            activeStoryText[currentImage],
            width,
            height
        );

        drawNextPageText(
            graphicsHandler,
            width
        );
    }

    private void drawNextPageText(
            GraphicsHandler graphicsHandler,
            int width) {

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
            int height) {

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

        int maxTextWidth = width - 100;

        ArrayList<String> lines = new ArrayList<>();

        String[] words = text.split("\\s+");

        StringBuilder line = new StringBuilder();

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

        int textY = height - 40
            - totalHeight
            + metrics.getAscent();

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