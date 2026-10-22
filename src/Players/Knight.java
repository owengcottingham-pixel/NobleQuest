package Players;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.Mouse;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.LevelState;
import Level.Player;
import Utils.Direction;


import java.util.HashMap;
import java.util.ArrayList;

public class Knight extends Player {

    // The knight's own sheet has a blue crest on the helmet, right where a hat
    // goes. The bare sheet is the same art with the crest taken off, so wearing
    // a hat swaps to that one and taking it off swaps back.
    private static final String CRESTED_SHEET = "CAPEKnight.png";
    private static final String BARE_SHEET = "CAPEKnightBare.png";

    // static, so a hat survives dying and the level reloading -- a new Knight is
    // built every time the level restarts, and losing a bought hat to a goblin
    // would be a rough deal
    private static int equippedHat = HatSet.NONE;

    private String sheetInUse;

    private static String sheetFor(int hatIndex) {
        return hatIndex == HatSet.NONE ? CRESTED_SHEET : BARE_SHEET;
    }

    public Knight(float x, float y) {
        super(new SpriteSheet(ImageLoader.load(sheetFor(equippedHat)), 112, 84), x, y, "STAND_RIGHT");
        sheetInUse = sheetFor(equippedHat);
        gravity = .5f;
        terminalVelocityY = 6f;
        jumpHeight = 14.5f;
        jumpDegrade = .5f;
        walkSpeed = 3.5f;
        momentumYIncrease = .5f;
    }
    
        // CROSSBOW 
    private static final int SHOOT_TIME = 26;      // the SHOOT animation
    private static final int RELEASE_AT = 18;      // bolt flies on frame 1 of SHOOT, after 8 frames of aiming
    private static final float BOLT_SPEED = 16f;
    private int shootTimer = 0;
    private Direction aimSide = Direction.RIGHT;
    private final ArrayList<CrossbowBolt> bolts = new ArrayList<>();

    @Override
    public void update() {
        if (levelState == LevelState.RUNNING) {
            if (playerState == Player.PlayerState.DASHING || isAttacking()) {
                shootTimer = 0;                    // dashing or the sword cancels a shot
            } else if (shootTimer == 0 && Mouse.isLeftDown()) {
                shootTimer = SHOOT_TIME;           // holding the button keeps firing
            }
            if (shootTimer > 0) {
                aimSide = mouseMapX() < getBounds().getX1() + getBounds().getWidth() / 2f ? Direction.LEFT : Direction.RIGHT;
                if (shootTimer == RELEASE_AT) {
                    fireBolt();
                }
                shootTimer--;
            }
        }

        super.update();

        for (int i = bolts.size() - 1; i >= 0; i--) {
            bolts.get(i).update(map, this);
            if (bolts.get(i).isDone()) {
                bolts.remove(i);
            }
        }
    }

