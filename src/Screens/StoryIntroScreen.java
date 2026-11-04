
package Screens;

import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.Screen;
import Engine.ScreenManager;
import Game.GameState;
import Game.ScreenCoordinator;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class StoryIntroScreen extends Screen {

    private final ScreenCoordinator screenCoordinator;

    private BufferedImage[] images;

    private int currentImage;
    private long imageStartTime;

    private static final int IMAGE_COUNT = 5;
    private static final long IMAGE_TIME = 10000;

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
        imageStartTime = System.nanoTime();
    }

    @Override
    public void update() {

        long elapsed =
            (System.nanoTime() - imageStartTime) / 1000000L;

        if (elapsed >= IMAGE_TIME) {

            currentImage++;

            imageStartTime = System.nanoTime();

            if (currentImage >= IMAGE_COUNT) {
                screenCoordinator.setGameState(GameState.LEVEL);
            }
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        int width = ScreenManager.getScreenWidth();
        int height = ScreenManager.getScreenHeight();

        graphicsHandler.drawFilledRectangle(
            0,
            0,
            width,
            height,
            Color.BLACK
        );

        if (currentImage >= 0 && currentImage < IMAGE_COUNT) {

            BufferedImage image = images[currentImage];

            if (image != null) {

                graphicsHandler.drawImage(
                    image,
                    0,
                    0,
                    width,
                    height
                );
            }
        }
    }
}
