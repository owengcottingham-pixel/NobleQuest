package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.ForestChunk;
import Engine.GraphicsHandler;
import GameObject.Rectangle;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.Point;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;

// A whirlpool in the water that teleports you back to the normal world
public class Whirlpool extends EnhancedMapTile {
    private final BiConsumer<Player, Float> onEnter;
    private boolean spinning;
    private int tick = 0, spinUp = 0;
    private int pulseOn = 0, pulseOff = 0, pulseTimer = 0;
    private float driftMin, driftMax, driftSpeed = 0;
    private int driftDir = 1;

    public Whirlpool(Point location, boolean spinning, BiConsumer<Player, Float> onEnter) {
        super(location.x, location.y,
                new FrameBuilder(new java.awt.image.BufferedImage(32, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB))
                        .withScale(3).withBounds(6, 8, 20, 8).build(),
                TileType.PASSABLE);
        this.spinning = spinning;
        this.spinUp = spinning ? 40 : 0;   // (pulsing ones start on)
        this.onEnter = onEnter;
        initialize();
        setIsUpdateOffScreen(true);
    }

    public void start() {
        spinning = true;
        pulseTimer = 0;
    }

    public Whirlpool pulse(int onFrames, int offFrames) {
        pulseOn = onFrames;
        pulseOff = offFrames;
        return this;
    }

    public Whirlpool drift(float fromX, float toX, float speed) {
        driftMin = Math.min(fromX, toX);
        driftMax = Math.max(fromX, toX);
        driftSpeed = speed;
        return this;
    }

    // spinning hard enough right now to pull you in
    private boolean live() {
        return spinning && spinUp >= 20 && (pulseOn == 0 || pulseTimer < pulseOn);
    }

    public boolean isSpinning() {
        return spinning;
    }

    @Override
    public void update(Player player) {
        super.update(player);
        tick++;
        boolean on = spinning;
        if (spinning && pulseOn > 0) {
            pulseTimer = (pulseTimer + 1) % (pulseOn + pulseOff);
            on = pulseTimer < pulseOn;
        }
        if (on && spinUp < 40) spinUp += 2;
        if (!on && spinUp > 0) spinUp--;
        if (spinning && driftSpeed > 0) {
            float next = getX() + driftSpeed * driftDir;
            if (next >= driftMax) { next = driftMax; driftDir = -1; }
            if (next <= driftMin) { next = driftMin; driftDir = 1; }
            setX(next);
        }
        if (live() && intersects(player) && player.getHealth() > 0) {
            Rectangle b = getBounds();
            ForestChunk.burst(map, b.getX1() + b.getWidth() / 2f, b.getY2(), new int[] { ForestChunk.WISP }, 6, 3f);
            onEnter.accept(player, b.getX1() + b.getWidth() / 2f);
        }
    }

    // The spinning funnel is drawn once into a loop of frames, shared by every whirlpool, and then
    // just stamped each frame (drawing all the thick spinning rings live was slow).
    private static final int LOOP = 60, FW = 128, FH = 128, FCX = 64, FCY = 104;
    private static BufferedImage[] frames;

    private static BufferedImage[] frames() {
        if (frames != null) return frames;
        frames = new BufferedImage[LOOP];
        for (int t = 0; t < LOOP; t++) {
            BufferedImage img = new BufferedImage(FW, FH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            double turn = 2 * Math.PI * t / LOOP;
            // a funnel of spinning rings, widest at the water and narrowing away from it
            for (int i = 0; i < 7; i++) {
                float k = i / 6f;
                int ry = Math.round(13 - 7 * k), rx = Math.round(50 - 34 * k);
                int y = FCY - i * 13;
                int sway = (int) Math.round(Math.sin(turn + i * 0.9) * 5 * k);
                g.setColor(new Color(16, 54, 82, Math.round(110 * (1 - k * 0.5f))));
                g.fillOval(FCX - rx + sway, y - ry, rx * 2, ry * 2);
                g.setColor(new Color(220, 248, 255, Math.round(230 * (1 - k * 0.6f))));
                g.setStroke(new BasicStroke(3f - 1.5f * k));
                int start = (int) Math.round(Math.toDegrees(turn) * (3 - i * 0.25) + i * 50) % 360;
                g.drawArc(FCX - rx + sway, y - ry, rx * 2, ry * 2, start, 200);
                g.drawArc(FCX - rx + sway, y - ry, rx * 2, ry * 2, start + 220, 70);
            }
            // spray flicking off the rim
            for (int i = 0; i < 6; i++) {
                double a = turn * 2 + i * Math.PI / 3;
                int sx = FCX + (int) (Math.cos(a) * 52), sy = FCY + (int) (Math.sin(a) * 12) - 4;
                g.setColor(new Color(235, 250, 255, 200));
                g.fillOval(sx - 2, sy - 2, 5, 5);
            }
            g.dispose();
            frames[t] = img;
        }
        return frames;
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        int cx = Math.round(getCalibratedXLocation()) + 48;
        int cy = Math.round(getCalibratedYLocation()) + 48;     // on the water, under his feet
        Graphics2D g = graphicsHandler.getGraphics();
        float power = spinUp / 40f;
        if (power <= 0) {
            g.setColor(new Color(170, 220, 236));
            g.drawOval(cx - 40, cy - 8, 80, 16);
            return;
        }
        BufferedImage frame = frames()[tick % LOOP];
        if (power >= 1) {
            g.drawImage(frame, cx - FCX, cy - FCY, null);
        } else {
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, power));
            g.drawImage(frame, cx - FCX, cy - FCY, null);
            g.setComposite(old);
        }
    }
}
