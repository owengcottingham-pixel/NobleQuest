package Players;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Player;
import java.util.HashMap;

public class Knight extends Player {

    public Knight(float x, float y) {
        super(new SpriteSheet(ImageLoader.load("Knight.png"), 112, 84), x, y, "STAND_RIGHT");        gravity = .5f;
        terminalVelocityY = 6f;
        jumpHeight = 14.5f;
        jumpDegrade = .5f;
        walkSpeed = 2.3f;
        momentumYIncrease = .5f;
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{

            put("STAND_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(1)
                    .withBounds(43, 10, 25, 70)
                    .build()
            });

            put("STAND_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(1)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .withBounds(44, 10, 25, 70)
                    .build()
            });

            put("WALK_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0), 14)
                    .withScale(1)
                    .withBounds(43, 10, 25, 70)
                    .build(),
                new FrameBuilder(spriteSheet.getSprite(0, 1), 14)
                    .withScale(1)
                    .withBounds(43, 10, 25, 70)
                    .build()
            });

            put("WALK_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0), 14)
                    .withScale(1)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .withBounds(44, 10, 25, 70)
                    .build(),
                new FrameBuilder(spriteSheet.getSprite(0, 1), 14)
                    .withScale(1)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .withBounds(44, 10, 25, 70)
                    .build()
            });

            put("JUMP_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 1))
                    .withScale(1)
                    .withBounds(43, 10, 25, 70)
                    .build()
            });

            put("JUMP_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 1))
                    .withScale(1)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .withBounds(44, 10, 25, 70)
                    .build()
            });

            put("FALL_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 1))
                    .withScale(1)
                    .withBounds(43, 10, 25, 70)
                    .build()
            });

            put("FALL_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 1))
                    .withScale(1)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .withBounds(44, 10, 25, 70)
                    .build()
            });

            put("ATTACK_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(1, 0), 6)
                    .withScale(1)
                    .withBounds(43, 10, 25, 70)
                    .build(),
                new FrameBuilder(spriteSheet.getSprite(1, 1), 6)
                    .withScale(1)
                    .withBounds(43, 10, 25, 70)
                    .build()
            });

            put("ATTACK_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(1, 0), 6)
                    .withScale(1)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .withBounds(44, 10, 25, 70)
                    .build(),
                new FrameBuilder(spriteSheet.getSprite(1, 1), 6)
                    .withScale(1)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .withBounds(44, 10, 25, 70)
                    .build()
            });

            put("CROUCH_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(1)
                    .withBounds(43, 10, 25, 70)
                    
                    .build()
            });

            put("CROUCH_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(1)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .withBounds(44, 10, 25, 70)
                    .build()
            });
        }};
    }
}