package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.Goblin;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.Key;
import Engine.KeyBinds;
import Engine.KeyPrompt;
import Engine.KeyLocker;
import Engine.Keyboard;
import GameObject.Frame;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.Direction;
import Utils.Point;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Random;

// Interactive wishing well.
// Stand near it and press E to drop a coin in. The well beams a column of light
// that cycles through colours quickly, slows down, and settles on one at random.
// Rarer colours are far less likely -- the reward hookup goes in onResult().
public class WishingWell extends EnhancedMapTile {

    // ---- the light colours, rarest last ----
    public static final int BLUE = 0, GREEN = 1, RED = 2, PURPLE = 3, GOLD = 4;

    private static final Color[] LIGHTS = {
        new Color( 70, 130, 255),   // blue
        new Color( 70, 210, 110),   // green
        new Color(225,  60,  55),   // red
        new Color(170,  80, 220),   // purple
        new Color(255, 205,  70)    // gold
    };

    // relative odds -- blue is common, gold is the jackpot
    private static final int[] WEIGHTS = { 40, 27, 20, 9, 4 };

    // ---- tuning ----
    private static final int COIN_COST     = 2;
    private static final int SPIN_FRAMES   = 150;   // ~2.5s of cycling
    private static final int SETTLE_HOLD   = 90;    // ~1.5s showing the result
    private static final int FAST_STEP     = 2;     // frames per colour at the start
    private static final int SLOW_STEP     = 16;    // frames per colour at the end
    private static final int INTERACT_X    = 150;   // how close you must stand
    private static final int INTERACT_Y    = 120;

    // ---- geometry, measured from this tile's top-left corner ----
    // place the tile on the well's top-left tile and these line up with the art
    private static final int TILE          = 48;
    private static final int MOUTH_X       = TILE * 2;      // centre of the 4-wide well
    private static final int MOUTH_Y       = TILE * 3;      // top of the stone drum
    private static final int BEAM_WIDTH    = 86;    // width at the base
    private static final int BEAM_FLARE    = 4;     // extra width per segment going up
    private static final int BEAM_SEGMENTS = 6;
    private static final int SEGMENT_H     = 16;    // 6 x 16 = 96px tall, stops under the arch
    private static final int CORE_WIDTH    = 28;
    private static final int PROMPT_SIZE   = 26;    // on-screen size of the "E" key
    private static final int PROMPT_LIFT   = 64;    // how far above the mouth it floats
    private static final int DENY_FRAMES   = 36;    // flash when you cannot afford it
    private static final int GOBLIN_WIDTH  = 104;   // Goblin frame size, for placing spawns
    private static final int GOBLIN_HEIGHT = 80;

    // ---- light show ----
    private static final int MOTE_COUNT   = 16;   // sparks riding the beam up
    private static final int FLASH_FRAMES = 5;    // white kick on each colour flip
    private static final int BURST_FRAMES = 30;   // ring that snaps out when it lands
    private static final int GLOW_RADIUS  = 64;   // soft pool of light at the mouth

    // ---- result banner ----
    private static final int MESSAGE_FRAMES = 150;  // ~2.5s on screen if left alone
    private static final int MESSAGE_FADE   = 30;   // frames spent fading out
    private static final int MESSAGE_LIFT   = 100;  // height above the mouth
    private static final int MESSAGE_RISE   = 18;   // px it drifts upward as it lands
    private static final int SPAWN_JITTER   = 26;   // px of slop on guard spawn points

    private static final String[] RESULT_TEXT = {
        "NOTHING...",     // blue
        "+5 COINS",       // green
        "AMBUSH!",        // red
        "+10 COINS",      // purple
        "JACKPOT! +25"    // gold
    };
    private static final Font MESSAGE_FONT = new Font("Monospaced", Font.BOLD, 18);

    private enum WellState { IDLE, SPINNING, SETTLED }

