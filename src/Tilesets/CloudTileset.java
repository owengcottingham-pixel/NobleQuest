package Tilesets;

import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.Rectangle;
import Level.TileType;
import Level.Tileset;

import java.util.ArrayList;

/*
 * Tiles for the cloud level, from CloudTileset.png.
 *
 * Most of this level is open sky, so tile 0 is empty and the map file is almost
 * entirely 0s. The shelves (tiles 1-3) are the handful of places with solid
 * footing; everything else the player lands on is a BouncyPlatform placed by
 * CloudLevel.
 *
 * The shelves are JUMP_THROUGH_PLATFORM rather than NOT_PASSABLE. A cloud you
 * bonk your head on is not a cloud, and the whole level is built around being
 * flung upwards, so rising through one and landing on top is the behaviour that
 * matches both the fiction and the movement.
 */
public class CloudTileset extends Tileset {

    // The art sits a couple of rows down inside its cell, so the bounds have to
    // as well. Left at the default the player would stand on the cell's top
    // edge, floating a visible 9px above the cloud.
    private static final int SHELF_TOP = 3;
    private static final int SHELF_DEPTH = 13;

    public CloudTileset() {
        super(ImageLoader.load("CloudTileset.png"), 16, 16, 3);
    }

    @Override
    public ArrayList<MapTileBuilder> defineTiles() {
        ArrayList<MapTileBuilder> mapTiles = new ArrayList<>();

        // 0 - open sky
        mapTiles.add(new MapTileBuilder(plain(0)).withTileType(TileType.PASSABLE));

        // 1, 2, 3 - left cap, middle, right cap of a cloud shelf
        for (int cell = 1; cell <= 3; cell++) {
            mapTiles.add(new MapTileBuilder(shelf(cell))
                    .withTileType(TileType.JUMP_THROUGH_PLATFORM));
        }

        // 4 - solid body, for stacking underneath a shelf
        mapTiles.add(new MapTileBuilder(plain(4)).withTileType(TileType.NOT_PASSABLE));

        // 5 - a thin wisp, scenery only
        mapTiles.add(new MapTileBuilder(plain(5)).withTileType(TileType.PASSABLE));

        return mapTiles;
    }

    private Frame plain(int cell) {
        return new FrameBuilder(getSubImage(0, cell)).withScale(tileScale).build();
    }

    private Frame shelf(int cell) {
        return new FrameBuilder(getSubImage(0, cell))
                .withScale(tileScale)
                .withBounds(new Rectangle(0, SHELF_TOP, 16, SHELF_DEPTH))
                .build();
    }
}
