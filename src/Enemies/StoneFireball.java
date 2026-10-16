package Enemies;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.MapEntity;
import Level.MapEntityStatus;
import Level.Player;
import Utils.Direction;
import Utils.Point;

import java.util.HashMap;

public class StoneFireball extends Enemy {
    private static final float MOVEMENT_SPEED = 6f;
    private static final float SPRITE_SCALE = 0.12f;

    private final Direction direction;
    private final float maxDistance;
    private float distanceTravelled;

    public StoneFireball(Point location, Direction direction, float maxDistance) {
        super(location.x, location.y,
              new SpriteSheet(ImageLoader.load("Fireball.png"), 171, 176),
              "DEFAULT");
        this.direction = direction;
        this.maxDistance = maxDistance;
        initialize();
    }

    @Override
    public void update(Player player) {
        if (mapEntityStatus == MapEntityStatus.REMOVED) {
            return;
        }

        float distanceRemaining = maxDistance - distanceTravelled;
        if (distanceRemaining <= 0) {
            mapEntityStatus = MapEntityStatus.REMOVED;
            return;
        }

        float distanceToMove = Math.min(MOVEMENT_SPEED, distanceRemaining);
        float moved = moveXHandleCollision(direction.getVelocity() * distanceToMove);
        distanceTravelled += Math.abs(moved);

        if (mapEntityStatus == MapEntityStatus.REMOVED || distanceTravelled >= maxDistance) {
            mapEntityStatus = MapEntityStatus.REMOVED;
            return;
        }

        super.update();
        if (intersects(player)) {
            player.damage(1);
            mapEntityStatus = MapEntityStatus.REMOVED;
        }
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (hasCollided) {
            mapEntityStatus = MapEntityStatus.REMOVED;
        }
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("DEFAULT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0))
                            .withScale(SPRITE_SCALE)
                            .build()
            });
        }};
    }
}
