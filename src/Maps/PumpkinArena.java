package Maps;

import Enemies.Gourdzilla;
import Engine.Config;
import EnhancedMapTiles.BossArenaTrigger;
import EnhancedMapTiles.PumpkinGate;
import Level.Enemy;
import Level.EnhancedMapTile;
import Level.Map;
import Level.MapTile;
import Tilesets.PumpkinArenaTileset;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

// Gourdzilla's arena: the top 24 x 14 tiles of pumpkin_arena_map.txt.
// Two ways to use it:
//   new PumpkinArena()                 a little test map that's just the arena, to try the fight quickly
//   PumpkinArena.buildInto(level,x,y)  builds the arena into a real level (see Level1)
// Either way: walk in, the gates slam shut, and Gourdzilla drops in.
public class PumpkinArena extends Map {
    private static final int ARENA_ROWS = 14;   // rows under this in the map file are just filler for the test map

    public PumpkinArena() {
        super("pumpkin_arena_map.txt", new PumpkinArenaTileset());
        this.playerStartPosition = getMapTile(3, 9).getLocation();
    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        return new ArrayList<>();   // the boss is spawned by the trigger, not at load
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        return fightPieces(this, 0, 0);
    }

    // Builds the arena into another level with its top-left corner at tile (tileX, tileY).
    // The arena floor ends up on row tileY + 11, so use tileY = (the row your ground is on) - 11.
    // It copies the arena tiles over whatever was there, cuts a doorway through both side walls,
    // and adds the gates, the trigger and the boss.
    public static void buildInto(Map level, int tileX, int tileY) {
        PumpkinArenaTileset tileset = new PumpkinArenaTileset();
        int size = tileset.getScaledSpriteWidth();
        try (Scanner file = new Scanner(new File(Config.MAP_FILES_PATH + "pumpkin_arena_map.txt"))) {
            int width = file.nextInt();
            file.nextInt();
            for (int y = 0; y < ARENA_ROWS; y++) {
                for (int x = 0; x < width; x++) {
                    int index = file.nextInt();
                    boolean doorway = (x == 0 || x == width - 1) && (y == 9 || y == 10);
                    if (doorway) {
                        index = 60;   // bg_dark instead of bramble, so you can walk in and out
                    }
                    MapTile tile = tileset.getTile(index).build((tileX + x) * size, (tileY + y) * size);
                    tile.setMap(level);
                    level.setMapTile(tileX + x, tileY + y, tile);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Can't find MapFiles/pumpkin_arena_map.txt", e);
        }
        for (EnhancedMapTile piece : fightPieces(level, tileX, tileY)) {
            level.addEnhancedMapTile(piece);
        }
    }

    // the gates, the trigger and the boss, for an arena whose top-left tile is (tileX, tileY)
    private static ArrayList<EnhancedMapTile> fightPieces(Map map, int tileX, int tileY) {
        PumpkinGate leftGate = new PumpkinGate(map.getMapTile(tileX + 1, tileY + 8).getLocation());
        PumpkinGate rightGate = new PumpkinGate(map.getMapTile(tileX + 22, tileY + 8).getLocation());

        // the trigger goes all the way up to the roof, so you can't jump over it and sneak out the other side
        BossArenaTrigger trigger = new BossArenaTrigger(map.getMapTile(tileX + 9, tileY + 1).getLocation(), 4, 10);
        trigger.addGate(leftGate);
        trigger.addGate(rightGate);

        // where he lands: his 144px frame ends right at the floor (arena row 11). He drops in from way above it.
        trigger.setBoss(new Gourdzilla(map.getMapTile(tileX + 15, tileY + 8).getLocation()));

        ArrayList<EnhancedMapTile> pieces = new ArrayList<>();
        pieces.add(leftGate);
        pieces.add(rightGate);
        pieces.add(trigger);
        return pieces;
    }
}
