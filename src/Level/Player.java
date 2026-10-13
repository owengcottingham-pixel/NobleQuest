package Level;

import Engine.GraphicsHandler;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Utils.AirGroundState;
import Utils.Direction;
import EnhancedMapTiles.FallingObject;

import java.util.ArrayList;

public abstract class Player extends GameObject {

    
    protected int maxHealth = 5;
    protected int health = maxHealth;
    protected int invincibilityTimer = 0;
    protected int invincibilityDuration = 90;   // 1.5s at 60fps
    protected int deathTimer = 0;
    
    // values that affect player movement

    // these should be set in a subclass

    protected int coins = 0;
    protected float walkSpeed = 0;
    protected float gravity = 0;
    protected float jumpHeight = 0;
    protected float jumpDegrade = 0;
    protected float terminalVelocityY = 0;
    protected float momentumYIncrease = 0;

    // values used to handle player movement
    protected float jumpForce = 0;
    protected float momentumY = 0;
    protected float moveAmountX, moveAmountY;
    protected float lastAmountMovedX, lastAmountMovedY;
    protected float dashSpeed = 7f;   // walkSpeed is usually ~2.3f, so this is a big jump
    protected int dashDuration = 17;   // frames the dash lasts (~1/6 sec at 60fps)
    protected int dashCooldown = 120;
    protected java.util.ArrayList<Float> dashTrailX = new java.util.ArrayList<>();
    protected java.util.ArrayList<Float> dashTrailY = new java.util.ArrayList<>();
    protected int dashTrailLength = 5;
    protected float velocityX = 0;
    protected float acceleration = 0.3f;
    protected float deceleration = 0.4f;

    // values used to keep track of player's current state
    protected PlayerState playerState;
    protected PlayerState previousPlayerState;
    protected Direction facingDirection;
    protected AirGroundState airGroundState;
    protected AirGroundState previousAirGroundState;
    protected LevelState levelState;
    protected int dashTimer = 0;
    protected int dashCooldownTimer = 0;
    protected int attackTimer = 0;
    protected Direction dashDirection = Direction.RIGHT;

    // classes that listen to player events can be added to this list
    protected ArrayList<PlayerListener> listeners = new ArrayList<>();

    // define keys
    protected KeyLocker keyLocker = new KeyLocker();
    protected Key JUMP_KEY = Key.W;
    protected Key MOVE_LEFT_KEY = Key.A;
    protected Key MOVE_RIGHT_KEY = Key.D;
    protected Key CROUCH_KEY = Key.S;

    protected Key DASH_KEY = Key.SPACE;

    protected Key ATTACK_KEY = Key.J;

    //Dash
    public enum PlayerState
    {
        STANDING, WALKING, JUMPING, CROUCHING, DASHING, ATTACKING
    }

    // flags
    protected boolean isInvincible = false;

