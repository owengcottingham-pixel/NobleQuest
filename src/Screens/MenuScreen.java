 
package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import Maps.TitleScreenMap;
import SpriteFont.SpriteFont;
import java.awt.*;
import java.awt.image.BufferedImage;

// Main menu screen
public class MenuScreen extends Screen {

    protected ScreenCoordinator screenCoordinator;
    protected int currentMenuItemHovered = 0;
    protected int menuItemSelected = -1;

    protected SpriteFont playGame;
    protected SpriteFont settings;
    protected SpriteFont credits;
    protected Map background;

    // Logo image
    protected BufferedImage logo;

    // Logo position and size
    protected int logoX = 400;
    protected int logoY = 50;
    protected int logoWidth = 500;
    protected int logoHeight = 500;

    protected int keyPressTimer;
    protected int pointerLocationX, pointerLocationY;
    protected KeyLocker keyLocker = new KeyLocker();

    public MenuScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {

        // Load the logo image
        logo = ImageLoader.load("logo.png");

        playGame = new SpriteFont(
            "PLAY GAME", 200, 123,
            "Arial", 30, new Color(49, 207, 240)
        );
        playGame.setOutlineColor(Color.black);
        playGame.setOutlineThickness(3);

        settings = new SpriteFont(
            "SETTINGS", 200, 223,
            "Arial", 30, new Color(49, 207, 240)
        );
        settings.setOutlineColor(Color.black);
        settings.setOutlineThickness(3);

        credits = new SpriteFont(
            "CREDITS", 200, 323,
            "Arial", 30, new Color(49, 207, 240)
        );
        credits.setOutlineColor(Color.black);
        credits.setOutlineThickness(3);

        background = new TitleScreenMap();
        background.setAdjustCamera(false);

        keyPressTimer = 0;
        menuItemSelected = -1;
        currentMenuItemHovered = 0;

        keyLocker.lockKey(Key.SPACE);
    }

    @Override
    public void update() {

        // Update background animations
        background.update(null);

        // Move menu selection
        if (Keyboard.isKeyDown(Key.DOWN) && keyPressTimer == 0) {
            keyPressTimer = 14;
            currentMenuItemHovered++;

        } else if (Keyboard.isKeyDown(Key.UP) && keyPressTimer == 0) {
            keyPressTimer = 14;
            currentMenuItemHovered--;

        } else {
            if (keyPressTimer > 0) {
                keyPressTimer--;
            }
        }

        // Loop through all three menu options
        if (currentMenuItemHovered > 2) {
            currentMenuItemHovered = 0;
        } else if (currentMenuItemHovered < 0) {
            currentMenuItemHovered = 2;
        }

        // Default colors
        playGame.setColor(new Color(49, 207, 240));
        settings.setColor(new Color(49, 207, 240));
        credits.setColor(new Color(49, 207, 240));

        // Highlight selected menu option
        if (currentMenuItemHovered == 0) {

            playGame.setColor(new Color(255, 215, 0));

            pointerLocationX = 170;
            pointerLocationY = 130;

        } else if (currentMenuItemHovered == 1) {

            settings.setColor(new Color(255, 215, 0));

            pointerLocationX = 170;
            pointerLocationY = 230;

        } else if (currentMenuItemHovered == 2) {

            credits.setColor(new Color(255, 215, 0));

            pointerLocationX = 170;
            pointerLocationY = 330;
        }

        // Unlock space when released
        if (Keyboard.isKeyUp(Key.SPACE)) {
            keyLocker.unlockKey(Key.SPACE);
        }

        // Select menu option
        if (!keyLocker.isKeyLocked(Key.SPACE)
                && Keyboard.isKeyDown(Key.SPACE)) {

            keyLocker.lockKey(Key.SPACE);
            menuItemSelected = currentMenuItemHovered;

            if (menuItemSelected == 0) {
                // Start the game
                screenCoordinator.setGameState(GameState.LEVEL);

            } else if (menuItemSelected == 1) {
                // SETTINGS - Not implemented yet

            } else if (menuItemSelected == 2) {
                // CREDITS - Not implemented yet
            }
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        // Draw menu background
        background.draw(graphicsHandler);

        // Draw logo over the background
        if (logo != null) {
            graphicsHandler.drawImage(
                logo,
                logoX,
                logoY,
                logoWidth,
                logoHeight
            );
        }

        // Draw menu buttons
        playGame.draw(graphicsHandler);
        settings.draw(graphicsHandler);
        credits.draw(graphicsHandler);

        // Draw blue selection square
        graphicsHandler.drawFilledRectangleWithBorder(
            pointerLocationX,
            pointerLocationY,
            20,
            20,
            new Color(49, 207, 240),
            Color.black,
            2
        );
    }
}
