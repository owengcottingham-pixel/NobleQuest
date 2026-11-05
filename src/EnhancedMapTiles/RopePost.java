package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.ForestChunk;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.Direction;
import Utils.Effects;
import Utils.Point;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;


public class RopePost extends EnhancedMapTile {
    private final Point ropeEnd;
    private final Runnable onCut;
    private final BufferedImage image;
    private boolean cut = false, wasAttacking = false;
    private int snap = 0;

    public RopePost(Point location, Point ropeEnd, Runnable onCut) {
        super(location.x, location.y,
                new FrameBuilder(new BufferedImage(16, 48, BufferedImage.TYPE_INT_ARGB)).withScale(3).build(),
                TileType.PASSABLE);
        this.ropeEnd = ropeEnd;
        this.onCut = onCut;
        image = ImageLoader.load("RopePost.png");
        initialize();
        setIsUpdateOffScreen(true);
    }

    public boolean isCut() {
        return cut;
    }

    private float knotX() { return getX() + 30; }
    private float knotY() { return getY() + 36; }

    @Override
    public void update(Player player) {
        super.update(player);
        boolean attacking = player.isAttacking();
        if (!cut && attacking && !wasAttacking && inSwordRange(player)) {
            cut = true;
            snap = 14;
            ForestChunk.burst(map, knotX(), knotY(), new int[] { ForestChunk.SEED, ForestChunk.STEM }, 8, 4f);
            Effects.shake(3, 8);
            if (onCut != null) onCut.run();
        }
        wasAttacking = attacking;
        if (snap > 0) snap--;
    }

    private boolean inSwordRange(Player player) {
        float px = player.getBounds().getX1() + player.getBounds().getWidth() / 2f;
        float dx = knotX() - px;
        float dy = Math.abs(knotY() - (player.getBounds().getY1() + 30));
        if (dy > 110) return false;
        return player.getFacingDirection() == Direction.RIGHT ? (dx > -40 && dx < 120) : (dx < 40 && dx > -120);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        int camX = Math.round(map.getCamera().getX()), camY = Math.round(map.getCamera().getY());
        graphicsHandler.drawImage(image, Math.round(getX()) - camX, Math.round(getY()) - camY, 48, 144);
        Graphics2D g = graphicsHandler.getGraphics();
        Stroke old = g.getStroke();
        g.setColor(new Color(150, 140, 110));
        g.setStroke(new BasicStroke(3));
        int kx = Math.round(knotX()) - camX, ky = Math.round(knotY()) - camY;
        if (!cut) {
            g.drawLine(kx, ky, Math.round(ropeEnd.x) - camX, Math.round(ropeEnd.y) - camY);
        } else {
            // the cut end, whipping back and then hanging
            float t = snap / 14f;
            g.drawLine(kx, ky, kx + Math.round(14 + 20 * t), ky + Math.round(22 - 30 * t));
        }
        g.setStroke(old);
    }
}
