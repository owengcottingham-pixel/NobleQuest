package Maps;

import Enemies.LavaBoss;
import EnhancedMapTiles.BossArenaTrigger;
import EnhancedMapTiles.CaveEntrance;
import EnhancedMapTiles.LavaBossGate;
import EnhancedMapTiles.LavaBossPlatform;
import Level.EnhancedMapTile;
import Level.Enemy;
import Level.Map;
import Level.MapTile;
import Tilesets.LavaBossTileset;
import Utils.Point;

import java.util.ArrayList;

public class LavaBossLevel extends Map {
    private static final int BOSS_TILE_X = 12;
    private static final int FLOOR_TILE_Y = 17;
    private static final int BOSS_HEIGHT = 256;
    private static final int[][] PLATFORM_TILE_LOCATIONS = {
        {10, 11},
        {3, 13},
        {17, 13}
    };

    public LavaBossLevel() {
        super("lava_boss_level.txt", new LavaBossTileset());
        MapTile playerStart = getMapTile(2, FLOOR_TILE_Y - 1);
        if (playerStart != null) {
            playerStartPosition = playerStart.getLocation();
        }
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedTiles = new ArrayList<>();
        int tileWidth = tileset.getScaledSpriteWidth();
        int tileHeight = tileset.getScaledSpriteHeight();
        for (int[] location : PLATFORM_TILE_LOCATIONS) {
            enhancedTiles.add(new LavaBossPlatform(new Point(location[0] * tileWidth, location[1] * tileHeight)));
        }

        MapTile floorTile = getMapTile(BOSS_TILE_X, FLOOR_TILE_Y);
        if (floorTile != null) {
            BossArenaTrigger bossTrigger = new BossArenaTrigger(
                new Point(3 * tileWidth, (FLOOR_TILE_Y - 7) * tileHeight),
                3,
                8
            );
            for (EnhancedMapTile editorTile : getEditorEnhancedMapTiles()) {
                if (editorTile instanceof LavaBossGate) {
                    bossTrigger.addGate((LavaBossGate) editorTile);
                }
            }
            bossTrigger.setBoss(new LavaBoss(floorTile.getLocation().subtractY(BOSS_HEIGHT)));
            MapTile caveTile = getMapTile(0, FLOOR_TILE_Y - 4);
            if (caveTile != null) {
                CaveEntrance caveEntrance = new CaveEntrance(caveTile.getLocation());
                caveEntrance.setLocation(
                    Math.max(0, getWidthPixels() - caveEntrance.getWidth()),
                    caveTile.getY()
                );
                caveEntrance.setEnabled(false);
                enhancedTiles.add(caveEntrance);
                bossTrigger.addCaveEntrance(caveEntrance);
            }
            enhancedTiles.add(bossTrigger);
        }
        return enhancedTiles;
    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        return new ArrayList<>();
    }
}
