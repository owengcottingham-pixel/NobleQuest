package Maps;

import Enemies.Goblin;
import Enemies.DinosaurEnemy;
import Enemies.HobGoblin;
import Engine.ImageLoader;
import EnhancedMapTiles.EndLevelBox;
import EnhancedMapTiles.HorizontalMovingPlatform;
import EnhancedMapTiles.VerticalMovingPlatform;
import EnhancedMapTiles.DisappearingPlatform;
import EnhancedMapTiles.BouncyPlatform;
import EnhancedMapTiles.SuperBouncyPlatform;
import GameObject.Rectangle;
import Level.*;
import NPCs.Walrus;
import Tilesets.CommonTileset;
import Utils.Direction;
import EnhancedMapTiles.FallingObject;
import EnhancedMapTiles.AmbushTrigger;
import EnhancedMapTiles.Gate;
import EnhancedMapTiles.CaveEntrance;


import java.util.ArrayList;

// Represents a test map to be used in a level
public class Level1 extends Map {

    public Level1() {
        super("level1_map.txt", new CommonTileset());
        this.playerStartPosition = getMapTile(3, 71).getLocation();
        //this.playerStartPosition = getMapTile(200, 45).getLocation();
        //this.playerStartPosition = getMapTile(255, 23).getLocation();

        this.background = ImageLoader.load("Background.png");
    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        
        ArrayList<Enemy> enemies = new ArrayList<>();

        Goblin gob1 = new Goblin(getMapTile(60, 65).getLocation().subtractY(80), Direction.LEFT);
        enemies.add(gob1);

        Goblin gob2 = new Goblin(getMapTile(63, 65).getLocation().subtractY(80), Direction.LEFT);
        enemies.add(gob2);

        Goblin gob3 = new Goblin(getMapTile(68, 65).getLocation().subtractY(80), Direction.LEFT);
        enemies.add(gob3);

        Goblin gob4 = new Goblin(getMapTile(50, 84).getLocation().subtractY(80), Direction.LEFT);
        enemies.add(gob4);

        return enemies;
    }

    @Override
public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
    ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

    VerticalMovingPlatform vmp1 = new VerticalMovingPlatform(
        ImageLoader.load("GreenPlatform.png"),
        getMapTile(48, 65).getLocation(),
        getMapTile(48, 71).getLocation(),
        TileType.JUMP_THROUGH_PLATFORM,
        3,
        new Rectangle(0, 6,16,4),
        Direction.DOWN,
        3
    );
    vmp1.setStartOffset(0);
    enhancedMapTiles.add(vmp1);


    VerticalMovingPlatform vmp2 = new VerticalMovingPlatform(
        ImageLoader.load("GreenPlatform.png"),
        getMapTile(208, 38).getLocation(),
        getMapTile(208, 44).getLocation(),
        TileType.JUMP_THROUGH_PLATFORM,
        3,
        new Rectangle(0, 6,16,4),
        Direction.DOWN,
        3
    );
    vmp2.setStartOffset(6);
    enhancedMapTiles.add(vmp2);

    VerticalMovingPlatform vmp3 = new VerticalMovingPlatform(
        ImageLoader.load("GreenPlatform.png"),
        getMapTile(214, 30).getLocation(),
        getMapTile(214, 36).getLocation(),
        TileType.JUMP_THROUGH_PLATFORM,
        3,
        new Rectangle(0, 6,16,4),
        Direction.DOWN,
        3
    );
    vmp3.setStartOffset(0);
    enhancedMapTiles.add(vmp3);

    VerticalMovingPlatform vmp4 = new VerticalMovingPlatform(
        ImageLoader.load("GreenPlatform.png"),
        getMapTile(220, 22).getLocation(),
        getMapTile(220, 28).getLocation(),
        TileType.JUMP_THROUGH_PLATFORM,
        3,
        new Rectangle(0, 6,16,4),
        Direction.DOWN,
        3
    );
    vmp4.setStartOffset(6);
    enhancedMapTiles.add(vmp4);

    VerticalMovingPlatform vmp5 = new VerticalMovingPlatform(
        ImageLoader.load("GreenPlatform.png"),
        getMapTile(214, 14).getLocation(),
        getMapTile(214, 20).getLocation(),
        TileType.JUMP_THROUGH_PLATFORM,
        3,
        new Rectangle(0, 6,16,4),
        Direction.DOWN,
        3
    );

    enhancedMapTiles.add(vmp5);

    DisappearingPlatform dp1 = new DisappearingPlatform(
        getMapTile(44, 74).getX(),
        getMapTile(44, 74).getY(),
        3,
        new Rectangle(0, 6, 16, 4),
        4
    );

    enhancedMapTiles.add(dp1);

