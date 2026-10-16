package Tilesets;

import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import Level.TileType;
import Level.Tileset;

import java.util.ArrayList;

// This class represents a "common" tileset of standard tiles defined in the Level2Tileset.png file
public class Level2Tileset extends Tileset {

    public Level2Tileset() {
        super(ImageLoader.load("Level2Tileset.png"), 16, 16, 3);
    }

    @Override
    public ArrayList<MapTileBuilder> defineTiles() {
        ArrayList<MapTileBuilder> mapTiles = new ArrayList<>();

        //Stone
        Frame Stone = new FrameBuilder(getSubImage(0, 0))
                .withScale(tileScale)
                .build();

        MapTileBuilder StoneTile = new MapTileBuilder(Stone)
                .withTileType(TileType.NOT_PASSABLE);

        mapTiles.add(StoneTile);
        
        //FullFireStone
        Frame FullFireStone = new FrameBuilder(getSubImage(0, 1))
                .withScale(tileScale)
                .build();

        MapTileBuilder FullFireStoneTile = new MapTileBuilder(FullFireStone)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(FullFireStoneTile);

        //halfFireStone
        Frame halfFireStone = new FrameBuilder(getSubImage(0, 2))
                .withScale(tileScale)
                .build();

        MapTileBuilder halfFireStoneTile = new MapTileBuilder(halfFireStone)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(halfFireStoneTile);
        
        //MossyStone1
        Frame MossyStone1 = new FrameBuilder(getSubImage(0, 3))
                .withScale(tileScale)
                .build();

        MapTileBuilder MossyStone1Tile = new MapTileBuilder(MossyStone1)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(MossyStone1Tile);

        //MossyStone2
        Frame MossyStone2 = new FrameBuilder(getSubImage(0, 4))
                .withScale(tileScale)
                .build();

        MapTileBuilder MossyStone2Tile = new MapTileBuilder(MossyStone2)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(MossyStone2Tile);

        //Flame
        Frame[] Flame = new Frame[] {
                new FrameBuilder(getSubImage(1, 0), 35)
                        .withScale(tileScale)
                        .build(),
                new FrameBuilder(getSubImage(1, 1), 35)
                        .withScale(tileScale)
                        .build(),
                new FrameBuilder(getSubImage(1, 2), 35)
                        .withScale(tileScale)
                        .build(),
                new FrameBuilder(getSubImage(1, 3), 35)
                        .withScale(tileScale)
                        .build(),
                new FrameBuilder(getSubImage(2, 0), 35)
                        .withScale(tileScale)
                        .build(),
                new FrameBuilder(getSubImage(2, 1), 35)
                        .withScale(tileScale)
                        .build(),
                new FrameBuilder(getSubImage(2, 2), 35)
                        .withScale(tileScale)
                        .build(),
                
        };

        MapTileBuilder FlameTile = new MapTileBuilder(Flame)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(FlameTile);

        //StoneBackground
        Frame StoneBackground = new FrameBuilder(getSubImage(2, 3))
                .withScale(tileScale)
                .build();

        MapTileBuilder StoneBackgroundTile = new MapTileBuilder(StoneBackground)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(StoneBackgroundTile);

         //RemoveBlock
        Frame RemoveBlock = new FrameBuilder(getSubImage(2, 4))
                .withScale(tileScale)
                .build();

        MapTileBuilder RemoveBlockTile = new MapTileBuilder(RemoveBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(RemoveBlockTile);

        // These tiles collide only on the tan pillar pixels; the gray stone remains passable.
        Frame PillerBlock1 = new FrameBuilder(getSubImage(0, 5))
                .withScale(tileScale)
                .build();

        MapTileBuilder PillerBlock1Tile = new MapTileBuilder(PillerBlock1)
                .withTileType(TileType.PILLAR);

        mapTiles.add(PillerBlock1Tile);

        //PillerBlock2
        Frame PillerBlock2 = new FrameBuilder(getSubImage(0, 6))
                .withScale(tileScale)
                .build();

        MapTileBuilder PillerBlock2Tile = new MapTileBuilder(PillerBlock2)
                .withTileType(TileType.PILLAR);

        mapTiles.add(PillerBlock2Tile);

        //PillerBlock3
        Frame PillerBlock3 = new FrameBuilder(getSubImage(0, 7))
                .withScale(tileScale)
                .build();

        MapTileBuilder PillerBlock3Tile = new MapTileBuilder(PillerBlock3)
                .withTileType(TileType.PILLAR);

        mapTiles.add(PillerBlock3Tile);

        //PillerBlock4
        Frame PillerBlock4 = new FrameBuilder(getSubImage(0, 8))
                .withScale(tileScale)
                .build();

        MapTileBuilder PillerBlock4Tile = new MapTileBuilder(PillerBlock4)
                .withTileType(TileType.PILLAR);

        mapTiles.add(PillerBlock4Tile);

        //PillerBlock5
        Frame PillerBlock5 = new FrameBuilder(getSubImage(1, 6))
                .withScale(tileScale)
                .build();

        MapTileBuilder PillerBlock5Tile = new MapTileBuilder(PillerBlock5)
                .withTileType(TileType.PILLAR);

        mapTiles.add(PillerBlock5Tile);

        //PillerBlock7
        Frame PillerBlock7 = new FrameBuilder(getSubImage(1, 7))
                .withScale(tileScale)
                .build();

        MapTileBuilder PillerBlock7Tile = new MapTileBuilder(PillerBlock7)
                .withTileType(TileType.PILLAR);

        mapTiles.add(PillerBlock7Tile);

        //PillerBlock8
        Frame PillerBlock8 = new FrameBuilder(getSubImage(2, 6))
                .withScale(tileScale)
                .build();

        MapTileBuilder PillerBlock8Tile = new MapTileBuilder(PillerBlock8)
                .withTileType(TileType.PILLAR);

        mapTiles.add(PillerBlock8Tile);

        //PillerBlock9
        Frame PillerBlock9 = new FrameBuilder(getSubImage(2, 7))
                .withScale(tileScale)
                .build();

        MapTileBuilder PillerBlock9Tile = new MapTileBuilder(PillerBlock9)
                .withTileType(TileType.PILLAR);

        mapTiles.add(PillerBlock9Tile);








        return mapTiles;
    }
}

     
