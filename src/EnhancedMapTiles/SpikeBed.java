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


public class SpikeBed extends EnhancedMapTile implements EditorObject {
    public enum Facing { UP, DOWN, RIGHT, LEFT }

    private final Facing facing;
    private final int length;
    private final BufferedImage art;
    private float popForce = 17.5f;   // about 6 tiles
    public SpikeBed(Point location, int length, Facing facing) {
        super(location.x, location.y,
                new FrameBuilder(new BufferedImage(
                        facing == Facing.UP || facing == Facing.DOWN ? 16 * length : 16,
                        facing == Facing.UP || facing == Facing.DOWN ? 16 : 16 * length,
                        BufferedImage.TYPE_INT_ARGB))
                        .withScale(3)
                        .withBounds(bounds(facing, length))
                        .build(),
                TileType.PASSABLE);
        this.facing = facing;
        this.length = length;
        art = new SpriteSheet(ImageLoader.load("SpikeBed.png"), 16, 16).getSprite(0, facing.ordinal());
        initialize();
    }

    // the dangerous part is the thorns, not the empty air in the rest of the tile
    private static Rectangle bounds(Facing facing, int length) {
        switch (facing) {
            case UP: return new Rectangle(0, 7, 16 * length, 9);
            case DOWN: return new Rectangle(0, 0, 16 * length, 9);
            case RIGHT: return new Rectangle(0, 0, 9, 16 * length);
            default: return new Rectangle(7, 0, 9, 16 * length);
        }
    }

    public SpikeBed popForce(float force) {
        this.popForce = force;
        return this;
    }

    @Override
    public String editorLine() {
        return "SPIKES " + EditorObjects.tiles(getX()) + " " + EditorObjects.tiles(getY()) + " " + length + " " + facing
                + (facing == Facing.UP && popForce != 17.5f ? " " + EditorObjects.num(popForce) : "");
    }

    @Override
    public void update(Player player) {
        super.update(player);
        if (!intersects(player) || player.getHealth() <= 0) return;

        boolean stung = !player.isHurtInvincible();
        if (stung) {
            player.damage(1);
        }
        Rectangle p = player.getBounds();
        float cx = p.getX1() + p.getWidth() / 2f;
        if (facing == Facing.UP) {
            player.bounce(popForce);
            GourdProjectile.puff(map, cx, getBounds().getY1() + 20, 0);
        } else if (facing == Facing.DOWN) {
            player.bounce(0);         // stop going up
        } else if (stung) {
            // on a wall: knocked away from it
            player.moveXHandleCollision(facing == Facing.RIGHT ? 30 : -30);
            player.bounce(7f);
        }
        if (stung) {
            ForestChunk.burst(map, cx, facing == Facing.UP ? p.getY2() : p.getY1(),
                    new int[] { ForestChunk.STONE, ForestChunk.STONE, ForestChunk.ROCK }, 7, 4f);
            Effects.shake(4, 8);
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        int x = Math.round(getCalibratedXLocation());
        int y = Math.round(getCalibratedYLocation());
        boolean across = facing == Facing.UP || facing == Facing.DOWN;
        for (int i = 0; i < length; i++) {
            graphicsHandler.drawImage(art, across ? x + i * 48 : x, across ? y : y + i * 48, 48, 48);
        }
    }
}
