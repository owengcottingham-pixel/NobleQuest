package NPCs;

import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.SpriteSheet;
import Level.Camera;
import Level.Player;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

// The dragon the temple gong calls
public class SkyDragon {
    private static final int SCALE = 3;
    private static final int SEGMENTS = 14;
    private static final float SPACING = 34;
    private static final float SPEED = 9f;
    private static final int SEAT = 2;          // which body piece you sit on

    public enum Phase { AWAY, ARRIVING, WAITING, MOUNTING, LEAVING, GONE }

    private final BufferedImage headShut, headOpen, bodyA, bodyB, leg, tail;
    private final ArrayList<float[]> trail = new ArrayList<>();
    private Phase phase = Phase.AWAY;
    private float[][] path;
    private int pathIndex;
    private float hx, hy;          // where the head is on the path
    private float travelled = 0;
    private int tick = 0, phaseTick = 0;
    private float facing = -1;
    private float mountFromX, mountFromY;
    private boolean roar = false;

    // arrive/leave = the paths in map pixels, hover = where the head waits
    private final float[][] arrive, leave;

    public SkyDragon(float[][] arrive, float hoverX, float hoverY, float[][] leave) {
        SpriteSheet sheet = new SpriteSheet(ImageLoader.load("SkyDragon.png"), 56, 36);
        headShut = sheet.getSprite(0, 0);
        headOpen = sheet.getSprite(0, 1);
        bodyA = sheet.getSprite(1, 0).getSubimage(0, 0, 20, 20);
        bodyB = sheet.getSprite(1, 1).getSubimage(0, 0, 20, 20);
        leg = sheet.getSprite(1, 2).getSubimage(0, 0, 20, 20);
        tail = sheet.getSprite(1, 3).getSubimage(0, 0, 20, 20);
        this.arrive = smooth(append(arrive, new float[] { hoverX, hoverY }));
        this.leave = leave;
    }

    public Phase getPhase() {
        return phase;
    }

    public int getPhaseTick() {
        return phaseTick;
    }

    // the gong rang
    public void call() {
        if (phase != Phase.AWAY) return;
        setPhase(Phase.ARRIVING);
        path = arrive;
        pathIndex = 0;
        hx = path[0][0];
        hy = path[0][1];
        // the body starts strung out behind the head, off screen
        trail.clear();
        float dx = path[0][0] - path[1][0], dy = path[0][1] - path[1][1];
        float len = (float) Math.hypot(dx, dy);
        for (int i = (int) (SEGMENTS * SPACING) + 80; i > 0; i -= 4) {
            trail.add(new float[] { hx + dx / len * i, hy + dy / len * i });
        }
        trail.add(new float[] { hx, hy });
    }

    // the knight hops on
    public void ride(Player player) {
        if (phase != Phase.WAITING) return;
        player.setFrozen(true);
        mountFromX = player.getX();
        mountFromY = player.getY();
        setPhase(Phase.MOUNTING);
    }

    // where the knight's feet go
    public float[] seat() {
        float[] p = along(SEAT * SPACING);
        float[] bob = bob(SEAT);
        return new float[] { p[0] + bob[0], p[1] + bob[1] - 22 };
    }

    public boolean canRideFrom(Player player) {
        if (phase != Phase.WAITING) return false;
        float[] s = seat();
        float px = player.getBounds().getX1() + player.getBounds().getWidth() / 2f;
        return Math.abs(px - s[0]) < 240 && Math.abs(player.getBounds().getY2() - s[1]) < 140;
    }

    private void setPhase(Phase p) {
        phase = p;
        phaseTick = 0;
    }

