package Screens;

import Engine.Config;
import Engine.GraphicsHandler;
import Engine.Key;
import Engine.KeyBinds;
import Engine.KeyLocker;
import Engine.Keyboard;
import Level.Player;
import Players.HatSet;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/*
 * The dwarf shopkeeper's cosmetics menu.
 *
 * While this is open PlayLevelScreen stops updating the player and the map, so
 * the world is frozen behind it -- otherwise you could walk away mid-purchase
 * and leave the menu floating over empty ground.
 *
 * Items only list costumes there is actually art for. Adding another is one
 * entry in buildStock() plus the sprite sheet.
 */
public class ShopMenu {

    public static class Item {
        public final String name;
        public final String blurb;
        public final int hat;              // index into HatSet, or HatSet.NONE
        public final int price;
        public boolean owned;

        public Item(String name, String blurb, int hat, int price, boolean owned) {
            this.name = name;
            this.blurb = blurb;
            this.hat = hat;
            this.price = price;
            this.owned = owned;
        }
    }

    // ---- layout ----
    private static final int PANEL_W = 520;
    private static final int PANEL_H = 298;
    private static final int ROW_H = 46;
    private static final int PAD = 24;

    // ---- palette, borrowed from the stall art ----
    private static final Color DIM        = new Color(0, 0, 0, 160);
    private static final Color PANEL      = new Color(84, 56, 34);
    private static final Color PANEL_EDGE = new Color(226, 200, 134);
    private static final Color PARCHMENT  = new Color(206, 186, 150);
    private static final Color ROW        = new Color(66, 42, 24);
    private static final Color ROW_PICKED = new Color(124, 84, 50);
    private static final Color INK        = new Color(32, 22, 18);
    private static final Color GOLD       = new Color(226, 182, 86);
    private static final Color GREEN      = new Color(120, 200, 120);
    private static final Color RED        = new Color(226, 110, 100);
    private static final Color WHITE      = new Color(248, 252, 255);

    private static final Font TITLE_FONT  = new Font("Monospaced", Font.BOLD, 22);
    private static final Font ITEM_FONT   = new Font("Monospaced", Font.BOLD, 18);
    private static final Font SMALL_FONT  = new Font("Monospaced", Font.PLAIN, 13);

    private static final int MESSAGE_FRAMES = 110;

    private final KeyLocker keyLocker = new KeyLocker();

    // static: what has been bought outlives this screen, so dying and reloading
    // the level does not empty the player's wardrobe
    private static final List<Item> stock = buildStock();

    private boolean open = false;
    private int selected = 0;
    private String message = "";
    private int messageTimer = 0;

    private static List<Item> buildStock() {
        List<Item> items = new ArrayList<>();
        // First row takes the hat back off, so the shop is also how you undo a
        // purchase you have gone off. It is free and owned from the start.
        items.add(new Item("Bare Helmet", "No hat. Back to the blue crest.",
                HatSet.NONE, 0, true));
        for (int i = 0; i < HatSet.count(); i++) {
            items.add(new Item(HatSet.NAMES[i], HatSet.BLURBS[i], i, HatSet.PRICES[i], false));
        }
        return items;
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        open = true;
        selected = 0;
        message = "";
        messageTimer = 0;
        // lock the keys that are held at the moment of opening, so the same E
        // press that opened the shop cannot immediately buy the first row
        keyLocker.lockKey(confirmKey());
        keyLocker.lockKey(Key.ENTER);
        keyLocker.lockKey(Key.ESC);
    }

    public void close() {
        open = false;
    }

    public void update(Player player) {
        if (!open) {
            return;
        }
        if (messageTimer > 0) {
            messageTimer--;
        }

        for (Key k : new Key[] { Key.W, Key.S, Key.UP, Key.DOWN, confirmKey(), Key.ENTER, Key.ESC }) {
            if (Keyboard.isKeyUp(k)) {
                keyLocker.unlockKey(k);
            }
        }

        if (!stock.isEmpty()) {
            if (pressed(Key.W) || pressed(Key.UP)) {
                selected = (selected - 1 + stock.size()) % stock.size();
            }
            if (pressed(Key.S) || pressed(Key.DOWN)) {
                selected = (selected + 1) % stock.size();
            }
            if (pressed(confirmKey()) || pressed(Key.ENTER)) {
                buyOrEquip(player);
            }
        }
        if (pressed(Key.ESC)) {
            close();
        }
    }

