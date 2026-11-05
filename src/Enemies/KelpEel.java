package Enemies;

import Builders.FrameBuilder;
import EnhancedMapTiles.Coin;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.Rectangle;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.MapEntityStatus;
import Level.Player;
import Utils.Direction;
import Utils.Effects;
import Utils.Point;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;


public class KelpEel extends Enemy {
    private enum State { HIDDEN, EYES, LUNGE, OUT, RETRACT, BEATEN }

    private static final int HEAD_W = 96, HEAD_H = 48;
    private static final int WAIT = 70, EYES_TIME = 50, EYES_LOCK = 18, OUT_TIME = 34, HIDE_AFTER_HIT = 220;
    private static final float LUNGE_SPEED = 16f, RETRACT_SPEED = 5f, FLINCH_SPEED = 18f;

    private final float lairX, minY, maxY, reach;
    private final int dir;                 // +1 lunges right, -1 lunges left
    private final BufferedImage headShut, headOpen, body, eyes;
    private State state = State.HIDDEN;
    private float laneY, extend = 0;
    private int timer = WAIT, tick = 0, health = 3, flash = 0;
    private boolean flinching = false, wasAttacking = false;

    public KelpEel(Point lair, Direction out, float reach, float minY, float maxY) {
        super(lair.x, lair.y - HEAD_H / 2f, new SpriteSheet(ImageLoader.load("KelpEel.png"), 32, 16), "HEAD");
        this.lairX = lair.x;
        this.dir = out == Direction.RIGHT ? 1 : -1;
        this.reach = reach;
        this.minY = minY;
        this.maxY = maxY;
        this.laneY = Math.max(minY, Math.min(maxY, lair.y));
        SpriteSheet s = new SpriteSheet(ImageLoader.load("KelpEel.png"), 32, 16);
        headShut = s.getSprite(0, 0);
        headOpen = s.getSprite(0, 1);
        body = s.getSprite(0, 2);
        eyes = s.getSprite(0, 3);
        initialize();
        setIsUpdateOffScreen(true);
        placeHead();
    }

    public boolean wasHit() {
        return health < 3;
    }

    private float headX() {
        return dir > 0 ? lairX - HEAD_W + extend : lairX - extend;
    }

    private void placeHead() {
        setX(headX());
        setY(laneY - HEAD_H / 2f);
    }

    private boolean out() {
        return state == State.LUNGE || state == State.OUT || state == State.RETRACT;
    }

    @Override
    public void update(Player player) {
        if (state == State.BEATEN) return;
        tick++;
        if (flash > 0) flash--;
        Rectangle p = player.getBounds();
        float pcx = p.getX1() + p.getWidth() / 2f, pcy = p.getY1() + p.getHeight() / 2f;
        boolean inZone = (dir > 0 ? pcx > lairX - 40 && pcx < lairX + reach + 150 : pcx < lairX + 40 && pcx > lairX - reach - 150)
                && pcy > minY - 220 && pcy < maxY + 220;

        switch (state) {
            case HIDDEN:
                if (timer > 0) timer--;
                if (timer == 0 && inZone && player.getHealth() > 0) {
                    state = State.EYES;
                    timer = EYES_TIME;
                }
                break;
            case EYES:
                // the eyes slide to line up with you, then hold still for a moment before it goes
                if (timer > EYES_LOCK) {
                    float want = Math.max(minY, Math.min(maxY, pcy));
                    laneY += Math.max(-4f, Math.min(4f, want - laneY));
                }
                if (--timer <= 0) state = State.LUNGE;
                break;
            case LUNGE:
                extend = Math.min(reach, extend + LUNGE_SPEED);
                if (extend >= reach) { state = State.OUT; timer = OUT_TIME; }
                break;
            case OUT:
                if (--timer <= 0) state = State.RETRACT;
                break;
            case RETRACT:
                extend = Math.max(0, extend - (flinching ? FLINCH_SPEED : RETRACT_SPEED));
                if (extend <= 0) {
                    state = State.HIDDEN;
                    timer = flinching ? HIDE_AFTER_HIT : WAIT + (tick % 3) * 25;
                    flinching = false;
                }
                break;
            default:
                break;
        }
        placeHead();

        if (out()) {
            boolean attacking = player.isAttacking();
            if (attacking && !wasAttacking && inSwordRange(player)) {
                hit();
            }
            wasAttacking = attacking;
            if (state != State.BEATEN && !flinching && player.getHealth() > 0 && !player.isHurtInvincible() && touches(p)) {
                player.damage(1);
                player.moveXHandleCollision(dir * 44);       // shoved along the way it's lunging
                player.bounce(8f);
                Effects.shake(5, 10);
                ForestChunk.burst(map, pcx, pcy, new int[] { ForestChunk.WISP }, 6, 3f);
            }
        } else {
            wasAttacking = player.isAttacking();
        }
        super.update(player);
    }

    // the head and the body between it and the kelp
    private boolean touches(Rectangle p) {
        float hx = headX();
        float x1 = dir > 0 ? lairX : hx + 10, x2 = dir > 0 ? hx + HEAD_W - 10 : lairX;
        float y1 = laneY - HEAD_H / 2f + 10, y2 = laneY + HEAD_H / 2f - 10;
        return p.getX2() > x1 && p.getX1() < x2 && p.getY2() > y1 && p.getY1() < y2;
    }

