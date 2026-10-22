package Players;

import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Rectangle;
import Level.Enemy;
import Level.EnhancedMapTile;
import Level.Map;
import Level.MapEntityStatus;
import Level.MapTile;
import Level.Player;
import Level.TileType;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class CrossbowBolt {
    private static final BufferedImage IMAGE = ImageLoader.load("CrossbowBolt.png");
    private static final int MAX_LIFE = 50;       // range: 50 frames x 16px = 800px
    private static final int STUCK_TIME = 40;     // how long it stays stuck in a wall

    private float x, y;                           // the tip of the bolt
    private final float velocityX, velocityY;
    private final double angle;
    private int life = MAX_LIFE;
    private int stuckTimer = 0;
    private boolean done = false;

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
        if (stuckTimer > 0) {
            if (--stuckTimer == 0) done = true;
            return;
        }
        if (--life <= 0) {
            done = true;
            return;
        }

        // move in small steps so a fast bolt can't skip over a thin wall or a small enemy
        int steps = (int) Math.ceil(Math.max(Math.abs(velocityX), Math.abs(velocityY)) / 6f);
        for (int i = 0; i < steps; i++) {
            x += velocityX / steps;
            y += velocityY / steps;

            if (map.getTileByPosition(x, y) == null) {
                done = true;
                return;
            }
            if (hitsWall(map)) {
                x += velocityX * 0.3f;            // sink the tip into the wall a little
                y += velocityY * 0.3f;
                stuckTimer = STUCK_TIME;
                return;
            }
            for (Enemy enemy : map.getActiveEnemies()) {
                if (enemy.getMapEntityStatus() != MapEntityStatus.REMOVED && contains(enemy.getBounds())
                        && enemy.hitByBolt(shooter)) {
                    done = true;
                    return;
                }
            }
        }
    }

    private boolean hitsWall(Map map) {
        MapTile tile = map.getTileByPosition(x, y);
        if (tile.getTileType() == TileType.NOT_PASSABLE) {
            return true;
        }
        for (EnhancedMapTile enhancedMapTile : map.getActiveEnhancedMapTiles()) {
            if (enhancedMapTile.getTileType() == TileType.NOT_PASSABLE && contains(enhancedMapTile.getBounds())) {
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