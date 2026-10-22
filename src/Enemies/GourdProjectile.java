package Enemies;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.Map;
import Level.MapEntity;
import Level.MapEntityStatus;
import Level.Player;
import Utils.Direction;
import Utils.Point;

import java.util.HashMap;

// Everything Gourdzilla throws at you, drawn from GourdzillaFX.png
//   SEED           - arcs through the air, pops on the ground
//   FLAMING_SEED   - (enraged) same, but leaves a FIRE patch where it lands
//   SHOCKWAVE      - rolls along the floor from a jump slam; jump over it
//   FIRE           - burns on the floor for a few seconds
//   GROUND_WARNING - a glowing crack, harmless. When its time runs out ROOT_SPIKES burst out of it
//   ROOT_SPIKES    - shoot up, stay up for a bit, sink back down
//   DUST           - a puff of dust, just for looks
public class GourdProjectile extends Enemy {
    public enum Kind { SEED, FLAMING_SEED, SHOCKWAVE, FIRE, GROUND_WARNING, ROOT_SPIKES, DUST }

    private static final int SINK_TIME = 6;
    public static final float SEED_GRAVITY = 0.35f;

    private final Kind kind;
    private float velocityX, velocityY;
    private int life;
    private int spikeTime;         
    private boolean erupted;       
    private boolean flipped;

    public GourdProjectile(Point location, Kind kind, float velocityX, float velocityY) {
        this(location, kind, velocityX, velocityY, defaultLife(kind));
    }

    public GourdProjectile(Point location, Kind kind, float velocityX, float velocityY, int life) {
        super(location.x, location.y, new SpriteSheet(ImageLoader.load("GourdzillaFX.png"), 16, 16), kind.name() + (velocityX > 0 ? "_RIGHT" : "_LEFT"));
        this.kind = kind;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.life = life;
        this.flipped = velocityX > 0;
        initialize();
        setIsUpdateOffScreen(true);   // removes itself when its life runs out
    }

    private static int defaultLife(Kind kind) {
        switch (kind) {
            case FIRE: return 150;
            case SHOCKWAVE: return 75;
            case DUST: return 16;
            default: return 240;
        }
    }

    // a glowing crack in the floor: after warnTime frames roots burst out of it and stay up for spikeTime frames
    // location = top left of the 48x48 cell, so its bottom edge sits on the floor
    public static GourdProjectile rootCrack(Point location, int warnTime, int spikeTime, boolean flipped) {
        GourdProjectile crack = new GourdProjectile(location, Kind.GROUND_WARNING, 0, 0, warnTime);
        crack.spikeTime = spikeTime;
        crack.setFlipped(flipped);
        return crack;
    }

    // a dust puff whose bottom center is at (x, groundY), drifting sideways a little
    public static void puff(Map map, float x, float groundY, float driftX) {
        addOnTop(map, new GourdProjectile(new Point(x - 24, groundY - 42), Kind.DUST, driftX, -0.4f));
    }

    // the camera draws the enemy list back to front, so whatever is FIRST in the list is drawn on top.
    // dust and pumpkinlings go there so the boss's big body doesn't cover them
    public static void addOnTop(Map map, Enemy enemy) {
        enemy.setMap(map);
        map.getEnemies().add(0, enemy);
    }

    private void setFlipped(boolean flipped) {
        this.flipped = flipped;
        currentAnimationName = kind.name() + (flipped ? "_RIGHT" : "_LEFT");
    }

    // the boss died: poof, gone
    public void vanish() {
        if (mapEntityStatus == MapEntityStatus.REMOVED || kind == Kind.DUST) return;
        mapEntityStatus = MapEntityStatus.REMOVED;
        puff(map, getBounds().getX1() + getBounds().getWidth() / 2f, getBounds().getY2() + 1, 0);
    }

