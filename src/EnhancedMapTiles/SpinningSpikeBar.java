package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.ForestChunk;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Rectangle;
import GameObject.SpriteSheet;
import Level.EditorObject;
import Level.EditorObjects;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.Effects;
import Utils.Point;

import java.awt.image.BufferedImage;


public class SpinningSpikeBar extends EnhancedMapTile implements EditorObject {
    private static final float LINK_GAP = 26f, LINK_RADIUS = 9f, BALL_RADIUS = 22f;

    private final float hubX, hubY, armLength, speed;
    private final int arms;
    private double angle;
    private final float startAngle;
    private final BufferedImage ball, link, hub;

    public SpinningSpikeBar(Point hub, float armLength, int arms, float speed, float startAngle) {
        super(hub.x - 24, hub.y - 24,
                new FrameBuilder(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)).withScale(3).build(),
                TileType.PASSABLE);
        this.hubX = hub.x;
        this.hubY = hub.y;
        this.armLength = armLength;
        this.arms = arms;
        this.speed = speed;
        this.angle = startAngle;
        this.startAngle = startAngle;
        SpriteSheet sheet = new SpriteSheet(ImageLoader.load("SpikeBar.png"), 16, 16);
        ball = sheet.getSprite(0, 0);
        link = sheet.getSprite(0, 1);
        this.hub = sheet.getSprite(0, 2);
        initialize();
        setIsUpdateOffScreen(true);
    }

    // placeable with the map editor
    @Override
    public String editorLine() {
        return "SPIKE_BAR " + EditorObjects.tiles(hubX) + " " + EditorObjects.tiles(hubY) + " " + EditorObjects.tiles(armLength)
                + " " + arms + " " + EditorObjects.num(speed) + " " + EditorObjects.num(startAngle);
    }

    @Override
    public void update(Player player) {
        super.update(player);
        angle = (angle + speed) % 360;
        if (player.getHealth() <= 0) return;
        Rectangle p = player.getBounds();
        for (int a = 0; a < arms; a++) {
            double rad = Math.toRadians(angle + a * 360.0 / arms);
            float cos = (float) Math.cos(rad), sin = (float) -Math.sin(rad);
            for (float d = LINK_GAP; d <= armLength + 0.1f; d += LINK_GAP) {
                boolean end = d + LINK_GAP > armLength + 0.1f;
                float dist = end ? armLength : d;
                float r = end ? BALL_RADIUS : LINK_RADIUS;
                if (hits(p, hubX + cos * dist, hubY + sin * dist, r)) {
                    hurt(player, hubX + cos * dist, hubY + sin * dist);
                    return;
                }
            }
        }
    }

    private static boolean hits(Rectangle p, float x, float y, float r) {
        float nx = Math.max(p.getX1() + 4, Math.min(x, p.getX2() - 4));
        float ny = Math.max(p.getY1() + 4, Math.min(y, p.getY2() - 2));
        return (nx - x) * (nx - x) + (ny - y) * (ny - y) < r * r;
    }

    private void hurt(Player player, float x, float y) {
        if (player.isHurtInvincible()) return;
        player.damage(1);
        Rectangle p = player.getBounds();
        float pcx = p.getX1() + p.getWidth() / 2f;
        player.moveXHandleCollision(pcx < hubX ? -36 : 36);
        player.bounce(9f);
        ForestChunk.burst(map, x, y, new int[] { ForestChunk.SPARK, ForestChunk.STONE }, 8, 4f);
        Effects.shake(5, 10);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        float camX = map.getCamera().getX(), camY = map.getCamera().getY();
        float sx = hubX - camX, sy = hubY - camY;
        for (int a = 0; a < arms; a++) {
            double rad = Math.toRadians(angle + a * 360.0 / arms);
            float cos = (float) Math.cos(rad), sin = (float) -Math.sin(rad);
            for (float d = LINK_GAP; d < armLength - LINK_GAP * 0.5f; d += LINK_GAP) {
                graphicsHandler.drawImage(link, Math.round(sx + cos * d) - 16, Math.round(sy + sin * d) - 16, 32, 32);
            }
            graphicsHandler.drawImage(ball, Math.round(sx + cos * armLength) - 24, Math.round(sy + sin * armLength) - 24, 48, 48);
        }
        graphicsHandler.drawImage(hub, Math.round(sx) - 24, Math.round(sy) - 24, 48, 48);
    }
}