    public Player(SpriteSheet spriteSheet, float x, float y, String startingAnimationName) {
        super(spriteSheet, x, y, startingAnimationName);
        facingDirection = Direction.RIGHT;
        airGroundState = AirGroundState.AIR;
        previousAirGroundState = airGroundState;
        playerState = PlayerState.STANDING;
        previousPlayerState = playerState;
        levelState = LevelState.RUNNING;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isHurtInvincible() {
        return invincibilityTimer > 0;
    }

    public void damage(int amount) {
        if (invincibilityTimer > 0) {
            return;
        }
        health -= amount;
        invincibilityTimer = invincibilityDuration;

        if (health <= 0) {
            health = 0;
            levelState = LevelState.PLAYER_DEAD;
        }
    }

    // hurts the player if they are touching any spike tile placed in the map editor
    private void checkSpikeTiles() {
        if (map == null) {
            return;
        }
        int x1 = Math.round(getBounds().getX1());
        int x2 = Math.round(getBounds().getX2());
        int y1 = Math.round(getBounds().getY1());
        int y2 = Math.round(getBounds().getY2());
        int step = 24; // half a tile, so no spike tile gets skipped

        for (int x = x1; x <= x2 + step; x += step) {
            for (int y = y1; y <= y2 + step; y += step) {
                MapTile tile = map.getTileByPosition(Math.min(x, x2), Math.min(y, y2));
                if (tile != null && tile.getTileType() == TileType.SPIKES && tile.intersects(this)) {
                    damage(3); // 5 health, so two hits kills
                    return;
                }
            }
        }
    }

    public boolean isAttacking() {
        return currentAnimationName.startsWith("ATTACK");
    }

    public float getLastAmountMovedY() {
    return lastAmountMovedY;
}

    public void update() {
        if (invincibilityTimer > 0) {
            invincibilityTimer--;
        }
        moveAmountX = 0;
        moveAmountY = 0;

        // if player is currently playing through level (has not won or lost)
        if (levelState == LevelState.RUNNING) {
            applyGravity();

            if (dashCooldownTimer > 0)
            {
                dashCooldownTimer--;
            }

            if (Keyboard.isKeyDown(DASH_KEY) && !keyLocker.isKeyLocked(DASH_KEY)
            && playerState != PlayerState.DASHING && dashCooldownTimer == 0) {
                keyLocker.lockKey(DASH_KEY);
                dashDirection = facingDirection;
                dashTimer = dashDuration;
                playerState = PlayerState.DASHING;
            }

            // update player's state and current actions
            do {
                previousPlayerState = playerState;
                handlePlayerState();
            } while (previousPlayerState != playerState);

            previousAirGroundState = airGroundState;

            // move player with respect to map collisions
            float targetX = moveAmountX;

            if (playerState == PlayerState.DASHING) {
                velocityX = targetX;
            } else {
                float rate = (targetX != 0) ? acceleration : deceleration;
                if (targetX != 0 && Math.signum(targetX) != Math.signum(velocityX)) {
                    rate = acceleration * 3.0f;
                }
                if (velocityX < targetX) {
                    velocityX = Math.min(velocityX + rate, targetX);
                } else if (velocityX > targetX) {
                    velocityX = Math.max(velocityX - rate, targetX);
                }
            }

            lastAmountMovedX = super.moveXHandleCollision(velocityX);
            
            lastAmountMovedY = super.moveYHandleCollision(moveAmountY);

            checkSpikeTiles();

            handlePlayerAnimation();

            updateLockedKeys();

            // update player's animation
            super.update();
        }

        // if player has beaten level
        else if (levelState == LevelState.LEVEL_COMPLETED) {
            updateLevelCompleted();
        }

        // if player has lost level
        else if (levelState == LevelState.PLAYER_DEAD) {
            updatePlayerDead();
        }
    }

    

    // add gravity to player, which is a downward force
    protected void applyGravity() {
        moveAmountY += gravity + momentumY;
    }

    // based on player's current state, call appropriate player state handling method
    protected void handlePlayerState() {
        switch (playerState) {
            case STANDING:
                playerStanding();
                break;

            case WALKING:
                playerWalking();
                break;

            case CROUCHING:
                playerCrouching();
                break;

            case JUMPING:
                playerJumping();
                break;

            case DASHING:
                playerDashing();
                break;

            case ATTACKING:
                playerAttacking();
                break;
            
        }
    }

    // player STANDING state logic
    protected void playerStanding() {
        if (canAttack() && Keyboard.isKeyDown(ATTACK_KEY) && !keyLocker.isKeyLocked(ATTACK_KEY)) {
            startAttack();
        }

        else if (Keyboard.isKeyDown(MOVE_LEFT_KEY) || Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            playerState = PlayerState.WALKING;
        }

        else if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }

        else if (Keyboard.isKeyDown(CROUCH_KEY)) {
            playerState = PlayerState.CROUCHING;
        }
    }

