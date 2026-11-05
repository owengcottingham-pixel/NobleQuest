package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.Map;
import Level.MapEntity;
import Level.MapEntityStatus;
import Level.Player;
import Utils.Direction;
import Utils.Point;

import java.util.HashMap;
import java.util.Random;

// A little bit of particle that flies out when something gets smashed.

public class ForestChunk extends Enemy {
    public static final int ROCK = 0, MOSS_ROCK = 1, LEAF = 2, SEED = 3, THORN = 4, STEM = 5,
            STONE = 6, WISP = 7, BARK = 8, SPARK = 9;

    private static final Random random = new Random();
    private static final int MAX_ALIVE = 90;    // never let a big smash flood the map
    private static int alive = 0;

    private float velocityX, velocityY;
    private int life;
    private boolean counted = true;

    public ForestChunk(Point location, int kind, float velocityX, float velocityY) {
        super(location.x, location.y, new SpriteSheet(ImageLoader.load("ForestChunks.png"), 6, 6), "K" + kind);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.life = 35 + random.nextInt(25);
        initialize();
        setIsUpdateOffScreen(true);   // so it always times out
    }

    public static void burst(Map map, float x, float y, int[] kinds, int count, float power) {
        if (map == null) return;
        for (int i = 0; i < count && alive < MAX_ALIVE; i++) {
            float angle = (float) (Math.PI * (1.1 + 0.8 * random.nextFloat()));   // mostly upward
            float speed = power * (0.5f + random.nextFloat());
            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed - 1.5f;
            ForestChunk chunk = new ForestChunk(new Point(x - 6, y - 6), kinds[random.nextInt(kinds.length)], vx, vy);
            chunk.setMap(map);
            map.getEnemies().add(0, chunk);   // first in the list = drawn on top
            alive++;
        }
    }

    public static void resetCount() {
        alive = 0;
    }

    @Override
    public void update(Player player) {
        if (--life <= 0) {
            mapEntityStatus = MapEntityStatus.REMOVED;
            if (counted) { counted = false; alive = Math.max(0, alive - 1); }
            return;
        }
        velocityY = Math.min(velocityY + 0.35f, 9f);
        moveYHandleCollision(velocityY);
        moveXHandleCollision(velocityX);
        super.update(player);
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (hasCollided) velocityX = -velocityX * 0.5f;
    }

    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (hasCollided && direction == Direction.DOWN) {
            velocityY = -velocityY * 0.35f;   // little bounce
            velocityX *= 0.6f;
        } else if (hasCollided) {
            velocityY = 0;
        }
    }

    @Override
    public void touchedPlayer(Player player) {
        // harmless
    }

    @Override
    public boolean hitByBolt(Player player) {
        return false;
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (life < 12 && (life / 2) % 2 == 0) return;   // blink out
        super.draw(graphicsHandler);
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet s) {
        HashMap<String, Frame[]> a = new HashMap<>();
        for (int i = 0; i <= SPARK; i++) {
            a.put("K" + i, new Frame[] { new FrameBuilder(s.getSprite(0, i), 0).withScale(2).withBounds(1, 1, 4, 4).build() });
        }
        return a;
    }
}
