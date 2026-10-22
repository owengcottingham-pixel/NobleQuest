
package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import Maps.TitleScreenMap;
import SpriteFont.SpriteFont;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class CreditsScreen extends Screen {

    protected ScreenCoordinator screenCoordinator;

    protected int currentMenuItemHovered = 0;
    protected int menuItemSelected = -1;

    protected SpriteFont playGame;
    protected SpriteFont settings;
    protected SpriteFont credits;

    protected Map background;

    // Logo
    protected BufferedImage logo;

    protected int logoX = 400;
    protected int logoY = 50;
    protected int logoWidth = 500;
    protected int logoHeight = 500;

    // Menu controls
    protected int keyPressTimer = 0;
    protected int pointerLocationX = 170;
    protected int pointerLocationY = 130;

    protected KeyLocker keyLocker = new KeyLocker();

    // Menu colors
    protected Color normalColor = new Color(49, 207, 240);
    protected Color selectedColor = new Color(255, 215, 0);

    public CreditsScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {

        // Load logo
        logo = ImageLoader.load("logo.png");

        // PLAY GAME
        playGame = new SpriteFont(
            "PLAY GAME",
            200,
            123,
            "Arial",
            30,
            normalColor
        );

        playGame.setOutlineColor(Color.black);
        playGame.setOutlineThickness(3);

        // SETTINGS
        settings = new SpriteFont(
            "SETTINGS",
            200,
            223,
            "Arial",
            30,
            normalColor
        );

        settings.setOutlineColor(Color.black);
        settings.setOutlineThickness(3);

        // CREDITS
        credits = new SpriteFont(
            "CREDITS",
            200,
            323,
            "Arial",
            30,
            normalColor
        );

        credits.setOutlineColor(Color.black);
        credits.setOutlineThickness(3);

        // Background
        background = new TitleScreenMap();
        background.setAdjustCamera(false);

        // Reset selection
        currentMenuItemHovered = 0;
        menuItemSelected = -1;
        keyPressTimer = 0;

        pointerLocationX = 170;
        pointerLocationY = 130;

        // Prevent accidental selection
        keyLocker.lockKey(Key.SPACE);
    }

    @Override
    public void update() {

        // Update background
        background.update(null);

        // Move selection down
        if (Keyboard.isKeyDown(Key.DOWN)
                && keyPressTimer == 0) {

            currentMenuItemHovered++;
            keyPressTimer = 14;

        }

        // Move selection up
        else if (Keyboard.isKeyDown(Key.UP)
                && keyPressTimer == 0) {

            currentMenuItemHovered--;
            keyPressTimer = 14;

        }

        // Countdown
        else if (keyPressTimer > 0) {
            keyPressTimer--;
        }

        // Wrap selection
        if (currentMenuItemHovered > 2) {
            currentMenuItemHovered = 0;
        }

        if (currentMenuItemHovered < 0) {
            currentMenuItemHovered = 2;
        }

        // Reset colors
        playGame.setColor(normalColor);
        settings.setColor(normalColor);
        credits.setColor(normalColor);

        // PLAY GAME selected
        if (currentMenuItemHovered == 0) {

            playGame.setColor(selectedColor);

            pointerLocationX = 170;
            pointerLocationY = 130;

        }

        // SETTINGS selected
        else if (currentMenuItemHovered == 1) {

            settings.setColor(selectedColor);

            pointerLocationX = 170;
            pointerLocationY = 230;

        }

        // CREDITS selected
        else if (currentMenuItemHovered == 2) {

            credits.setColor(selectedColor);

            pointerLocationX = 170;
            pointerLocationY = 330;

        }

        // Unlock SPACE after release
        if (Keyboard.isKeyUp(Key.SPACE)) {
            keyLocker.unlockKey(Key.SPACE);
        }

        // Handle selection
        if (!keyLocker.isKeyLocked(Key.SPACE)
                && Keyboard.isKeyDown(Key.SPACE)) {

            keyLocker.lockKey(Key.SPACE);

            menuItemSelected = currentMenuItemHovered;

            // PLAY GAME
            if (menuItemSelected == 0) {

                screenCoordinator.setGameState(
                    GameState.LEVEL
                );

            }

            // SETTINGS
            else if (menuItemSelected == 1) {

                // Settings not implemented yet

            }

            // CREDITS
            else if (menuItemSelected == 2) {

                screenCoordinator.setGameState(
                    GameState.CREDITS
                );

            }
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        // Draw background
        background.draw(graphicsHandler);

        // Draw logo
        if (logo != null) {

            graphicsHandler.drawImage(
                logo,
                logoX,
                logoY,
                logoWidth,
                logoHeight
            );

        }

        // Draw menu text
        playGame.draw(graphicsHandler);
        settings.draw(graphicsHandler);
        credits.draw(graphicsHandler);

        // Draw selection square
        graphicsHandler.drawFilledRectangleWithBorder(
            pointerLocationX,
            pointerLocationY,
            20,
            20,
            normalColor,
            Color.black,
            2
        );
    }
}
