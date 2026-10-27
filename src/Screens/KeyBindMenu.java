package Screens;

import Engine.GraphicsHandler;
import Engine.Key;
import Engine.KeyBinds;
import Engine.KeyLocker;
import Engine.Keyboard;
import Engine.ScreenManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.EnumSet;

/*
 * The Controls screen, opened from the pause menu.
 *
 * Up/Down picks a row, Enter listens for the next key you press and binds it,
 * R puts that one row back to its default, and Esc backs out. The last row puts
 * everything back at once.
 *
 * Esc and Enter are never bound to anything -- see KeyBinds.isReserved -- or you
 * could bind jump to Esc and have no way out of this menu afterwards.
 *
 * Drawn in the pause menu's colours rather than the dwarf shop's, since this is
 * a settings screen and not part of the world.
 */
public class KeyBindMenu {

    private static final int PANEL_W = 560;
    private static final int PANEL_H = 476;
    private static final int ROW_H = 28;
    private static final int PAD = 26;

    private static final Color PANEL = new Color(25, 25, 35, 240);
    private static final Color BORDER = Color.white;
    private static final Color TITLE = Color.white;
    private static final Color ROW = new Color(200, 200, 200);
    private static final Color PICKED = new Color(255, 215, 80);
    private static final Color KEY_TEXT = new Color(235, 235, 245);
    private static final Color LISTENING = new Color(120, 230, 150);
    private static final Color SHARED = new Color(240, 150, 110);
    private static final Color FOOTER = new Color(170, 170, 170);

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 30);
    private static final Font ROW_FONT = new Font("Arial", Font.PLAIN, 18);
    private static final Font SMALL_FONT = new Font("Arial", Font.PLAIN, 13);

    private static final KeyBinds.Action[] ACTIONS = KeyBinds.Action.values();
    private static final int RESET_ROW = ACTIONS.length;      // the extra row on the end

    private final KeyLocker keyLocker = new KeyLocker();
    private static final Key[] MENU_KEYS = {
            Key.UP, Key.DOWN, Key.W, Key.S, Key.ENTER, Key.ESC, Key.R
    };

    private boolean open = false;
    private int selected = 0;
    private boolean listening = false;

    // Keys already held when we started listening. Without this the Enter press
    // that opened the prompt would immediately bind itself, and holding a
    // movement key while opening the menu would bind that.
    private final EnumSet<Key> heldOnEntry = EnumSet.noneOf(Key.class);

    private String message = "";
    private int messageTimer = 0;
    private static final int MESSAGE_FRAMES = 120;

    public boolean isOpen() {
        return open;
    }

    public void open() {
        open = true;
        selected = 0;
        listening = false;
        message = "";
        messageTimer = 0;
        for (Key key : MENU_KEYS) {
            keyLocker.lockKey(key);
        }
    }

    public void close() {
        open = false;
        listening = false;
    }

    public void update() {
        if (!open) {
            return;
        }
        if (messageTimer > 0) {
            messageTimer--;
        }
        for (Key key : MENU_KEYS) {
            if (Keyboard.isKeyUp(key)) {
                keyLocker.unlockKey(key);
            }
        }

        if (listening) {
            updateListening();
            return;
        }

        if (pressed(Key.UP) || pressed(Key.W)) {
            selected = (selected + RESET_ROW) % (RESET_ROW + 1);
        } else if (pressed(Key.DOWN) || pressed(Key.S)) {
            selected = (selected + 1) % (RESET_ROW + 1);
        } else if (pressed(Key.ENTER)) {
            if (selected == RESET_ROW) {
                KeyBinds.restoreDefaultsAndSave();
                say("Controls restored to defaults.");
            } else {
                startListening();
            }
        } else if (pressed(Key.R) && selected != RESET_ROW) {
            KeyBinds.reset(ACTIONS[selected]);
            say(ACTIONS[selected].label + " back to "
                    + KeyBinds.label(ACTIONS[selected]) + ".");
        } else if (pressed(Key.ESC)) {
            close();
        }
    }

    private void startListening() {
        listening = true;
        heldOnEntry.clear();
        for (Key key : Key.values()) {
            if (Keyboard.isKeyDown(key)) {
                heldOnEntry.add(key);
            }
        }
        message = "";
        messageTimer = 0;
    }

    private void updateListening() {
        // a key only counts once it has been released and pressed again
        for (Key key : Key.values()) {
            if (Keyboard.isKeyUp(key)) {
                heldOnEntry.remove(key);
            }
        }

        if (Keyboard.isKeyDown(Key.ESC) && !heldOnEntry.contains(Key.ESC)) {
            listening = false;
            keyLocker.lockKey(Key.ESC);     // so the same press does not also close the menu
            say("Left as " + KeyBinds.label(ACTIONS[selected]) + ".");
            return;
        }

        for (Key key : Key.values()) {
            if (KeyBinds.isReserved(key) || heldOnEntry.contains(key)) {
                continue;
            }
            if (Keyboard.isKeyDown(key)) {
                KeyBinds.Action action = ACTIONS[selected];
                KeyBinds.set(action, key);
                listening = false;
                keyLocker.lockKey(key);
                say(action.label + " is now " + KeyBinds.label(key)
                        + (KeyBinds.isShared(action) ? "  (shared with another action)" : ""));
                return;
            }
        }
    }

    private boolean pressed(Key key) {
        if (Keyboard.isKeyDown(key) && !keyLocker.isKeyLocked(key)) {
            keyLocker.lockKey(key);
            return true;
        }
        return false;
    }

    private void say(String text) {
        message = text;
        messageTimer = MESSAGE_FRAMES;
    }

    public void draw(GraphicsHandler graphicsHandler) {
        if (!open) {
            return;
        }
        int screenW = ScreenManager.getScreenWidth();
        int screenH = ScreenManager.getScreenHeight();

        graphicsHandler.drawFilledRectangle(0, 0, screenW, screenH, new Color(0, 0, 0, 190));

        int px = (screenW - PANEL_W) / 2;
        int py = (screenH - PANEL_H) / 2;
        graphicsHandler.drawFilledRectangleWithBorder(px, py, PANEL_W, PANEL_H, PANEL, BORDER, 3);

        FontMetrics titleMetrics = graphicsHandler.getGraphics().getFontMetrics(TITLE_FONT);
        graphicsHandler.drawString("CONTROLS",
                px + (PANEL_W - titleMetrics.stringWidth("CONTROLS")) / 2,
                py + 44, TITLE_FONT, TITLE);

        FontMetrics rowMetrics = graphicsHandler.getGraphics().getFontMetrics(ROW_FONT);
        int rowY = py + 84;

        for (int i = 0; i < ACTIONS.length; i++) {
            KeyBinds.Action action = ACTIONS[i];
            boolean picked = (i == selected);
            boolean waiting = picked && listening;

            if (picked) {
                graphicsHandler.drawFilledRectangle(px + PAD - 8, rowY - rowMetrics.getAscent() - 3,
                        PANEL_W - (PAD - 8) * 2, ROW_H - 2, new Color(255, 255, 255, 28));
            }

            graphicsHandler.drawString((picked ? "> " : "  ") + action.label,
                    px + PAD, rowY, ROW_FONT, picked ? PICKED : ROW);

            String keyText = waiting ? "press a key..." : KeyBinds.label(action);
            Color keyColour = waiting ? LISTENING
                    : (KeyBinds.isShared(action) ? SHARED : KEY_TEXT);
            graphicsHandler.drawString(keyText,
                    px + PANEL_W - PAD - rowMetrics.stringWidth(keyText),
                    rowY, ROW_FONT, keyColour);

            rowY += ROW_H;
        }

        rowY += 6;
        boolean resetPicked = (selected == RESET_ROW);
        if (resetPicked) {
            graphicsHandler.drawFilledRectangle(px + PAD - 8, rowY - rowMetrics.getAscent() - 3,
                    PANEL_W - (PAD - 8) * 2, ROW_H - 2, new Color(255, 255, 255, 28));
        }
        graphicsHandler.drawString((resetPicked ? "> " : "  ") + "Restore Defaults",
                px + PAD, rowY, ROW_FONT, resetPicked ? PICKED : ROW);

        if (messageTimer > 0) {
            FontMetrics smallMetrics = graphicsHandler.getGraphics().getFontMetrics(SMALL_FONT);
            graphicsHandler.drawString(message,
                    px + (PANEL_W - smallMetrics.stringWidth(message)) / 2,
                    py + PANEL_H - 56, SMALL_FONT, LISTENING);
        }

        String hint = listening
                ? "Press the key you want    Esc to keep the current one"
                : "Up/Down select    Enter rebind    R reset row    Esc back";
        FontMetrics hintMetrics = graphicsHandler.getGraphics().getFontMetrics(SMALL_FONT);
        graphicsHandler.drawString(hint,
                px + (PANEL_W - hintMetrics.stringWidth(hint)) / 2,
                py + PANEL_H - 32, SMALL_FONT, FOOTER);

        String reserved = "Esc and Enter stay reserved for menus";
        graphicsHandler.drawString(reserved,
                px + (PANEL_W - hintMetrics.stringWidth(reserved)) / 2,
                py + PANEL_H - 14, SMALL_FONT, new Color(130, 130, 140));
    }
}
