package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.GourdProjectile;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.SpriteSheet;
import Level.EditorObject;
import Level.EditorObjects;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.AirGroundState;
import Utils.Point;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;


public class SinkingPillar extends EnhancedMapTile implements EditorObject {
    private enum State { UP, SHAKING, SINKING, DOWN, RISING }

    private static final float FORCE = 15f;          // the same small hop as a LOW spring
    private static final int SHAKE_TIME = 14, DOWN_TIME = 80;
    private static final float SINK_SPEED = 4.5f, RISE_SPEED = 2.5f;

    private final float topY, floorY, bottomY;
    private final BufferedImage capL, capR, coil, slateL, slateR, slateTopL, slateTopR;
    private State state = State.UP;
    private int timer = 0, squash = 0;

    public SinkingPillar(Point location, float floorY) {
        super(location.x, location.y,
                new FrameBuilder(new BufferedImage(32, 16, BufferedImage.TYPE_INT_ARGB)).withScale(3).withBounds(0, 6, 32, 4).build(),
                TileType.JUMP_THROUGH_PLATFORM);
        topY = location.y;
        this.floorY = floorY;
        bottomY = floorY + 24;                       // the spring ends up hidden below the spikes
        SpriteSheet springs = new SpriteSheet(ImageLoader.load("SpringPad.png"), 16, 16);
        capL = springs.getSprite(0, 0);
        capR = springs.getSprite(0, 2);
        coil = springs.getSprite(0, 3);
        SpriteSheet day = new SpriteSheet(ImageLoader.load("DaylightTileset.png"), 16, 16);
        slateL = day.getSprite(2, 4);      // (8 tiles to a row: 20 = slate face_l, 23 = face_r,
        slateR = day.getSprite(2, 7);      //  49 / 50 = the smooth slate top_l / top_r)
        slateTopL = day.getSprite(6, 1);
        slateTopR = day.getSprite(6, 2);
        initialize();
        setIsUpdateOffScreen(true);
    }

    // placeable with the map editor: SINKING_PILLAR x y floorRow (see Level.EditorObjects)
    @Override
    public String editorLine() {
        return "SINKING_PILLAR " + EditorObjects.tiles(getX()) + " " + EditorObjects.tiles(topY) + " " + EditorObjects.tiles(floorY);
    }

    @Override
    public void update(Player player) {
        boolean standing = state != State.DOWN && touching(player)
                && Math.abs((player.getBounds().getY2() + 1) - getBounds().getY1()) <= 3
                && player.getAirGroundState() == AirGroundState.GROUND;
        if (standing) {
            player.bounce(FORCE);
            squash = 12;
            float cx = player.getBounds().getX1() + player.getBounds().getWidth() / 2f;
            GourdProjectile.puff(map, cx - 18, getBounds().getY1(), -1f);
            GourdProjectile.puff(map, cx + 18, getBounds().getY1(), 1f);
            if (state == State.UP || state == State.RISING) {
                state = State.SHAKING;
                timer = SHAKE_TIME;
            }
        }
        if (squash > 0) squash--;
        switch (state) {
            case SHAKING:
                if (--timer <= 0) state = State.SINKING;
                break;
            case SINKING:
                moveTo(getY() + SINK_SPEED);
                if (getY() >= bottomY) {
                    moveTo(bottomY);
                    state = State.DOWN;
                    timer = DOWN_TIME;
                    tileType = TileType.PASSABLE;
                }
                break;
            case DOWN:
                if (--timer <= 0) {
                    state = State.RISING;
                    tileType = TileType.JUMP_THROUGH_PLATFORM;
                }
                break;
            case RISING:
                moveTo(getY() - RISE_SPEED);
                if (getY() <= topY) {
                    moveTo(topY);
                    state = State.UP;
                }
                break;
            default:
                break;
        }
        super.update(player);
    }

    private void moveTo(float y) {
        setY(y);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        int x = Math.round(getCalibratedXLocation());
        int y = Math.round(getCalibratedYLocation());
        if (state == State.SHAKING) x += (timer % 4 < 2) ? -2 : 2;
        Graphics2D g = graphicsHandler.getGraphics();
        Shape oldClip = g.getClip();
        int clipBottom = Math.round(floorY - map.getCamera().getY());
        g.clipRect(x - 10, -100000, 120, clipBottom + 100000);
        // the slate column, from just under the spring down past the floor
        for (int i = 1; i <= 6; i++) {
            int ty = y + i * 48;
            if (ty > clipBottom) break;
            graphicsHandler.drawImage(i == 1 ? slateTopL : slateL, x, ty, 48, 48);
            graphicsHandler.drawImage(i == 1 ? slateTopR : slateR, x + 48, ty, 48, 48);
        }
        // the spring (squashes when you land on it)
        int s = squash > 8 ? 3 : squash > 4 ? 1 : squash > 0 ? -1 : 0;
        int coilTop = 10 + Math.max(s, 0);
        for (int i = 0; i < 2; i++) {
            int tx = x + i * 48;
            graphicsHandler.drawImage(coil.getSubimage(0, 10, 16, 6), tx, y + coilTop * 3, 48, (16 - coilTop) * 3);
            graphicsHandler.drawImage((i == 0 ? capL : capR).getSubimage(0, 5, 16, 6), tx, y + (5 + s) * 3, 48, 18);
        }
        g.setClip(oldClip);
    }
}
