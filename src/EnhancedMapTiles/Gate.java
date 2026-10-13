package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import Level.EnhancedMapTile;
import Level.MapEntityStatus;
import Level.TileType;
import Utils.Point;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// A solid stone wall that blocks the player until open() is called.
// location = the TOP tile of the wall, heightInTiles = how many tiles tall it is.
public class Gate extends EnhancedMapTile {

    public Gate(Point location, int heightInTiles) {
        super(location.x, location.y,
                new FrameBuilder(buildImage(heightInTiles)).withScale(3).build(),
                TileType.NOT_PASSABLE);
    }

    // stacks the grey rock tile from the tileset into a tall wall
    private static BufferedImage buildImage(int heightInTiles) {
        BufferedImage rock = ImageLoader.loadSubImage("CommonTileset.png", 2 * 17, 3 * 17, 16, 16);
        BufferedImage wall = new BufferedImage(16, 16 * heightInTiles, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = wall.createGraphics();
        for (int i = 0; i < heightInTiles; i++) {
            g.drawImage(rock, 0, i * 16, null);
        }
        g.dispose();
        return wall;
    }

    public void open() {
        setMapEntityStatus(MapEntityStatus.REMOVED);
    }
}