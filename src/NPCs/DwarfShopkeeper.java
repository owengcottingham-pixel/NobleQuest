package NPCs;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.NPC;
import Level.Player;
import Utils.Direction;
import Utils.Point;

import java.util.HashMap;
import java.util.Random;

// The cosmetics stall's shopkeeper.
//
// Left alone he mooches up and down inside the stall, stopping every so often.
// The moment the player steps into the stall he drops that, hurries to the right
// hand end by the counter and holds a hand up in greeting, turning to face
// whoever walked in. When they leave he goes back to mooching.
//
// He has no gravity and never crosses his two x bounds, so he needs flat ground
// to stand on -- which is what the stall sits on.
public class DwarfShopkeeper extends NPC {

    private static final float WANDER_SPEED = 0.45f;
    private static final float RUN_SPEED    = 1.70f;

    private static final int PAUSE_MIN    = 45;    // frames spent standing still
    private static final int PAUSE_MAX    = 160;
    private static final int ARRIVE_SLACK = 2;     // px -- close enough to the target

    // How near the player has to be before he breaks off and greets them. This is
    // sized to the stall footprint rather than to the window, deliberately: if it
    // triggered on "the stall is visible on screen" he would already be stood at
    // his post every single time you could see him, and you would never once catch
    // him wandering.
    private static final float GREET_RANGE_X = 230f;
    private static final float GREET_RANGE_Y = 160f;

    // sprite geometry, measured off DwarfShopkeeper.png
    private static final int CELL_W = 40, CELL_H = 48;
    private static final int BOUND_X = 9, BOUND_Y = 6, BOUND_W = 22, BOUND_H = 40;

    private enum State { WANDER, PAUSED, RUNNING, GREETING }

    private final float walkLeft;
    private final float walkRight;
    private final Random random = new Random();

    private State state = State.PAUSED;
    private float targetX;
    private int pauseTimer = 30;
    private Direction facing = Direction.RIGHT;

    public DwarfShopkeeper(Point location, float walkLeft, float walkRight) {
        super(location.x, location.y,
              new SpriteSheet(ImageLoader.load("DwarfShopkeeper.png"), CELL_W, CELL_H),
              "STAND_RIGHT");
        this.walkLeft = walkLeft;
        this.walkRight = walkRight;
        this.targetX = location.x;
    }

    @Override
    public void update(Player player) {
        if (playerIsHere(player)) {
            if (state != State.GREETING && state != State.RUNNING) {
                state = State.RUNNING;
            }
        } else if (state == State.GREETING || state == State.RUNNING) {
            state = State.PAUSED;                 // customer gone, back to mooching
            pauseTimer = 40;
        }

        switch (state) {
            case WANDER:
                if (!stepToward(targetX, WANDER_SPEED)) {
                    state = State.PAUSED;
                    pauseTimer = PAUSE_MIN + random.nextInt(PAUSE_MAX - PAUSE_MIN);
                }
                break;

            case PAUSED:
                pauseTimer--;
                if (pauseTimer <= 0) {
                    targetX = walkLeft + random.nextFloat() * (walkRight - walkLeft);
                    state = State.WANDER;
                }
                break;

            case RUNNING:
                if (!stepToward(walkRight, RUN_SPEED)) {
                    setX(walkRight);
                    state = State.GREETING;
                }
                break;

            case GREETING:
                facing = (player.getX() < getX()) ? Direction.LEFT : Direction.RIGHT;
                break;
        }

        currentAnimationName = animationFor();
        super.update(player);
    }

    // walks one step toward goalX; returns false once it has arrived
    private boolean stepToward(float goalX, float speed) {
        float dx = goalX - getX();
        if (Math.abs(dx) <= ARRIVE_SLACK) {
            return false;
        }
        facing = dx > 0 ? Direction.RIGHT : Direction.LEFT;
        // collision-aware, so the counter actually stops him rather than him
        // walking through it; the clamp below is a backstop for the x bounds
        moveXHandleCollision(dx > 0 ? speed : -speed);
        if (getX() < walkLeft) {
            setX(walkLeft);
        }
        if (getX() > walkRight) {
            setX(walkRight);
        }
        return true;
    }

    private boolean playerIsHere(Player player) {
        float shopCentre = (walkLeft + walkRight) / 2f + CELL_W / 2f;
        return Math.abs(player.getX() - shopCentre) <= GREET_RANGE_X
            && Math.abs(player.getY() - getY()) <= GREET_RANGE_Y;
    }

    private String animationFor() {
        String dir = (facing == Direction.RIGHT) ? "RIGHT" : "LEFT";
        switch (state) {
            case GREETING: return "GREET_" + dir;
            case PAUSED:   return "STAND_" + dir;
            default:       return "WALK_" + dir;
        }
    }

    // static so it is safe to call from loadAnimations, which the superclass
    // constructor runs before this class's own fields exist
    private static Frame frame(SpriteSheet sheet, int row, int col, int delay, boolean flip) {
        FrameBuilder builder = new FrameBuilder(sheet.getSprite(row, col), delay)
                .withScale(1)
                .withBounds(BOUND_X, BOUND_Y, BOUND_W, BOUND_H);
        if (flip) {
            builder = builder.withImageEffect(ImageEffect.FLIP_HORIZONTAL);
        }
        return builder.build();
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("STAND_RIGHT", new Frame[] { frame(spriteSheet, 1, 0, 0, false) });
            put("STAND_LEFT",  new Frame[] { frame(spriteSheet, 1, 0, 0, true)  });

            // the raised hand
            put("GREET_RIGHT", new Frame[] { frame(spriteSheet, 1, 1, 0, false) });
            put("GREET_LEFT",  new Frame[] { frame(spriteSheet, 1, 1, 0, true)  });

            put("WALK_RIGHT", new Frame[] {
                    frame(spriteSheet, 0, 0, 11, false),
                    frame(spriteSheet, 0, 1, 11, false),
                    frame(spriteSheet, 0, 2, 11, false),
                    frame(spriteSheet, 0, 3, 11, false)
            });
            put("WALK_LEFT", new Frame[] {
                    frame(spriteSheet, 0, 0, 11, true),
                    frame(spriteSheet, 0, 1, 11, true),
                    frame(spriteSheet, 0, 2, 11, true),
                    frame(spriteSheet, 0, 3, 11, true)
            });
        }};
    }
}
