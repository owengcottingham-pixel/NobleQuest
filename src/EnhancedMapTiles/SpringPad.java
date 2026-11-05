package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.ForestChunk;
import Enemies.GourdProjectile;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.SpriteSheet;
import Level.EditorObject;
import Level.EditorObjects;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.AirGroundState;
import Utils.Effects;
import Utils.Point;

import java.awt.image.BufferedImage;


public class SpringPad extends EnhancedMapTile implements EditorObject {
    public enum Kind {
        LOW(0, 15f), NORMAL(1, 23f), SUPER(2, 27f);
        final int row;
        final float force;
        Kind(int row, float force) { this.row = row; this.force = force; }
    }

    private static final int SQUASH_TIME = 16;
    private static final int CHARGE_TIME = 22;      // frames of wind-up before it fires
    private static final int TRAIL_TIME = 60;       // frames of sparkle trail after it fires

    private final Kind kind;
    private final float startX, startY;
    private final int width;
    private float bounceForce;
    // how many times anyone has bounced on a spring this session
    private static int bounces = 0;
    private final BufferedImage capL, capM, capR, coil;
    private int squashTimer = 0;
    private ChuteRing[] rings = new ChuteRing[0];
    private boolean ringsAdded = false;
    private int chargeTimer = 0, trailTimer = 0;
    private Player flyer;

    // sliding back and forth
    private float minX, maxX, speed = 0;
    private int direction = 1;

    public SpringPad(Point location, int width, Kind kind) {
        super(location.x, location.y,
                new FrameBuilder(new BufferedImage(16 * width, 16, BufferedImage.TYPE_INT_ARGB))
                        .withScale(3)
                        .withBounds(0, 6, 16 * width, 4)
                        .build(),
                TileType.JUMP_THROUGH_PLATFORM);
        this.kind = kind;
        this.width = width;
        this.startX = location.x;
        this.startY = location.y;
        this.bounceForce = kind.force;
        SpriteSheet sheet = new SpriteSheet(ImageLoader.load("SpringPad.png"), 16, 16);
        capL = sheet.getSprite(kind.row, 0);
        capM = sheet.getSprite(kind.row, 1);
        capR = sheet.getSprite(kind.row, 2);
        coil = sheet.getSprite(kind.row, 3);
        initialize();
        if (kind == Kind.SUPER) {
            setIsUpdateOffScreen(true);
        }
    }
    

    public SpringPad bounceForce(float force) {
        this.bounceForce = force;
        return this;
    }

    // placeable with the map editor
    @Override
    public String editorLine() {
        return "SPRING " + EditorObjects.tiles(startX) + " " + EditorObjects.tiles(startY) + " " + width + " " + kind
                + (bounceForce != kind.force ? " " + EditorObjects.num(bounceForce) : "");
    }

    public static int getBounces() {
        return bounces;
    }

    // slide between two x positions (pixels, the pad's left edge) at 'speed' px per frame
    public SpringPad moveBetween(float leftX, float rightX, float speed) {
        this.minX = leftX;
        this.maxX = rightX;
        this.speed = speed;
        setIsUpdateOffScreen(true);
        return this;
    }

    // chute rings above a SUPER pad: they show up (and start boosting) once you launch
    public SpringPad addRings(ChuteRing... rings) {
        this.rings = rings;
        return this;
    }

    @Override
    public void update(Player player) {
        boolean standing = touching(player)
                && Math.abs((player.getBounds().getY2() + 1) - getBounds().getY1()) <= 2
                && player.getAirGroundState() == AirGroundState.GROUND;

        if (speed > 0) {
            float before = getX();
            float next = before + speed * direction;
            if (next >= maxX) { next = maxX; direction = -1; }
            if (next <= minX) { next = minX; direction = 1; }
            setX(next);
            if (standing) {
                player.moveXHandleCollision(next - before);   // carry the knight along
            }
        }

        if (kind == Kind.SUPER) {
            updateLauncher(player, standing);
        } else if (standing) {
            player.bounce(bounceForce);
            boing(player);
        }
        if (squashTimer > 0) squashTimer--;
        super.update(player);
    }

