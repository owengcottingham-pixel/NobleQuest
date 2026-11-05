package Maps;

import EnhancedMapTiles.Scenery;
import EnhancedMapTiles.TempleGong;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.KeyBinds;
import Engine.ScreenManager;
import Level.Checklist;
import Level.EnhancedMapTile;
import Level.Map;
import Level.NPC;
import Level.Player;
import NPCs.SkyDragon;
import NPCs.TempleMonk;
import Tilesets.CommonTileset;
import Utils.Effects;
import Utils.Point;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;


public class TempleLevel extends Map {

    private static final String SHADOW_LINE =
            "So... a knight walks out\nof the fire, and all the\nway up my mountain.";
    private static final String[] MONK_PAGES = {
            "Surprised? I have walked\nbeside you since the\nmeadow, young knight.",
            "This is the Temple of\nthe Elders. For a thousand\nyears it kept the\nScroll of Wisdom.",
            "Three nights ago the scroll\nwas stolen, and carried up\nabove the clouds, where\nno one can follow.",
            "No one on foot, that is.\nStrike the temple gong.\nThe old sky dragon still\nanswers it.",
            "Bring the scroll home,\nand the wisdom you came\nfor is yours.",
    };
    private static final String MONK_REMINDER = "Strike the gong with\nyour sword, young knight.";
    private static final String MONK_GO = "The dragon waits for you\nat the cliff's edge. Go!";

    private static final int T = 48;
    private static final float TEMPLE_X = 47.5f * T - 252, TEMPLE_FLOOR = 14 * T;

    private TempleMonk monk;
    private TempleGong gong;
    private SkyDragon dragon;
    private Checklist checklist;
    private float shakeX, shakeY, lookY;
    private boolean interactWasDown = true;
    private boolean sawTitle = false;
    private int titleTimer = 0;
    private int fadeTimer = 0;
    private int dragonDelay = -1;

    public TempleLevel() {
        super("temple_level_map.txt", new CommonTileset());
        this.playerStartPosition = getMapTile(4, 26).getLocation();
        this.background = ImageLoader.load("TempleSky.png");
        Effects.reset();
    }

    private Point tile(float x, float y) {
        return new Point(x * T, y * T);
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> tiles = new ArrayList<>();
        // the camera draws these last-added-first, so the gong goes in before the temple
        gong = new TempleGong(new Point(TEMPLE_X + 252 - 48, TEMPLE_FLOOR - 104), () -> dragonDelay = 70);
        tiles.add(gong);
        tiles.add(new Scenery(ImageLoader.load("Temple.png"), new Point(TEMPLE_X, TEMPLE_FLOOR - 480), 3));
        tiles.add(new Scenery(ImageLoader.load("Cave.png"), tile(0, 24), 3));
        return tiles;
    }

    @Override
    public ArrayList<NPC> loadNPCs() {
        ArrayList<NPC> npcs = new ArrayList<>();
        monk = new TempleMonk(new Point(TEMPLE_X + 338, TEMPLE_FLOOR - 108),
                SHADOW_LINE, MONK_PAGES, MONK_REMINDER, () -> gong.arm());
        npcs.add(monk);
        return npcs;
    }

    private SkyDragon dragon() {
        if (dragon == null) {
            // in over the temple from the left, a loop below the cliff, then up beside it
            float[][] arrive = {
                    { 24 * T, -4 * T }, { 34 * T, 6 * T }, { 44 * T, 9.5f * T }, { 54 * T, 8 * T }, { 63 * T, 10 * T },
                    { 70 * T, 18 * T }, { 77 * T, 22 * T }, { 80 * T, 16 * T }, { 73 * T, 13.8f * T },
            };
            float[][] leave = {
                    { 72 * T, 8 * T }, { 66 * T, 1 * T }, { 56 * T, -5 * T }, { 44 * T, -13 * T }, { 34 * T, -26 * T },
            };
            dragon = new SkyDragon(arrive, 67.0f * T, 13.4f * T, leave);
        }
        return dragon;
    }

