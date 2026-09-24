package Utils;

import Engine.GraphicsHandler;
import GameObject.SpriteSheet;
import java.awt.image.BufferedImage;

public class NumberDisplay {
    private SpriteSheet digits;
    private int scale;
    private static final int DIGIT_W = 8;
    private static final int DIGIT_H = 10;

    public NumberDisplay(SpriteSheet digits, int scale) {
        this.digits = digits;
        this.scale = scale;
    }

    public int widthOf(int value) {
        return String.valueOf(value).length() * DIGIT_W * scale;
    }

    public void draw(GraphicsHandler graphicsHandler, int value, int x, int y) {
        String s = String.valueOf(value);
        for (int i = 0; i < s.length(); i++) {
            BufferedImage glyph = digits.getSprite(0, s.charAt(i) - '0');
            graphicsHandler.drawImage(
                glyph,
                x + i * DIGIT_W * scale, y,
                DIGIT_W * scale, DIGIT_H * scale
            );
        }
    }
}