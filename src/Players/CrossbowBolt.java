package Players;

import Engine.GraphicsHandler;
import Engine.ImageLoader;
import EnhancedMapTiles.Coin;
import EnhancedMapTiles.LavaBossPlatform;
import GameObject.Rectangle;
import Level.Enemy;
import Level.EnhancedMapTile;
import Level.Map;
import Level.MapEntityStatus;
import Level.MapTile;
import Level.NPC;
import Level.Player;
import Level.TileType;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

public class CrossbowBolt {
    private static final BufferedImage IMAGE = ImageLoader.load("CrossbowBolt.png");
    private static final int PICKUP_RADIUS = 8;

    private float x, y;                           // the tip of the bolt
    private final float velocityX, velocityY;
    private final double angle;
    private boolean stuck = false;
    private boolean done = false;

    public enum PickupResult {
        NONE, RECOVERED, BROKEN
    }

    public CrossbowBolt(float x, float y, float velocityX, float velocityY) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.angle = Math.atan2(velocityY, velocityX);
    }

    public boolean isDone() {
        return done;
    }

    public void update(Map map, Player shooter) {
        if (stuck) {
            return;
        }

        // Check each pixel so fast bolts cannot tunnel through thin platform art.
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(velocityX), Math.abs(velocityY))));
        for (int i = 0; i < steps; i++) {
            float nextX = x + velocityX / steps;
            float nextY = y + velocityY / steps;

            if (map.getTileByPosition(nextX, nextY) == null) {
                stuck = true;
                return;
            }
            x = nextX;
            y = nextY;

            for (Enemy enemy : map.getActiveEnemies()) {
                if (enemy.getMapEntityStatus() != MapEntityStatus.REMOVED && contains(enemy.getBounds())) {
                    enemy.hitByBoltAt(shooter, x, y);
                    done = true;
                    return;
                }
            }
            if (hitsSurface(map)) {
                stuck = true;
                return;
            }
            for (NPC npc : map.getNPCs()) {
                if (npc.getMapEntityStatus() != MapEntityStatus.REMOVED && contains(npc.getBounds())) {
                    stuck = true;
                    return;
                }
            }
        }
    }

    public boolean isStuck() {
        return stuck;
    }

    public PickupResult tryPickup(Player player) {
        if (!stuck || !new Rectangle(x - PICKUP_RADIUS, y - PICKUP_RADIUS,
                PICKUP_RADIUS * 2, PICKUP_RADIUS * 2).intersects(player.getBounds())) {
            return PickupResult.NONE;
        }

        done = true;
        return ThreadLocalRandom.current().nextInt(100) < 15
                ? PickupResult.BROKEN
                : PickupResult.RECOVERED;
    }

    private boolean hitsSurface(Map map) {
        MapTile tile = map.getTileByPosition(x, y);
        if (tile.getTileType() == TileType.NOT_PASSABLE) {
            return true;
        }
        for (EnhancedMapTile enhancedMapTile : map.getEnhancedMapTiles()) {
            if (enhancedMapTile.getMapEntityStatus() == MapEntityStatus.REMOVED) {
                continue;
            }
            if (enhancedMapTile instanceof Coin) {
                continue;
            }
            if (enhancedMapTile instanceof LavaBossPlatform) {
                if (((LavaBossPlatform) enhancedMapTile).containsVisiblePixel(x, y)) {
                    return true;
                }
                continue;
            }
            if (contains(enhancedMapTile.getBounds())) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(Rectangle r) {
        return x >= r.getX1() && x <= r.getX2() && y >= r.getY1() && y <= r.getY2();
    }

    public void draw(GraphicsHandler graphicsHandler, Map map) {
        Graphics2D g = graphicsHandler.getGraphics();
        AffineTransform before = g.getTransform();
        g.translate(Math.round(x - map.getCamera().getX()), Math.round(y - map.getCamera().getY()));
        g.rotate(angle);
        g.drawImage(IMAGE, -IMAGE.getWidth(), -IMAGE.getHeight() / 2, null);
        g.setTransform(before);
    }
}