    private WellState state = WellState.IDLE;
    private int spinTimer = 0;
    private int stepTimer = 0;
    private int holdTimer = 0;
    private int lightIndex = 0;
    private int pulse = 0;
    private boolean playerNearby = false;   // draw() has no player, so update() caches it
    private int denyTimer = 0;

    private final KeyLocker keyLocker = new KeyLocker();
    // read fresh each frame: the Controls menu can change it mid-level
    private Key interactKey() {
        return KeyBinds.get(KeyBinds.Action.INTERACT);
    }
    private final Random random = new Random();

    // motes are tracked relative to the well mouth, so they ride along with the camera
    private final float[] moteX     = new float[MOTE_COUNT];   // px either side of centre
    private final float[] moteY     = new float[MOTE_COUNT];   // px above the rim
    private final float[] moteSpeed = new float[MOTE_COUNT];
    private final int[]   moteSize  = new int[MOTE_COUNT];
    private int flashTimer = 0;
    private int burstTimer = 0;
    private int messageTimer = 0;
    private int messageIndex = 0;

    // the "press E" prompt, drawn by hand rather than as this tile's own sprite
    private final SpriteSheet promptSheet =
        new SpriteSheet(ImageLoader.load("KeyPromptE.png"), 16, 16);

    public WishingWell(Point location) {
        super(location.x, location.y,
              new SpriteSheet(ImageLoader.load("KeyPromptE.png"), 16, 16),
              TileType.PASSABLE);
    }

    @Override
    public void update(Player player) {
        super.update(player);
        pulse++;
        playerNearby = canInteract(player);

        if (denyTimer > 0) {
            denyTimer--;
        }
        if (flashTimer > 0) {
            flashTimer--;
        }
        if (burstTimer > 0) {
            burstTimer--;
        }
        if (messageTimer > 0) {
            messageTimer--;
        }
        if (state != WellState.IDLE) {
            updateMotes();
        }

        if (Keyboard.isKeyUp(interactKey())) {
            keyLocker.unlockKey(interactKey());
        }

        switch (state) {
            case IDLE:
                if (playerNearby
                        && Keyboard.isKeyDown(interactKey())
                        && !keyLocker.isKeyLocked(interactKey())) {
                    keyLocker.lockKey(interactKey());
                    if (player.spendCoins(COIN_COST)) {
                        startSpin();
                    } else {
                        denyTimer = DENY_FRAMES;   // not enough coins -- flash the prompt
                    }
                }
                break;

            case SPINNING:
                spinTimer--;
                stepTimer--;
                if (stepTimer <= 0) {
                    lightIndex = (lightIndex + 1) % LIGHTS.length;
                    stepTimer = currentStep();
                    flashTimer = FLASH_FRAMES;      // snap on every flip
                }
                if (spinTimer <= 0) {
                    lightIndex = weightedPick();
                    state = WellState.SETTLED;
                    holdTimer = SETTLE_HOLD;
                    flashTimer = FLASH_FRAMES;
                    burstTimer = BURST_FRAMES;      // payoff ring
                    messageIndex = lightIndex;
                    messageTimer = MESSAGE_FRAMES;  // tell the player what they won
                    onResult(lightIndex, player);
                }
                break;

            case SETTLED:
                holdTimer--;
                if (holdTimer <= 0) {
                    state = WellState.IDLE;
                }
                break;
        }
    }

    private void startSpin() {
        state = WellState.SPINNING;
        messageTimer = 0;              // last result clears the moment a new roll starts
        spinTimer = SPIN_FRAMES;
        stepTimer = FAST_STEP;
        lightIndex = random.nextInt(LIGHTS.length);
        for (int i = 0; i < MOTE_COUNT; i++) {
            seedMote(i);
            moteY[i] = random.nextInt(Math.round(moteCeiling()));   // fill the column at once
        }
    }

