package Maps;

import EnhancedMapTiles.BouncyPlatform;
import EnhancedMapTiles.EndLevelBox;
import Engine.ImageLoader;
import GameObject.Rectangle;
import Level.EnhancedMapTile;
import Level.Map;
import Level.MapTile;
import Tilesets.CloudTileset;

import java.util.ArrayList;

/*
 * Level 4 - the cloud level, reached from the monk temple.
 *
 * Open sky with almost nothing solid in it. Four cloud shelves give the player
 * somewhere to stand and think; everything between them is a bouncy cloud, so
 * the way across is not a series of jumps but one long bounce the player steers.
 * Landing is automatic -- BouncyPlatform throws you the moment you touch it --
 * which is what makes it feel like clouds rather than trampolines bolted to the
 * sky. Falling past the bottom of the map kills, the same as anywhere else.
 *
 * The spacing is not arbitrary. Player.bounce sets jumpForce = f and the jump
 * loop subtracts 0.5 a frame, so a launch climbs roughly f*(f+0.5) pixels over
 * 2f frames while the player steers at walkSpeed 3.5 -- about 6 tiles up and 6
 * across at these forces. Every cloud sits at a gap the launch before it clears
 * with ~2 tiles to spare. Move one a long way and the jump INTO it may stop
 * being possible, so check the cloud before it as well as the one after.
 */
public class CloudLevel extends Map {

    private static final int TILE_SCALE = 3;

    // Where the player lands on each sprite, measured off the art rather than
    // guessed. The top sits at the row where the cloud first covers 60% of its
    // width -- the shoulder under the lobes -- so the player stands on the mass
    // and not on the tip of one bump. The width is taken lower down, through
    // the body: taking it at the shoulder too would leave a box narrower than
    // the cloud looks, and you would drop through its visible edges.
    //
    // The sprites are 24px tall against a 16px tile, so a cloud hangs half a
    // tile below the row it is anchored to. That is the depth; the bounds stay
    // at the crown, so it does not change where the player lands.
    private enum Puff {
        SMALL("CloudBouncySmall.png", 1, 3, 31),
        MEDIUM("CloudBouncyMedium.png", 2, 3, 45),
        LARGE("CloudBouncyLarge.png", 3, 3, 76);

        private final String sheet;
        private final int x, y, width;

        Puff(String sheet, int x, int y, int width) {
            this.sheet = sheet;
            this.x = x;
            this.y = y;
            this.width = width;
        }

        // a fresh Rectangle each time: one shared instance would be handed to
        // every cloud built from this size
        private Rectangle bounds() {
            return new Rectangle(x, y, width, 4);
        }
    }

    public CloudLevel() {
        super("cloud_level_map.txt", new CloudTileset());

        // two rows above the start shelf, so the player drops onto it rather
        // than beginning embedded in it
        MapTile start = getMapTile(4, 30);
        if (start != null) {
            this.playerStartPosition = start.getLocation();
        }

        this.background = ImageLoader.load("SkyBackground.png");
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> tiles = new ArrayList<>();

        // opening run, low and gentle
        addCloud(tiles, 13, 30, Puff.MEDIUM, 17f);
        addCloud(tiles, 20, 28, Puff.SMALL, 16f);
        addCloud(tiles, 25, 31, Puff.MEDIUM, 18f);
        addCloud(tiles, 32, 27, Puff.SMALL, 16f);
        addCloud(tiles, 37, 30, Puff.LARGE, 19f);
        addCloud(tiles, 47, 26, Puff.MEDIUM, 17f);
        addCloud(tiles, 54, 29, Puff.SMALL, 16f);

        // climbing towards the high run
        addCloud(tiles, 66, 27, Puff.MEDIUM, 18f);
        addCloud(tiles, 73, 23, Puff.SMALL, 16f);
        addCloud(tiles, 78, 26, Puff.MEDIUM, 17f);
        addCloud(tiles, 85, 22, Puff.SMALL, 17f);
        addCloud(tiles, 91, 25, Puff.LARGE, 20f);     // the big launch
        addCloud(tiles, 102, 19, Puff.MEDIUM, 17f);
        addCloud(tiles, 109, 22, Puff.SMALL, 16f);
        addCloud(tiles, 114, 26, Puff.MEDIUM, 17f);

        // the descent to the finish
        addCloud(tiles, 128, 28, Puff.MEDIUM, 18f);
        addCloud(tiles, 135, 24, Puff.SMALL, 16f);
        addCloud(tiles, 140, 27, Puff.LARGE, 19f);
        addCloud(tiles, 150, 23, Puff.MEDIUM, 17f);
        addCloud(tiles, 157, 26, Puff.SMALL, 16f);
        addCloud(tiles, 162, 29, Puff.MEDIUM, 18f);
        addCloud(tiles, 169, 27, Puff.SMALL, 16f);

        MapTile goal = getMapTile(177, 25);
        if (goal != null) {
            tiles.add(new EndLevelBox(goal.getLocation()));
        }

        return tiles;
    }

    private void addCloud(ArrayList<EnhancedMapTile> tiles, int col, int row,
                          Puff puff, float bounceForce) {
        MapTile anchor = getMapTile(col, row);
        if (anchor == null) {
            return;
        }
        BouncyPlatform cloud = new BouncyPlatform(
                ImageLoader.load(puff.sheet),
                anchor.getLocation(),
                TILE_SCALE,
                puff.bounds());
        // Each cloud gets its own force instead of the 23f default, which throws
        // the player about 11 tiles and overshoots everything here. 16-20 keeps
        // the bounce big enough to feel weightless and small enough to aim.
        cloud.setBounceForce(bounceForce);
        tiles.add(cloud);
    }
}