    DisappearingPlatform dp2 = new DisappearingPlatform(
        getMapTile(44, 76).getX(),
        getMapTile(44, 76).getY(),
        3,
        new Rectangle(0, 6, 16, 4),
        4
    );

    enhancedMapTiles.add(dp2);
    
    DisappearingPlatform dp3 = new DisappearingPlatform(
        getMapTile(44, 78).getX(),
        getMapTile(44, 78).getY(),
        3,
        new Rectangle(0, 6, 16, 4),
        4
    );

    enhancedMapTiles.add(dp3);

    DisappearingPlatform dp4 = new DisappearingPlatform(
        getMapTile(131, 37).getX(),
        getMapTile(131, 37).getY(),
        3,
        new Rectangle(0, 6, 16, 4),
        3
    );

    enhancedMapTiles.add(dp4);

    DisappearingPlatform dp5 = new DisappearingPlatform(
        getMapTile(138, 39).getX(),
        getMapTile(138, 39).getY(),
        3,
        new Rectangle(0, 6, 16, 4),
        3
    );

    enhancedMapTiles.add(dp5);

    DisappearingPlatform dp6 = new DisappearingPlatform(
        getMapTile(146, 41).getX(),
        getMapTile(146, 41).getY(),
        3,
        new Rectangle(0, 6, 16, 4),
        2
    );

    enhancedMapTiles.add(dp6);

    DisappearingPlatform dp7 = new DisappearingPlatform(
        getMapTile(153, 39).getX(),
        getMapTile(153, 39).getY(),
        3,
        new Rectangle(0, 6, 16, 4),
        3
    );

    enhancedMapTiles.add(dp7);

    DisappearingPlatform dp8 = new DisappearingPlatform(
        getMapTile(160, 37).getX(),
        getMapTile(160, 37).getY(),
        3,
        new Rectangle(0, 6, 16, 4),
        3
    );

    enhancedMapTiles.add(dp8);

    DisappearingPlatform dp9 = new DisappearingPlatform(
        getMapTile(236, 12).getX(),
        getMapTile(236, 12).getY(),
        3,
        new Rectangle(0, 6, 16, 4),
        2
    );

    enhancedMapTiles.add(dp9);

    DisappearingPlatform dp10 = new DisappearingPlatform(
        getMapTile(241, 17).getX(),
        getMapTile(241, 17).getY(),
        3,
        new Rectangle(0, 6, 16, 4),
        2
    );

    enhancedMapTiles.add(dp10);

    DisappearingPlatform dp11 = new DisappearingPlatform(
        getMapTile(246, 21).getX(),
        getMapTile(246, 21).getY(),
        3,
        new Rectangle(0, 6, 16, 4),
        2
    );

    enhancedMapTiles.add(dp11);


  

    BouncyPlatform bouncy1 = new BouncyPlatform(
    ImageLoader.load("BouncyPlatform.png"),
    getMapTile(85, 84).getLocation(),
    3,
    new Rectangle(0, 6, 16, 4),
    3
    );

    enhancedMapTiles.add(bouncy1);

    BouncyPlatform bouncy2 = new BouncyPlatform(
    ImageLoader.load("BouncyPlatform.png"),
    getMapTile(89, 82).getLocation(),
    3,
    new Rectangle(0, 6, 16, 4),
    3
    );

    enhancedMapTiles.add(bouncy2);

    BouncyPlatform bouncy3 = new BouncyPlatform(
    ImageLoader.load("BouncyPlatform.png"),
    getMapTile(94, 80).getLocation(),
    3,
    new Rectangle(0, 6, 16, 4),
    3
    );

    enhancedMapTiles.add(bouncy3);

    BouncyPlatform bouncy4 = new BouncyPlatform(
    ImageLoader.load("BouncyPlatform.png"),
    getMapTile(100, 78).getLocation(),
    3,
    new Rectangle(0, 6, 16, 4),
    3
    );

    enhancedMapTiles.add(bouncy4);

    BouncyPlatform bouncy5 = new BouncyPlatform(
    ImageLoader.load("BouncyPlatform.png"),
    getMapTile(270, 23).getLocation(),
    3,
    new Rectangle(0, 6, 16, 4),
    4
    );

    enhancedMapTiles.add(bouncy5);

    SuperBouncyPlatform superBouncy1 = new SuperBouncyPlatform(
    ImageLoader.load("SuperBouncyPlatform.png"),
    getMapTile(105, 81).getLocation(),
    3,
    new Rectangle(0, 6, 16, 4),
    3
    );

    enhancedMapTiles.add(superBouncy1);

    SuperBouncyPlatform chute1 = new SuperBouncyPlatform(
        ImageLoader.load("SuperBouncyPlatform.png"),
        getMapTile(105, 70).getLocation(),
        3,
        new Rectangle(0, 6, 16, 4),
        3
        );

