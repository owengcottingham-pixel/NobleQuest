package Screens;

import Engine.*;
import SpriteFont.SpriteFont;

import java.awt.*;

// This is the class for the level lose screen
public class LevelLoseScreen extends Screen {
    protected SpriteFont loseMessage;
    protected SpriteFont instructions;
    protected KeyLocker keyLocker = new KeyLocker();
    protected PlayLevelScreen playLevelScreen;

    public LevelLoseScreen(PlayLevelScreen playLevelScreen) {
        this.playLevelScreen = playLevelScreen;
        initialize();
    }

    @Override
    public void initialize() {
        loseMessage = new SpriteFont("You lose!", 0, 0, "Arial", 30, Color.white);
        instructions = new SpriteFont("Press Space to try again or Escape to go back to the main menu", 0, 0, "Arial", 20, Color.white);
        keyLocker.lockKey(Key.SPACE);
        keyLocker.lockKey(Key.ESC);
    }

    @Override
    public void update() {
        if (Keyboard.isKeyUp(Key.SPACE)) {
            keyLocker.unlockKey(Key.SPACE);
        }
        if (Keyboard.isKeyUp(Key.ESC)) {
            keyLocker.unlockKey(Key.ESC);
        }

        // if space is pressed, reset level. if escape is pressed, go back to main menu
        if (Keyboard.isKeyDown(Key.SPACE) && !keyLocker.isKeyLocked(Key.SPACE)) {
            playLevelScreen.resetLevel();
        } else if (Keyboard.isKeyDown(Key.ESC) && !keyLocker.isKeyLocked(Key.ESC)) {
            playLevelScreen.goBackToMenu();
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {
        int screenWidth = ScreenManager.getScreenWidth();
        int screenHeight = ScreenManager.getScreenHeight();
        graphicsHandler.drawFilledRectangle(0, 0, screenWidth, screenHeight, Color.black);

        float scale = Math.min(screenWidth / 800.0f, screenHeight / 600.0f);
        int messageFontSize = Math.max(12, Math.round(30 * scale));
        int instructionsFontSize = Math.max(10, Math.round(20 * scale));
        Graphics2D graphics = graphicsHandler.getGraphics();
        int availableWidth = Math.max(1, screenWidth - 40);

        instructions.setFontSize(instructionsFontSize);
        FontMetrics instructionsMetrics = graphics.getFontMetrics(instructions.getFont());
        while (instructionsMetrics.stringWidth(instructions.getText()) > availableWidth && instructionsFontSize > 10) {
            instructions.setFontSize(--instructionsFontSize);
            instructionsMetrics = graphics.getFontMetrics(instructions.getFont());
        }

        loseMessage.setFontSize(messageFontSize);
        FontMetrics messageMetrics = graphics.getFontMetrics(loseMessage.getFont());
        int gap = Math.max(8, Math.round(16 * scale));
        int contentHeight = messageMetrics.getHeight() + gap + instructionsMetrics.getHeight();
        int top = (screenHeight - contentHeight) / 2;

        loseMessage.setLocation((screenWidth - messageMetrics.stringWidth(loseMessage.getText())) / 2,
            top);
        instructions.setLocation((screenWidth - instructionsMetrics.stringWidth(instructions.getText())) / 2,
            top + messageMetrics.getHeight() + gap);

        loseMessage.draw(graphicsHandler);
        instructions.draw(graphicsHandler);
    }
}
