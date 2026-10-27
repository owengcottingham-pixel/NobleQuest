package Engine;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

/*
 * A key cap drawn in code, for the floating "press this" prompts.
 *
 * The dwarf's counter and the wishing well both float a prompt made from
 * KeyPromptE.png, which has the letter E painted into it. That was fine while E
 * was the only possible answer, but now Interact can be rebound and a picture of
 * an E would be telling the player the wrong thing.
 *
 * Both of those call sites keep using the pixel art while the binding really is
 * E, so the default game looks exactly as it did, and fall back to this for any
 * other key. Caps grow to fit, so "Shift" and "Space" read properly instead of
 * being squashed into a square.
 */
public final class KeyPrompt {

    private static final Color BORDER = new Color(24, 20, 28);
    private static final Color FACE = new Color(206, 210, 214);
    private static final Color FACE_PRESSED = new Color(176, 180, 186);
    private static final Color SHADOW = new Color(138, 142, 150);
    private static final Color TEXT = new Color(30, 28, 36);

    private KeyPrompt() {
    }

    public static void draw(GraphicsHandler graphicsHandler, Key key,
                            int centreX, int topY, int size, boolean pressed) {
        String label = KeyBinds.label(key);
        Font font = new Font("Monospaced", Font.BOLD, Math.max(9, Math.round(size * 0.52f)));
        FontMetrics metrics = graphicsHandler.getGraphics().getFontMetrics(font);

        int textWidth = metrics.stringWidth(label);
        int width = Math.max(size, textWidth + size / 2);
        int x = centreX - width / 2;
        int y = topY + (pressed ? 1 : 0);     // the cap sinks when held, like the art does

        graphicsHandler.drawFilledRectangleWithBorder(x, y, width, size,
                pressed ? FACE_PRESSED : FACE, BORDER, 2);
        graphicsHandler.drawFilledRectangle(x + 2, y + size - 2 - size / 5,
                width - 4, size / 5, SHADOW);
        graphicsHandler.drawString(label, x + (width - textWidth) / 2,
                y + (size + metrics.getAscent()) / 2 - 2, font, TEXT);
    }
}
