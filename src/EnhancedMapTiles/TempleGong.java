package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.KeyBinds;
import GameObject.Rectangle;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.Direction;
import Utils.Effects;
import Utils.Point;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

// The temple gong.
public class TempleGong extends EnhancedMapTile {
    private static final int SCALE = 2;
    private static final int RING_TIME = 150;

    private final BufferedImage frame, disc, discLit;
    private final Runnable onRing;
    private boolean armed = false;
    private boolean called = false;
    private boolean wasAttacking = false;
    private boolean interactWasDown = true;
    private int ringTimer = 0;
    private int tick = 0;

    // location = top-left of the stand (48x52 art, drawn 2x)
    public TempleGong(Point location, Runnable onRing) {
        super(location.x, location.y,
                new FrameBuilder(new BufferedImage(48, 52, BufferedImage.TYPE_INT_ARGB)).withScale(SCALE).withBounds(8, 12, 32, 40).build(),
                TileType.PASSABLE);
        SpriteSheet sheet = new SpriteSheet(ImageLoader.load("Gong.png"), 48, 52);
        frame = sheet.getSprite(0, 0);
        disc = sheet.getSprite(0, 1).getSubimage(0, 0, 30, 30);
        discLit = sheet.getSprite(0, 2).getSubimage(0, 0, 30, 30);
        this.onRing = onRing;
    }

    public void arm() {
        armed = true;
    }

    public boolean hasCalled() {
        return called;
    }

    @Override
    public void update(Player player) {
        super.update(player);
        tick++;
        if (ringTimer > 0) ringTimer--;

        boolean attacking = player.isAttacking();
        boolean swung = attacking && !wasAttacking && inSwordRange(player);
        wasAttacking = attacking;
        boolean down = KeyBinds.isDown(KeyBinds.Action.INTERACT);
        boolean pressed = down && !interactWasDown && intersects(player) && armed;
        interactWasDown = down;

        if ((swung || pressed) && !player.isFrozen() && ringTimer < RING_TIME - 30) {
            ring();
        }
    }

    private boolean inSwordRange(Player player) {
        Rectangle me = getBounds(), p = player.getBounds();
        float dx = (me.getX1() + me.getWidth() / 2f) - (p.getX1() + p.getWidth() / 2f);
        if (p.getY2() < me.getY1() || p.getY1() > me.getY2()) return false;
        return player.getFacingDirection() == Direction.RIGHT ? (dx > -40 && dx < 130) : (dx < 40 && dx > -130);
    }

    private void ring() {
        ringTimer = RING_TIME;
        Effects.shake(armed ? 9 : 4, armed ? 40 : 16);
        if (armed && !called) {
            called = true;
            if (onRing != null) onRing.run();
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        Graphics2D g = graphicsHandler.getGraphics();
        int x = Math.round(getCalibratedXLocation());
        int y = Math.round(getCalibratedYLocation());

        // a soft glow while the monk wants you to ring it
        if (armed && !called) {
            float pulse = 0.25f + 0.15f * (float) Math.sin(tick * 0.08);
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
            g.setColor(new Color(255, 220, 140));
            g.fillOval(x + 24 * SCALE - 70, y + 30 * SCALE - 70, 140, 140);
            g.setComposite(old);
        }

        g.drawImage(frame, x, y, 48 * SCALE, 52 * SCALE, null);

        // the disc swings back and forth after a hit, less and less
        float t = ringTimer / (float) RING_TIME;
        int sway = Math.round((float) Math.sin(ringTimer * 0.35) * 10 * t);
        BufferedImage face = ringTimer > RING_TIME - 12 ? discLit : disc;
        g.drawImage(face, x + 9 * SCALE + sway, y + 15 * SCALE, 30 * SCALE, 30 * SCALE, null);

        // rings of sound going out
        if (ringTimer > 0) {
            Stroke oldStroke = g.getStroke();
            Composite old = g.getComposite();
            int cx = x + 24 * SCALE + sway, cy = y + 30 * SCALE;
            for (int i = 0; i < 3; i++) {
                float age = (RING_TIME - ringTimer) - i * 14;
                if (age < 0 || age > 70) continue;
                float r = 40 + age * 5;
                float a = Math.max(0f, 0.7f * (1 - age / 70f));
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
                g.setStroke(new BasicStroke(5));
                g.setColor(new Color(255, 230, 160));
                g.drawOval(Math.round(cx - r), Math.round(cy - r * 0.7f), Math.round(r * 2), Math.round(r * 1.4f));
            }
            g.setComposite(old);
            g.setStroke(oldStroke);
        }
    }
}