    // whatever Interact is bound to -- the same key that opened the shop
    private static Key confirmKey() {
        return KeyBinds.get(KeyBinds.Action.INTERACT);
    }

    private boolean pressed(Key key) {
        if (Keyboard.isKeyDown(key) && !keyLocker.isKeyLocked(key)) {
            keyLocker.lockKey(key);
            return true;
        }
        return false;
    }

    private void buyOrEquip(Player player) {
        Item item = stock.get(selected);

        if (!item.owned) {
            if (!player.spendCoins(item.price)) {
                say("Not enough coins.", RED);
                return;
            }
            item.owned = true;
        }

        if (player.getHat() == item.hat) {
            say("Already wearing that.", PARCHMENT);
            return;
        }

        player.setHat(item.hat);
        say(item.hat == HatSet.NONE ? "Hat off." : "Wearing the " + item.name + ".", GREEN);
    }

    private Color messageColor = PARCHMENT;

    private void say(String text, Color colour) {
        message = text;
        messageColor = colour;
        messageTimer = MESSAGE_FRAMES;
    }

    public void draw(GraphicsHandler graphicsHandler, Player player) {
        if (!open) {
            return;
        }

        int screenW = Config.GAME_WINDOW_WIDTH;
        int screenH = Config.GAME_WINDOW_HEIGHT;
        graphicsHandler.drawFilledRectangle(0, 0, screenW, screenH, DIM);

        int px = (screenW - PANEL_W) / 2;
        int py = (screenH - PANEL_H) / 2;
        graphicsHandler.drawFilledRectangleWithBorder(px, py, PANEL_W, PANEL_H, PANEL, PANEL_EDGE, 3);

        graphicsHandler.drawStringWithOutline("DWARF'S HATS", px + PAD, py + 34,
                TITLE_FONT, PARCHMENT, INK, 3f);

        String purse = player.getCoins() + "c";
        graphicsHandler.drawStringWithOutline(purse,
                px + PANEL_W - PAD - purse.length() * 13, py + 34, TITLE_FONT, GOLD, INK, 3f);

        if (stock.isEmpty()) {
            graphicsHandler.drawStringWithOutline("The shelves are bare.",
                    px + PAD, py + 96, ITEM_FONT, PARCHMENT, INK, 2f);
            graphicsHandler.drawString("Come back when the dwarf has restocked.",
                    px + PAD, py + 118, SMALL_FONT, PARCHMENT);
        }

        int rowY = py + 56;
        for (int i = 0; i < stock.size(); i++) {
            Item item = stock.get(i);
            boolean picked = (i == selected);

            graphicsHandler.drawFilledRectangle(px + PAD, rowY, PANEL_W - PAD * 2, ROW_H - 6,
                    picked ? ROW_PICKED : ROW);
            if (picked) {
                graphicsHandler.drawRectangle(px + PAD, rowY, PANEL_W - PAD * 2, ROW_H - 6,
                        PANEL_EDGE, 2);
            }

            graphicsHandler.drawStringWithOutline((picked ? "> " : "  ") + item.name,
                    px + PAD + 10, rowY + 21, ITEM_FONT, picked ? WHITE : PARCHMENT, INK, 2f);
            graphicsHandler.drawString(item.blurb, px + PAD + 28, rowY + 36, SMALL_FONT, PARCHMENT);

            String tag;
            Color tagColour;
            if (player.getHat() == item.hat) {
                tag = "WORN";      tagColour = GREEN;
            } else if (item.owned) {
                tag = "OWNED";     tagColour = PARCHMENT;
            } else {
                tag = item.price + "c"; tagColour = GOLD;
            }
            graphicsHandler.drawStringWithOutline(tag,
                    px + PANEL_W - PAD - 20 - tag.length() * 11, rowY + 25,
                    ITEM_FONT, tagColour, INK, 2f);

            rowY += ROW_H;
        }

        if (messageTimer > 0) {
            graphicsHandler.drawStringWithOutline(message, px + PAD, py + PANEL_H - 44,
                    ITEM_FONT, messageColor, INK, 2f);
        }

        graphicsHandler.drawString(stock.isEmpty()
                        ? "ESC close"
                        : "W/S select    " + KeyBinds.label(KeyBinds.Action.INTERACT)
                          + " buy or wear    ESC close",
                px + PAD, py + PANEL_H - 18, SMALL_FONT, PARCHMENT);
    }
}
