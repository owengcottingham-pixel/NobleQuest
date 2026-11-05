package Enemies;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.Player;
import Utils.Effects;
import Utils.Point;

import java.util.HashMap;


public class ArcheryTarget extends Enemy {
    private final Runnable onHit;
    private java.util.function.BooleanSupplier shield;   // while this says so, bolts can't reach it
    private boolean hit = false;
    private int spin = 0;

    public ArcheryTarget(Point location, Runnable onHit) {
        super(location.x, location.y, new SpriteSheet(ImageLoader.load("Target.png"), 16, 16), "IDLE");
        this.onHit = onHit;
        initialize();
        setIsUpdateOffScreen(true);
    }

    // something in front of it shields it (eel)
    public ArcheryTarget shieldedWhile(java.util.function.BooleanSupplier shield) {
        this.shield = shield;
        return this;
    }

    public boolean isHit() {
        return hit;
    }

    // back to un-hit when a pair of targets that have to be hit close together
    public void reset() {
        if (!hit) return;
        hit = false;
        spin = 16;
        ForestChunk.burst(map, getX() + 24, getY() + 24, new int[] { ForestChunk.STONE }, 5, 2f);
    }

    @Override
    public boolean hitByBolt(Player player) {
        if (shield != null && shield.getAsBoolean()) {
            return false;
        }
        if (!hit) {
            hit = true;
            spin = 60;
            Effects.shake(3, 8);
            ForestChunk.burst(map, getX() + 24, getY() + 24, new int[] { ForestChunk.SPARK, ForestChunk.SEED }, 12, 4f);
            if (onHit != null) onHit.run();
        }
        return true;
    }

    @Override
    public void touchedPlayer(Player player) {
        // harmless
    }

    @Override
    public void update(Player player) {
        if (spin > 0) {
            spin--;
            currentAnimationName = (spin / 3) % 2 == 0 ? "HIT" : "IDLE";
        } else {
            currentAnimationName = hit ? "HIT" : "IDLE";
        }
        super.update(player);
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet s) {
        HashMap<String, Frame[]> a = new HashMap<>();
        a.put("IDLE", new Frame[] { new FrameBuilder(s.getSprite(0, 0), 0).withScale(3).withBounds(1, 1, 14, 14).build() });
        a.put("HIT", new Frame[] { new FrameBuilder(s.getSprite(0, 1), 0).withScale(3).withBounds(1, 1, 14, 14).build() });
        return a;
    }
}