    private boolean inSwordRange(Player player) {
        Rectangle p = player.getBounds();
        float hx = headX() + HEAD_W / 2f;
        float dx = hx - (p.getX1() + p.getWidth() / 2f);
        if (laneY + HEAD_H / 2f < p.getY1() - 20 || laneY - HEAD_H / 2f > p.getY2() + 20) return false;
        return player.getFacingDirection() == Direction.RIGHT ? (dx > -30 && dx < 120) : (dx < 30 && dx > -120);
    }

    @Override
    public boolean hitByBolt(Player player) {
        if (!out() || flinching) return false;
        hit();
        return true;
    }

    // A bolt in the head hurts it; a bolt anywhere else along its body just shields whatever's behind it
    @Override
    public boolean hitByBoltAt(Player player, float x, float y) {
        if (!out() || flinching) return false;
        float hx = headX();
        if (x >= hx && x <= hx + HEAD_W) {
            hit();
        } else {
            ForestChunk.burst(map, x, y, new int[] { ForestChunk.WISP }, 3, 2f);
        }
        return true;
    }

    @Override
    public Rectangle getBounds() {
        if (!out()) return super.getBounds();
        float hx = headX();
        float x1 = dir > 0 ? lairX : hx, x2 = dir > 0 ? hx + HEAD_W : lairX;
        return new Rectangle(x1, laneY - HEAD_H / 2f + 8, Math.round(x2 - x1), HEAD_H - 16);
    }

    public boolean covers(Rectangle r) {
        return out() && !flinching && getBounds().intersects(r);
    }

    private void hit() {
        if (flinching) return;
        health--;
        flash = 14;
        float hx = headX() + HEAD_W / 2f;
        ForestChunk.burst(map, hx, laneY, new int[] { ForestChunk.WISP, ForestChunk.SPARK }, 10, 4f);
        Effects.shake(4, 10);
        if (health <= 0) {
            state = State.BEATEN;
            Effects.banner(Effects.BannerStyle.HINT, "The eel slinks off into the kelp!", null, 120);
            for (int i = 0; i < 3; i++) {
                map.addEnhancedMapTile(new Coin(hx - 40 + i * 36, laneY - 20));
            }
            mapEntityStatus = MapEntityStatus.REMOVED;
            return;
        }
        flinching = true;
        state = State.RETRACT;
    }

    @Override
    public void touchedPlayer(Player player) {
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {

    }

    public void drawEel(GraphicsHandler graphicsHandler) {
        if (state == State.BEATEN) return;
        Graphics2D g = graphicsHandler.getGraphics();
        float camX = map.getCamera().getX(), camY = map.getCamera().getY();
        int ly = Math.round(laneY - camY);
        if (state == State.EYES || (state == State.HIDDEN && timer < 25 && timer > 0 && false)) {
            // glowing eyes, just inside the kelp (they blink open)
            float open = Math.min(1f, (EYES_TIME - timer) / 8f);
            int ex = Math.round(lairX - camX) - (dir > 0 ? 70 : 26);
            int eh = Math.max(2, Math.round(48 * open));
            drawFlipped(g, eyes, ex, ly - eh / 2, 96, eh, false);
            return;
        }
        if (!out()) return;
        // the body: segments from the kelp out to the head, with a wave running along it
        float hx = headX();
        float bodyFrom = dir > 0 ? lairX : hx + HEAD_W - 6, bodyTo = dir > 0 ? hx + 6 : lairX;
        for (float x = bodyFrom; x < bodyTo; x += 90) {
            float w = Math.min(96, bodyTo - x);
            float along = dir > 0 ? (x - lairX) : (lairX - x);
            int wave = Math.round((float) Math.sin(tick * 0.35 - along * 0.02) * 6 * Math.min(1f, along / 150f));
            BufferedImage seg = w >= 96 ? body : body.getSubimage(0, 0, Math.max(1, Math.round(w / 3)), 16);
            drawFlipped(g, seg, Math.round(x - camX), ly - HEAD_H / 2 + wave, Math.round(w), HEAD_H, dir < 0);
        }
        boolean open = state == State.LUNGE || (state == State.OUT && (tick / 6) % 2 == 0);
        BufferedImage head = open ? headOpen : headShut;
        if (flash > 0 && (flash / 2) % 2 == 0) head = headShut;
        int hxs = Math.round(hx - camX);
        drawFlipped(g, head, hxs, ly - HEAD_H / 2, HEAD_W, HEAD_H, dir < 0);
        if (flash > 0 && (flash / 2) % 2 == 1) {
            g.setColor(new java.awt.Color(255, 255, 255, 120));
            g.fillRect(hxs + 6, ly - HEAD_H / 2 + 6, HEAD_W - 12, HEAD_H - 12);
        }
    }

    // drawn upside down (it's the right way up once the reflection world is flipped), and mirrored
    // left-right when it faces left
    private static void drawFlipped(Graphics2D g, BufferedImage img, int x, int y, int w, int h, boolean mirror) {
        g.drawImage(img, mirror ? x + w : x, y + h, mirror ? x : x + w, y, 0, 0, img.getWidth(), img.getHeight(), null);
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet s) {
        HashMap<String, Frame[]> a = new HashMap<>();
        a.put("HEAD", new Frame[] { new FrameBuilder(s.getSprite(0, 0), 0).withScale(3).withBounds(4, 3, 26, 11).build() });
        return a;
    }
}
