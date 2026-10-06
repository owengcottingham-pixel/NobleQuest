package Maps;

import Enemies.BugEnemy;
import Enemies.DinosaurEnemy;
import Enemies.HobGoblin;
import Engine.ImageLoader;
import EnhancedMapTiles.EndLevelBox;
import EnhancedMapTiles.HorizontalMovingPlatform;
import EnhancedMapTiles.VerticalMovingPlatform;
import EnhancedMapTiles.BouncyPlatform;
import EnhancedMapTiles.SuperBouncyPlatform;
import GameObject.Rectangle;
import Level.*;
import NPCs.Walrus;
import Tilesets.CommonTileset;
import Utils.Direction;
import EnhancedMapTiles.FallingObject;

import java.util.ArrayList;

// Represents a test map to be used in a level
public class TestMap extends Map {

    public TestMap() {
        super("test_map.txt", new CommonTileset());
        this.playerStartPosition = getMapTile(3, 40).getLocation();
        this.background = ImageLoader.load("Background.png");
    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        
        ArrayList<Enemy> enemies = new ArrayList<>();

        BugEnemy bugEnemy1 = new BugEnemy(getMapTile(50, 53).getLocation().subtractY(80), Direction.LEFT);
        enemies.add(bugEnemy1);

<<<<<<< HEAD
        BugEnemy bugEnemy2 = new BugEnemy(getMapTile(54, 27).getLocation().subtractY(80), Direction.RIGHT);
        enemies.add(bugEnemy2);

        BugEnemy bugEnemy3 = new BugEnemy(getMapTile(77, 27).getLocation().subtractY(80), Direction.LEFT);
        enemies.add(bugEnemy3);
=======
        HobGoblin hobgob = new HobGoblin(getMapTile(19, 1).getLocation().subtractY(80), Direction.LEFT);
        enemies.add(hobgob);
        
        
>>>>>>> fa9d6bf28ab9cf682d1da72df123834b06ab1db7

        return enemies;
    }

    @Override
public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
    ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

    VerticalMovingPlatform vmp = new VerticalMovingPlatform(
        ImageLoader.load("GreenPlatform.png"),
        getMapTile(48, 35).getLocation(),
        getMapTile(48, 41).getLocation(),
        TileType.JUMP_THROUGH_PLATFORM,
        3,
        new Rectangle(0, 6,16,4),
        Direction.DOWN,
        3
    );

    enhancedMapTiles.add(vmp);

    BouncyPlatform bouncy1 = new BouncyPlatform(
    ImageLoader.load("BouncyPlatform.png"),
    getMapTile(85, 54).getLocation(),
    3,
    new Rectangle(0, 6, 16, 4),
    3
    );

    enhancedMapTiles.add(bouncy1);

    BouncyPlatform bouncy2 = new BouncyPlatform(
    ImageLoader.load("BouncyPlatform.png"),
    getMapTile(89, 52).getLocation(),
    3,
    new Rectangle(0, 6, 16, 4),
    3
    );

    enhancedMapTiles.add(bouncy2);

    BouncyPlatform bouncy3 = new BouncyPlatform(
    ImageLoader.load("BouncyPlatform.png"),
    getMapTile(94, 50).getLocation(),
    3,
    new Rectangle(0, 6, 16, 4),
    3
    );

    enhancedMapTiles.add(bouncy3);

    BouncyPlatform bouncy4 = new BouncyPlatform(
    ImageLoader.load("BouncyPlatform.png"),
    getMapTile(100, 48).getLocation(),
    3,
    new Rectangle(0, 6, 16, 4),
    3
    );

    enhancedMapTiles.add(bouncy4);

    SuperBouncyPlatform superBouncy1 = new SuperBouncyPlatform(
    ImageLoader.load("SuperBouncyPlatform.png"),
    getMapTile(105, 51).getLocation(),
    3,
    new Rectangle(0, 6, 16, 4),
    3
    );

    enhancedMapTiles.add(superBouncy1);

    SuperBouncyPlatform chute1 = new SuperBouncyPlatform(
        ImageLoader.load("SuperBouncyPlatform.png"),
        getMapTile(105, 40).getLocation(),
        3,
        new Rectangle(0, 6, 16, 4),
        3
        );

    superBouncy1.addChutePlatform(chute1);

    SuperBouncyPlatform chute2 = new SuperBouncyPlatform(
        ImageLoader.load("SuperBouncyPlatform.png"),
        getMapTile(105, 29).getLocation(),
        3,
        new Rectangle(0, 6, 16, 4),
        3
        );

    superBouncy1.addChutePlatform(chute2);

    SuperBouncyPlatform chute3 = new SuperBouncyPlatform(
        ImageLoader.load("SuperBouncyPlatform.png"),
        getMapTile(105, 18).getLocation(),
        3,
        new Rectangle(0, 6, 16, 4),
        3
        );

    superBouncy1.addChutePlatform(chute3);

    HorizontalMovingPlatform hmp1 = new HorizontalMovingPlatform(
        ImageLoader.load("GreenPlatform.png"),
        getMapTile(55, 52).getLocation(),
        getMapTile(60, 52).getLocation(),
        TileType.JUMP_THROUGH_PLATFORM,
        3,
        new Rectangle(0, 6, 16, 4),
        Direction.RIGHT,
        3
    );

    enhancedMapTiles.add(hmp1);
    
    HorizontalMovingPlatform hmp2 = new HorizontalMovingPlatform(
        ImageLoader.load("GreenPlatform.png"),
        getMapTile(62, 49).getLocation(),
        getMapTile(67, 49).getLocation(),
        TileType.JUMP_THROUGH_PLATFORM,
        3,
        new Rectangle(0, 6, 16, 4),
        Direction.RIGHT,
        3
    );

    enhancedMapTiles.add(hmp2);

    HorizontalMovingPlatform hmp3 = new HorizontalMovingPlatform(
        ImageLoader.load("GreenPlatform.png"),
        getMapTile(69, 52).getLocation(),
        getMapTile(74, 52).getLocation(),
        TileType.JUMP_THROUGH_PLATFORM,
        3,
        new Rectangle(0, 6, 16, 4),
        Direction.RIGHT,
        3
    );

    enhancedMapTiles.add(hmp3);

    HorizontalMovingPlatform hmp4 = new HorizontalMovingPlatform(
        ImageLoader.load("GreenPlatform.png"),
        getMapTile(76, 49).getLocation(),
        getMapTile(81, 49).getLocation(),
        TileType.JUMP_THROUGH_PLATFORM,
        3,
        new Rectangle(0, 6, 16, 4),
        Direction.RIGHT,
        3
    );

    enhancedMapTiles.add(hmp4);

    FallingObject fallingObject = new FallingObject(
        getMapTile(6, 5).getX(),
        getMapTile(6, 5).getY()
    );

    enhancedMapTiles.add(fallingObject);

    return enhancedMapTiles;
}

    @Override
    public ArrayList<NPC> loadNPCs() {
        ArrayList<NPC> npcs = new ArrayList<>();

        Walrus walrus = new Walrus(getMapTile(30, 10).getLocation().subtractY(13));
        npcs.add(walrus);

        return npcs;
    }
}
