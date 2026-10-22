
package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import SpriteFont.SpriteFont;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;

public class MenuScreen extends Screen {

    protected ScreenCoordinator screenCoordinator;

    protected int currentMenuItemHovered = 0;

    protected SpriteFont playGame;
    protected SpriteFont settings;
    protected SpriteFont credits;

    // Images
    protected BufferedImage logo;
    protected BufferedImage menuButton;
    protected BufferedImage backgroundImage;

    // Screen dimensions
    protected int screenWidth = 800;
    protected int screenHeight = 600;

    // Logo dimensions and position
    protected int logoWidth = 450;
    protected int logoHeight = 450;
    protected int logoX;
    protected int logoY = -50;

    // Button dimensions
    protected int buttonWidth = 250;
    protected int buttonHeight = 150;
    protected int buttonX;

    // Button positions
    protected int playButtonY = 275;
    protected int settingsButtonY = 375;
    protected int creditsButtonY = 475;

    // Text settings
    protected int fontSize = 30;

    // Keyboard controls
    protected int keyPressTimer = 0;
    protected KeyLocker keyLocker = new KeyLocker();

    // Colors
    protected Color normalColor = Color.WHITE;
    protected Color selectedColor = new Color(255, 215, 0);

    public MenuScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {

        // Load images
        logo = ImageLoader.load("logo.png");
        menuButton = ImageLoader.load("Menubutton.PNG");
        backgroundImage = ImageLoader.load("Background.png");

        // Center logo and button images
        logoX = (screenWidth - logoWidth) / 2;
        buttonX = (screenWidth - buttonWidth) / 2;

        // Measure text to center it
        Font menuFont = new Font(
            "Arial",
            Font.PLAIN,
            fontSize
        );

        FontMetrics metrics =
            new Canvas().getFontMetrics(menuFont);

        int playX = (screenWidth -
            metrics.stringWidth("PLAY GAME")) / 2;

        int settingsX = (screenWidth -
            metrics.stringWidth("SETTINGS")) / 2;

        int creditsX = (screenWidth -
            metrics.stringWidth("CREDITS")) / 2;

        // Calculate vertical text position
        int textOffset =
            (buttonHeight - metrics.getHeight()) / 2;

        // PLAY GAME
        playGame = new SpriteFont(
            "PLAY GAME",
            playX,
            playButtonY + textOffset,
            "Arial",
            fontSize,
            normalColor
        );

        playGame.setOutlineColor(Color.BLACK);
        playGame.setOutlineThickness(3);

        // SETTINGS
        settings = new SpriteFont(
            "SETTINGS",
            settingsX,
            settingsButtonY + textOffset,
            "Arial",
            fontSize,
            normalColor
        );

        settings.setOutlineColor(Color.BLACK);
        settings.setOutlineThickness(3);

        // CREDITS
        credits = new SpriteFont(
            "CREDITS",
            creditsX,
            creditsButtonY + textOffset,
            "Arial",
            fontSize,
            normalColor
        );

        credits.setOutlineColor(Color.BLACK);
        credits.setOutlineThickness(3);

        // Reset menu selection
        currentMenuItemHovered = 0;
        keyPressTimer = 0;

        keyLocker.lockKey(Key.SPACE);
    }

    @Override
    public void update() {

        // Move selection down
        if (Keyboard.isKeyDown(Key.DOWN)
                && keyPressTimer == 0) {

            currentMenuItemHovered++;
            keyPressTimer = 14;

        } else if (Keyboard.isKeyDown(Key.UP)
                && keyPressTimer == 0) {

            currentMenuItemHovered--;
            keyPressTimer = 14;

        } else if (keyPressTimer > 0) {
            keyPressTimer--;
        }

        // Loop through menu options
        if (currentMenuItemHovered > 2) {
            currentMenuItemHovered = 0;
        } else if (currentMenuItemHovered < 0) {
            currentMenuItemHovered = 2;
        }

        // Reset text colors
        playGame.setColor(normalColor);
        settings.setColor(normalColor);
        credits.setColor(normalColor);

        // Highlight selected option
        if (currentMenuItemHovered == 0) {
            playGame.setColor(selectedColor);

        } else if (currentMenuItemHovered == 1) {
            settings.setColor(selectedColor);

        } else if (currentMenuItemHovered == 2) {
            credits.setColor(selectedColor);
        }

        // Unlock SPACE when released
        if (Keyboard.isKeyUp(Key.SPACE)) {
            keyLocker.unlockKey(Key.SPACE);
        }

        // Select menu option
        if (!keyLocker.isKeyLocked(Key.SPACE)
                && Keyboard.isKeyDown(Key.SPACE)) {

            keyLocker.lockKey(Key.SPACE);

            if (currentMenuItemHovered == 0) {

                // Start game
                screenCoordinator.setGameState(
                    GameState.LEVEL
                );

            } else if (currentMenuItemHovered == 1) {

                // Settings not implemented yet

            } else if (currentMenuItemHovered == 2) {

                // Open credits screen
                screenCoordinator.setGameState(
                    GameState.CREDITS
                );
            }
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        // Draw Background.png across entire menu
        if (backgroundImage != null) {

            graphicsHandler.drawImage(
                backgroundImage,
                0,
                0,
                screenWidth,
                screenHeight
            );

        } else {

            // Fallback sky-blue background
            graphicsHandler.drawFilledRectangle(
                0,
                0,
                screenWidth,
                screenHeight,
                new Color(135, 206, 235)
            );
        }

        // Draw centered logo
        if (logo != null) {

            graphicsHandler.drawImage(
                logo,
                logoX,
                logoY,
                logoWidth,
                logoHeight
            );
        }

        // Draw button images behind text
        if (menuButton != null) {

            // PLAY GAME
            graphicsHandler.drawImage(
                menuButton,
                buttonX,
                playButtonY,
                buttonWidth,
                buttonHeight
            );

            // SETTINGS
            graphicsHandler.drawImage(
                menuButton,
                buttonX,
                settingsButtonY,
                buttonWidth,
                buttonHeight
            );

            // CREDITS
            graphicsHandler.drawImage(
                menuButton,
                buttonX,
                creditsButtonY,
                buttonWidth,
                buttonHeight
            );
        }

        // Draw text on top of buttons
        playGame.draw(graphicsHandler);
        settings.draw(graphicsHandler);
        credits.draw(graphicsHandler);
    }
}
