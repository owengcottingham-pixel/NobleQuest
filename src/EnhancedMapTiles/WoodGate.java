package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.ForestChunk;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.ScreenManager;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.Effects;
import Utils.Point;

import java.awt.Graphics2D;
import java.awt.Shape;


public class WoodGate extends EnhancedMapTile {
    private final float closedY;
    private boolean opening = false, open = false;

    public WoodGate(Point location) {
        super(location.x, location.y, new FrameBuilder(ImageLoader.load("WoodGate.png")).withScale(3).build(), TileType.NOT_PASSABLE);
        closedY = location.y;
        initialize();
        setIsUpdateOffScreen(true);
    }

    public void open() {
        if (opening || open) return;
        opening = true;
        Effects.shake(4, 30);
        ForestChunk.burst(map, getX() + 24, closedY, new int[] { ForestChunk.STONE, ForestChunk.BARK }, 8, 3f);
    }

    public boolean isOpen() {
        return open;
    }

    @Override
    public void update(Player player) {
        super.update(player);
        if (opening && !open) {
            setY(getY() - 2.4f);
            if (getY() <= closedY - 144) {
                open = true;
                opening = false;
                tileType = TileType.PASSABLE;
            }
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (open) return;
        // hide the part that has slid up into the rock
        Graphics2D g = graphicsHandler.getGraphics();
        Shape oldClip = g.getClip();
        int clipTop = Math.round(closedY - map.getCamera().getY());
        g.clipRect(0, clipTop, ScreenManager.getScreenWidth(), ScreenManager.getScreenHeight());
        super.draw(graphicsHandler);
        g.setClip(oldClip);
    }
}
