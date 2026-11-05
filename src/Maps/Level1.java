package Maps;

import Enemies.ArcheryTarget;
import Enemies.ForestChunk;
import Enemies.Goblin;
import Enemies.KelpEel;
import Enemies.Gourdzilla;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.KeyBinds;
import Engine.Mouse;
import Engine.ScreenManager;
import EnhancedMapTiles.BossArenaTrigger;
import EnhancedMapTiles.BrambleWall;
import EnhancedMapTiles.CaveEntrance;
import EnhancedMapTiles.ChuteRing;
import EnhancedMapTiles.Coin;
import EnhancedMapTiles.CrackedFloor;
import EnhancedMapTiles.CrackedWall;
import EnhancedMapTiles.DisappearingPlatform;
import EnhancedMapTiles.Drawbridge;
import EnhancedMapTiles.FallingObject;
import EnhancedMapTiles.ForestEntrance;
import EnhancedMapTiles.Catapult;
import EnhancedMapTiles.FallingRock;
import EnhancedMapTiles.RuinWall;
import EnhancedMapTiles.HorizontalMovingPlatform;
import EnhancedMapTiles.BoulderTrigger;
import EnhancedMapTiles.PumpkinGate;
import EnhancedMapTiles.SlashBush;
import EnhancedMapTiles.SpringPad;
import EnhancedMapTiles.SunkenLog;
import EnhancedMapTiles.VerticalMovingPlatform;
import EnhancedMapTiles.WishingWell;
import EnhancedMapTiles.Whirlpool;
import EnhancedMapTiles.RopePost;
import EnhancedMapTiles.WoodGate;
import GameObject.Rectangle;
import Level.*;
import NPCs.DwarfShopkeeper;
import NPCs.WanderingMonk;
import Tilesets.CommonTileset;
import Utils.AirGroundState;
import Utils.Direction;
import Utils.Effects;
import Utils.Point;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.VolatileImage;
import java.util.ArrayList;

// Level 1
public class Level1 extends Map {

    // where the chute is
    private static final int CHUTE_X = 138, CHUTE_TOP = 36, CHUTE_BOTTOM = 86;
    // the dwarf's stall has see-through bits; in the forest those need a dark backing
    private static final int STALL_X = 215, STALL_Y = 43;

    // screen shake
    private float shakeX, shakeY, lookY;
    private ArrayList<Enemy> meadowGoblins;
    private ArcheryTarget target;
    private WoodGate gate;
    private Drawbridge drawbridge;
    private CrackedWall crackedWall;
    private CrackedFloor crackedFloor;
    // the reflection world under the lake
    private SunkenLog lakeLog;
    private RopePost mirrorRope;
    private ArcheryTarget mirrorTarget, mirrorTarget2;
    private ArrayList<KelpEel> eels;
    private int pairTimer;
    private Whirlpool nearWhirlpool, farWhirlpool;
    private boolean inMirror;
    private int mirrorCooldown, flash, dives, surfacings;
    private Graphics2D mirrorScreen, mirrorGraphics;
    private Image mirrorImage;
    private Checklist checklist;
    private BufferedImage bannerImage;
    private String bannerKey;
    private int updraftTick;

    public Level1() {
        super("level1_map.txt", new CommonTileset());
        this.playerStartPosition = getMapTile(14, 71).getLocation();

        //this.playerStartPosition = getMapTile(85, 82).getLocation();     // spring gauntlet
        //this.playerStartPosition = getMapTile(151, 35).getLocation();    // top of the chute
        //this.playerStartPosition = getMapTile(286, 23).getLocation();    // just before the chase
        //this.playerStartPosition = getMapTile(384, 23).getLocation();    // just before the arena

        this.background = ImageLoader.load("Background.png");
        Effects.reset();
        ForestChunk.resetCount();
        Mouse.setFlipY(false);
    }

    private Point tile(int x, int y) {
        return getMapTile(x, y).getLocation();
    }

    @Override
    public ArrayList<Enemy> loadEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        // the goblin field in the meadow
        enemies.add(new Goblin(getMapTile(37, 72).getLocation().subtractY(80), Direction.LEFT));
        enemies.add(new Goblin(getMapTile(42, 72).getLocation().subtractY(80), Direction.RIGHT));
        meadowGoblins = new ArrayList<>(enemies);
        // the target on the hill's rock face
        target = new ArcheryTarget(tile(46, 64), () -> gate.open());
        enemies.add(target);
        // two targets that wake the far whirlpool if you hit both quickly (see updateLake)
        mirrorTarget = new ArcheryTarget(tile(76, MIRROR_FLOOR - 5), this::mirrorTargetHit);
        mirrorTarget2 = new ArcheryTarget(tile(84, MIRROR_FLOOR - 4), this::mirrorTargetHit);
        enemies.add(mirrorTarget);
        enemies.add(mirrorTarget2);

