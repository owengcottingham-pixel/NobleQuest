package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.ForestChunk;
import Enemies.GourdProjectile;
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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

// A wooden drawbridge, cut the rope to drop it down
public class Drawbridge extends EnhancedMapTile {
    private enum State { UP, FALLING, DOWN }

    private final float hingeX, hingeY, length;
    private final Point post;
    private final BufferedImage bridge, postImage;
    private State state = State.UP;
    private double angle = 90;         // degrees: 90 = standing straight up, 0 = lying flat across the gap
    private double angularSpeed = 0;
    private boolean wasAttacking = false;
    private int wobble = 0;
    private Point postRopeEnd, tipRopeEnd;      // where the rope goes instead (null = post to tip)
    private Runnable onDown;

    public Drawbridge(Point hinge, int lengthTiles, Point post) {
        super(hinge.x - lengthTiles * 48, hinge.y,
                new FrameBuilder(new BufferedImage(16 * lengthTiles, 4, BufferedImage.TYPE_INT_ARGB))
                        .withScale(3).withBounds(0, 0, 16 * lengthTiles, 4).build(),
                TileType.PASSABLE);
        this.hingeX = hinge.x;
        this.hingeY = hinge.y;
        this.length = lengthTiles * 48;
        this.post = post;
        bridge = ImageLoader.load("Drawbridge.png");
        postImage = ImageLoader.load("RopePost.png");
        initialize();
        setIsUpdateOffScreen(true);
    }

    public boolean isDown() {
        return state == State.DOWN;
    }

    // the rope from the post goes to postEnd, and a rope from the bridge's tip goes to tipEnd
    public Drawbridge ropes(Point postEnd, Point tipEnd) {
        this.postRopeEnd = postEnd;
        this.tipRopeEnd = tipEnd;
        return this;
    }

    public Drawbridge onDown(Runnable onDown) {
        this.onDown = onDown;
        return this;
    }

    public void drop() {
        if (state != State.UP) return;
        state = State.FALLING;
        if (post != null) {
            ForestChunk.burst(map, knotX(), knotY(), new int[] { ForestChunk.SEED, ForestChunk.STEM }, 8, 4f);
        }
        Effects.shake(2, 6);
    }

    // where the rope is tied on the post
    private float knotX() { return post.x + 30; }
    private float knotY() { return post.y + 36; }

    @Override
    public void update(Player player) {
        super.update(player);
        boolean attacking = player.isAttacking();
        if (state == State.UP && post != null && attacking && !wasAttacking && inSwordRange(player)) {
            drop();
        }
        wasAttacking = attacking;
        if (state == State.FALLING) {
            angularSpeed += 0.35;
            angle -= angularSpeed;
            if (angle <= 0) {
                angle = 0;
                state = State.DOWN;
                tileType = TileType.JUMP_THROUGH_PLATFORM;
                wobble = 12;
                Effects.shake(9, 16);
                for (int i = 0; i <= 4; i++) {
                    GourdProjectile.puff(map, hingeX - length * i / 4f, hingeY + 2, i < 2 ? -1f : 1f);
                }
                ForestChunk.burst(map, hingeX - length / 2f, hingeY, new int[] { ForestChunk.BARK, ForestChunk.STONE }, 14, 5f);
                if (onDown != null) onDown.run();
            }
        }
        if (wobble > 0) wobble--;
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
        Graphics2D g = graphicsHandler.getGraphics();
        if (post != null) {
            graphicsHandler.drawImage(postImage, Math.round(post.x) - camX, Math.round(post.y) - camY, 48, 144);
        }
        double rad = Math.toRadians(angle);
        float tipX = (float) (hingeX - Math.cos(rad) * length), tipY = (float) (hingeY - Math.sin(rad) * length);
        if (state == State.UP) {
            // the rope, pulled tight
            g.setColor(new Color(150, 140, 110));
            java.awt.Stroke old = g.getStroke();
            g.setStroke(new BasicStroke(3));
            if (post != null) {
                float ex = postRopeEnd != null ? postRopeEnd.x : tipX, ey = postRopeEnd != null ? postRopeEnd.y : tipY;
                g.drawLine(Math.round(knotX()) - camX, Math.round(knotY()) - camY, Math.round(ex) - camX, Math.round(ey) - camY);
            }
            if (tipRopeEnd != null) {
                g.drawLine(Math.round(tipX) - camX, Math.round(tipY) - camY, Math.round(tipRopeEnd.x) - camX, Math.round(tipRopeEnd.y) - camY);
            }
            g.setStroke(old);
        }
        // the bridge, turned about its hinge
        AffineTransform saved = g.getTransform();
        int bounce = wobble > 0 ? (wobble % 4 < 2 ? 2 : -1) : 0;
        g.translate(Math.round(hingeX) - camX, Math.round(hingeY) - camY + bounce);
        g.rotate(rad);
        g.drawImage(bridge, -Math.round(length), -6, Math.round(length), 30, null);
        g.setTransform(saved);
    }
}
