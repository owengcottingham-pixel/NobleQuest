package Enemies;

import Engine.ImageLoader;
import GameObject.SpriteSheet;
import Utils.Direction;
import Utils.Point;

public class HobGoblin extends BugEnemy {

    public HobGoblin(Point location, Direction facingDirection) {
        super(location, facingDirection,
              new SpriteSheet(ImageLoader.load("Hobgoblin.png"), 104, 80));

        maxHealth = 5;
        health = maxHealth;
        movementSpeed = 0.8f;
        attackDamage = 2;
        attackRange = 95;
        windupFrames = 18;
        attackCooldown = 55;
        knockbackSpeed = 5f;
    }
}