        float laneTop = tile(0, MIRROR_FLOOR - 6).y + 30, laneBottom = tile(0, MIRROR_FLOOR).y - 34;
        eels = new ArrayList<>();
        eels.add(new KelpEel(new Point(72 * 48, laneBottom), Direction.LEFT, 4.6f * 48, laneTop, laneBottom));
        float targetLane = mirrorTarget.getY() + 24;
        KelpEel guard = new KelpEel(new Point(74 * 48, targetLane), Direction.RIGHT, 7f * 48, laneTop, targetLane);
        eels.add(guard);
        enemies.addAll(eels);
        mirrorTarget.shieldedWhile(() -> guard.covers(mirrorTarget.getBounds()));
        return enemies;
    }

    private VerticalMovingPlatform verticalPlatform(String image, int x, int topY, int bottomY, int offset) {
        VerticalMovingPlatform p = new VerticalMovingPlatform(
                ImageLoader.load(image), tile(x, topY), tile(x, bottomY),
                TileType.JUMP_THROUGH_PLATFORM, 3, new Rectangle(0, 6, 16, 4), Direction.DOWN, 3);
        p.setStartOffset(offset);
        return p;
    }

    private HorizontalMovingPlatform horizontalPlatform(String image, int leftX, int rightX, int y, int length) {
        return horizontalPlatform(image, leftX, rightX, y, length, Direction.RIGHT);
    }

    private HorizontalMovingPlatform horizontalPlatform(String image, int leftX, int rightX, int y, int length, Direction start) {
        return new HorizontalMovingPlatform(
                ImageLoader.load(image), tile(leftX, y), tile(rightX, y),
                TileType.JUMP_THROUGH_PLATFORM, 3, new Rectangle(0, 6, 16, 4), start, length);
    }

    private DisappearingPlatform crumblingPlatform(String image, int x, int y, int length) {
        return new DisappearingPlatform(ImageLoader.load(image), getMapTile(x, y).getX(), getMapTile(x, y).getY(),
                3, new Rectangle(0, 6, 16, 4), length);
    }

    private Coin coin(int x, int y) {
        // coins are 32x32; this centres one in the tile
        return new Coin(getMapTile(x, y).getX() + 8, getMapTile(x, y).getY() + 8);
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> tiles = new ArrayList<>();

        // THE MEADOW 
        tiles.add(new WishingWell(tile(2, 68)));          // at the far left of the village
        tiles.add(new FallingObject(getMapTile(6, 5).getX(), getMapTile(6, 5).getY()));
        // bushes to slash
        tiles.add(new SlashBush(tile(15, 71), false));
        tiles.add(new SlashBush(tile(20, 71), true));
        tiles.add(new SlashBush(tile(34, 71), true));
        tiles.add(new SlashBush(tile(44, 71), false));
        tiles.add(new SlashBush(tile(26, 77), true));     // a consolation prize down in the ravine
        // the drawbridge
        drawbridge = new Drawbridge(tile(30, 72), 6, tile(23, 69));
        tiles.add(drawbridge);
        // the hill
        gate = new WoodGate(tile(47, 69));
        tiles.add(gate);
        crackedWall = new CrackedWall(tile(51, 69));
        tiles.add(crackedWall);
        crackedFloor = new CrackedFloor(tile(54, 72));
        tiles.add(crackedFloor);
        // the lake
        lakeLog = new SunkenLog(tile(66, WATER_ROW), 8);
        tiles.add(lakeLog);

        mirrorRope = new RopePost(tile(70, MIRROR_FLOOR - 3), new Point(69 * 48, MIRROR_FLOOR * 48), () -> {
            lakeLog.release();
            nearWhirlpool.start();
            Effects.banner(Effects.BannerStyle.HINT, "Something floated up above!", null, 150);
        });
        tiles.add(mirrorRope);
        nearWhirlpool = new Whirlpool(tile(67, MIRROR_FLOOR - 1), false, (p, x) -> surface(p, x, 15f)).pulse(110, 80);
        tiles.add(nearWhirlpool);

        farWhirlpool = new Whirlpool(tile(76, MIRROR_FLOOR - 1), false, (p, x) -> surface(p, x, 17f))
                .drift(76 * 48, 81 * 48, 1.3f);
        tiles.add(farWhirlpool);
        int[][] meadowCoins = {
                { 26, 70 }, { 27, 70 }, { 28, 70 },            
                { 32, 69 },                                  
                { 52, 70 }, { 53, 70 },                       
                { 56, 79 }, { 57, 78 },                        
                { 62, 80 }, { 69, 83 }, { 71, 83 },            
                { 64, 19 }, { 65, 19 }, { 68, 21 }, { 69, 20 }, 
                { 78, 21 }, { 81, 21 }, { 83, 22 }, { 83, 21 },
        };
        for (int[] c : meadowCoins) {
            tiles.add(coin(c[0], c[1]));
        }

        // THE SPIKE GAUNTLET
        // mostly in map editor
        // the chute
        ChuteRing[] rings = {
                new ChuteRing(tile(CHUTE_X, 73)), new ChuteRing(tile(CHUTE_X, 62)),
                new ChuteRing(tile(CHUTE_X, 51)), new ChuteRing(tile(CHUTE_X, 40)),
        };
        tiles.add(new SpringPad(tile(CHUTE_X, 85), 3, SpringPad.Kind.SUPER).addRings(rings));
        tiles.add(new ForestEntrance(tile(CHUTE_X, 32), 3, 4, "THE DARK FOREST", "the old woods of the Noble Land"));

        // THE DARK FOREST
        tiles.add(crumblingPlatform("VinePlatform.png", 164, 37, 3));
        tiles.add(crumblingPlatform("VinePlatform.png", 171, 39, 3));
        tiles.add(crumblingPlatform("VinePlatform.png", 179, 41, 2));
        tiles.add(crumblingPlatform("VinePlatform.png", 186, 39, 3));
        tiles.add(crumblingPlatform("VinePlatform.png", 193, 37, 3));

        tiles.add(verticalPlatform("VinePlatform.png", 241, 38, 44, 6));
        tiles.add(verticalPlatform("VinePlatform.png", 247, 30, 36, 0));
        tiles.add(verticalPlatform("VinePlatform.png", 253, 22, 28, 6));
        tiles.add(verticalPlatform("VinePlatform.png", 247, 14, 20, 0));
        tiles.add(horizontalPlatform("VinePlatform.png", 253, 263, 12, 2));

        // coins over the crumbling platforms and up the climb
        int[][] forestCoins = {
                { 168, 35 }, { 176, 37 }, { 183, 37 }, { 190, 35 },
                { 244, 34 }, { 250, 26 }, { 256, 18 }, { 258, 10 }, { 272, 15 }, { 277, 19 },
        };
        for (int[] c : forestCoins) {
            tiles.add(coin(c[0], c[1]));
        }

        tiles.add(crumblingPlatform("VinePlatform.png", 269, 12, 2));
        tiles.add(crumblingPlatform("VinePlatform.png", 274, 17, 2));
        tiles.add(crumblingPlatform("VinePlatform.png", 279, 21, 2));

        // THE BOULDER
        RuinWall ruinWall = new RuinWall(tile(288, 21));
        tiles.add(ruinWall);
        tiles.add(new BoulderTrigger(tile(298, 16), 1, 8, tile(288, 21)).wall(ruinWall));
        tiles.add(new BrambleWall(tile(311, 19)));     // on the first rise
        tiles.add(new BrambleWall(tile(334, 26)));     // at the bottom of the valley
        tiles.add(new BrambleWall(tile(348, 20)));     // on the last rise
        // rocks shaken loose from the canopy
        tiles.add(new FallingRock(tile(319, 14), getMapTile(319, 24).getY()).triggerDistance(5.2f));
        tiles.add(new FallingRock(tile(333, 18), getMapTile(333, 28).getY()).triggerDistance(5.2f));
        tiles.add(new FallingRock(tile(357, 15), getMapTile(357, 26).getY()).triggerDistance(5.6f));
        tiles.add(new Catapult(tile(360, 24).subtractY(32)).launch(19f, 10f)); 
        int[][] chaseCoins = {
                { 322, 21 }, { 323, 21 }, { 334, 23 }, { 338, 24 }, { 347, 18 }, { 349, 18 },
                { 364, 17 }, { 367, 16 }, { 370, 16 }, { 373, 18 }, { 376, 20 }, 
        };
        for (int[] c : chaseCoins) {
            tiles.add(coin(c[0], c[1]));
        }

        // GOURDZILLA'S ARENA
        PumpkinGate leftGate = new PumpkinGate(tile(393, 21));
        PumpkinGate rightGate = new PumpkinGate(tile(414, 21));
        tiles.add(leftGate);
        tiles.add(rightGate);

        BossArenaTrigger bossTrigger = new BossArenaTrigger(tile(401, 14), 4, 10);
        bossTrigger.addGate(leftGate);
        bossTrigger.addGate(rightGate);
        bossTrigger.setBoss(new Gourdzilla(tile(407, 21)));
        tiles.add(bossTrigger);

        tiles.add(new CaveEntrance(tile(436, 20)));

        return tiles;
    }

    @Override
    public ArrayList<NPC> loadNPCs() {
        ArrayList<NPC> npcs = new ArrayList<>();

        npcs.add(new WanderingMonk(tile(12, 71).subtractY(58), "Many paths lie ahead, \nyoung knight. Do not fear, \nyou will always come \nback to me when you fail."));
        npcs.add(new WanderingMonk(tile(57, 81).subtractY(58), "This lake has another side. \nJump in and see. Watch the \nkelp for eyes, and look for \na whirlpool to bring you up."));
        npcs.add(new WanderingMonk(tile(85, 82).subtractY(58), "Stone spikes ahead. Let the \nspiked ball swing past, \nand don't stay long on the \npillars -- they sink!"));
        npcs.add(new WanderingMonk(tile(151, 35).subtractY(58), "The stones ahead crumble \nbeneath slow feet. \nDo not linger, young knight."));
        npcs.add(new WanderingMonk(tile(230, 45).subtractY(58), "Have you met our shopkeeper? \nHe's got the finest hats \nin all the Noble Land."));
        npcs.add(new WanderingMonk(tile(291, 23).subtractY(58), "Do you hear that rumbling? \nSomething big rolls in \nthese woods. Whatever you \ndo, don't stop running."));
        npcs.add(new WanderingMonk(tile(381, 23).subtractY(58), "Beyond these gates waits \nthe Gourd King himself. \nFew who enter walk out."));

        float stallX = getMapTile(STALL_X, STALL_Y).getX();
        float stallGroundY = getMapTile(STALL_X, 46).getY();
        DwarfShopkeeper dwarf = new DwarfShopkeeper(
                new Point(stallX + 60, stallGroundY - 46),
                stallX + 24,        // just inside the left post
                stallX + 205);      // his greeting spot, just clear of the solid counter
        // The "press E" prompt floats over the counter rather than over the
        // dwarf. The counter is the last stall column, and its solid top sits 3
        // unscaled px down the tile -- 9 on screen at tileScale 3.
        dwarf.setCounterAnchor(getMapTile(STALL_X + 5, 45).getX() + 24,
                               getMapTile(STALL_X + 5, 45).getY() + 9);
        npcs.add(dwarf);

        return npcs;
    }

    // the to-do list
    @Override
    public Checklist getChecklist() {
        if (checklist != null) return checklist;
        final float[] startX = { -1 };
        final int startBounces = SpringPad.getBounces();
        checklist = new Checklist()
                .add("Walk", () -> KeyBinds.label(KeyBinds.Action.MOVE_LEFT) + " / " + KeyBinds.label(KeyBinds.Action.MOVE_RIGHT),
                        p -> true,
                        p -> {
                            float x = p.getBounds().getX1();
                            if (startX[0] < 0) startX[0] = x;
                            return Math.abs(x - startX[0]) > 72;
                        })
                .add("Jump", () -> KeyBinds.label(KeyBinds.Action.JUMP),
                        p -> true,
                        p -> p.getAirGroundState() == AirGroundState.AIR && KeyBinds.isDown(KeyBinds.Action.JUMP))
                .add("Talk to the monk", () -> KeyBinds.label(KeyBinds.Action.INTERACT),
                        p -> true,
                        p -> KeyBinds.isDown(KeyBinds.Action.INTERACT) && Math.abs(tileX(p) - 12.5f) < 3 && tileY(p) > 66)
                .add("Slash a bush", () -> KeyBinds.label(KeyBinds.Action.ATTACK),
                        p -> tileX(p) >= 13,
                        Player::isAttacking)
                .add("Cut the drawbridge rope", null,
                        p -> tileX(p) >= 19,
                        p -> drawbridge.isDown())
                .add("Beat the goblins", null,
                        p -> tileX(p) >= 30 && tileY(p) < 74,
                        p -> meadowGoblins.stream().allMatch(e -> e.getMapEntityStatus() == MapEntityStatus.REMOVED))
                .add("Shoot the target", () -> "Left Click",
                        p -> tileX(p) >= 36 && tileY(p) < 74,
                        p -> target.isHit())
                .add("Dash through the wall", () -> KeyBinds.label(KeyBinds.Action.DASH),
                        p -> tileX(p) >= 47.5f && tileY(p) < 74,
                        p -> crackedWall.getMapEntityStatus() == MapEntityStatus.REMOVED)
                .add("Jump, then dash down", () -> KeyBinds.label(KeyBinds.Action.CROUCH) + " + " + KeyBinds.label(KeyBinds.Action.DASH),
                        p -> tileX(p) >= 52 && tileY(p) < 74,
                        p -> crackedFloor.getMapEntityStatus() == MapEntityStatus.REMOVED)
                .add("Dive into the lake", null,
                        p -> tileX(p) >= 57 && tileY(p) > 76 && !inMirror,
                        p -> dives > 0)
                .add("Cut the rope down here", () -> KeyBinds.label(KeyBinds.Action.ATTACK),
                        p -> inMirror,
                        p -> mirrorRope.isCut())
                .add("Hit an eel on the nose", () -> KeyBinds.label(KeyBinds.Action.ATTACK),
                        p -> inMirror && tileX(p) >= 65,
                        p -> eels.stream().anyMatch(KelpEel::wasHit))
                .add("Shoot both targets, quick!", () -> "Left Click",
                        p -> inMirror && tileX(p) >= 74,
                        p -> farWhirlpool.isSpinning())
                .add("Ride a whirlpool back up", null,
                        p -> inMirror,
                        p -> surfacings > 0)
                .add("Bounce on the spring", null,
                        p -> tileX(p) >= 85 && tileY(p) > 76,
                        p -> SpringPad.getBounces() > startBounces);
        return checklist;
    }

    private static float tileX(Player p) {
        return (p.getBounds().getX1() + p.getBounds().getWidth() / 2f) / 48f;
    }

    private static float tileY(Player p) {
        return p.getBounds().getY2() / 48f;
    }


    @Override
    public void update(Player player) {
        updateLake(player);
        // take last frame's shake back out before the camera follows the knight, then add this frame's
        camera.moveX(-shakeX);
        camera.moveY(-shakeY - lookY);
        super.update(player);
        lookY += (lookDownAmount(player) - lookY) * 0.12f;
        float[] offset = Effects.nextShakeOffset();
        shakeX = offset[0];
        shakeY = offset[1];
        camera.moveX(shakeX);
        camera.moveY(shakeY + lookY);
        Effects.tickBanner();
        updraftTick++;
    }

    // In the spring gauntlet the bounces are tall, so the camera leans down to keep the thorn pit
    // and the next pad on screen (while keeping the knight well clear of the top edge).
    private static final int GAUNTLET_FLOOR = 87;

    private float lookDownAmount(Player player) {
        Rectangle b = player.getBounds();
        float tileX = b.getX1() / 48f, feetRow = b.getY2() / 48f;
        if (tileX < 83 || tileX >= 137 || feetRow < 55) return 0;
        float wantBottom = GAUNTLET_FLOOR * 48 + 40;
        float need = wantBottom - (camera.getY() + ScreenManager.getScreenHeight());
        float room = (b.getY1() - camera.getY()) - 130;
        return Math.max(0, Math.min(need, room));
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (inMirror) {

            mirrorScreen = graphicsHandler.getGraphics();
            mirrorGraphics = createGraphics(mirrorCanvas(mirrorScreen));
            graphicsHandler.setGraphics(mirrorGraphics);
        }
        BufferedImage bg = inMirror ? mirrorBackground() : background;
        if (bg != null) {
            graphicsHandler.drawImage(bg, 0, 0, ScreenManager.getScreenWidth(), ScreenManager.getScreenHeight());
        }
        int camX = Math.round(camera.getX()), camY = Math.round(camera.getY());
        // dark backing for the stall's see-through corners
        graphicsHandler.drawFilledRectangle(STALL_X * 48 - camX, STALL_Y * 48 - camY, 6 * 48, 3 * 48, new Color(11, 21, 18));
        camera.draw(graphicsHandler);
        if (inMirror && eels != null) {
            for (KelpEel eel : eels) {
                eel.drawEel(graphicsHandler);      // in front of everything else down there
            }
        }
        drawLightRays(graphicsHandler, camX, camY);
        drawFireflies(graphicsHandler, camX, camY);
        drawButterflies(graphicsHandler, camX, camY);
        drawUpdraft(graphicsHandler, camX, camY);
    }

    @Override
    public void drawOverPlayer(GraphicsHandler graphicsHandler) {
        if (mirrorGraphics != null) {
            mirrorGraphics.dispose();
            mirrorGraphics = null;
            graphicsHandler.setGraphics(mirrorScreen);
            int w = ScreenManager.getScreenWidth(), h = ScreenManager.getScreenHeight();
            mirrorScreen.drawImage(mirrorImage, 0, h, w, 0, 0, 0, w, h, null);
            drawUnderwater(graphicsHandler);
        }
        if (flash > 0) {
            int a = Math.round(200 * flash / 18f);
            graphicsHandler.drawFilledRectangle(0, 0, ScreenManager.getScreenWidth(), ScreenManager.getScreenHeight(),
                    new Color(200, 236, 250, Math.min(255, a)));
        }
        drawBanner(graphicsHandler);
    }

    // the lake's reflection
    private static final int MIRROR_FLOOR = 24, WATER_ROW = 86, LAKE_X0 = 62, LAKE_X1 = 83;

    // the two targets in the reflection
    private void mirrorTargetHit() {
        if (mirrorTarget.isHit() && mirrorTarget2.isHit()) {
            pairTimer = 0;
            farWhirlpool.start();
            Effects.banner(Effects.BannerStyle.HINT, "The far whirlpool is spinning!", null, 150);
        } else {
            pairTimer = 150;
        }
    }

    private void updateLake(Player player) {
        if (pairTimer > 0 && --pairTimer == 0 && !(mirrorTarget.isHit() && mirrorTarget2.isHit())) {
            mirrorTarget.reset();
            mirrorTarget2.reset();
            Effects.banner(Effects.BannerStyle.HINT, "Too slow -- hit both targets quickly!", null, 120);
        }
        if (mirrorCooldown > 0) mirrorCooldown--;
        if (flash > 0) flash--;
        Rectangle b = player.getBounds();
        float cx = b.getX1() + b.getWidth() / 2f;
        if (!inMirror && mirrorCooldown == 0 && player.getHealth() > 0
                && cx > LAKE_X0 * 48 && cx < LAKE_X1 * 48 && b.getY2() > WATER_ROW * 48 + 16) {
            // dived in: come out on the underside of the water, at the same x
            ForestChunk.burst(this, cx, WATER_ROW * 48, new int[] { ForestChunk.WISP }, 10, 4f);
            player.setY(player.getY() + (MIRROR_FLOOR * 48 - 1) - b.getY2());
            player.bounce(8f);
            inMirror = true;
            Mouse.setFlipY(true);
            flash = 18;
            dives++;
            Effects.shake(4, 10);
            ForestChunk.burst(this, cx, MIRROR_FLOOR * 48, new int[] { ForestChunk.WISP }, 10, 4f);
        }
    }

    // a whirlpool pulls you back up: you shoot up out of the lake at x
    private void surface(Player player, float x, float force) {
        if (!inMirror) return;
        Rectangle b = player.getBounds();
        player.setX(player.getX() + x - (b.getX1() + b.getWidth() / 2f));
        player.setY(player.getY() + (WATER_ROW * 48 + 30) - b.getY2());
        player.bounce(force);
        inMirror = false;
        Mouse.setFlipY(false);
        mirrorCooldown = 45;
        flash = 18;
        surfacings++;
        Effects.shake(5, 12);
        ForestChunk.burst(this, x, WATER_ROW * 48, new int[] { ForestChunk.WISP, ForestChunk.WISP }, 16, 5f);
    }

    public boolean isInMirror() {
        return inMirror;
    }


    private static BufferedImage mirrorBg, shaftImage;


    private Image mirrorCanvas(Graphics2D screen) {
        int w = ScreenManager.getScreenWidth(), h = ScreenManager.getScreenHeight();
        GraphicsConfiguration gc = screen.getDeviceConfiguration();
        if (mirrorImage instanceof VolatileImage) {
            VolatileImage v = (VolatileImage) mirrorImage;
            if (v.getWidth() == w && v.getHeight() == h && v.validate(gc) != VolatileImage.IMAGE_INCOMPATIBLE) {
                return v;
            }
        } else if (mirrorImage != null && mirrorImage.getWidth(null) == w && mirrorImage.getHeight(null) == h) {
            return mirrorImage;
        }
        try {
            mirrorImage = gc.createCompatibleVolatileImage(w, h);
        } catch (RuntimeException e) {
            mirrorImage = null;
        }
        if (mirrorImage == null) {
            mirrorImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        }
        return mirrorImage;
    }

    private static Graphics2D createGraphics(Image image) {
        return image instanceof VolatileImage ? ((VolatileImage) image).createGraphics() : ((BufferedImage) image).createGraphics();
    }

    private BufferedImage mirrorBackground() {
        if (mirrorBg == null && background != null) {
            mirrorBg = new BufferedImage(background.getWidth(), background.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = mirrorBg.createGraphics();
            g.drawImage(background, 0, 0, null);
            g.setColor(new Color(8, 40, 70, 95));
            g.fillRect(0, 0, mirrorBg.getWidth(), mirrorBg.getHeight());
            g.dispose();
        }
        return mirrorBg;
    }

    private static BufferedImage shaft() {
        if (shaftImage == null) {
            int w = 160, h = 520;
            shaftImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < h; y++) {
                float fadeDown = 1 - y / (float) h;
                int left = Math.round(y * 0.18f), width = 60 + Math.round(y * 0.12f);
                for (int x = left; x < Math.min(w, left + width); x++) {
                    float edge = Math.min(x - left, left + width - x) / 18f;
                    int a = Math.round(26 * fadeDown * Math.min(1f, edge));
                    if (a > 0) shaftImage.setRGB(x, y, (a << 24) | 0xC8F0FF);
                }
            }
        }
        return shaftImage;
    }

    private void drawUnderwater(GraphicsHandler graphicsHandler) {
        int w = ScreenManager.getScreenWidth(), h = ScreenManager.getScreenHeight();
        Graphics2D g = graphicsHandler.getGraphics();
        // where the surface is on screen (it's at the top, because it's all upside down)
        int surface = h - Math.round(MIRROR_FLOOR * 48 - camera.getY());
        int camX = Math.round(camera.getX());
        BufferedImage light = shaft();
        for (int i = 0; i < 3; i++) {
            int x = Math.floorMod(i * 420 - camX / 3, w + 400) - 200;
            g.drawImage(light, x, surface, null);
        }
        g.setColor(new Color(220, 245, 255, 120));
        for (int i = 0; i < 18; i++) {
            int seed = hash(i, 77);
            int speed = 1 + (seed & 3);
            int x = Math.floorMod((seed >>> 4) % 2000 - camX, w + 40) - 20 + (int) (Math.sin(updraftTick * 0.05 + i) * 6);
            int y = h - Math.floorMod(updraftTick * speed + (seed >>> 12) % h, h - Math.max(0, surface));
            int r = 2 + (seed >>> 20) % 3;
            g.drawOval(x - r, y - r, r * 2, r * 2);
        }
    }

    // the dark forest
    private static boolean inForest(int tileX, int tileY) {
        return (tileX >= 113 && tileY <= 48) || tileX >= 151;
    }

    private static int hash(int a, int b) {
        int n = a * 73856093 ^ b * 19349663;
        n ^= n >>> 13;
        n *= 0x5bd1e995;
        return (n ^ (n >>> 15)) & 0x7fffffff;
    }


    private static final int RAY_SPACING = 1150, RAY_IMAGE_W = 420, RAY_IMAGE_H = 820;
    private static final float RAY_PARALLAX = 0.8f;
    private static BufferedImage[] rayImages;

    private static BufferedImage makeRay(int seed) {
        java.util.Random rnd = new java.util.Random(seed);
        BufferedImage img = new BufferedImage(RAY_IMAGE_W, RAY_IMAGE_H, BufferedImage.TYPE_INT_ARGB);
        float x0 = 70 + rnd.nextInt(40), slant = 170 + rnd.nextInt(80), topWidth = 100 + rnd.nextInt(60);
        float p1 = rnd.nextFloat() * 6, p2 = rnd.nextFloat() * 6;
        for (int y = 0; y < RAY_IMAGE_H; y++) {
            float t = y / (float) RAY_IMAGE_H;
            float center = x0 + slant * t, half = topWidth / 2f * (1 + 0.7f * t);
            float fade = (float) Math.pow(1 - t, 1.15) * Math.min(1, t / 0.06f);
            for (int x = 0; x < RAY_IMAGE_W; x++) {
                float d = Math.abs(x - center) / half;
                if (d >= 1) continue;
                float profile = (float) Math.pow(Math.cos(d * Math.PI / 2), 2);
                // leaves in the way: slow wobbly streaks along the beam
                float dapple = 0.7f + 0.3f * (float) (Math.sin((x - center) * 0.09 + p1 + t * 3) * Math.sin(y * 0.021 + p2));
                int alpha = Math.round(46 * profile * fade * dapple);
                if (alpha > 0) {
                    img.setRGB(x, y, (alpha << 24) | (255 << 16) | (248 << 8) | 214);
                }
            }
        }
        return img;
    }

    private void drawLightRays(GraphicsHandler graphicsHandler, int camX, int camY) {
        if (rayImages == null) {
            rayImages = new BufferedImage[] { makeRay(3), makeRay(8), makeRay(21) };
        }
        int screenW = ScreenManager.getScreenWidth(), screenH = ScreenManager.getScreenHeight();
        int viewRow = (camY + screenH / 2) / 48;
        float shifted = camX * RAY_PARALLAX;
        Graphics2D g = graphicsHandler.getGraphics();
        Composite old = g.getComposite();
        for (int i = (int) ((shifted - RAY_IMAGE_W) / RAY_SPACING); i <= (shifted + screenW) / RAY_SPACING; i++) {
            int screenX = Math.round(i * RAY_SPACING + hash(i, 4) % 400 - shifted);
            int worldTile = (screenX + camX + RAY_IMAGE_W / 2) / 48;
            if (!inForest(worldTile, viewRow) || !inForest(worldTile - 4, viewRow)) continue;
            float breathe = 0.8f + 0.2f * (float) Math.sin(updraftTick * 0.012 + i * 1.7);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, breathe));
            graphicsHandler.drawImage(rayImages[Math.abs(i) % rayImages.length], screenX, 0, RAY_IMAGE_W, Math.max(RAY_IMAGE_H, screenH));
        }
        g.setComposite(old);
    }

    // a few butterflies round the village at the start
    private static final Color[] BUTTERFLY = {
            new Color(255, 226, 90), new Color(250, 250, 250), new Color(250, 150, 190) };
    private static final int[][] BUTTERFLY_SPOTS = { { 9, 66 }, { 17, 68 }, { 41, 67 } };   // tiles

    private void drawButterflies(GraphicsHandler graphicsHandler, int camX, int camY) {
        if (camX > 50 * 48) return;
        for (int i = 0; i < BUTTERFLY_SPOTS.length; i++) {
            double t = updraftTick * 0.03 + i * 2.1;
            int x = BUTTERFLY_SPOTS[i][0] * 48 - camX + (int) (Math.sin(t) * 60 + Math.sin(t * 2.3) * 14);
            int y = BUTTERFLY_SPOTS[i][1] * 48 - camY + (int) (Math.cos(t * 1.3) * 26);
            Color c = BUTTERFLY[i % BUTTERFLY.length];
            boolean up = ((updraftTick + i * 3) / 5) % 2 == 0;      // flap
            int wing = up ? 7 : 4, wingH = up ? 7 : 4;
            graphicsHandler.drawFilledRectangle(x - wing - 1, y - wingH + 2, wing, wingH, c);
            graphicsHandler.drawFilledRectangle(x + 3, y - wingH + 2, wing, wingH, c);
            graphicsHandler.drawFilledRectangle(x, y - 3, 3, 7, new Color(60, 40, 30));
        }
    }

    // fireflies drifting about the dark forest
    private void drawFireflies(GraphicsHandler graphicsHandler, int camX, int camY) {
        int screenW = ScreenManager.getScreenWidth(), screenH = ScreenManager.getScreenHeight();
        int cell = 144;     // one firefly (maybe) per 3x3 tiles
        for (int by = camY / cell - 1; by <= (camY + screenH) / cell; by++) {
            for (int bx = camX / cell - 1; bx <= (camX + screenW) / cell; bx++) {
                int k = hash(bx, by);
                if (k % 100 >= 40) continue;
                int wx = bx * cell + k % cell, wy = by * cell + (k / cell) % cell;
                if (!inForest(wx / 48, wy / 48) || wy / 48 > 80) continue;
                double t = updraftTick * 0.02 + (k % 628) / 100.0;
                int x = wx - camX + (int) (Math.sin(t) * 22);
                int y = wy - camY + (int) (Math.cos(t * 0.8) * 14);
                double glow = 0.5 + 0.5 * Math.sin(updraftTick * 0.07 + k % 97);
                if (glow < 0.15) continue;
                graphicsHandler.drawFilledRectangle(x - 4, y - 4, 9, 9, new Color(200, 255, 120, (int) (45 * glow)));
                graphicsHandler.drawFilledRectangle(x - 1, y - 1, 3, 3, new Color(235, 255, 170, (int) (230 * glow)));
            }
        }
    }

    // streaks of wind rushing up the chute
    private void drawUpdraft(GraphicsHandler graphicsHandler, int camX, int camY) {
        int left = CHUTE_X * 48 - camX, top = CHUTE_TOP * 48 - camY, bottom = CHUTE_BOTTOM * 48 - camY;
        int screenH = ScreenManager.getScreenHeight();
        if (left > ScreenManager.getScreenWidth() || left + 144 < 0 || bottom < 0 || top > screenH) return;
        Color streak = new Color(255, 250, 220, 70);
        for (int i = 0; i < 7; i++) {
            int x = left + 10 + (i * 53) % 124;
            int length = 40 + (i * 37) % 60;
            int y = bottom - ((updraftTick * (9 + i % 3 * 3) + i * 211) % (bottom - top + length));
            int y0 = Math.max(top, y), y1 = Math.min(bottom, y + length);
            if (y1 > y0) {
                graphicsHandler.drawFilledRectangle(x, y0, 3, y1 - y0, streak);
            }
        }
    }

    // the big "RUN!" and "THE DARK FOREST" banners
    private void drawBanner(GraphicsHandler graphicsHandler) {
        if (!Effects.hasBanner()) return;
        String key = Effects.getBannerStyle() + "|" + Effects.getBannerTitle() + "|" + Effects.getBannerSubtitle();
        if (!key.equals(bannerKey)) {
            bannerKey = key;
            bannerImage = renderBanner(Effects.getBannerStyle(), Effects.getBannerTitle(), Effects.getBannerSubtitle());
        }
        int shown = Effects.getBannerFramesShown(), left = Effects.getBannerFramesLeft();
        float alpha = Math.min(1f, Math.min(shown / 10f, left / 20f));
        int screenW = ScreenManager.getScreenWidth(), screenH = ScreenManager.getScreenHeight();
        // pops in a little bigger and settles
        float scale = shown < 8 ? 1.25f - 0.25f * shown / 8f : 1f;
        int w = Math.round(bannerImage.getWidth() * scale), hgt = Math.round(bannerImage.getHeight() * scale);
        int x = (screenW - w) / 2;
        int y = Math.round(screenH * (Effects.getBannerStyle() == Effects.BannerStyle.HINT ? 0.8f : 0.2f)) - hgt / 2;
        Graphics2D g = graphicsHandler.getGraphics();
        Composite old = g.getComposite();
        Object oldInterpolation = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, alpha)));
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(bannerImage, x, y, w, hgt, null);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                oldInterpolation != null ? oldInterpolation : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setComposite(old);
    }

    private static BufferedImage renderBanner(Effects.BannerStyle style, String title, String subtitle) {
        boolean run = style == Effects.BannerStyle.RUN, hint = style == Effects.BannerStyle.HINT;
        Font titleFont = new Font("Serif", Font.BOLD, run ? 96 : hint ? 30 : 60);
        Font subFont = new Font("Serif", Font.ITALIC, 26);
        Color fill = run ? new Color(255, 236, 170) : hint ? new Color(255, 252, 236) : new Color(190, 236, 190);
        Color edge = run ? new Color(70, 40, 10) : hint ? new Color(30, 26, 40) : new Color(8, 24, 20);

        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pg = probe.createGraphics();
        FontMetrics tm = pg.getFontMetrics(titleFont), sm = pg.getFontMetrics(subFont);
        pg.dispose();
        int width = Math.max(tm.stringWidth(title), subtitle == null ? 0 : sm.stringWidth(subtitle)) + 40;
        int height = tm.getHeight() + (subtitle == null ? 0 : sm.getHeight()) + 24;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int ty = 12 + tm.getAscent();
        outlined(g, title, (width - tm.stringWidth(title)) / 2, ty, titleFont, fill, edge, run ? 9 : hint ? 5 : 7);
        if (subtitle != null) {
            outlined(g, subtitle, (width - sm.stringWidth(subtitle)) / 2, ty + tm.getDescent() + sm.getAscent() + 4,
                    subFont, new Color(220, 210, 240), edge, 5);
        }
        g.dispose();
        return image;
    }

    private static void outlined(Graphics2D g, String text, int x, int y, Font font, Color fill, Color edge, float thickness) {
        GlyphVector glyphs = font.createGlyphVector(g.getFontRenderContext(), text);
        java.awt.Shape shape = glyphs.getOutline(x, y);
        g.setColor(edge);
        g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(shape);
        g.setColor(fill);
        g.fill(shape);
    }
}