    // player WALKING state logic
    protected void playerWalking() {
        if (canAttack() && Keyboard.isKeyDown(ATTACK_KEY) && !keyLocker.isKeyLocked(ATTACK_KEY)) {
            startAttack();
            return;
        }

        if (Keyboard.isKeyDown(MOVE_LEFT_KEY)) {
            moveAmountX -= walkSpeed;
            facingDirection = Direction.LEFT;
        }

        else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            moveAmountX += walkSpeed;
            facingDirection = Direction.RIGHT;
        }

        else if (Keyboard.isKeyUp(MOVE_LEFT_KEY) && Keyboard.isKeyUp(MOVE_RIGHT_KEY)) {
            playerState = PlayerState.STANDING;
        }

        if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }

        else if (Keyboard.isKeyDown(CROUCH_KEY)) {
            playerState = PlayerState.CROUCHING;
        }
    }

    protected void playerDashing()
    {
        currentAnimationName = dashDirection == Direction.RIGHT ? "DASH_RIGHT" : "DASH_LEFT";

        // cancel gravity so the dash travels in a straight horizontal line
        
        momentumY = 0;
        jumpForce = 0;

        moveAmountX = dashDirection == Direction.RIGHT ? dashSpeed : -dashSpeed;

        dashTrailX.add(getX());
        dashTrailY.add(getY());
        if (dashTrailX.size() > dashTrailLength) {
            dashTrailX.remove(0);
            dashTrailY.remove(0);
        }

        dashTimer--;

        if (dashTimer <= 0) {

            dashTrailX.clear();
            dashTrailY.clear();
            dashCooldownTimer = dashCooldown;

            playerState = airGroundState == AirGroundState.GROUND
                ? PlayerState.STANDING
                : PlayerState.JUMPING;
        }
    }

    protected void startAttack() {
        keyLocker.lockKey(ATTACK_KEY);
        attackTimer = 12;
        playerState = PlayerState.ATTACKING;
    }

    protected void playerAttacking() {
        moveAmountX = 0;
        attackTimer--;

        if (attackTimer <= 0) {
            playerState = Keyboard.isKeyDown(MOVE_LEFT_KEY) || Keyboard.isKeyDown(MOVE_RIGHT_KEY)
                    ? PlayerState.WALKING
                    : PlayerState.STANDING;
        }
    }

    protected boolean canAttack() {
        return animations.containsKey("ATTACK_RIGHT") && animations.containsKey("ATTACK_LEFT");
    }

    public int getDashCooldownTimer()
    {
        return dashCooldownTimer;
    }

    public int getDashCooldown()
    {
        return dashCooldown;
    }

    // player CROUCHING state logic
    protected void playerCrouching() {
        if (Keyboard.isKeyUp(CROUCH_KEY)) {
            playerState = PlayerState.STANDING;
        }

        if (Keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }
    }

    // player JUMPING state logic
    protected void playerJumping() {
        if (previousAirGroundState == AirGroundState.GROUND && airGroundState == AirGroundState.GROUND) {

            currentAnimationName = facingDirection == Direction.RIGHT ? "JUMP_RIGHT" : "JUMP_LEFT";

            airGroundState = AirGroundState.AIR;
            jumpForce = jumpHeight;

            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;

                if (jumpForce < 0) {
                    jumpForce = 0;
                }
            }
        }

        else if (airGroundState == AirGroundState.AIR) {

            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;

                if (jumpForce < 0) {
                    jumpForce = 0;
                }
            }

            if (Keyboard.isKeyDown(MOVE_LEFT_KEY)) {
                moveAmountX -= walkSpeed;
                facingDirection = Direction.LEFT;
                currentAnimationName = "JUMP_LEFT";
            }

            else if (Keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
                moveAmountX += walkSpeed;
                facingDirection = Direction.RIGHT;
                currentAnimationName = "JUMP_RIGHT";
            }

            if (moveAmountY > 0) {
                increaseMomentum();
            }
        }

        else if (previousAirGroundState == AirGroundState.AIR && airGroundState == AirGroundState.GROUND) {
            playerState = PlayerState.STANDING;
        }
    }

    public void bounce(float force) {
        jumpForce = force;
        momentumY = 0;
        airGroundState = AirGroundState.AIR;
        playerState = PlayerState.JUMPING;
    }

    protected void increaseMomentum() {
        momentumY += momentumYIncrease;

        if (momentumY > terminalVelocityY) {
            momentumY = terminalVelocityY;
        }
    }

    protected void updateLockedKeys() {
        if (Keyboard.isKeyUp(JUMP_KEY)) {
            keyLocker.unlockKey(JUMP_KEY);
        }

        if (Keyboard.isKeyUp(DASH_KEY)) {
            keyLocker.unlockKey(DASH_KEY);
        }

        if (Keyboard.isKeyUp(ATTACK_KEY)) {
            keyLocker.unlockKey(ATTACK_KEY);
        }
    }

    // anything extra the player should do based on interactions can be handled here
    protected void handlePlayerAnimation() {
        if (playerState == PlayerState.STANDING) {

            this.currentAnimationName =
                facingDirection == Direction.RIGHT
                ? "STAND_RIGHT"
                : "STAND_LEFT";

            int centerX =
                Math.round(getBounds().getX1()) +
                Math.round(getBounds().getWidth() / 2f);

            int centerY =
                Math.round(getBounds().getY1()) +
                Math.round(getBounds().getHeight() / 2f);

            MapTile currentMapTile =
                map.getTileByPosition(centerX, centerY);

            if (currentMapTile != null &&
                currentMapTile.getTileType() == TileType.WATER) {

                this.currentAnimationName =
                    facingDirection == Direction.RIGHT
                    ? "SWIM_STAND_RIGHT"
                    : "SWIM_STAND_LEFT";
            }
        }

        else if (playerState == PlayerState.WALKING) {

            this.currentAnimationName =
                facingDirection == Direction.RIGHT
                ? "WALK_RIGHT"
                : "WALK_LEFT";
        }

        else if (playerState == PlayerState.CROUCHING) {

            this.currentAnimationName =
                facingDirection == Direction.RIGHT
                ? "CROUCH_RIGHT"
                : "CROUCH_LEFT";
        }

        else if (playerState == PlayerState.JUMPING) {

            if (lastAmountMovedY <= 0) {
                this.currentAnimationName =
                    facingDirection == Direction.RIGHT
                    ? "JUMP_RIGHT"
                    : "JUMP_LEFT";
            }

            else {
                this.currentAnimationName =
                    facingDirection == Direction.RIGHT
                    ? "FALL_RIGHT"
                    : "FALL_LEFT";
            }
        }

        else if (playerState == PlayerState.ATTACKING) {

            this.currentAnimationName =
                facingDirection == Direction.RIGHT
                ? "ATTACK_RIGHT"
                : "ATTACK_LEFT";
        }
        else if (playerState == PlayerState.ATTACKING) {
            this.currentAnimationName = facingDirection == Direction.RIGHT ? "ATTACK_RIGHT" : "ATTACK_LEFT";
        }
        else if (playerState == PlayerState.DASHING) {
            this.currentAnimationName = dashDirection == Direction.RIGHT ? "DASH_RIGHT" : "DASH_LEFT";
        }
    }

    @Override
    public void onEndCollisionCheckX(
        boolean hasCollided,
        Direction direction,
        MapEntity entityCollidedWith) {
        if (hasCollided) {
            velocityX = 0;
        }
    }

    @Override
    public void onEndCollisionCheckY(
        boolean hasCollided,
        Direction direction,
        MapEntity entityCollidedWith) {

        if (direction == Direction.DOWN) {

            if (hasCollided) {
                if (airGroundState == AirGroundState.AIR) {
                dashCooldownTimer = 0; // just landed, dash is ready again
                }
                momentumY = 0;
                airGroundState = AirGroundState.GROUND;
            } else {
            if (playerState != PlayerState.DASHING) {
                playerState = PlayerState.JUMPING;
            }

           

               
        airGroundState = AirGroundState.AIR;
        }
    }

        else if (direction == Direction.UP) {

            if (hasCollided) {
                jumpForce = 0;
            }
        }
    }

    public void hurtPlayer(MapEntity mapEntity) {
        if (mapEntity == null || isInvincible || levelState != LevelState.RUNNING) {
            return;
        }

        // Enemies and falling objects are lethal on contact.
        if (mapEntity instanceof Enemy || mapEntity instanceof FallingObject) {
            levelState = LevelState.PLAYER_DEAD;
        }
    }

    // other entities can call this to tell the player they beat a level
    public void completeLevel() {
        levelState = LevelState.LEVEL_COMPLETED;
    }

    // if player has beaten level, this will be the update cycle
    public void updateLevelCompleted() {

        if (airGroundState != AirGroundState.GROUND &&
            map.getCamera().containsDraw(this)) {

            currentAnimationName = "FALL_RIGHT";

            applyGravity();
            increaseMomentum();

            super.update();
            moveYHandleCollision(moveAmountY);
        }

        else if (map.getCamera().containsDraw(this)) {

            currentAnimationName = "WALK_RIGHT";

            super.update();
            float moved = moveXHandleCollision(walkSpeed);

            // if something is blocking the walk-off (like the cave wall), end the level right away
            if (moved == 0) {
                for (PlayerListener listener : listeners) {
                    listener.onLevelCompleted();
                }
            }
        }

        else {

            for (PlayerListener listener : listeners) {
                listener.onLevelCompleted();
            }
        }
    }

    // if player has died, this will be the update cycle
    public void updatePlayerDead() {

        // change player animation to DEATH
        if (!currentAnimationName.startsWith("DEATH")) {

            if (facingDirection == Direction.RIGHT) {
                currentAnimationName = "DEATH_RIGHT";
            }

            else {
                currentAnimationName = "DEATH_LEFT";
            }

            super.update();
        }

        // continue death animation
        else if (currentFrameIndex != getCurrentAnimation().length - 1) {
            super.update();
        }

        // player falls off screen after death animation
        // after the death animation, pause for a moment then show the death screen
        else if (currentFrameIndex == getCurrentAnimation().length - 1) {
            deathTimer++;
            if (deathTimer >= 45) {   // about 3/4 of a second
                for (PlayerListener listener : listeners) {
                    listener.onDeath();
                }
            }
        }
    }

    public PlayerState getPlayerState()
    {
        return playerState;
    }

    public void setPlayerState(PlayerState playerState)
    {
        this.playerState = playerState;
    }

    public AirGroundState getAirGroundState()
    {
        return airGroundState;
    }

    public Direction getFacingDirection()
    {
        return facingDirection;
    }

    public void setFacingDirection(Direction facingDirection)
    {
        this.facingDirection = facingDirection;
    }

    public void setLevelState(LevelState levelState)
    {
        this.levelState = levelState;
    }

    public void addListener(PlayerListener listener)
    {
        listeners.add(listener);
    }

    public int getCoins()
    {
        return coins;
    }

    public void addCoins(int amount)
    {
        coins += amount;
    }

    public boolean spendCoins(int amount)
    {
        if (coins >= amount)
        {
            coins -= amount;
            return true;
        }

        return false;
    }

        @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (invincibilityTimer > 0 && (invincibilityTimer / 4) % 2 == 0) {
            return;
        }

        if (playerState == PlayerState.DASHING) {
            float realX = getX();
            float realY = getY();
            for (int i = 0; i < dashTrailX.size(); i++) {
                setX(dashTrailX.get(i));
                setY(dashTrailY.get(i));
                super.draw(graphicsHandler);
            }
            setX(realX);
            setY(realY);
        }

        super.draw(graphicsHandler);
    }
    // Uncomment this to have game draw player's bounds to make it easier to visualize
    /*
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
        drawBounds(graphicsHandler, new Color(255, 0, 0, 100));
    }
    */
}
