package Maps;

import Enemies.LavaBoss;
import Level.Enemy;
import Level.Map;
import Level.MapTile;
import Tilesets.Level2Tileset;
import Utils.Point;

import java.util.ArrayList;

public class LavaBossLevel extends Map {
    private static final int BOSS_TILE_X = 12;
    private static final int FLOOR_TILE_Y = 17;
    private static final int BOSS_HEIGHT = 256;

    public LavaBossLevel() {
        super("lava_boss_level.txt", new Level2Tileset());
        MapTile playerStart = getMapTile(2, FLOOR_TILE_Y - 1);
        if (playerStart != null) {
            playerStartPosition = playerStart.getLocation();
        }
    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        MapTile floorTile = getMapTile(BOSS_TILE_X, FLOOR_TILE_Y);
        if (floorTile != null) {
            Point bossLocation = floorTile.getLocation().subtractY(BOSS_HEIGHT);
            enemies.add(new LavaBoss(bossLocation));
        }
        return enemies;
    }
}
