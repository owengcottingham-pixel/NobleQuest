package Maps;

import Enemies.HobGoblin;
import Enemies.MushrumEnemy;
import Enemies.StoneEnemy;
import Enemies.VerticalStoneEnemy;
import EnhancedMapTiles.BouncyPlatform;
import EnhancedMapTiles.CaveEntrance;
import EnhancedMapTiles.Coin;
import EnhancedMapTiles.DisappearingPlatform;
import EnhancedMapTiles.FallingObject;
import EnhancedMapTiles.HorizontalMovingBouncyPlatform;
import EnhancedMapTiles.HorizontalMovingPlatform;
import EnhancedMapTiles.StationaryPlatform;
import EnhancedMapTiles.SuperBouncyPlatform;
import Engine.ImageLoader;
import GameObject.Rectangle;
import Level.Enemy;
import Level.EnhancedMapTile;
import Level.Map;
import Level.MapTile;
import Level.TileType;
import Tilesets.Level2Tileset;
import Utils.Direction;
import Utils.Point;

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

         MapTile hobgoblinTile = getMapTile(53, 157);
        if (hobgoblinTile != null) {
            HobGoblin hobgoblin = new HobGoblin(hobgoblinTile.getLocation().subtractY(0), Direction.LEFT);
            enemies.add(hobgoblin);
     }

      MapTile hobgoblinTile2 = getMapTile(92, 144);
        if (hobgoblinTile2 != null) {
            HobGoblin hobgoblin = new HobGoblin(hobgoblinTile2.getLocation().subtractY(0), Direction.LEFT);
            enemies.add(hobgoblin);
     }

        MapTile mushrumStartTile = getMapTile(10, 199);
        MapTile mushrumEndTile = getMapTile(30, 199);
        if (mushrumStartTile != null && mushrumEndTile != null) {
            Point mushrumStart = mushrumStartTile.getLocation().subtractY(103);
            Point mushrumEnd = mushrumEndTile.getLocation().subtractY(103);
            enemies.add(new MushrumEnemy(mushrumStart, mushrumEnd, Direction.RIGHT));
        }

        MapTile stoneEnemyTile = getMapTile(13, 195);
        if (stoneEnemyTile != null) {
            StoneEnemy stoneEnemy = new StoneEnemy(stoneEnemyTile.getLocation().subtractY(83), Direction.LEFT);
            enemies.add(stoneEnemy);
        }

        MapTile verticalStoneEnemyTile = getMapTile(19, 161);
        MapTile verticalStoneEnemyUpperBound = getMapTile(19, 157);
        if (verticalStoneEnemyTile != null && verticalStoneEnemyUpperBound != null) {
            Point spawnLocation = verticalStoneEnemyTile.getLocation().subtractY(0);
            float lowerBoundY = spawnLocation.y;
            float upperBoundY = verticalStoneEnemyUpperBound.getY();
            enemies.add(new VerticalStoneEnemy(spawnLocation, Direction.LEFT, upperBoundY, lowerBoundY));
        }

        MapTile secondVerticalStoneEnemyTile = getMapTile(77, 173);
        MapTile secondVerticalStoneEnemyUpperBound = getMapTile(77, 160);
        if (secondVerticalStoneEnemyTile != null && secondVerticalStoneEnemyUpperBound != null) {
            Point spawnLocation = secondVerticalStoneEnemyTile.getLocation();
            float lowerBoundY = spawnLocation.y;
            float upperBoundY = secondVerticalStoneEnemyUpperBound.getY();
            enemies.add(new VerticalStoneEnemy(spawnLocation, Direction.LEFT, upperBoundY, lowerBoundY));
        }

        MapTile thirdVerticalStoneEnemyTile = getMapTile(71, 173);
        MapTile thirdVerticalStoneEnemyUpperBound = getMapTile(71, 160);
        if (thirdVerticalStoneEnemyTile != null && thirdVerticalStoneEnemyUpperBound != null) {
            Point spawnLocation = thirdVerticalStoneEnemyTile.getLocation();
            float lowerBoundY = spawnLocation.y;
            float upperBoundY = thirdVerticalStoneEnemyUpperBound.getY();
            enemies.add(new VerticalStoneEnemy(spawnLocation, Direction.RIGHT, upperBoundY, lowerBoundY));
        }

        return enemies;
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        MapTile coinTile = getMapTile(29, 138);
        if (coinTile != null) {
            enhancedMapTiles.add(new Coin(coinTile.getX(), coinTile.getY()));
        }
        MapTile coinTile2 = getMapTile(2, 198);
        if (coinTile2 != null) {
            enhancedMapTiles.add(new Coin(coinTile2.getX(), coinTile2.getY()));
        }
        

        addFallingObjectAt(enhancedMapTiles, 19, 191);
        addFallingObjectAt(enhancedMapTiles, 22, 191);
        addFallingObjectAt(enhancedMapTiles, 30, 159);
        addFallingObjectAt(enhancedMapTiles, 31, 159);
        addFallingObjectAt(enhancedMapTiles, 32, 159);
        addFallingObjectAt(enhancedMapTiles, 33, 159);
        addFallingObjectAt(enhancedMapTiles, 34, 159);
        addFallingObjectAt(enhancedMapTiles, 35, 159);
        addFallingObjectAt(enhancedMapTiles, 36, 160);
        addFallingObjectAt(enhancedMapTiles, 37, 160);
        addFallingObjectAt(enhancedMapTiles, 38, 160);
        addFallingObjectAt(enhancedMapTiles, 39, 160);
        addFallingObjectAt(enhancedMapTiles, 89, 129);
        addFallingObjectAt(enhancedMapTiles, 91, 129);
        addFallingObjectAt(enhancedMapTiles, 93, 129);


        

        enhancedMapTiles.add(new BouncyPlatform(
                ImageLoader.load("BouncyPlatform.png"),
                getMapTile(4, 175).getLocation(),
                3,
                new Rectangle(0, 6, 16, 4),
                1
        ));

        enhancedMapTiles.add(new HorizontalMovingPlatform(
                ImageLoader.load("GreenPlatform.png"),
                getMapTile(10, 192).getLocation(),
                getMapTile(13, 192).getLocation(),
                TileType.JUMP_THROUGH_PLATFORM,
                3,
                new Rectangle(0, 6, 16, 4),
                Direction.RIGHT,
                3
        ));

        enhancedMapTiles.add(new HorizontalMovingPlatform(
                ImageLoader.load("GreenPlatform.png"),
                getMapTile(8, 189).getLocation(),
                getMapTile(11, 189).getLocation(),
                TileType.JUMP_THROUGH_PLATFORM,
                3,
                new Rectangle(0, 6, 16, 4),
                Direction.RIGHT,
                3
        ));
        enhancedMapTiles.add(new HorizontalMovingPlatform(
                ImageLoader.load("GreenPlatform.png"),
                getMapTile(46, 178).getLocation(),
                getMapTile(50, 178).getLocation(),
                TileType.JUMP_THROUGH_PLATFORM,
                4,
                new Rectangle(0, 6, 16, 4),
                Direction.RIGHT,
                1
        ));

        enhancedMapTiles.add(new HorizontalMovingPlatform(
                ImageLoader.load("GreenPlatform.png"),
                getMapTile(45, 182).getLocation(),
                getMapTile(50, 182).getLocation(),
                TileType.JUMP_THROUGH_PLATFORM,
                4,
                new Rectangle(0, 6, 16, 4),
                Direction.LEFT,
                1
        ));

        enhancedMapTiles.add(new HorizontalMovingPlatform(
                ImageLoader.load("GreenPlatform.png"),
                getMapTile(43, 186).getLocation(),
                getMapTile(49, 186).getLocation(),
                TileType.JUMP_THROUGH_PLATFORM,
                4,
                new Rectangle(0, 6, 16, 4),
                Direction.LEFT,
                1
        ));

        enhancedMapTiles.add(new HorizontalMovingBouncyPlatform(
                ImageLoader.load("BouncyPlatform.png"),
                getMapTile(78, 156).getLocation(),
                getMapTile(71, 156).getLocation(),
                3,
                new Rectangle(0, 6, 16, 4),
                Direction.RIGHT,
                1
        ));

        enhancedMapTiles.add(new HorizontalMovingBouncyPlatform(
                ImageLoader.load("BouncyPlatform.png"),
                getMapTile(78, 151).getLocation(),
                getMapTile(71, 151).getLocation(),
                3,
                new Rectangle(0, 6, 16, 4),
                Direction.RIGHT,
                1
        ));

        enhancedMapTiles.add(new StationaryPlatform(
                ImageLoader.load("GreenPlatform.png"),
                getMapTile(76, 146).getLocation(),
                3,
                new Rectangle(0, 6, 16, 4),
                3
        ));


        enhancedMapTiles.add(new StationaryPlatform(
                ImageLoader.load("GreenPlatform.png"),
                getMapTile(54, 155).getLocation(),
                4,
                new Rectangle(0, 6, 16, 4),
                1
        ));

        enhancedMapTiles.add(new StationaryPlatform(
                ImageLoader.load("GreenPlatform.png"),
                getMapTile(56, 152).getLocation(),
                4,
                new Rectangle(0, 6, 16, 4),
                1
        ));

        enhancedMapTiles.add(new StationaryPlatform(
                ImageLoader.load("GreenPlatform.png"),
                getMapTile(53, 150).getLocation(),
                4,
                new Rectangle(0, 6, 16, 4),
                1
        ));

        enhancedMapTiles.add(new StationaryPlatform(
                ImageLoader.load("GreenPlatform.png"),
                getMapTile(55, 147).getLocation(),
                4,
                new Rectangle(0, 6, 16, 4),
                1
        ));

         enhancedMapTiles.add(new StationaryPlatform(
                ImageLoader.load("GreenPlatform.png"),
                getMapTile(52, 139).getLocation(),
                3,
                new Rectangle(0, 6, 16, 4),
                3
        ));

        enhancedMapTiles.add(new SuperBouncyPlatform(
                ImageLoader.load("SuperBouncyPlatform.png"),
                getMapTile(53, 144).getLocation(),
                3,
                new Rectangle(0, 6, 16, 4),
                2
        ));

        MapTile disappearingPlatformStart = getMapTile(12, 185);
        enhancedMapTiles.add(new DisappearingPlatform(
                disappearingPlatformStart.getX(),
                disappearingPlatformStart.getY(),
                3,
                new Rectangle(0, 6, 16, 4),
                3
        ));

        MapTile secondDisappearingPlatformStart = getMapTile(9, 181);
        enhancedMapTiles.add(new DisappearingPlatform(
                secondDisappearingPlatformStart.getX(),
                secondDisappearingPlatformStart.getY(),
                3,
                new Rectangle(0, 6, 16, 4),
                2
        ));

        enhancedMapTiles.add(new BouncyPlatform(
                ImageLoader.load("BouncyPlatform.png"),
                getMapTile(46, 192).getLocation(),
                3,
                new Rectangle(0, 6, 16, 4),
                3
        ));

        MapTile thirdDisappearingPlatformStart = getMapTile(74, 171);
        enhancedMapTiles.add(new DisappearingPlatform(
                thirdDisappearingPlatformStart.getX(),
                thirdDisappearingPlatformStart.getY(),
                3,
                new Rectangle(0, 6, 16, 4),
                3
        ));

        MapTile fourthDisappearingPlatformStart = getMapTile(74, 167);
        enhancedMapTiles.add(new DisappearingPlatform(
                fourthDisappearingPlatformStart.getX(),
                fourthDisappearingPlatformStart.getY(),
                3,
                new Rectangle(0, 6, 16, 4),
                3
        ));

        MapTile fifthDisappearingPlatformStart = getMapTile(74, 163);
        enhancedMapTiles.add(new DisappearingPlatform(
                fifthDisappearingPlatformStart.getX(),
                fifthDisappearingPlatformStart.getY(),
                3,
                new Rectangle(0, 6, 16, 4),
                3
        ));

        MapTile sixthDisappearingPlatformStart = getMapTile(74, 159);
        enhancedMapTiles.add(new DisappearingPlatform(
                sixthDisappearingPlatformStart.getX(),
                sixthDisappearingPlatformStart.getY(),
                3,
                new Rectangle(0, 6, 16, 4),
                3
        ));

        enhancedMapTiles.add(new CaveEntrance(getMapTile(94, 143).getLocation(), 1, 3));

        return enhancedMapTiles;
    }

    private void addFallingObjectAt(ArrayList<EnhancedMapTile> enhancedMapTiles, int tileX, int tileY) {
        MapTile tile = getMapTile(tileX, tileY);
        if (tile != null) {
            enhancedMapTiles.add(new FallingObject(tile.getX(), tile.getY()));
        }
    }
}
