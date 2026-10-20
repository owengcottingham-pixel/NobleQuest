package Players;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.LevelState;
import Level.Player;
import Utils.Direction;

import java.util.HashMap;

public class Knight extends Player {
    


    public Knight(float x, float y) {
        super(new SpriteSheet(ImageLoader.load("CAPEKnight.png"), 112, 84), x, y, "STAND_RIGHT");        gravity = .5f;
        terminalVelocityY = 6f;
        jumpHeight = 14.5f;
        jumpDegrade = .5f;
        walkSpeed = 3.5f;
        momentumYIncrease = .5f;
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