package Players;

import Engine.ImageLoader;

import java.awt.image.BufferedImage;
import java.util.HashMap;

/*
 * The knight's hats: the art, the prices, and where each one sits on his head.
 *
 * Hats.png is one row of 28x22 cells with the usual 1px separators. Each cell
 * is drawn so that its BOTTOM CENTRE is the point that lands on the helmet, and
 * nothing is drawn below that point, so positioning a hat is a single offset.
 *
 * The anchors below are that point, per animation frame, in the knight's own
 * 112x84 cell. They are not hand-typed -- the head moves every frame (it drifts,
 * bobs and leans, and the walk cycle alone shifts it 3px), so they were measured
 * by sliding the helmet from the STAND frame over every other frame and taking
 * the best match. gen_hatset.py regenerates this file.
 *
 * DEATH frames 3-5 are null. He is flat on the floor there with his helmet on
 * its side, so no upright hat position is correct; the hat is simply not drawn.
 */
public final class HatSet {

    public static final int NONE = -1;
    public static final int CELL_W = 28;
    public static final int CELL_H = 22;

    // the knight's own cell, needed to mirror the anchor when he faces left
    private static final int KNIGHT_CELL_W = 112;
    private static final int GAP = 1;

    public static final String[] NAMES = {
            "Party Hat",
            "Horned Helm",
            "Gold Crown"
    };

    public static final String[] BLURBS = {
            "Somebody has to keep morale up.",
            "Twice the horns, same amount of skull.",
            "Heavy lies the head, and so on."
    };

    public static final int[] PRICES = { 10, 20, 35 };

    private static final HashMap<String, int[][]> ANCHORS =
            new HashMap<String, int[][]>();

    static {
        ANCHORS.put("STAND", new int[][] { { 57, 28 }, { 57, 28 }, { 57, 28 }, { 57, 28 } });
        ANCHORS.put("SWIM_STAND", new int[][] { { 57, 28 }, { 57, 28 }, { 57, 28 }, { 57, 28 } });
        ANCHORS.put("CROUCH", new int[][] { { 57, 28 } });
        ANCHORS.put("WALK", new int[][] {
                { 60, 28 }, { 60, 30 }, { 60, 30 }, { 60, 29 }, { 60, 29 }, { 60, 31 }, { 60, 30 } });
        ANCHORS.put("JUMP", new int[][] { { 59, 30 }, { 59, 23 }, { 59, 21 } });
        ANCHORS.put("FALL", new int[][] { { 58, 21 }, { 58, 22 }, { 57, 23 } });
        ANCHORS.put("ATTACK", new int[][] {
                { 54, 28 }, { 54, 28 }, { 63, 30 }, { 62, 30 }, { 62, 29 } });
        ANCHORS.put("AIR_ATTACK", new int[][] { { 55, 21 }, { 61, 22 }, { 60, 21 }, { 60, 21 } });
        ANCHORS.put("DASH", new int[][] { { 64, 29 }, { 67, 31 }, { 67, 31 } });
        ANCHORS.put("SHOOT", new int[][] {
                { 56, 28 }, { 54, 28 }, { 55, 28 }, { 55, 28 }, { 56, 28 } });
        ANCHORS.put("DEATH", new int[][] { { 51, 28 }, { 51, 30 }, { 60, 34 }, null, null, null });
    }

    private static BufferedImage[] cells;

    private static BufferedImage[] cells() {
        if (cells == null) {
            BufferedImage sheet = ImageLoader.load("Hats.png");
            cells = new BufferedImage[NAMES.length];
            for (int i = 0; i < NAMES.length; i++) {
                cells[i] = sheet.getSubimage(i * (CELL_W + GAP), 0, CELL_W, CELL_H);
            }
        }
        return cells;
    }

    public static int count() {
        return NAMES.length;
    }

    public static BufferedImage image(int index) {
        return cells()[index];
    }

    /*
     * The anchor for what is on screen right now, or null if this frame has none.
     * Animation names carry a _LEFT / _RIGHT suffix that the table does not.
     */
    public static int[] anchor(String animationName, int frameIndex) {
        if (animationName == null) {
            return null;
        }
        String base = animationName;
        if (base.endsWith("_LEFT")) {
            base = base.substring(0, base.length() - "_LEFT".length());
        } else if (base.endsWith("_RIGHT")) {
            base = base.substring(0, base.length() - "_RIGHT".length());
        }
        int[][] frames = ANCHORS.get(base);
        if (frames == null || frameIndex < 0 || frameIndex >= frames.length) {
            return null;
        }
        return frames[frameIndex];
    }

    /*
     * Left edge of the hat cell, relative to the knight's cell.
     *
     * Facing left, the engine flips the whole 112px cell, so the hat has to be
     * mirrored within that same cell -- not just flipped in place, which would
     * leave it offset by however far the head is from the centre.
     */
    public static int drawX(int[] anchor, boolean facingLeft) {
        int x = anchor[0] - CELL_W / 2;
        return facingLeft ? KNIGHT_CELL_W - x - CELL_W : x;
    }

    public static int drawY(int[] anchor) {
        return anchor[1] - (CELL_H - 1);
    }

    private HatSet() {
    }
}
