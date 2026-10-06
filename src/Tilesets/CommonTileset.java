package Tilesets;

import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import Level.TileLayout;
import Level.TileType;
import Level.Tileset;
import Utils.SlopeTileLayoutUtils;

import Level.TileLayout;
import Utils.Direction;

import java.util.ArrayList;

// This class represents a "common" tileset of standard tiles defined in the CommonTileset.png file
public class CommonTileset extends Tileset {

    public CommonTileset() {
        super(ImageLoader.load("CommonTileset.png"), 16, 16, 3);
    }

    @Override
    public ArrayList<MapTileBuilder> defineTiles() {
        ArrayList<MapTileBuilder> mapTiles = new ArrayList<>();

        // grass
        Frame grassFrame = new FrameBuilder(getSubImage(0, 0))
                .withScale(tileScale)
                .build();

        MapTileBuilder grassTile = new MapTileBuilder(grassFrame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(grassTile);

        // sky
        Frame skyFrame = new FrameBuilder(getSubImage(0, 1))
                .withScale(tileScale)
                .build();

        MapTileBuilder skyTile = new MapTileBuilder(skyFrame);

        mapTiles.add(skyTile);

        // dirt
        Frame dirtFrame = new FrameBuilder(getSubImage(0, 2))
                .withScale(tileScale)
                .build();

        MapTileBuilder dirtTile = new MapTileBuilder(dirtFrame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(dirtTile);
       
        

        // sun
        Frame[] sunFrames = new Frame[]{
                new FrameBuilder(getSubImage(2, 0), 50)
                        .withScale(tileScale)
                        .build(),
                new FrameBuilder(getSubImage(2, 1), 50)
                        .withScale(tileScale)
                        .build()
        };

        MapTileBuilder sunTile = new MapTileBuilder(sunFrames);

        mapTiles.add(sunTile);



        // left end branch
        Frame leftEndBranchFrame = new FrameBuilder(getSubImage(1, 5))
                .withScale(tileScale)
                .withBounds(0, 6, 16, 4)
                .build();

        MapTileBuilder leftEndBranchTile = new MapTileBuilder(leftEndBranchFrame)
                .withTileType(TileType.JUMP_THROUGH_PLATFORM);

        mapTiles.add(leftEndBranchTile);

       

        // tree trunk
        Frame treeTrunkFrame = new FrameBuilder(getSubImage(1, 0))
                .withScale(tileScale)
                .build();

        MapTileBuilder treeTrunkTile = new MapTileBuilder(treeTrunkFrame)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(treeTrunkTile);

        // tree top leaves
        Frame treeTopLeavesFrame = new FrameBuilder(getSubImage(1, 1))
                .withScale(tileScale)
                .build();

        MapTileBuilder treeTopLeavesTile = new MapTileBuilder(treeTopLeavesFrame)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(treeTopLeavesTile);

        // yellow flower
        Frame[] yellowFlowerFrames = new Frame[] {
                new FrameBuilder(getSubImage(1, 2), 65)
                        .withScale(tileScale)
                        .build(),
                new FrameBuilder(getSubImage(1, 3), 65)
                        .withScale(tileScale)
                        .build(),
                new FrameBuilder(getSubImage(1, 2), 65)
                        .withScale(tileScale)
                        .build(),
                new FrameBuilder(getSubImage(1, 4), 65)
                        .withScale(tileScale)
                        .build()
        };

        MapTileBuilder yellowFlowerTile = new MapTileBuilder(yellowFlowerFrames);

        mapTiles.add(yellowFlowerTile);

        // purple flower
        Frame[] purpleFlowerFrames = new Frame[] {
                new FrameBuilder(getSubImage(0, 3), 65)
                        .withScale(tileScale)
                        .build(),
                new FrameBuilder(getSubImage(0, 4), 65)
                        .withScale(tileScale)
                        .build(),
                new FrameBuilder(getSubImage(0, 3), 65)
                        .withScale(tileScale)
                        .build(),
                new FrameBuilder(getSubImage(0, 5), 65)
                        .withScale(tileScale)
                        .build()
        };

        MapTileBuilder purpleFlowerTile = new MapTileBuilder(purpleFlowerFrames);

        mapTiles.add(purpleFlowerTile);


        // top water
        Frame topWaterFrame = new FrameBuilder(getSubImage(3, 0))
                .withScale(tileScale)
                .build();

        MapTileBuilder topWaterTile = new MapTileBuilder(topWaterFrame);

        mapTiles.add(topWaterTile);

        // water
        Frame waterFrame = new FrameBuilder(getSubImage(3, 1))
                .withScale(tileScale)
                .build();

        MapTileBuilder waterTile = new MapTileBuilder(waterFrame)
                .withTileType(TileType.WATER);

        mapTiles.add(waterTile);

        // grey rock
        Frame greyRockFrame = new FrameBuilder(getSubImage(3, 2))
                .withScale(tileScale)
                .build();

        MapTileBuilder greyRockTile = new MapTileBuilder(greyRockFrame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(greyRockTile);


        //hole with eyes
        
        Frame[] holeWithEyesFrames = new Frame[] {
                new FrameBuilder(getSubImage(2, 2), 65)
                        .withScale(tileScale)
                        .build(),
                new FrameBuilder(getSubImage(2, 3), 65)
                        .withScale(tileScale)
                        .build(),
                new FrameBuilder(getSubImage(2, 4), 65)
                        .withScale(tileScale)
                        .build(),
                new FrameBuilder(getSubImage(2, 5), 65)
                        .withScale(tileScale)
                        .build(),
                new FrameBuilder(getSubImage(2, 4), 65)
                        .withScale(tileScale)
                        .build(),
                 new FrameBuilder(getSubImage(2, 2), 65)
                        .withScale(tileScale)
                        .build(),
                
        };
        
        MapTileBuilder holeWithEyestile = new MapTileBuilder(holeWithEyesFrames)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(holeWithEyestile);

        //well top 1
        Frame wellTop1Frame = new FrameBuilder(getSubImage(3, 3))
                .withScale(tileScale)
                .build();

        MapTileBuilder wellTop1Tile = new MapTileBuilder(wellTop1Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(wellTop1Tile);

        //well top 2
        Frame wellTop2Frame = new FrameBuilder(getSubImage(3, 4))
                .withScale(tileScale)
                .build();

        MapTileBuilder wellTop2Tile = new MapTileBuilder(wellTop2Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(wellTop2Tile);

        //well top 3
        Frame wellTop3Frame = new FrameBuilder(getSubImage(3, 5))
                .withScale(tileScale)
                .build();

        MapTileBuilder wellTop3Tile = new MapTileBuilder(wellTop3Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(wellTop3Tile);

        //well top 4
        Frame wellTop4Frame = new FrameBuilder(getSubImage(4, 0))
                .withScale(tileScale)
                .build();

        MapTileBuilder wellTop4Tile = new MapTileBuilder(wellTop4Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(wellTop4Tile);

        //well top 5
        Frame wellTop5Frame = new FrameBuilder(getSubImage(4, 1))
                .withScale(tileScale)
                .build();

        MapTileBuilder wellTop5Tile = new MapTileBuilder(wellTop5Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(wellTop5Tile);

        //well top 6
        Frame wellTop6Frame = new FrameBuilder(getSubImage(4, 2))
                .withScale(tileScale)
                .build();

        MapTileBuilder wellTop6Tile = new MapTileBuilder(wellTop6Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(wellTop6Tile);

        //well top 7
        Frame wellTop7Frame = new FrameBuilder(getSubImage(4, 3))
                .withScale(tileScale)
                .build();

        MapTileBuilder wellTop7Tile = new MapTileBuilder(wellTop7Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(wellTop7Tile);

        //well top 8
        Frame wellTop8Frame = new FrameBuilder(getSubImage(4, 4))
                .withScale(tileScale)
                .build();

        MapTileBuilder wellTop8Tile = new MapTileBuilder(wellTop8Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(wellTop8Tile);

        //well top 9
        Frame wellTop9Frame = new FrameBuilder(getSubImage(4, 5))
                .withScale(tileScale)
                .build();

        MapTileBuilder wellTop9Tile = new MapTileBuilder(wellTop9Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(wellTop9Tile);

        //well top 10
        Frame wellTop10Frame = new FrameBuilder(getSubImage(5, 0))
                .withScale(tileScale)
                .build();

        MapTileBuilder wellTop10Tile = new MapTileBuilder(wellTop10Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(wellTop10Tile);

        //well top 11
        Frame wellTop11Frame = new FrameBuilder(getSubImage(5, 1))
                .withScale(tileScale)
                .build();

        MapTileBuilder wellTop11Tile = new MapTileBuilder(wellTop11Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(wellTop11Tile);

        //well top 12
        Frame wellTop12Frame = new FrameBuilder(getSubImage(5, 2))
                .withScale(tileScale)
                .build();

        MapTileBuilder wellTop12Tile = new MapTileBuilder(wellTop12Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(wellTop12Tile);

        //well top 13
        Frame wellTop13Frame = new FrameBuilder(getSubImage(5, 3))
                .withScale(tileScale)
                .build();

        MapTileBuilder wellTop13Tile = new MapTileBuilder(wellTop13Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(wellTop13Tile);

        //well top 14
        Frame wellTop14Frame = new FrameBuilder(getSubImage(5, 4))
                .withScale(tileScale)
                .build();

        MapTileBuilder wellTop14Tile = new MapTileBuilder(wellTop14Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(wellTop14Tile);

        //well top 15
        Frame wellTop15Frame = new FrameBuilder(getSubImage(5, 5))
                .withScale(tileScale)
                .build();

        MapTileBuilder wellTop15Tile = new MapTileBuilder(wellTop15Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(wellTop15Tile);

        //well top 16
        Frame wellTop16Frame = new FrameBuilder(getSubImage(6, 0))
                .withScale(tileScale)
                .build();

        MapTileBuilder wellTop16Tile = new MapTileBuilder(wellTop16Frame)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(wellTop16Tile);


        // invisible tile
        Frame invisibleFrame = new FrameBuilder(getSubImage(6, 1))
                .withScale(tileScale)
                .build();

        MapTileBuilder invisibleTile = new MapTileBuilder(invisibleFrame)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(invisibleTile);

        // village house - roof
        mapTiles.add(roofSlope(7, 0, SlopeTileLayoutUtils.createBottomLeft30SlopeLayout(spriteWidth, (int) tileScale)));
        mapTiles.add(roofSlope(7, 1, SlopeTileLayoutUtils.createTopLeft30SlopeLayout(spriteWidth, (int) tileScale)));
        mapTiles.add(houseTile(7, 2, TileType.NOT_PASSABLE));
        mapTiles.add(houseTile(7, 3, TileType.NOT_PASSABLE));
        mapTiles.add(roofSlope(7, 4, right30Top()));
        mapTiles.add(roofSlope(7, 5, right30Bottom()));

        // village house - inside (walk-through)
        for (int row = 8; row <= 10; row++) {
            for (int col = 0; col <= 5; col++) {
                mapTiles.add(houseTile(row, col, TileType.PASSABLE));
            }
        }

        return mapTiles;
    }

        private MapTileBuilder houseTile(int row, int col, TileType type) {
        Frame frame = new FrameBuilder(getSubImage(row, col)).withScale(tileScale).build();
        return new MapTileBuilder(frame).withTileType(type);
    }

    private MapTileBuilder roofSlope(int row, int col, TileLayout layout) {
        Frame frame = new FrameBuilder(getSubImage(row, col)).withScale(tileScale).build();
        return new MapTileBuilder(frame).withTileType(TileType.SLOPE).withTileLayout(layout);
    }

    // the engine only has left 30 degree slopes, so these are mirrored copies for the right side
    private TileLayout right30Bottom() {
        int size = spriteWidth * (int) tileScale;
        int[][] layout = new int[size][size];
        int colCounter = 0;
        for (int i = size - 1; i >= size / 2; i--) {
            for (int j = 0; j < size - colCounter; j++) {
                layout[i][j] = 1;
            }
            colCounter += 2;
        }
        return new TileLayout(layout, Direction.RIGHT);
    }

    private TileLayout right30Top() {
        int size = spriteWidth * (int) tileScale;
        int[][] layout = new int[size][size];
        int colCounter = 0;
        for (int i = size - 1; i >= 0; i--) {
            for (int j = 0; j < size - colCounter; j++) {
                layout[i][j] = 1;
            }
            if (i < size / 2) {
                colCounter += 2;
            }
        }
        return new TileLayout(layout, Direction.RIGHT);
    }

}