    // frames between colour changes -- short at the start, stretching out as it winds down
    private int currentStep() {
        float t = 1f - ((float) spinTimer / SPIN_FRAMES);     // 0 at the start, 1 at the end
        return FAST_STEP + Math.round(t * t * (SLOW_STEP - FAST_STEP));
    }

    private int weightedPick() {
        int total = 0;
        for (int w : WEIGHTS) {
            total += w;
        }
        int roll = random.nextInt(total);
        for (int i = 0; i < WEIGHTS.length; i++) {
            roll -= WEIGHTS[i];
            if (roll < 0) {
                return i;
            }
        }
        return 0;
    }

    private boolean canInteract(Player player) {
        float wellCentreX = getX() + MOUTH_X;
        float wellCentreY = getY() + MOUTH_Y;
        return Math.abs(player.getX() - wellCentreX) <= INTERACT_X
            && Math.abs(player.getY() - wellCentreY) <= INTERACT_Y;
    }

    // ---- payouts ----
    protected void onResult(int colour, Player player) {
        switch (colour) {
            case BLUE:                       // nothing -- you lose the coin
                break;
            case GREEN:
                player.addCoins(5);
                break;
            case RED:                        // ambush: a goblin either side of the well
                spawnGuards();
                break;
            case PURPLE:
                player.addCoins(10);
                break;
            case GOLD:
                player.addCoins(25);
                break;
        }
    }

    // drops a goblin just off each side of the well, both facing inward.
    // no cap -- roll red again and you get another pair on top of whatever is
    // already chasing you.
    private void spawnGuards() {
        if (map == null) {
            return;
        }
        float groundY = getY() + TILE * 4 - GOBLIN_HEIGHT;   // stand them on the grass below
        // a little slop either way, so a second pair does not land on top of the first
        float leftX   = getX() - GOBLIN_WIDTH + jitter();
        float rightX  = getX() + TILE * 4 + 8 + jitter();

        map.addEnemy(new Goblin(new Point(leftX,  groundY), Direction.RIGHT));
        map.addEnemy(new Goblin(new Point(rightX, groundY), Direction.LEFT));
    }

    private int jitter() {
        return random.nextInt(SPAWN_JITTER * 2 + 1) - SPAWN_JITTER;
    }

    public boolean isSpinning() {
        return state == WellState.SPINNING;
    }

    public int getLastResult() {
        return lightIndex;
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        // the well itself is drawn by the map tiles -- this tile is invisible,
        // so deliberately no super.draw() here
        int x = Math.round(getCalibratedXLocation());
        int y = Math.round(getCalibratedYLocation());

        if (state == WellState.IDLE) {
            if (playerNearby) {
                drawPrompt(graphicsHandler, x, y);
            }
        } else {
            drawBeam(graphicsHandler, x, y, LIGHTS[lightIndex]);
        }

        // outlives the beam by design, so the result is still readable once the
        // well has gone quiet -- cleared early if another coin goes in
        if (messageTimer > 0) {
            drawResultText(graphicsHandler, x, y);
        }
    }

    private void drawResultText(GraphicsHandler graphicsHandler, int x, int y) {
        String text = RESULT_TEXT[messageIndex];

        int opacity = messageTimer < MESSAGE_FADE
                ? Math.round(255f * messageTimer / MESSAGE_FADE)
                : 255;
        int rise = Math.min(MESSAGE_FRAMES - messageTimer, MESSAGE_RISE);

        int textWidth = graphicsHandler.getGraphics().getFontMetrics(MESSAGE_FONT).stringWidth(text);
        int tx = x + MOUTH_X - textWidth / 2;
        int ty = y + MOUTH_Y - MESSAGE_LIFT - rise;

        graphicsHandler.drawStringWithOutline(text, tx, ty, MESSAGE_FONT,
                alpha(mix(LIGHTS[messageIndex], Color.WHITE, 0.55f), opacity),
                alpha(new Color(18, 14, 24), opacity),
                3f);
    }