    public void update(Player player) {
        tick++;
        phaseTick++;
        switch (phase) {
            case ARRIVING:
                if (followPath()) {
                    setPhase(Phase.WAITING);
                    facing = -1;
                }
                roar = phaseTick > 50 && phaseTick < 80;
                break;
            case WAITING:
                roar = (phaseTick % 240) < 26 && phaseTick > 30;
                break;
            case MOUNTING: {
                float t = Math.min(1f, phaseTick / 26f);
                float[] s = seat();
                float tx = s[0] - (player.getBounds().getX1() - player.getX()) - player.getBounds().getWidth() / 2f;
                float ty = s[1] - (player.getBounds().getY2() - player.getY());
                float hop = (float) Math.sin(t * Math.PI) * 80;
                player.setX(mountFromX + (tx - mountFromX) * t);
                player.setY(mountFromY + (ty - mountFromY) * t - hop);
                if (t >= 1f && phaseTick > 40) {
                    setPhase(Phase.LEAVING);
                    hx = headX();
                    hy = headY();
                    path = smooth(prepend(new float[] { hx, hy }, leave));
                    pathIndex = 0;
                    roar = true;
                }
                break;
            }
            case LEAVING:
                followPath();
                roar = phaseTick < 30;
                pinRider(player);
                if (pathIndex >= path.length - 1) {
                    setPhase(Phase.GONE);
                }
                break;
            case GONE:
                pinRider(player);
                break;
            default:
                break;
        }
    }

    private void pinRider(Player player) {
        float[] s = seat();
        player.setX(s[0] - (player.getBounds().getX1() - player.getX()) - player.getBounds().getWidth() / 2f);
        player.setY(s[1] - (player.getBounds().getY2() - player.getY()));
    }

    // moves the head along the path; true when it reaches the end
    private boolean followPath() {
        float left = SPEED;
        while (left > 0 && pathIndex < path.length - 1) {
            float[] next = path[pathIndex + 1];
            float dx = next[0] - hx, dy = next[1] - hy;
            float d = (float) Math.hypot(dx, dy);
            if (d <= left) {
                hx = next[0];
                hy = next[1];
                left -= d;
                pathIndex++;
            } else {
                hx += dx / d * left;
                hy += dy / d * left;
                left = 0;
            }
        }
        travelled += SPEED;
        float[] w = wiggled();
        float[] last = trail.get(trail.size() - 1);
        if (Math.hypot(w[0] - last[0], w[1] - last[1]) > 0.5) {
            if (Math.abs(w[0] - last[0]) > 0.3) facing = Math.signum(w[0] - last[0]);
            trail.add(w);
            if (trail.size() > 2000) trail.remove(0);
        }
        return pathIndex >= path.length - 1;
    }

    // the head swims side to side across its path, so the body snakes
    private float[] wiggled() {
        if (pathIndex >= path.length - 1) return new float[] { hx, hy };
        float[] next = path[pathIndex + 1];
        float dx = next[0] - hx, dy = next[1] - hy;
        float d = (float) Math.max(0.001, Math.hypot(dx, dy));
        float fade = Math.min(1f, Math.min(pathIndex, path.length - 1 - pathIndex) / 30f);
        float off = (float) Math.sin(travelled / 70f) * 18 * fade;
        return new float[] { hx - dy / d * off, hy + dx / d * off };
    }

    private float headX() {
        return trail.isEmpty() ? hx : trail.get(trail.size() - 1)[0];
    }

    private float headY() {
        return trail.isEmpty() ? hy : trail.get(trail.size() - 1)[1];
    }

    // a point 'dist' pixels back along the body from the head
    private float[] along(float dist) {
        float left = dist;
        float px = headX(), py = headY();
        for (int i = trail.size() - 1; i >= 0; i--) {
            float[] p = trail.get(i);
            float d = (float) Math.hypot(p[0] - px, p[1] - py);
            if (d >= left && d > 0) {
                float t = left / d;
                return new float[] { px + (p[0] - px) * t, py + (p[1] - py) * t };
            }
            left -= d;
            px = p[0];
            py = p[1];
        }
        return new float[] { px, py };
    }

    // while it waits it breathes: a slow wave that runs down the body
    private float[] bob(int segment) {
        if (phase != Phase.WAITING && phase != Phase.MOUNTING) return new float[] { 0, 0 };
        float fade = phase == Phase.WAITING ? Math.min(1f, phaseTick / 40f) : Math.max(0f, 1f - phaseTick / 26f);
        float t = tick - segment * 5;
        return new float[] { (float) Math.sin(t * 0.04) * 5 * fade, (float) Math.sin(t * 0.07) * 12 * fade };
    }