    superBouncy1.addChutePlatform(chute1);

    SuperBouncyPlatform chute2 = new SuperBouncyPlatform(
        ImageLoader.load("SuperBouncyPlatform.png"),
        getMapTile(105, 59).getLocation(),
        3,
        new Rectangle(0, 6, 16, 4),
        3
        );

    superBouncy1.addChutePlatform(chute2);

    SuperBouncyPlatform chute3 = new SuperBouncyPlatform(
        ImageLoader.load("SuperBouncyPlatform.png"),
        getMapTile(105, 48).getLocation(),
        3,
        new Rectangle(0, 6, 16, 4),
        3
        );

    superBouncy1.addChutePlatform(chute3);

    HorizontalMovingPlatform hmp1 = new HorizontalMovingPlatform(
        ImageLoader.load("GreenPlatform.png"),
        getMapTile(55, 82).getLocation(),
        getMapTile(60, 82).getLocation(),
        TileType.JUMP_THROUGH_PLATFORM,
        3,
        new Rectangle(0, 6, 16, 4),
        Direction.RIGHT,
        3
    );

    enhancedMapTiles.add(hmp1);
    
    HorizontalMovingPlatform hmp2 = new HorizontalMovingPlatform(
        ImageLoader.load("GreenPlatform.png"),
        getMapTile(62, 79).getLocation(),
        getMapTile(67, 79).getLocation(),
        TileType.JUMP_THROUGH_PLATFORM,
        3,
        new Rectangle(0, 6, 16, 4),
        Direction.RIGHT,
        3
    );

    enhancedMapTiles.add(hmp2);

    HorizontalMovingPlatform hmp3 = new HorizontalMovingPlatform(
        ImageLoader.load("GreenPlatform.png"),
        getMapTile(69, 82).getLocation(),
        getMapTile(74, 82).getLocation(),
        TileType.JUMP_THROUGH_PLATFORM,
        3,
        new Rectangle(0, 6, 16, 4),
        Direction.RIGHT,
        3
    );

    enhancedMapTiles.add(hmp3);

    HorizontalMovingPlatform hmp4 = new HorizontalMovingPlatform(
        ImageLoader.load("GreenPlatform.png"),
        getMapTile(76, 79).getLocation(),
        getMapTile(81, 79).getLocation(),
        TileType.JUMP_THROUGH_PLATFORM,
        3,
        new Rectangle(0, 6, 16, 4),
        Direction.RIGHT,
        3
    );

    enhancedMapTiles.add(hmp4);

    HorizontalMovingPlatform hmp5 = new HorizontalMovingPlatform(
        ImageLoader.load("GreenPlatform.png"),
        getMapTile(269, 17).getLocation(),
        getMapTile(273, 17).getLocation(),
        TileType.JUMP_THROUGH_PLATFORM,
        3,
        new Rectangle(0, 6, 16, 4),
        Direction.RIGHT,
        2
    );

    enhancedMapTiles.add(hmp5);

    HorizontalMovingPlatform hmp6 = new HorizontalMovingPlatform(
        ImageLoader.load("GreenPlatform.png"),
        getMapTile(220, 12).getLocation(),
        getMapTile(230, 12).getLocation(),
        TileType.JUMP_THROUGH_PLATFORM,
        3,
        new Rectangle(0, 6, 16, 4),
        Direction.RIGHT,
        2
    );

    enhancedMapTiles.add(hmp6);

    FallingObject fallingObject = new FallingObject(
        getMapTile(6, 5).getX(),
        getMapTile(6, 5).getY()
    );

    enhancedMapTiles.add(fallingObject);

    Gate gate = new Gate(getMapTile(288, 16).getLocation(), 9);
    enhancedMapTiles.add(gate);

    AmbushTrigger ambush = new AmbushTrigger(getMapTile(270, 20).getLocation(), 4, 4);
    ambush.addEnemy(new HobGoblin(getMapTile(262, 12).getLocation(), Direction.RIGHT));
    ambush.addEnemy(new HobGoblin(getMapTile(281, 12).getLocation(), Direction.LEFT));
    ambush.addGate(gate);
    enhancedMapTiles.add(ambush);

    CaveEntrance cave = new CaveEntrance(getMapTile(309, 22).getLocation(), 2, 2);    enhancedMapTiles.add(cave);

    return enhancedMapTiles;
}

    @Override
    public ArrayList<NPC> loadNPCs() {
        ArrayList<NPC> npcs = new ArrayList<>();

        Walrus walrus = new Walrus(getMapTile(12, 71).getLocation().subtractY(13));
        npcs.add(walrus);

        return npcs;
    }
}
