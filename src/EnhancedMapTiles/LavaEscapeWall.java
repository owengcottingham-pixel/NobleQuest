package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.Rectangle;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.Point;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class LavaEscapeWall extends EnhancedMapTile {
    private static final int WALL_TILE_COLUMNS = 3;
    private static final int TILE_SIZE = 16;
    private static final int TILE_STRIDE = 17;
    private static final int FRAME_COUNT = 5;
    private static final int FRAME_DELAY = 12;
    private static final float CHASE_SPEED = 2.5f;
    private static final float OFFSCREEN_CHASE_SPEED = 8f;
    private static final float CHASE_GAP = 48f;

    public LavaEscapeWall(Point location, int mapHeightInTiles) {
        super(location.x, location.y, createFrames(mapHeightInTiles), TileType.LAVA);
        setIsUpdateOffScreen(true);
    }

    @Override
    public void update(Player player) {
        super.update(player);
        float cameraX = map.getCamera().getX();
        boolean wallIsOffscreen = getX() + getWidth() < cameraX;
        if (getX() < cameraX) {
            setX(cameraX);
        }

        float targetX = player.getBounds().getX1() - getWidth() - CHASE_GAP;
        if (targetX > getX()) {
            float chaseSpeed = wallIsOffscreen
                ? OFFSCREEN_CHASE_SPEED
                : CHASE_SPEED;
            setX(Math.max(cameraX, Math.min(targetX, getX() + chaseSpeed)));
        }
        if (getBounds().intersects(player.getBounds())) {
            player.damage(1);
        }
    }

    private static Frame[] createFrames(int mapHeightInTiles) {
        BufferedImage tileset = ImageLoader.load("LaveBossTileSet.png");
        Frame[] frames = new Frame[FRAME_COUNT];
        for (int frameIndex = 0; frameIndex < FRAME_COUNT; frameIndex++) {
            BufferedImage tile = tileset.getSubimage(
                frameIndex * TILE_STRIDE,
                0,
                TILE_SIZE,
                TILE_SIZE
            );
            BufferedImage wallImage = new BufferedImage(
                WALL_TILE_COLUMNS * TILE_SIZE,
                mapHeightInTiles * TILE_SIZE,
                BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D graphics = wallImage.createGraphics();
            for (int row = 0; row < mapHeightInTiles; row++) {
                for (int column = 0; column < WALL_TILE_COLUMNS; column++) {
                    graphics.drawImage(tile, column * TILE_SIZE, row * TILE_SIZE, null);
                }
            }
            graphics.dispose();
            frames[frameIndex] = new FrameBuilder(wallImage, FRAME_DELAY)
                .withBounds(new Rectangle(0, 0, wallImage.getWidth(), wallImage.getHeight()))
                .withScale(3)
                .build();
        }
        return frames;
    }
}