    public void draw(GraphicsHandler graphicsHandler, Camera camera) {
        if (phase == Phase.AWAY || trail.isEmpty()) return;
        Graphics2D g = graphicsHandler.getGraphics();
        float cx = camera.getX(), cy = camera.getY();
        int seg = 20 * SCALE;

        // tail first so the head ends up on top
        float[] t = along((SEGMENTS + 1) * SPACING);
        float[] tb = bob(SEGMENTS + 1);
        float[] before = along(SEGMENTS * SPACING);
        drawFacing(g, tail, t[0] + tb[0] - cx, t[1] + tb[1] - cy, seg, seg, before[0] > t[0] ? 1 : -1);
        for (int i = SEGMENTS; i >= 1; i--) {
            float[] p = along(i * SPACING);
            float[] b = bob(i);
            float x = p[0] + b[0] - cx, y = p[1] + b[1] - cy;
            if (i == 3 || i == 10) {
                g.drawImage(leg, Math.round(x - seg / 2f), Math.round(y - seg / 2f + 18), seg, seg, null);
            }
            BufferedImage img = i % 2 == 0 ? bodyA : bodyB;
            g.drawImage(img, Math.round(x - seg / 2f), Math.round(y - seg / 2f), seg, seg, null);
        }

        // head: turned toward where it is going, tipped up or down a little
        float[] neck = along(SPACING);
        float[] hb = bob(0);
        float angle = (float) Math.atan2(headY() - neck[1], Math.abs(headX() - neck[0]) + 0.001f);
        angle = Math.max(-0.6f, Math.min(0.6f, angle));
        BufferedImage head = roar ? headOpen : headShut;
        int hw = 56 * SCALE, hh = 36 * SCALE;
        AffineTransform old = g.getTransform();
        g.translate(headX() + hb[0] - cx, headY() + hb[1] - cy);
        if (facing < 0) {
            g.scale(-1, 1);
        }
        g.rotate(angle);
        // the neck joint is about (16, 18) in the head art
        g.drawImage(head, -16 * SCALE, -18 * SCALE, hw, hh, null);
        g.setTransform(old);
    }

    private static void drawFacing(Graphics2D g, BufferedImage img, float x, float y, int w, int h, float dir) {
        int ix = Math.round(x - w / 2f), iy = Math.round(y - h / 2f);
        if (dir < 0) {
            g.drawImage(img, ix + w, iy, -w, h, null);
        } else {
            g.drawImage(img, ix, iy, w, h, null);
        }
    }

    private static float[][] append(float[][] rest, float[] last) {
        float[][] out = new float[rest.length + 1][];
        System.arraycopy(rest, 0, out, 0, rest.length);
        out[rest.length] = last;
        return out;
    }

    private static float[][] prepend(float[] first, float[][] rest) {
        float[][] out = new float[rest.length + 1][];
        out[0] = first;
        System.arraycopy(rest, 0, out, 1, rest.length);
        return out;
    }

    // Catmull-Rom through the points, so the dragon curves instead of zig-zagging
    private static float[][] smooth(float[][] pts) {
        ArrayList<float[]> out = new ArrayList<>();
        for (int i = 0; i < pts.length - 1; i++) {
            float[] p0 = pts[Math.max(0, i - 1)], p1 = pts[i], p2 = pts[i + 1], p3 = pts[Math.min(pts.length - 1, i + 2)];
            for (int s = 0; s < 24; s++) {
                float t = s / 24f, t2 = t * t, t3 = t2 * t;
                float x = 0.5f * ((2 * p1[0]) + (-p0[0] + p2[0]) * t + (2 * p0[0] - 5 * p1[0] + 4 * p2[0] - p3[0]) * t2 + (-p0[0] + 3 * p1[0] - 3 * p2[0] + p3[0]) * t3);
                float y = 0.5f * ((2 * p1[1]) + (-p0[1] + p2[1]) * t + (2 * p0[1] - 5 * p1[1] + 4 * p2[1] - p3[1]) * t2 + (-p0[1] + 3 * p1[1] - 3 * p2[1] + p3[1]) * t3);
                out.add(new float[] { x, y });
            }
        }
        out.add(pts[pts.length - 1]);
        return out.toArray(new float[0][]);
    }
}
