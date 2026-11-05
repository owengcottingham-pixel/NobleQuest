package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.ForestChunk;
import Enemies.GourdProjectile;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Rectangle;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.Direction;
import Utils.Point;

import java.awt.image.BufferedImage;

// A bush you can slash with the sword
public class SlashBush extends EnhancedMapTile {
    private static final int REGROW_TIME = 900, GROW_FRAMES = 30;

    private final BufferedImage left, right;
    private final boolean hasCoin;
    private boolean coinGiven = false;
    private boolean wasAttacking = false;
    private int goneTimer = 0;

    public SlashBush(Point location, boolean hasCoin) {
        super(location.x, location.y,
                new FrameBuilder(new BufferedImage(32, 16, BufferedImage.TYPE_INT_ARGB)).withScale(3).withBounds(2, 4, 28, 12).build(),
                TileType.PASSABLE);
        SpriteSheet sheet = new SpriteSheet(ImageLoader.load("DaylightTileset.png"), 16, 16);
        left = sheet.getSprite(4, 0);
        right = sheet.getSprite(4, 1);
        this.hasCoin = hasCoin;
        initialize();
    }

    @Override
    public void update(Player player) {
        super.update(player);
        boolean attacking = player.isAttacking();
        if (goneTimer > 0) {
            goneTimer--;
        } else {
            boolean swung = attacking && !wasAttacking && inSwordRange(player);
            boolean dashed = player.getPlayerState() == Player.PlayerState.DASHING && intersects(player);
            if (swung || dashed) {
                slash();
            }
        }
        wasAttacking = attacking;
    }

    private boolean inSwordRange(Player player) {
        Rectangle me = getBounds(), p = player.getBounds();
        float dx = (me.getX1() + me.getWidth() / 2f) - (p.getX1() + p.getWidth() / 2f);
        if (p.getY2() < me.getY1() - 30 || p.getY1() > me.getY2()) return false;
        return player.getFacingDirection() == Direction.RIGHT ? (dx > -30 && dx < 110) : (dx < 30 && dx > -110);
    }

    private void slash() {
        goneTimer = REGROW_TIME;
        Rectangle b = getBounds();
        float cx = b.getX1() + b.getWidth() / 2f;
        ForestChunk.burst(map, cx, b.getY1() + 10, new int[] { ForestChunk.LEAF, ForestChunk.LEAF, ForestChunk.STEM }, 14, 5f);
        GourdProjectile.puff(map, cx, b.getY2(), 0);
        if (hasCoin && !coinGiven) {
            coinGiven = true;
            map.addEnhancedMapTile(new Coin(cx - 16, b.getY1() - 40));
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        int x = Math.round(getCalibratedXLocation());
        int y = Math.round(getCalibratedYLocation());
        if (goneTimer > GROW_FRAMES) {
            return;
        }
        // growing back
        int h = goneTimer > 0 ? Math.round(48 * (1 - goneTimer / (float) GROW_FRAMES)) : 48;
        graphicsHandler.drawImage(left, x, y + 48 - h, 48, h);
        graphicsHandler.drawImage(right, x + 48, y + 48 - h, 48, h);
    }
}
