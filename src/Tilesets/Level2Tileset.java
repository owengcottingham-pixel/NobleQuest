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

        // Keep the stone background passable and collide with the visible pillar sections.
        mapTiles.add(pillarTile(0, 5, 13, 1, 3, 5));
        mapTiles.add(pillarTile(0, 6, 0, 0, 16, 16));
        mapTiles.add(pillarTile(0, 7, 0, 0, 16, 16));
        mapTiles.add(pillarTile(0, 8, 0, 1, 3, 5));
        mapTiles.add(pillarTile(1, 6, 9, 0, 7, 16));
        mapTiles.add(pillarTile(1, 7, 0, 0, 7, 16));
        mapTiles.add(pillarTile(2, 6, 5, 0, 11, 16));
        mapTiles.add(pillarTile(2, 7, 0, 0, 11, 16));

        //aBlock
        Frame ABlock = new FrameBuilder(getSubImage(3, 0))
                .withScale(tileScale)
                .build();

        MapTileBuilder ABlockTile = new MapTileBuilder(ABlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(ABlockTile);
        //bBlock
        Frame BBlock = new FrameBuilder(getSubImage(3, 1))
                .withScale(tileScale)
                .build();

        MapTileBuilder BBlockTile = new MapTileBuilder(BBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(BBlockTile);
        //CBlock
        Frame CBlock = new FrameBuilder(getSubImage(3, 2))
                .withScale(tileScale)
                .build();

        MapTileBuilder CBlockTile = new MapTileBuilder(CBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(CBlockTile);

        //DBlock
        Frame DBlock = new FrameBuilder(getSubImage(3, 3))
                .withScale(tileScale)
                .build();

        MapTileBuilder DBlockTile = new MapTileBuilder(DBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(DBlockTile);
        //EBlock
        Frame EBlock = new FrameBuilder(getSubImage(3, 4))
                .withScale(tileScale)
                .build();

        MapTileBuilder EBlockTile = new MapTileBuilder(EBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(EBlockTile);
        //FBlock
       Frame FBlock = new FrameBuilder(getSubImage(3, 5))
                .withScale(tileScale)
                .build();

        MapTileBuilder FBlockTile = new MapTileBuilder(FBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(FBlockTile);
        //GBlock
        Frame GBlock = new FrameBuilder(getSubImage(3, 6))
                .withScale(tileScale)
                .build();

        MapTileBuilder GBlockTile = new MapTileBuilder(GBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(GBlockTile);
        //HBlock
        Frame HBlock = new FrameBuilder(getSubImage(3, 7))
                .withScale(tileScale)
                .build();

        MapTileBuilder HBlockTile = new MapTileBuilder(HBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(HBlockTile);

       //IBlock
       Frame IBlock = new FrameBuilder(getSubImage(3, 8))
                .withScale(tileScale)
                .build();

        MapTileBuilder IBlockTile = new MapTileBuilder(IBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(IBlockTile);
        //JBlock
        Frame JBlock = new FrameBuilder(getSubImage(3, 9))
                .withScale(tileScale)
                .build();

        MapTileBuilder JBlockTile = new MapTileBuilder(JBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(JBlockTile);
        //KBlock
        Frame KBlock = new FrameBuilder(getSubImage(3, 10))
                .withScale(tileScale)
                .build();

        MapTileBuilder KBlockTile = new MapTileBuilder(KBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(KBlockTile);
        //LBlock
        Frame LBlock = new FrameBuilder(getSubImage(3, 11))
                .withScale(tileScale)
                .build();

        MapTileBuilder LBlockTile = new MapTileBuilder(LBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(LBlockTile);
        //MBlock
        Frame MBlock = new FrameBuilder(getSubImage(4, 0))
                .withScale(tileScale)
                .build();

        MapTileBuilder MBlockTile = new MapTileBuilder(MBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(MBlockTile);
        //NBlock
        Frame NBlock = new FrameBuilder(getSubImage(4, 1))
                .withScale(tileScale)
                .build();

        MapTileBuilder NBlockTile = new MapTileBuilder(NBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(NBlockTile);
        //OBlock
        Frame OBlock = new FrameBuilder(getSubImage(4, 2))
                .withScale(tileScale)
                .build();

        MapTileBuilder OBlockTile = new MapTileBuilder(OBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(OBlockTile);
        //PBlock
        Frame PBlock = new FrameBuilder(getSubImage(4, 3))
                .withScale(tileScale)
                .build();

        MapTileBuilder PBlockTile = new MapTileBuilder(PBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(PBlockTile);
        //QBlock
        Frame QBlock = new FrameBuilder(getSubImage(4, 4))
                .withScale(tileScale)
                .build();

        MapTileBuilder QBlockTile = new MapTileBuilder(QBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(QBlockTile);
        //RBlock
        Frame RBlock = new FrameBuilder(getSubImage(4, 5))
                .withScale(tileScale)
                .build();

        MapTileBuilder RBlockTile = new MapTileBuilder(RBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(RBlockTile);
        //SBlock
        Frame SBlock = new FrameBuilder(getSubImage(4, 6))
                .withScale(tileScale)
                .build();

        MapTileBuilder SBlockTile = new MapTileBuilder(SBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(SBlockTile);
        //TBlock
        Frame TBlock = new FrameBuilder(getSubImage(4, 7))
                .withScale(tileScale)
                .build();

        MapTileBuilder TBlockTile = new MapTileBuilder(TBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(TBlockTile);
        //UBlock
        Frame UBlock = new FrameBuilder(getSubImage(4, 8))
                .withScale(tileScale)
                .build();

        MapTileBuilder UBlockTile = new MapTileBuilder(UBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(UBlockTile);
        //VBlock
        Frame VBlock = new FrameBuilder(getSubImage(4, 9))
                .withScale(tileScale)
                .build();

        MapTileBuilder VBlockTile = new MapTileBuilder(VBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(VBlockTile);
        //WBlock
        Frame WBlock = new FrameBuilder(getSubImage(4, 10))
                .withScale(tileScale)
                .build();

        MapTileBuilder WBlockTile = new MapTileBuilder(WBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(WBlockTile);
        //XBlock
        Frame XBlock = new FrameBuilder(getSubImage(4, 11))
                .withScale(tileScale)
                .build();

        MapTileBuilder XBlockTile = new MapTileBuilder(XBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(XBlockTile);
        //YBlock
        Frame YBlock = new FrameBuilder(getSubImage(5, 0))
                .withScale(tileScale)
                .build();

        MapTileBuilder YBlockTile = new MapTileBuilder(YBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(YBlockTile);
        //ZBlock
        Frame ZBlock = new FrameBuilder(getSubImage(5, 1))
                .withScale(tileScale)
                .build();

        MapTileBuilder ZBlockTile = new MapTileBuilder(ZBlock)
                .withTileType(TileType.PASSABLE);

        mapTiles.add(ZBlockTile);


        //RockSpicks
        Frame RockSpicks = new FrameBuilder(getSubImage(1, 5))
                .withScale(tileScale)
                .build();

        MapTileBuilder RockSpicksTile = new MapTileBuilder(RockSpicks)
                .withTileType(TileType.SPIKES);

        mapTiles.add(RockSpicksTile);

        //Upside-Down RockSpicks
        Frame UpsideDownRockSpicks = new FrameBuilder(getSubImage(2, 5))
                .withScale(tileScale)
                .build();

        MapTileBuilder UpsideDownRockSpicksTile = new MapTileBuilder(UpsideDownRockSpicks)
                .withTileType(TileType.SPIKES);

        mapTiles.add(UpsideDownRockSpicksTile);

       



        return mapTiles;


    }

    private MapTileBuilder pillarTile(int row, int col, int x, int y, int width, int height) {
        Frame frame = new FrameBuilder(getSubImage(row, col))
                .withScale(tileScale)
                .withBounds(x, y, width, height)
                .build();
        return new MapTileBuilder(frame).withTileType(TileType.NOT_PASSABLE);
    }
}

     
