package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.ForestChunk;
import Enemies.GourdProjectile;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Level.EnhancedMapTile;
import Level.MapEntityStatus;
import Level.Player;
import Level.TileType;
import Utils.Effects;
import Utils.Point;

import java.awt.Color;

// A rock shaken loose from the canopy by the chase
public class FallingRock extends EnhancedMapTile {
    private enum State { HANGING, WOBBLING, FALLING, GONE }

    private static final int WOBBLE_TIME = 14;
    private final float groundY;
    private float triggerDistance = 4.5f * 48;
    private State state = State.HANGING;
    private int timer = 0;
    private float velocityY = 0;

    public FallingRock(Point location, float groundY) {
        super(location.x, location.y,
                new FrameBuilder(ImageLoader.load("FallingRock.png")).withScale(3).withBounds(2, 2, 12, 12).build(),
                TileType.PASSABLE);
        this.groundY = groundY;
        initialize();
        setIsUpdateOffScreen(true);
    }

    public FallingRock triggerDistance(float tiles) {
        this.triggerDistance = tiles * 48;
        return this;
    }

    @Override
    public void update(Player player) {
        if (state == State.GONE) return;
        super.update(player);
        float px = player.getBounds().getX1() + player.getBounds().getWidth() / 2f;
        float mx = getX() + 24;
        switch (state) {
            case HANGING:
                if (px < mx && mx - px < triggerDistance) {
                    state = State.WOBBLING;
                    timer = WOBBLE_TIME;
                }
                break;
            case WOBBLING:
                if (timer % 4 == 0) {
                    ForestChunk.burst(map, mx, getY() + 40, new int[] { ForestChunk.STONE, ForestChunk.LEAF }, 1, 1f);
                }
                if (--timer <= 0) state = State.FALLING;
                break;
            case FALLING:
                velocityY = Math.min(velocityY + 0.7f, 16f);
                setY(getY() + velocityY);
                if (intersects(player) && !player.isHurtInvincible() && player.getHealth() > 0) {
                    player.damage(1);
                    shatter(true);
                } else if (getY() + 48 >= groundY) {
                    setY(groundY - 48);
                    shatter(false);
                }
                break;
            default:
                break;
        }
    }

    private void shatter(boolean hitKnight) {
        state = State.GONE;
        mapEntityStatus = MapEntityStatus.REMOVED;
        float cx = getX() + 24, cy = getY() + 40;
        ForestChunk.burst(map, cx, cy, new int[] { ForestChunk.STONE, ForestChunk.ROCK, ForestChunk.MOSS_ROCK }, 12, 5f);
        GourdProjectile.puff(map, cx - 18, groundY, -1f);
        GourdProjectile.puff(map, cx + 18, groundY, 1f);
        Effects.shake(hitKnight ? 6 : 4, 8);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (state == State.GONE) return;
        if (state != State.HANGING) {
            // a shadow on the ground where it's going to land
            int sx = Math.round(getCalibratedXLocation()) + 6;
            int sy = Math.round(groundY - (getY() - getCalibratedYLocation())) - 6;
            graphicsHandler.drawFilledRectangle(sx, sy, 36, 6, new Color(0, 0, 0, 110));
        }
        if (state == State.WOBBLING) {
            float x = getX();
            setX(x + ((timer / 2) % 2 == 0 ? -2 : 2));
            super.draw(graphicsHandler);
            setX(x);
        } else {
            super.draw(graphicsHandler);
        }
    }
}
