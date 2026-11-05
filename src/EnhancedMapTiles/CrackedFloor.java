package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.ForestChunk;
import Enemies.GourdProjectile;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Rectangle;
import Level.EnhancedMapTile;
import Level.MapEntityStatus;
import Level.Player;
import Utils.AirGroundState;
import Level.TileType;
import Utils.Effects;
import Utils.Point;


public class CrackedFloor extends EnhancedMapTile {
    private int armed = 0;        // frames left in which a down-dash that started in the air counts
    private int stomps = 0;       // landed on it without a down-dash: it creaks

    public CrackedFloor(Point location) {
        super(location.x, location.y, new FrameBuilder(ImageLoader.load("CrackedFloor.png")).withScale(3).build(), TileType.NOT_PASSABLE);
        initialize();
        setIsUpdateOffScreen(true);
    }

    @Override
    public void update(Player player) {
        if (mapEntityStatus == MapEntityStatus.REMOVED) return;
        super.update(player);
        boolean dashingDown = player.getPlayerState() == Player.PlayerState.DASHING && player.getDashDirY() > 0;
        if (dashingDown && player.getAirGroundState() == AirGroundState.AIR) {
            armed = 8;
        } else if (armed > 0) {
            armed--;
        }
        Rectangle b = getBounds(), p = player.getBounds();
        float pcx = p.getX1() + p.getWidth() / 2f;
        boolean above = pcx > b.getX1() && pcx < b.getX2()
                && p.getY2() >= b.getY1() - 12 && p.getY2() <= b.getY1() + 4;
        if (above && armed > 0) {
            smash();
        }
    }

    public void smash() {
        if (mapEntityStatus == MapEntityStatus.REMOVED) return;
        mapEntityStatus = MapEntityStatus.REMOVED;
        tileType = TileType.PASSABLE;
        Rectangle b = getBounds();
        float cx = b.getX1() + b.getWidth() / 2f;
        ForestChunk.burst(map, cx, b.getY1() + 10, new int[] { ForestChunk.STONE, ForestChunk.ROCK, ForestChunk.STONE }, 26, 7f);
        GourdProjectile.puff(map, b.getX1() + 20, b.getY1(), -1.5f);
        GourdProjectile.puff(map, b.getX2() - 20, b.getY1(), 1.5f);
        Effects.shake(10, 18);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (mapEntityStatus == MapEntityStatus.REMOVED) return;
        super.draw(graphicsHandler);
    }
}
