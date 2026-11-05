package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.ForestChunk;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.Effects;
import Utils.Point;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

// A long log held down under the water by a rope
// pretty much just drawbridge just horizontal
public class SunkenLog extends EnhancedMapTile {
    private final float floatY, sunkY, length;
    private final BufferedImage planks;
    private boolean released = false, floating = false;
    private float speed = 0;
    private int tick = 0, wobble = 0;

    public SunkenLog(Point waterTile, int lengthTiles) {
        super(waterTile.x, waterTile.y - 18,
                new FrameBuilder(new BufferedImage(16 * lengthTiles, 8, BufferedImage.TYPE_INT_ARGB))
                        .withScale(3).withBounds(0, 0, 16 * lengthTiles, 4).build(),
                TileType.PASSABLE);
        floatY = waterTile.y - 18;          // floats with its top a little above the surface
        sunkY = waterTile.y + 80;
        length = lengthTiles * 48;
        planks = ImageLoader.load("Drawbridge.png");
        setY(sunkY);
        initialize();
        setIsUpdateOffScreen(true);
    }

    public void release() {
        released = true;
    }

    public boolean isFloating() {
        return floating;
    }

    @Override
    public void update(Player player) {
        super.update(player);
        tick++;
        if (released && !floating) {
            speed = Math.min(5f, speed + 0.25f);
            setY(Math.max(floatY, getY() - speed));
            if (getY() <= floatY) {
                floating = true;
                tileType = TileType.JUMP_THROUGH_PLATFORM;
                wobble = 30;
                Effects.shake(5, 12);
                for (int i = 0; i <= 4; i++) {
                    ForestChunk.burst(map, getX() + length * i / 4f, floatY + 10, new int[] { ForestChunk.WISP }, 3, 3f);
                }
            }
        }
        if (wobble > 0) wobble--;
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        int x = Math.round(getCalibratedXLocation());
        int y = Math.round(getCalibratedYLocation());
        Graphics2D g = graphicsHandler.getGraphics();
        if (!floating) {
            // under the water: dim, with its rope going down into the dark
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, released ? 0.7f : 0.45f));
            g.drawImage(planks, x, y, Math.round(length), 30, null);
            g.setComposite(old);
            if (!released) {
                Stroke s = g.getStroke();
                g.setColor(new Color(120, 120, 100, 150));
                g.setStroke(new BasicStroke(3));
                int mx = x + Math.round(length / 2);
                g.drawLine(mx, y + 20, mx - 20, y + 200);
                g.setStroke(s);
            }
            return;
        }
        int bob = wobble > 0 ? Math.round((float) Math.sin(wobble * 0.6) * wobble / 6f) : Math.round((float) Math.sin(tick * 0.05) * 1.5f);
        g.drawImage(planks, x, y + bob, Math.round(length), 30, null);
    }
}