    private void drawPrompt(GraphicsHandler graphicsHandler, int x, int y) {
        int px = x + MOUTH_X - PROMPT_SIZE / 2;
        int py = y + MOUTH_Y - PROMPT_LIFT;

        if (denyTimer > 0) {
            // can't afford it -- shake and flash red so it reads as a refusal
            px += ((denyTimer / 3) % 2 == 0) ? 2 : -2;
            graphicsHandler.drawFilledRectangle(
                px - 3, py - 3, PROMPT_SIZE + 6, PROMPT_SIZE + 6,
                new Color(220, 40, 40, 150));
        }

        // gentle bob, using the two frames of KeyPromptE.png
        int frame = (pulse / 30) % 2;
        if (interactKey() == Key.E) {
            // the painted cap says E, so it is only right while E is the binding
            graphicsHandler.drawImage(
                promptSheet.getSprite(0, frame),
                px, py, PROMPT_SIZE, PROMPT_SIZE);
        } else {
            KeyPrompt.draw(graphicsHandler, interactKey(),
                    px + PROMPT_SIZE / 2, py, PROMPT_SIZE, frame == 1);
        }
    }

    private void drawBeam(GraphicsHandler graphicsHandler, int x, int y, Color colour) {
        int cx = x + MOUTH_X;
        int baseY = y + MOUTH_Y;

        boolean settled = state == WellState.SETTLED;
        int peak = settled ? 190 + rarityBonus() : 150;

        // every colour flip blows the column out white for a few frames, which is
        // what makes the cycling read as a slot reel rather than a colour fade
        Color tint = colour;
        if (flashTimer > 0) {
            float k = (float) flashTimer / FLASH_FRAMES;
            tint = mix(colour, Color.WHITE, k * 0.7f);
            peak += Math.round(k * 50);
        }

        // slow breath so the beam is never perfectly still
        float breathe = 1f + 0.06f * (float) Math.sin(pulse * 0.18f);

        Graphics2D g = graphicsHandler.getGraphics();
        Object oldHint = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawColumn(graphicsHandler, g, cx, baseY, tint, peak, breathe);
        drawMouthGlow(g, cx, baseY, tint, peak);
        drawRim(graphicsHandler, cx, baseY, tint, peak, breathe);
        drawMotes(graphicsHandler, cx, baseY, tint, peak);
        if (burstTimer > 0) {
            drawBurst(graphicsHandler, cx, baseY, tint);
        }

        if (oldHint != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldHint);
        }
    }

    // Three nested cones, each fading to nothing at the top: a wide soft halo, a
    // body, and a hot core. Stacked flat rectangles read as a painted slab -- the
    // vertical gradient is what makes it look like light leaving the well.
    private void drawColumn(GraphicsHandler graphicsHandler, Graphics2D g, int cx, int baseY,
                            Color colour, int peak, float breathe) {
        int height = BEAM_SEGMENTS * SEGMENT_H;
        // the gradient reaches full transparency well before the cone's top edge,
        // otherwise that edge shows up as a hard horizontal line
        int fadeOutY = baseY - Math.round(height * 0.72f);

        float[] baseWidth = { BEAM_WIDTH * 0.74f, BEAM_WIDTH * 0.42f, CORE_WIDTH * 0.55f };
        float[] topWidth  = { BEAM_WIDTH + BEAM_SEGMENTS * BEAM_FLARE, BEAM_WIDTH * 0.66f, CORE_WIDTH };
        float[] strength  = { 0.34f, 0.62f, 1.00f };

        Paint oldPaint = g.getPaint();
        Polygon halo = cone(cx, baseY, height, baseWidth[0] * breathe, topWidth[0] * breathe);
        int sway = Math.round(3f * (float) Math.sin(pulse * 0.11f));   // core drifts like a flame

        for (int i = 0; i < strength.length; i++) {
            int a = Math.round(peak * strength[i]);
            if (a <= 0) {
                continue;
            }
            g.setPaint(new GradientPaint(0, baseY, alpha(colour, a), 0, fadeOutY, alpha(colour, 0)));
            Polygon layer = cone(cx, baseY, height, baseWidth[i] * breathe, topWidth[i] * breathe);
            if (i == strength.length - 1) {
                layer.xpoints[2] += sway;      // only the core sways, the halo stays put
                layer.xpoints[3] += sway;
            }
            g.fill(layer);
        }
        g.setPaint(oldPaint);

        // bright rungs sliding up inside the cone -- this is what sells it as
        // flowing light rather than a static stripe
        Shape oldClip = g.getClip();
        g.clip(halo);

        int scroll = (pulse * 2) % SEGMENT_H;
        Color rungColour = mix(colour, Color.WHITE, 0.4f);
        for (int i = 0; i <= BEAM_SEGMENTS; i++) {
            int above = i * SEGMENT_H + scroll;
            if (above <= 0 || above > height) {
                continue;
            }
            float fade = 1f - (float) above / height;
            float spread = above / (float) height;
            int w = Math.round((BEAM_WIDTH * 0.46f + spread * BEAM_SEGMENTS * BEAM_FLARE) * breathe);
            graphicsHandler.drawFilledRectangle(cx - w / 2, baseY - above, w, 2,
                    alpha(rungColour, Math.round(peak * fade * fade * 0.5f)));
        }

        g.setClip(oldClip);
    }

    private Polygon cone(int cx, int baseY, int height, float bottomWidth, float topWidth) {
        int b = Math.round(bottomWidth / 2f);
        int t = Math.round(topWidth / 2f);
        return new Polygon(
                new int[] { cx - b, cx + b, cx + t, cx - t },
                new int[] { baseY, baseY, baseY - height, baseY - height },
                4);
    }

    // soft pool of light sitting in the well mouth, clipped so it never spills
    // down over the stone drum
    private void drawMouthGlow(Graphics2D g, int cx, int baseY, Color colour, int peak) {
        Paint oldPaint = g.getPaint();
        Shape oldClip = g.getClip();

        g.setClip(cx - GLOW_RADIUS, baseY - GLOW_RADIUS, GLOW_RADIUS * 2, GLOW_RADIUS);
        g.setPaint(new RadialGradientPaint(
                new Point2D.Float(cx, baseY),
                GLOW_RADIUS,
                new float[] { 0f, 0.4f, 1f },
                new Color[] {
                        alpha(mix(colour, Color.WHITE, 0.35f), Math.min(255, peak + 50)),
                        alpha(colour, Math.round(peak * 0.55f)),
                        alpha(colour, 0)
                }));
        g.fillRect(cx - GLOW_RADIUS, baseY - GLOW_RADIUS, GLOW_RADIUS * 2, GLOW_RADIUS);

        g.setClip(oldClip);
        g.setPaint(oldPaint);
    }

    // hot line right at the lip, so the light reads as coming out of the hole
    private void drawRim(GraphicsHandler graphicsHandler, int cx, int baseY,
                         Color colour, int peak, float breathe) {
        int w = Math.round(BEAM_WIDTH * 0.74f * breathe);
        graphicsHandler.drawFilledRectangle(cx - w / 2, baseY - 3, w, 3,
                alpha(mix(colour, Color.WHITE, 0.55f), Math.min(255, peak + 45)));
        graphicsHandler.drawFilledRectangle(cx - w / 4, baseY - 5, w / 2, 5,
                alpha(mix(colour, Color.WHITE, 0.7f), Math.min(255, peak + 25)));
    }

    private void drawMotes(GraphicsHandler graphicsHandler, int cx, int baseY, Color colour, int peak) {
        Color sparkColour = mix(colour, Color.WHITE, 0.25f);

        for (int i = 0; i < MOTE_COUNT; i++) {
            // squared falloff, matched to where the column itself goes transparent --
            // a linear fade leaves grey specks hanging in the dark above the beam
            float fade = 1f - (moteY[i] / moteCeiling());
            if (fade <= 0f) {
                continue;
            }
            fade *= fade;
            int size = moteSize[i];
            graphicsHandler.drawFilledRectangle(
                    Math.round(cx + moteX[i]) - size / 2,
                    Math.round(baseY - moteY[i]) - size / 2,
                    size, size,
                    alpha(sparkColour, Math.round(Math.min(255, peak + 40) * fade)));
        }
    }

    // motes die off at the same height the column fades out at
    private float moteCeiling() {
        return BEAM_SEGMENTS * SEGMENT_H * 0.78f;
    }

    // a shockwave dome punching out of the rim the instant the colour locks in.
    // drawn as clipped ellipses -- a rectangle outline here just reads as a UI box.
    private void drawBurst(GraphicsHandler graphicsHandler, int cx, int baseY, Color colour) {
        float t = 1f - ((float) burstTimer / BURST_FRAMES);      // 0 at the hit, 1 at the end
        float decay = (1f - t) * (1f - t);
        int a = Math.round(215 * decay);
        if (a <= 0) {
            return;
        }

        Graphics2D g = graphicsHandler.getGraphics();
        Shape oldClip = g.getClip();
        Stroke oldStroke = g.getStroke();
        Color oldColor = g.getColor();

        int rx = Math.round(30 + t * 120);
        int ry = Math.round(22 + t * 84);
        // clip to the upper half so the wave is a dome and never crosses the stone
        g.setClip(cx - rx - 10, baseY - ry - 10, (rx + 10) * 2, ry + 10);

        g.setStroke(new BasicStroke(Math.max(2f, 7f * (1f - t))));
        g.setColor(alpha(mix(colour, Color.WHITE, 0.3f), a));
        g.draw(new Ellipse2D.Float(cx - rx, baseY - ry, rx * 2, ry * 2));

        // a slower inner ring, so the blast has some depth to it
        int innerRx = Math.round(18 + t * 76);
        int innerRy = Math.round(13 + t * 54);
        g.setStroke(new BasicStroke(Math.max(1.5f, 4f * (1f - t))));
        g.setColor(alpha(colour, Math.round(a * 0.7f)));
        g.draw(new Ellipse2D.Float(cx - innerRx, baseY - innerRy, innerRx * 2, innerRy * 2));

        g.setColor(oldColor);
        g.setStroke(oldStroke);
        g.setClip(oldClip);
    }

    private void seedMote(int i) {
        moteX[i] = random.nextInt(BEAM_WIDTH) - BEAM_WIDTH / 2f;
        moteY[i] = -random.nextInt(10);                  // born just inside the mouth
        moteSpeed[i] = 0.8f + random.nextFloat() * 1.9f;
        moteSize[i] = 2 + random.nextInt(3);
    }

    private void updateMotes() {
        for (int i = 0; i < MOTE_COUNT; i++) {
            moteY[i] += moteSpeed[i];
            moteX[i] += (float) Math.sin((pulse + i * 11) * 0.08f) * 0.35f;   // lazy drift
            if (moteY[i] > moteCeiling()) {
                seedMote(i);
            }
        }
    }

    // the rarer the result, the harder the light burns once it settles
    private int rarityBonus() {
        switch (lightIndex) {
            case GOLD:   return 55;
            case PURPLE: return 35;
            case RED:    return 20;
            case GREEN:  return 10;
            default:     return 0;
        }
    }

    private Color mix(Color from, Color to, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return new Color(
                Math.round(from.getRed()   + (to.getRed()   - from.getRed())   * t),
                Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * t),
                Math.round(from.getBlue()  + (to.getBlue()  - from.getBlue())  * t));
    }

    private Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("DEFAULT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0), 30)
                        .withScale(3)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(0, 1), 30)
                        .withScale(3)
                        .build()
            });
        }};
    }
}
