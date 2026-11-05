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
    private static final int ESCAPE_MAP_WIDTH = 140;
    private static final int ESCAPE_START_TILE_X = 25;
    private static final int[][] PLATFORM_TILE_LOCATIONS = {
        {10, 11},
        {3, 13},
        {17, 13}
    };
    private static final int[][] ESCAPE_PLATFORM_TILE_LOCATIONS = {
        {26, 14},
        {32, 11},
        {40, 13},
        {45, 10},
        {53, 13},
        {61, 12},
        {68, 10},
        {76, 14},
        {83, 11},
        {90, 13},
        {94, 12},
        {102, 14},
        {109, 11},
        {117, 13},
        {124, 10},
        {132, 13},
        {136, 12}
    };

    public LavaBossLevel() {
        super("lava_boss_level.txt", new LavaBossTileset());
        extendEscapeRun();
        MapTile playerStart = getMapTile(2, FLOOR_TILE_Y - 1);
        if (playerStart != null) {
            playerStartPosition = playerStart.getLocation();
        }
    }

    private void extendEscapeRun() {
        int oldWidth = width;
        int tileWidth = tileset.getScaledSpriteWidth();
        int tileHeight = tileset.getScaledSpriteHeight();
        MapTile[] extendedMapTiles = new MapTile[ESCAPE_MAP_WIDTH * height];
        for (int row = 0; row < height; row++) {
            System.arraycopy(mapTiles, row * oldWidth, extendedMapTiles, row * ESCAPE_MAP_WIDTH, oldWidth);
            int extensionTileIndex = row == FLOOR_TILE_Y ? 6 : 0;
            for (int column = oldWidth; column < ESCAPE_MAP_WIDTH; column++) {
                MapTile tile = tileset.getTile(extensionTileIndex)
                    .build(column * tileWidth, row * tileHeight);
                tile.setMap(this);
                extendedMapTiles[column + row * ESCAPE_MAP_WIDTH] = tile;
            }
        }
        width = ESCAPE_MAP_WIDTH;
        mapTiles = extendedMapTiles;
        endBoundX = getWidthPixels();

        for (int column = ESCAPE_START_TILE_X; column < width; column++) {
            MapTile lavaTile = tileset.getTile(LavaBossTileset.LAVA_TILE_INDEX)
                .build(column * tileWidth, FLOOR_TILE_Y * tileHeight);
            lavaTile.setMap(this);
            setMapTile(column, FLOOR_TILE_Y, lavaTile);
        }

        for (int[] location : ESCAPE_PLATFORM_TILE_LOCATIONS) {
            addEnhancedMapTile(new LavaBossPlatform(new Point(
                location[0] * tileWidth,
                location[1] * tileHeight
            )));
        }

        for (EnhancedMapTile enhancedTile : getEnhancedMapTiles()) {
            if (enhancedTile instanceof CaveEntrance) {
                enhancedTile.setLocation(
                    getWidthPixels() - enhancedTile.getWidth(),
                    enhancedTile.getY()
                );
            }
        }
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedTiles = new ArrayList<>();
        int tileWidth = tileset.getScaledSpriteWidth();
        int tileHeight = tileset.getScaledSpriteHeight();
        for (int[] location : PLATFORM_TILE_LOCATIONS) {
            enhancedTiles.add(new LavaBossPlatform(new Point(
                location[0] * tileWidth,
                location[1] * tileHeight
            )));
        }

        MapTile floorTile = getMapTile(BOSS_TILE_X, FLOOR_TILE_Y);
        if (floorTile != null) {
            BossArenaTrigger bossTrigger = new BossArenaTrigger(
                new Point(3 * tileWidth, (FLOOR_TILE_Y - 7) * tileHeight),
                3,
                8
            );
            float arenaRightBound = getWidthPixels();
            for (EnhancedMapTile editorTile : getEditorEnhancedMapTiles()) {
                if (editorTile instanceof LavaBossGate) {
                    bossTrigger.addGate((LavaBossGate) editorTile);
                    arenaRightBound = Math.min(arenaRightBound, editorTile.getX());
                }
            }
            bossTrigger.setBoss(new LavaBoss(
                floorTile.getLocation().subtractY(BOSS_HEIGHT),
                arenaRightBound
            ));
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
