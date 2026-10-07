package Maps;


import Enemies.DinosaurEnemy;
import Enemies.Goblin;
import Enemies.HobGoblin;
import Engine.ImageLoader;
import EnhancedMapTiles.EndLevelBox;
import EnhancedMapTiles.HorizontalMovingPlatform;
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
        this.playerStartPosition = getMapTile(2, 11).getLocation();
    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        
        ArrayList<Enemy> enemies = new ArrayList<>();

        Goblin gob1 = new Goblin(getMapTile(50, 53).getLocation().subtractY(80), Direction.LEFT);
        enemies.add(gob1);

        HobGoblin hobgob = new HobGoblin(getMapTile(50, 53).getLocation().subtractY(80), Direction.LEFT);
        enemies.add(hobgob);
        
        

        return enemies;
    }

    @Override
public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
    ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

    HorizontalMovingPlatform hmp = new HorizontalMovingPlatform(
        ImageLoader.load("GreenPlatform.png"),
        getMapTile(24, 6).getLocation(),
        getMapTile(27, 6).getLocation(),
        TileType.JUMP_THROUGH_PLATFORM,
        3,
        new Rectangle(0, 6,16,4),
        Direction.RIGHT
    );

    enhancedMapTiles.add(hmp);

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
