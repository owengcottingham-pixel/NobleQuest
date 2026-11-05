package Tilesets;

import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import Level.TileType;
import Level.Tileset;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class LavaBossTileset extends Tileset {
    public static final int LAVA_TILE_INDEX = 1;

    private static final int LAVA_FRAME_DELAY = 12;
    private static final int LAVA_FRAME_COUNT = 5;
    private static final int GROUND_IMAGE_INDEX = 5;
    private static final int FIRST_BACKGROUND_IMAGE_INDEX = 6;
    private static final int BACKGROUND_TILE_COUNT = 2;

    public LavaBossTileset() {
        super(ImageLoader.load("LaveBossTileSet.png"), 16, 16, 3);
    }

    @Override
    public ArrayList<MapTileBuilder> defineTiles() {
        ArrayList<MapTileBuilder> mapTiles = new ArrayList<>();

        Frame emptyFrame = new FrameBuilder(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB))
            .withScale(tileScale)
            .build();
        mapTiles.add(new MapTileBuilder(emptyFrame).withTileType(TileType.PASSABLE));

        Frame[] lavaFrames = new Frame[LAVA_FRAME_COUNT];
        for (int frameIndex = 0; frameIndex < LAVA_FRAME_COUNT; frameIndex++) {
            lavaFrames[frameIndex] = new FrameBuilder(getSubImage(0, frameIndex), LAVA_FRAME_DELAY)
                .withScale(tileScale)
                .build();
        }
        mapTiles.add(new MapTileBuilder(lavaFrames).withTileType(TileType.LAVA));

        Frame groundFrame = new FrameBuilder(getSubImage(0, GROUND_IMAGE_INDEX))
            .withScale(tileScale)
            .build();
        mapTiles.add(new MapTileBuilder(groundFrame).withTileType(TileType.NOT_PASSABLE));

        for (int backgroundIndex = 0; backgroundIndex < BACKGROUND_TILE_COUNT; backgroundIndex++) {
            Frame backgroundFrame = new FrameBuilder(getSubImage(0, FIRST_BACKGROUND_IMAGE_INDEX + backgroundIndex))
                .withScale(tileScale)
                .build();
            mapTiles.add(new MapTileBuilder(backgroundFrame).withTileType(TileType.PASSABLE));
        }

        mapTiles.add(new MapTileBuilder(emptyFrame).withTileType(TileType.PASSABLE));

        Frame stoneFrame = new FrameBuilder(getSubImage(0, 9))
            .withScale(tileScale)
            .withBounds(0, 0, 8, 16)
            .build();
        mapTiles.add(new MapTileBuilder(stoneFrame).withTileType(TileType.NOT_PASSABLE));

        return mapTiles;
    }
}