    private void fireBolt() {
        // the crossbow tip in the SHOOT frames (mirrored when facing left)
        float tipX = getX() + (aimSide == Direction.RIGHT ? 86 : 26);
        float tipY = getY() + 41;
        float dx = mouseMapX() - tipX;
        float dy = mouseMapY() - tipY;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 1) {
            dx = aimSide == Direction.RIGHT ? 1 : -1;
            dy = 0;
            length = 1;
        }
        bolts.add(new CrossbowBolt(tipX, tipY, dx / length * BOLT_SPEED, dy / length * BOLT_SPEED));
    }

    // the mouse is in screen pixels, so add the camera to get the spot on the map
    private float mouseMapX() {
        return Mouse.getX() + map.getCamera().getX();
    }

    private float mouseMapY() {
        return Mouse.getY() + map.getCamera().getY();
    }

    @Override
    protected void handlePlayerAnimation() {
        super.handlePlayerAnimation();
        if (shootTimer > 0) {
            facingDirection = aimSide;             // face where you're aiming
            currentAnimationName = aimSide == Direction.RIGHT ? "SHOOT_RIGHT" : "SHOOT_LEFT";
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
        for (CrossbowBolt bolt : bolts) {
            bolt.draw(graphicsHandler, map);
        }
    }

    @Override
    public int getHat() {
        return equippedHat;
    }

    @Override
    public void setHat(int hatIndex) {
        equippedHat = hatIndex;
        String wanted = sheetFor(hatIndex);
        if (wanted.equals(sheetInUse)) {
            return;
        }
        // Keep the animation and the frame he is on. Resetting to frame 0 here
        // would snap him back to the start of his stride the instant he puts a
        // hat on, which reads as a stutter.
        String animation = currentAnimationName;
        int frame = currentFrameIndex;
        this.animations = loadAnimations(new SpriteSheet(ImageLoader.load(wanted), 112, 84));
        this.sheetInUse = wanted;
        this.currentAnimationName = animation;
        this.currentFrameIndex = frame;
        updateCurrentFrame();
    }

    @Override
    protected void drawCosmetics(GraphicsHandler graphicsHandler) {
        if (equippedHat == HatSet.NONE) {
            return;
        }
        int[] anchor = HatSet.anchor(currentAnimationName, currentFrameIndex);
        if (anchor == null) {
            return;                 // no upright head this frame (he is on the floor)
        }
        boolean facingLeft = currentAnimationName.endsWith("_LEFT");
        graphicsHandler.drawImage(
                HatSet.image(equippedHat),
                Math.round(getCalibratedXLocation()) + HatSet.drawX(anchor, facingLeft),
                Math.round(getCalibratedYLocation()) + HatSet.drawY(anchor),
                HatSet.CELL_W,
                HatSet.CELL_H,
                facingLeft ? ImageEffect.FLIP_HORIZONTAL : ImageEffect.NONE);
    }

    // Rebuilds every animation from a different sheet, then refreshes the frame
    // being shown so the change is visible immediately rather than on the next
    // animation switch.
    @Override
    public void setCostume(String imageFileName) {
        HashMap<String, Frame[]> previous = this.animations;
        try {
            this.animations = loadAnimations(new SpriteSheet(ImageLoader.load(imageFileName), 112, 84));
            this.currentFrameIndex = 0;
            updateCurrentFrame();
        } catch (Exception e) {
            // A sheet with a different grid (Knight.png is 3x6, this one needs
            // 7x8) would throw while slicing frames and take the game down mid
            // frame. Put the old costume back instead.
            this.animations = previous;
            this.currentFrameIndex = 0;
            updateCurrentFrame();
            System.out.println("Costume '" + imageFileName
                    + "' does not fit the knight's sheet layout - keeping the current one. " + e);
        }
    }

    @Override
    public HashMap<String, Frame[]>loadAnimations(SpriteSheet spriteSheet) {
        HashMap<String, Frame[]> animations = new HashMap<>();

        for (String side : new String[] { "RIGHT", "LEFT" }) {
            boolean left = side.equals("LEFT");

            // row, starting column, delay for each frame (0 = stay on that frame)
            animations.put("STAND_" + side,      animate(spriteSheet, 0, 0, new int[] { 10, 10, 10, 10 }, left));
            animations.put("WALK_" + side,       animate(spriteSheet, 1, 0, new int[] { 5, 5, 5, 5, 5, 5, 5 }, left));
            animations.put("JUMP_" + side,       animate(spriteSheet, 2, 0, new int[] { 3, 4, 0 }, left));
            animations.put("FALL_" + side,       animate(spriteSheet, 2, 3, new int[] { 6, 6, 0 }, left));
            animations.put("ATTACK_" + side,     animate(spriteSheet, 3, 0, new int[] { 2, 2, 2, 3, 3 }, left));
            animations.put("AIR_ATTACK_" + side, animate(spriteSheet, 4, 0, new int[] { 3, 3, 3, 3 }, left));
            animations.put("DASH_" + side,       animate(spriteSheet, 5, 0, new int[] { 3, 5, 0 }, left));
            animations.put("SHOOT_" + side,      animate(spriteSheet, 6, 0, new int[] { 8, 3, 5, 5, 5 }, left));
            animations.put("SWIM_STAND_" + side, animate(spriteSheet, 0, 0, new int[] { 10, 10, 10, 10 }, left));
            animations.put("CROUCH_" + side,     animate(spriteSheet, 0, 0, new int[] { 0 }, left));
            animations.put("DEATH_" + side,      animate(spriteSheet, 7, 0, new int[] { 6, 6, 6, 6, 8, 0 }, left));
        }

        return animations;
    }
    // frames go left to right along one row of the sprite sheet
    private Frame[] animate(SpriteSheet sheet, int row, int startCol, int[] delays, boolean left) {
        Frame[] frames = new Frame[delays.length];
        for (int i = 0; i < delays.length; i++) {
            FrameBuilder builder = new FrameBuilder(sheet.getSprite(row, startCol + i), delays[i]).withScale(1);
            if (left) {
                builder = builder.withImageEffect(ImageEffect.FLIP_HORIZONTAL).withBounds(44, 10, 25, 70);
            } else {
                builder = builder.withBounds(43, 10, 25, 70);
            }
            frames[i] = builder.build();
        }
        return frames;
    }
}