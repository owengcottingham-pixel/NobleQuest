package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.ForestChunk;
import Enemies.GourdProjectile;
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


public class SpikeCrusher extends EnhancedMapTile implements EditorObject {
    private static final int REST = 100, WARN = 26, DOWN = 30;
    private static final float RISE_SPEED = 5f, MAX_FALL = 22f;
    private static final int HEIGHT = 72;

    private enum State { REST, WARN, SLAM, DOWN, RISE }

    private final float restY, stopTop;
    private final int offset;
    private final BufferedImage link, block;
    private State state = State.REST;
    private int timer;
    private float fallSpeed = 0;

    public SpikeCrusher(Point location, float stopY, int offset) {
        super(location.x, location.y, new FrameBuilder(ImageLoader.load("Crusher.png")).withScale(3).withBounds(1, 0, 30, 24).build(),
                TileType.PASSABLE);
        restY = location.y;
        stopTop = stopY - HEIGHT;
        block = ImageLoader.load("Crusher.png");
        link = new SpriteSheet(ImageLoader.load("SpikeBar.png"), 16, 16).getSprite(0, 1);
        timer = REST;
        this.offset = offset;
        initialize();
        setIsUpdateOffScreen(true);
        for (int i = 0; i < offset; i++) step();
    }

    // placeable with the map editor
    @Override
    public String editorLine() {
        return "CRUSHER " + EditorObjects.tiles(getX()) + " " + EditorObjects.tiles(restY) + " "
                + EditorObjects.tiles(stopTop + HEIGHT) + " " + offset;
    }

    private void step() {
        switch (state) {
            case REST:
                if (--timer <= 0) { state = State.WARN; timer = WARN; }
                break;
            case WARN:
                if (--timer <= 0) { state = State.SLAM; fallSpeed = 4; }
                break;
            case SLAM:
                fallSpeed = Math.min(MAX_FALL, fallSpeed + 2f);
                setY(Math.min(stopTop, getY() + fallSpeed));
                if (getY() >= stopTop) {
                    state = State.DOWN;
                    timer = DOWN;
                    if (map != null) landed();
                }
                break;
            case DOWN:
                if (--timer <= 0) state = State.RISE;
                break;
            case RISE:
                setY(Math.max(restY, getY() - RISE_SPEED));
                if (getY() <= restY) { state = State.REST; timer = REST; }
                break;
        }
    }

    private void landed() {
        Rectangle b = getBounds();
        // shake the screen only if it's near you (it keeps slamming the whole time, wherever you are)
        float camX = map.getCamera().getX() + Engine.ScreenManager.getScreenWidth() / 2f;
        float camY = map.getCamera().getY() + Engine.ScreenManager.getScreenHeight() / 2f;
        float dist = (float) Math.hypot(b.getX1() + b.getWidth() / 2f - camX, b.getY1() - camY);
        if (dist < 900) {
            Effects.shake(6 * (1 - dist / 900f) + 1, 12);
        }
        GourdProjectile.puff(map, b.getX1() + 10, b.getY2(), -1.5f);
        GourdProjectile.puff(map, b.getX2() - 10, b.getY2(), 1.5f);
        ForestChunk.burst(map, b.getX1() + b.getWidth() / 2f, b.getY2(), new int[] { ForestChunk.STONE, ForestChunk.SPARK }, 8, 4f);
    }

    @Override
    public void update(Player player) {
        step();
        if (state == State.WARN && timer % 6 == 0) {
            Rectangle b = getBounds();
            ForestChunk.burst(map, b.getX1() + (float) Math.random() * b.getWidth(), b.getY1(), new int[] { ForestChunk.STONE }, 1, 1.5f);
        }
        super.update(player);
        if (state == State.REST || state == State.WARN || player.getHealth() <= 0) return;
        Rectangle b = getBounds(), p = player.getBounds();
        if (!(p.getX2() > b.getX1() + 6 && p.getX1() < b.getX2() - 6 && p.getY1() < b.getY2() && p.getY2() > b.getY1())) return;
        if (!player.isHurtInvincible()) {
            player.damage(1);
            ForestChunk.burst(map, p.getX1() + p.getWidth() / 2f, p.getY1(), new int[] { ForestChunk.STONE, ForestChunk.SPARK }, 7, 4f);
            Effects.shake(5, 10);
        }
        // shove him out the nearer side
        float pcx = p.getX1() + p.getWidth() / 2f, bcx = b.getX1() + b.getWidth() / 2f;
        float dx = pcx < bcx ? (b.getX1() + 6) - p.getX2() - 2 : (b.getX2() - 6) - p.getX1() + 2;
        player.moveXHandleCollision(dx);
        player.bounce(0);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        int x = Math.round(getCalibratedXLocation());
        int y = Math.round(getCalibratedYLocation());
        if (state == State.WARN) x += (timer % 4 < 2) ? -2 : 2;
        // the chain up into the slot
        int top = Math.round(restY - map.getCamera().getY());
        for (int cy = y - 22; cy > top - 20; cy -= 22) {
            graphicsHandler.drawImage(link, x + 48 - 16, cy - 8, 32, 32);
        }
        graphicsHandler.drawImage(block, x, y, 96, HEIGHT);
        if (state == State.WARN || state == State.SLAM) {
            graphicsHandler.drawFilledRectangle(x + 39, y + 18, 18, 12, new java.awt.Color(255, 90, 60, 160));
        }
    }
}
