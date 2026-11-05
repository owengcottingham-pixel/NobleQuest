package EnhancedMapTiles;

import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Utils.Point;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class LavaBossGate extends PumpkinGate {
    private static final int GATE_TILE_WIDTH = 16;
    private static final int GATE_TILE_HEIGHT = 16;
    private static final int GATE_LEFT_TILE_COLUMN = 11;
    private static final int GATE_RIGHT_TILE_COLUMN = 12;
    private static final int SOURCE_TILE_STRIDE = 17;
    private static final int GATE_HEIGHT_IN_TILES = 8;
    private static final int GATE_HEIGHT = GATE_TILE_HEIGHT * GATE_HEIGHT_IN_TILES;
    private static final int TOP_RAIL_HEIGHT = 9;
    private static final int BAR_STRIP_Y = 9;
    private static final int BAR_STRIP_HEIGHT = 4;
    private static final int SPIKE_SECTION_Y = 9;
    private static final int SPIKE_SECTION_HEIGHT = GATE_TILE_HEIGHT - SPIKE_SECTION_Y;

    public LavaBossGate(Point location) {
        super(location, buildGateImage());
    }

    public void drawEditorPreview(GraphicsHandler graphicsHandler) {
        graphicsHandler.drawImage(
            currentFrame.getImage(),
            Math.round(getCalibratedXLocation()),
            Math.round(getCalibratedYLocation()),
            Math.round(getWidth()),
            Math.round(getHeight())
        );
    }

    private static BufferedImage buildGateImage() {
        BufferedImage tileset = ImageLoader.load("LaveBossTileSet.png");
        BufferedImage gate = new BufferedImage(
            GATE_TILE_WIDTH * 2,
            GATE_HEIGHT,
            BufferedImage.TYPE_INT_ARGB
        );
        BufferedImage[] gateTiles = {
            tileset.getSubimage(
            GATE_LEFT_TILE_COLUMN * SOURCE_TILE_STRIDE,
            0,
            GATE_TILE_WIDTH,
            GATE_TILE_HEIGHT
            ),
            tileset.getSubimage(
            GATE_RIGHT_TILE_COLUMN * SOURCE_TILE_STRIDE,
            0,
            GATE_TILE_WIDTH,
            GATE_TILE_HEIGHT
            )
        };
        Graphics2D graphics = gate.createGraphics();
        for (int column = 0; column < gateTiles.length; column++) {
            BufferedImage tile = gateTiles[column];
            int x = column * GATE_TILE_WIDTH;
            graphics.drawImage(
                tile.getSubimage(0, 0, GATE_TILE_WIDTH, TOP_RAIL_HEIGHT),
                x,
                0,
                null
            );

            int y = TOP_RAIL_HEIGHT;
            while (y + BAR_STRIP_HEIGHT <= GATE_HEIGHT - SPIKE_SECTION_HEIGHT) {
                graphics.drawImage(
                    tile.getSubimage(0, BAR_STRIP_Y, GATE_TILE_WIDTH, BAR_STRIP_HEIGHT),
                    x,
                    y,
                    null
                );
                y += BAR_STRIP_HEIGHT;
            }
            graphics.drawImage(
                tile.getSubimage(0, SPIKE_SECTION_Y, GATE_TILE_WIDTH, SPIKE_SECTION_HEIGHT),
                x,
                GATE_HEIGHT - SPIKE_SECTION_HEIGHT,
                null
            );
            tile.flush();
        }
        graphics.dispose();
        return gate;
    }
}