    private void updateLauncher(Player player, boolean standing) {
        float padCenter = getBounds().getX1() + getBounds().getWidth() / 2f;
        float cx = player.getBounds().getX1() + player.getBounds().getWidth() / 2f;
        if (standing && chargeTimer == 0) {
            chargeTimer = CHARGE_TIME;            // landed: start winding up
        }
        if (chargeTimer > 0) {
            if (!standing) {
                chargeTimer = 0;                  // walked or jumped off: never mind
            } else {
                // (you can still walk about on it while it charges)
                Effects.shake(1 + 2.5f * (1 - chargeTimer / (float) CHARGE_TIME), 2);
                if (chargeTimer % 4 == 0) {
                    ForestChunk.burst(map, padCenter + (float) (Math.random() * 120 - 60), getBounds().getY1(),
                            new int[] { ForestChunk.SPARK }, 1, 2.5f);
                }
                if (--chargeTimer == 0) {
                    // line him up with the middle so he goes straight up the chute
                    player.moveXHandleCollision(padCenter - cx);
                    player.bounce(bounceForce);
                    boing(player);
                    flyer = player;
                    trailTimer = TRAIL_TIME;
                }
            }
        }
        if (trailTimer > 0 && flyer != null) {
            trailTimer--;
            if (trailTimer % 3 == 0) {
                float fx = flyer.getBounds().getX1() + flyer.getBounds().getWidth() / 2f;
                ForestChunk.burst(map, fx, flyer.getBounds().getY2(), new int[] { ForestChunk.SPARK }, 1, 1.5f);
            }
        }
    }

    private void boing(Player player) {
        bounces++;
        squashTimer = SQUASH_TIME;
        float feet = getBounds().getY1();
        float cx = player.getBounds().getX1() + player.getBounds().getWidth() / 2f;
        GourdProjectile.puff(map, cx - 22, feet, -1.0f);
        GourdProjectile.puff(map, cx + 22, feet, 1.0f);
        if (kind == Kind.SUPER) {
            Effects.shake(8, 16);
            ForestChunk.burst(map, cx, feet, new int[] { ForestChunk.SPARK, ForestChunk.SPARK, ForestChunk.STONE }, 16, 6f);
            if (!ringsAdded) {
                ringsAdded = true;
                for (ChuteRing ring : rings) {
                    map.addEnhancedMapTile(ring);
                }
            }
        }
    }

    // how far the cap is pushed down right now, in art pixels (squash, then a little overshoot)
    private int squash() {
        if (chargeTimer > 0) {
            return 1 + Math.round(3 * (1 - chargeTimer / (float) CHARGE_TIME));   // sinking as it winds up
        }
        if (squashTimer <= 0) return 0;
        int t = SQUASH_TIME - squashTimer;     // 0 .. 15
        if (t < 3) return 3;
        if (t < 6) return 1;
        if (t < 9) return -1;                  // springs up past its rest height
        if (t < 12) return 1;
        return 0;
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        int s = squash();
        int x = Math.round(getCalibratedXLocation());
        int y = Math.round(getCalibratedYLocation());
        for (int i = 0; i < width; i++) {
            int tx = x + i * 48;
            // coil: rows 10-15 of the art, squashed as the cap comes down
            int coilTop = 10 + Math.max(s, 0);
            graphicsHandler.drawImage(coil.getSubimage(0, 10, 16, 6), tx, y + coilTop * 3, 48, (16 - coilTop) * 3);
            BufferedImage cap = width == 1 ? capM : i == 0 ? capL : i == width - 1 ? capR : capM;
            graphicsHandler.drawImage(cap.getSubimage(0, 5, 16, 6), tx, y + (5 + s) * 3, 48, 18);
        }
    }
}
