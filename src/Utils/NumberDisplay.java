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

    // one digit slot on screen
    public int slotWidth() {
        return DIGIT_W * scale;
    }

    public int slotHeight() {
        return DIGIT_H * scale;
    }

    // total width of a fixed field this many slots wide
    public int fieldWidth(int slots) {
        return slots * slotWidth();
    }

    public int widthOf(int value) {
        return String.valueOf(Math.max(value, 0)).length() * slotWidth();
    }

    // Draws value into a fixed field of `slots` slots, left aligned.
    // Slot 0 always sits exactly at x, slot 1 at x + slotWidth(), and so on, so a
    // digit never moves once it is on screen: 7 uses slot 0, 42 uses slots 0-1,
    // 999 uses slots 0-2. Unused slots are left empty -- no placeholder zeros.
    // The value is clamped so it can never spill past the reserved field.
    public void drawInSlots(GraphicsHandler graphicsHandler, int value, int x, int y, int slots) {
        if (slots <= 0) {
            return;
        }

        int v = Math.max(value, 0);
        int limit = 1;
        for (int i = 0; i < slots; i++) {
            limit *= 10;
        }
        if (v > limit - 1) {
            v = limit - 1;
        }

        String s = String.valueOf(v);
        for (int i = 0; i < s.length(); i++) {
            BufferedImage glyph = digits.getSprite(0, s.charAt(i) - '0');
            graphicsHandler.drawImage(
                glyph,
                x + i * slotWidth(), y,
                slotWidth(), slotHeight()
            );
        }
    }

    // draws just the digits of value, with no reserved field
    public void draw(GraphicsHandler graphicsHandler, int value, int x, int y) {
        int digitCount = String.valueOf(Math.max(value, 0)).length();
        drawInSlots(graphicsHandler, value, x, y, digitCount);
    }
}