    @Override
    public Checklist getChecklist() {
        if (checklist != null) return checklist;
        checklist = new Checklist()
                .add("Climb to the temple", null,
                        p -> true,
                        p -> p.getBounds().getY2() < 15 * T && p.getBounds().getX1() > 38 * T)
                .add("Speak to the figure", () -> KeyBinds.label(KeyBinds.Action.INTERACT),
                        p -> p.getBounds().getY2() < 15 * T,
                        p -> monk != null && monk.isDone())
                .add("Strike the gong", () -> KeyBinds.label(KeyBinds.Action.ATTACK),
                        p -> monk != null && monk.isDone(),
                        p -> gong != null && gong.hasCalled())
                .add("Ride the dragon", () -> KeyBinds.label(KeyBinds.Action.INTERACT),
                        p -> dragon != null && dragon.getPhase().ordinal() >= SkyDragon.Phase.WAITING.ordinal(),
                        p -> dragon != null && dragon.getPhase().ordinal() >= SkyDragon.Phase.MOUNTING.ordinal());
        return checklist;
    }

    @Override
    public void update(Player player) {
        camera.moveX(-shakeX);
        camera.moveY(-shakeY - lookY);

        SkyDragon d = dragon();
        if (dragonDelay > 0 && --dragonDelay == 0) {
            d.call();
            monk.setReminder(MONK_GO);
        }
        boolean down = KeyBinds.isDown(KeyBinds.Action.INTERACT);
        if (down && !interactWasDown && d.canRideFrom(player) && !player.isFrozen()) {
            d.ride(player);
        }
        interactWasDown = down;
        d.update(player);

        if (!sawTitle && player.getBounds().getY2() < 15 * T && player.getBounds().getX1() > 38 * T) {
            sawTitle = true;
            titleTimer = 220;
        }
        if (titleTimer > 0) titleTimer--;

        // up into the clouds: fade to white, then on to the next level
        if (d.getPhase() == SkyDragon.Phase.LEAVING && d.getPhaseTick() > 70 || d.getPhase() == SkyDragon.Phase.GONE) {
            fadeTimer++;
            if (fadeTimer == 80) {
                player.setFrozen(false);
                player.completeLevel();
            }
        }

        super.update(player);
        // up on the summit, lean the camera up so the whole temple and the sky are in view
        boolean onTop = player.getBounds().getY2() < 16 * T && player.getBounds().getX1() > 36 * T;
        float want = onTop ? -Math.min(170, camera.getY()) : 0;
        lookY += (want - lookY) * 0.06f;
        float[] offset = Effects.nextShakeOffset();
        shakeX = offset[0];
        shakeY = offset[1];
        camera.moveX(shakeX);
        camera.moveY(shakeY + lookY);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
        if (dragon != null) {
            dragon.draw(graphicsHandler, camera);
        }
    }

    @Override
    public void drawOverPlayer(GraphicsHandler graphicsHandler) {
        Graphics2D g = graphicsHandler.getGraphics();
        int w = ScreenManager.getScreenWidth(), h = ScreenManager.getScreenHeight();

        if (dragon != null && dragon.getPhase() == SkyDragon.Phase.WAITING) {
            float[] seat = dragon.seat();
            String label = "[" + KeyBinds.label(KeyBinds.Action.INTERACT) + "] Climb on";
            g.setFont(new Font("Serif", Font.BOLD, 18));
            int tw = g.getFontMetrics().stringWidth(label);
            int x = Math.round(seat[0] - camera.getX()) - tw / 2, y = Math.round(seat[1] - camera.getY()) - 120;
            g.setColor(new Color(20, 16, 30, 200));
            g.fillRoundRect(x - 10, y - 20, tw + 20, 28, 10, 10);
            g.setColor(new Color(251, 230, 160));
            g.drawString(label, x, y);
        }

        if (titleTimer > 0) {
            int shown = 220 - titleTimer;
            float a = Math.min(1f, Math.min(shown / 30f, titleTimer / 40f));
            drawCentered(g, "THE TEMPLE OF THE ELDERS", h / 5, new Font("Serif", Font.BOLD, 46), a, new Color(255, 230, 170));
        }

        if (fadeTimer > 0) {
            int alpha = Math.min(255, fadeTimer * 255 / 70);
            g.setColor(new Color(255, 250, 240, alpha));
            g.fillRect(0, 0, w, h);
        }
    }

    private static void drawCentered(Graphics2D g, String text, int y, Font font, float alpha, Color color) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int x = (ScreenManager.getScreenWidth() - fm.stringWidth(text)) / 2;
        int a = Math.round(255 * Math.max(0f, alpha));
        g.setColor(new Color(30, 18, 30, a));
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                g.drawString(text, x + dx, y + dy);
            }
        }
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), a));
        g.drawString(text, x, y);
    }
}
