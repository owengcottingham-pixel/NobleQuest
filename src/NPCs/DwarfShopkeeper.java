package NPCs;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.NPC;
import Level.Player;
import Utils.Direction;
import Utils.Point;

import java.awt.image.BufferedImage;
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

    // How near you must stand to actually talk to him. Much tighter than the
    // greet range -- he waves from across the stall, but you have to walk up to
    // the counter to trade.
    private static final float TALK_RANGE_X = 90f;
    private static final float TALK_RANGE_Y = 80f;

    private static final int PROMPT_SIZE = 22;    // on-screen size of the "E" key
    private static final int PROMPT_LIFT = 34;    // how far it floats above its anchor

    // sprite geometry, measured off DwarfShopkeeper.png
    private static final int CELL_W = 40, CELL_H = 48;
    private static final int BOUND_X = 9, BOUND_Y = 6, BOUND_W = 22, BOUND_H = 40;

    private enum State { WANDER, PAUSED, RUNNING, GREETING }

    private final float walkLeft;
    private final float walkRight;
    private final Random random = new Random();

    private final KeyLocker keyLocker = new KeyLocker();
    private final Key INTERACT_KEY = Key.E;
    // draw() has no player, so update() caches whether the prompt should show
    private boolean canTalk = false;
    private boolean shopRequested = false;
    private boolean shopOpen = false;
    private int pulse = 0;

    // The floating "press E" prompt, drawn by hand rather than as his own sprite.
    // KeyPromptE.png is a 33x16 two-frame sheet -- key up, key pressed, 1px apart --
    // so it has to be cut up; drawn whole it squashes both caps into one square.
    private static final int PROMPT_CELL = 16;
    private final BufferedImage promptUp   = promptFrame(0);
    private final BufferedImage promptDown = promptFrame(1);

    private static BufferedImage promptFrame(int index) {
        BufferedImage sheet = ImageLoader.load("KeyPromptE.png");
        int x = index * (PROMPT_CELL + 1);
        if (x + PROMPT_CELL > sheet.getWidth() || PROMPT_CELL > sheet.getHeight()) {
            return sheet;          // single-frame art: use it as it is
        }
        return sheet.getSubimage(x, 0, PROMPT_CELL, PROMPT_CELL);
    }

    // Where the prompt floats and where "close enough" is measured from: the
    // counter top, since the counter is what you actually walk up to, not the
    // dwarf -- he drifts along his side of it. Level1 sets this from the stall's
    // own tiles. Left unset it falls back to floating over his head.
    private float counterX = Float.NaN;
    private float counterY = Float.NaN;

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

    // PlayLevelScreen polls this: true exactly once per press, so one tap of E
    // opens the menu rather than reopening it every frame the key is held.
    public boolean consumeShopRequest() {
        boolean requested = shopRequested;
        shopRequested = false;
        return requested;
    }

    // world position of the middle of the counter top, in pixels
    public void setCounterAnchor(float centerX, float topY) {
        this.counterX = centerX;
        this.counterY = topY;
    }

    private boolean hasCounter() {
        return !Float.isNaN(counterX) && !Float.isNaN(counterY);
    }

    // the screen tells him the menu is up, so he stops wandering off mid-sale
    public void setShopOpen(boolean shopOpen) {
        this.shopOpen = shopOpen;
    }

    @Override
    public void update(Player player) {
        pulse++;
        canTalk = playerCanTalk(player);

        if (Keyboard.isKeyUp(INTERACT_KEY)) {
            keyLocker.unlockKey(INTERACT_KEY);
        }
        if (canTalk && !shopOpen
                && Keyboard.isKeyDown(INTERACT_KEY)
                && !keyLocker.isKeyLocked(INTERACT_KEY)) {
            keyLocker.lockKey(INTERACT_KEY);
            shopRequested = true;
        }

        if (shopOpen) {
            // hold still and face the customer while trading
            facing = (player.getX() < getX()) ? Direction.LEFT : Direction.RIGHT;
            currentAnimationName = animationFor();
            super.update(player);
            return;
        }

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

    private boolean playerCanTalk(Player player) {
        float anchorX = hasCounter() ? counterX : getX() + CELL_W / 2f;
        return Math.abs((player.getX() + 56f) - anchorX) <= TALK_RANGE_X
            && Math.abs(player.getY() - getY()) <= TALK_RANGE_Y;
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);

        // only prompt when you are close enough and the menu is not already up
        if (!canTalk || shopOpen) {
            return;
        }
        int bob = (pulse / 24) % 2;            // gentle bob
        int px, py;
        if (hasCounter()) {
            // The counter is a map tile, so it has no calibrated position of its
            // own. His does, and the difference between his world x and his
            // calibrated x is exactly the camera offset, so it converts any world
            // point on this map into screen space.
            float cameraX = getX() - getCalibratedXLocation();
            float cameraY = getY() - getCalibratedYLocation();
            px = Math.round(counterX - cameraX) - PROMPT_SIZE / 2;
            py = Math.round(counterY - cameraY) - PROMPT_LIFT - bob;
        } else {
            px = Math.round(getCalibratedXLocation()) + CELL_W / 2 - PROMPT_SIZE / 2;
            py = Math.round(getCalibratedYLocation()) - PROMPT_LIFT - bob;
        }
        BufferedImage cap = Keyboard.isKeyDown(INTERACT_KEY) ? promptDown : promptUp;
        graphicsHandler.drawImage(cap, px, py, PROMPT_SIZE, PROMPT_SIZE);
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
