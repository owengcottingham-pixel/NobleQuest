package Screens;

import java.awt.Color;
import java.awt.image.BufferedImage;

import Engine.GraphicsHandler;
import Engine.Screen;
import Engine.ImageLoader;
import Game.GameState;
import Game.ScreenCoordinator;
import GameObject.SpriteSheet;
import Level.Map;
import Level.Player;
import Level.PlayerListener;
import Maps.Level1;
import Maps.Level2;
import Players.Knight;
import Utils.NumberDisplay;

public class PlayLevelScreen extends Screen implements PlayerListener {

    protected ScreenCoordinator screenCoordinator;
    protected Map map;
    protected Player player;
    protected PlayLevelScreenState playLevelScreenState;
    protected int screenTimer;
    protected LevelClearedScreen levelClearedScreen;
    protected LevelLoseScreen levelLoseScreen;
    protected boolean levelCompletedStateChangeStart;
    protected NumberDisplay coinDisplay;
    protected BufferedImage coinIcon;

    private static final int SCREEN_WIDTH = 800;
    private static final int LEVEL_TRANSITION_TIME = 120;

    private int currentLevel = 1;

    public PlayLevelScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        // Load the current level.
        if (currentLevel == 1) {
            map = new Level1();
        } else {
            map = new Level2();
        }

        // Set up the player at the current level's starting position.
        player = new Knight(
            map.getPlayerStartPosition().x,
            map.getPlayerStartPosition().y
        );

        player.setMap(map);
        player.addListener(this);

        // Set up the coin display.
        coinIcon = ImageLoader.load("Coin.png");

        coinDisplay = new NumberDisplay(
            new SpriteSheet(ImageLoader.load("Digits.png"), 8, 10),
            3
        );

        // Set up the level completion and death screens.
        levelClearedScreen = new LevelClearedScreen();
        levelLoseScreen = new LevelLoseScreen(this);

        // Start the level.
        playLevelScreenState = PlayLevelScreenState.RUNNING;
        levelCompletedStateChangeStart = false;
        screenTimer = 0;
    }

    @Override
    public void update() {
        switch (playLevelScreenState) {

            case RUNNING:
                player.update();
                map.update(player);
                break;

            case LEVEL_COMPLETED:
                if (levelCompletedStateChangeStart) {
                    screenTimer = LEVEL_TRANSITION_TIME;
                    levelCompletedStateChangeStart = false;
                } else {
                    levelClearedScreen.update();
                    screenTimer--;

                    if (screenTimer <= 0) {
                        if (currentLevel == 1) {
                            // Level 1 is complete. Start Level 2.
                            currentLevel = 2;
                            initialize();
                        } else {
                            // Level 2 is complete. Return to the menu.
                            goBackToMenu();
                        }
                    }
                }
                break;

            case LEVEL_LOSE:
                levelLoseScreen.update();
                break;
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        switch (playLevelScreenState) {

            case RUNNING:
                map.draw(graphicsHandler);
                player.draw(graphicsHandler);

                graphicsHandler.drawImage(
                    coinIcon,
                    SCREEN_WIDTH - 112,
                    14,
                    28,
                    28
                );

                coinDisplay.draw(
                    graphicsHandler,
                    player.getCoins(),
                    SCREEN_WIDTH - 78,
                    12
                );

                drawDashBar(graphicsHandler);
                drawHealthBar(graphicsHandler);
                break;

            case LEVEL_COMPLETED:
                levelClearedScreen.draw(graphicsHandler);
                break;

            case LEVEL_LOSE:
                levelLoseScreen.draw(graphicsHandler);
                break;
        }
    }

    public PlayLevelScreenState getPlayLevelScreenState() {
        return playLevelScreenState;
    }

    private void drawDashBar(GraphicsHandler graphicsHandler) {
        int x = 20;
        int y = 20;
        int width = 100;
        int height = 12;

        float fill = 1f;

        if (player.getDashCooldownTimer() > 0) {
            fill = 1f - (
                (float) player.getDashCooldownTimer()
                / player.getDashCooldown()
            );
        }

        fill = Math.max(0f, Math.min(1f, fill));

        int fillWidth = (int) (width * fill);

        graphicsHandler.drawFilledRectangle(
            x, y, width, height,
            new Color(40, 40, 40)
        );

        Color fillColor = fill >= 1f
            ? new Color(80, 220, 100)
            : new Color(230, 150, 50);

        if (fillWidth > 0) {
            graphicsHandler.drawFilledRectangle(
                x, y, fillWidth, height, fillColor
            );
        }

        graphicsHandler.drawRectangle(
            x, y, width, height, Color.white, 2
        );
    }

    private void drawHealthBar(GraphicsHandler graphicsHandler) {
        int x = 20;
        int y = 45;
        int width = 120;
        int height = 14;

        float fill = (float) player.getHealth()
            / player.getMaxHealth();

        fill = Math.max(0f, Math.min(1f, fill));

        int fillWidth = (int) (width * fill);

        graphicsHandler.drawFilledRectangle(
            x, y, width, height,
            new Color(40, 40, 40)
        );

        Color color = fill > 0.5f
            ? new Color(80, 200, 90)
            : fill > 0.25f
                ? new Color(230, 170, 40)
                : new Color(210, 50, 45);

        if (fillWidth > 0) {
            graphicsHandler.drawFilledRectangle(
                x, y, fillWidth, height, color
            );
        }

        graphicsHandler.drawRectangle(
            x, y, width, height, Color.white, 2
        );
    }

    @Override
    public void onLevelCompleted() {
        if (playLevelScreenState != PlayLevelScreenState.LEVEL_COMPLETED) {
            playLevelScreenState = PlayLevelScreenState.LEVEL_COMPLETED;
            levelCompletedStateChangeStart = true;
        }
    }

    @Override
    public void onDeath() {
        if (playLevelScreenState != PlayLevelScreenState.LEVEL_LOSE) {
            playLevelScreenState = PlayLevelScreenState.LEVEL_LOSE;
        }
    }

    public void resetLevel() {
        // Restart the current level, not always Level 1.
        initialize();
    }

    public void goBackToMenu() {
        screenCoordinator.setGameState(GameState.MENU);
    }

    private enum PlayLevelScreenState {
        RUNNING,
        LEVEL_COMPLETED,
        LEVEL_LOSE
    }
}