    @Override
    public void update(Player player) {
        if (--life <= 0) {
            if (kind == Kind.GROUND_WARNING && !erupted) {
                erupt();
            } else {
                mapEntityStatus = MapEntityStatus.REMOVED;
            }
            return;
        }
        switch (kind) {
            case SEED:
            case FLAMING_SEED:
                velocityY = Math.min(velocityY + SEED_GRAVITY, 14f);
                moveYHandleCollision(velocityY);
                moveXHandleCollision(velocityX);
                break;
            case SHOCKWAVE:
                moveXHandleCollision(velocityX);
                break;
            case DUST:
                moveX(velocityX);                 // no collision, it's just smoke
                moveY(velocityY);
                velocityX *= 0.9f;
                break;
            case ROOT_SPIKES:
                if (life == SINK_TIME) currentAnimationName = "ROOT_SINK" + (flipped ? "_RIGHT" : "_LEFT");
                break;
            default:
                break;
        }
        super.update(player);
    }

    // GROUND_WARNING -> roots! The crack stays, glowing under them, until they sink back down
    private void erupt() {
        erupted = true;
        life = spikeTime + SINK_TIME;
        GourdProjectile roots = new GourdProjectile(new Point(getX(), getY()), Kind.ROOT_SPIKES, 0, 0, spikeTime + SINK_TIME);
        roots.setFlipped(flipped);
        roots.setMap(map);
        map.getEnemies().add(map.getEnemies().indexOf(this), roots);   // right in front of the crack, so they're drawn over it
        map.addEnemy(new GourdProjectile(new Point(getX(), getY() + 6), Kind.DUST, flipped ? 0.5f : -0.5f, -0.4f));   // dirt kicked up behind them
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (hasCollided && kind != Kind.FIRE) {
            mapEntityStatus = MapEntityStatus.REMOVED;
        }
    }

    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (hasCollided && direction == Direction.DOWN && (kind == Kind.SEED || kind == Kind.FLAMING_SEED)) {
            if (kind == Kind.FLAMING_SEED) {
                map.addEnemy(new GourdProjectile(new Point(getX(), getY() - 15), Kind.FIRE, 0, 0));
            } else { puff(map, getX() + 24, getY() + 33, 0); }
            mapEntityStatus = MapEntityStatus.REMOVED;
        } 
    }

    @Override
    public void touchedPlayer(Player player) {
        if (kind == Kind.DUST || kind == Kind.GROUND_WARNING) return;        // harmless
        if (kind == Kind.ROOT_SPIKES && life <= SINK_TIME) return;          // already sinking
        player.damage(1);
        if (kind == Kind.SEED || kind == Kind.FLAMING_SEED) {
            mapEntityStatus = MapEntityStatus.REMOVED;
        }
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet s) {
        HashMap<String, Frame[]> a = new HashMap<>();
        //            name              cells (boss_fx.json)    delays               hitbox inside the 16x16 cell
        add(a, s, "SEED",           new int[] { 0, 1 },   new int[] { 6, 6 },  5, 5, 6, 6);
        add(a, s, "FLAMING_SEED",   new int[] { 2, 3 },   new int[] { 6, 6 },  5, 5, 7, 6);
        add(a, s, "SHOCKWAVE",      new int[] { 10, 11 }, new int[] { 6, 6 },  1, 7, 14, 9);
        add(a, s, "FIRE",           new int[] { 12, 13 }, new int[] { 6, 6 },  2, 8, 12, 8);
        add(a, s, "GROUND_WARNING", new int[] { 6, 7 },   new int[] { 6, 6 },  0, 9, 16, 7);
        add(a, s, "ROOT_SPIKES",    new int[] { 8, 9 },   new int[] { 4, 0 },  3, 3, 11, 13);   // sprout, then hold full height
        add(a, s, "ROOT_SINK",      new int[] { 8 },      new int[] { 0 },     3, 9, 11, 7);
        add(a, s, "DUST",           new int[] { 14, 15 }, new int[] { 8, 8 },  2, 6, 11, 8);
        return a;
    }

    private void add(HashMap<String, Frame[]> a, SpriteSheet s, String name, int[] cells, int[] delays, int bx, int by, int bw, int bh) {
        for (String side : new String[] { "LEFT", "RIGHT" }) {
            Frame[] f = new Frame[cells.length];
            for (int i = 0; i < cells.length; i++) {
                FrameBuilder b = new FrameBuilder(s.getSprite(0, cells[i]), delays[i]).withScale(3).withBounds(bx, by, bw, bh);
                if (side.equals("RIGHT")) b.withImageEffect(ImageEffect.FLIP_HORIZONTAL);   // FX art faces left
                f[i] = b.build();
            }
            a.put(name + "_" + side, f);
        }
    }
}
