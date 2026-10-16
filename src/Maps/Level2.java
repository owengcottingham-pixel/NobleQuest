package Maps;

import Enemies.HobGoblin;
import Enemies.StoneEnemy;
import EnhancedMapTiles.FallingObject;
import Level.Enemy;
import Level.EnhancedMapTile;
import Level.Map;
import Level.MapTile;
import Tilesets.Level2Tileset;
import Utils.Direction;

import java.util.ArrayList;

public class Level2 extends Map {

    public Level2() {
        super("Level2_map.txt", new Level2Tileset());
        MapTile playerStartTile = getMapTile(3, 194);
        if (playerStartTile != null) {
            this.playerStartPosition = playerStartTile.getLocation();
        }
    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();

        // MapTile hobgoblinTile = getMapTile(13, 198);
        // if (hobgoblinTile != null) {
        //     HobGoblin hobgoblin = new HobGoblin(hobgoblinTile.getLocation().subtractY(80), Direction.LEFT);
        //     enemies.add(hobgoblin);
        // }

        MapTile stoneEnemyTile = getMapTile(13, 195);
        if (stoneEnemyTile != null) {
            StoneEnemy stoneEnemy = new StoneEnemy(stoneEnemyTile.getLocation().subtractY(83), Direction.LEFT);
            enemies.add(stoneEnemy);
        }

        return enemies;
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        addFallingObjectAt(enhancedMapTiles, 19, 191);
        addFallingObjectAt(enhancedMapTiles, 22, 191);

        return enhancedMapTiles;
    }

    private void addFallingObjectAt(ArrayList<EnhancedMapTile> enhancedMapTiles, int tileX, int tileY) {
        MapTile tile = getMapTile(tileX, tileY);
        if (tile != null) {
            enhancedMapTiles.add(new FallingObject(tile.getX(), tile.getY()));
        }
    }
}
