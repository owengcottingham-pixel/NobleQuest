package Level;

import Engine.GraphicsHandler;
import Engine.Key;
import Engine.KeyBinds;
import Engine.KeyLocker;
import Engine.Keyboard;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Utils.AirGroundState;
import Utils.Direction;
import EnhancedMapTiles.FallingObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

public abstract class Player extends GameObject {

    
    protected int maxHealth = 5;
    protected int health = maxHealth;
    protected int invincibilityTimer = 0;
    protected int invincibilityDuration = 90;   // 1.5s at 60fps
    protected int deathTimer = 0;
    
    // values that affect player movement

    // these should be set in a subclass

    protected int coins = 100;
    protected float walkSpeed = 0;
    protected float gravity = 0;
    protected float jumpHeight = 0;
    protected float jumpDegrade = 0;
    protected float terminalVelocityY = 0;
    protected float momentumYIncrease = 0;

    // values used to handle player movement
    protected float jumpForce = 0;
    protected boolean jumpHeld = false;       // true while the player is still holding jump from the ground
    protected float jumpCutMultiplier = 0.4f; // how much upward force is kept when jump is let go early
    protected float momentumY = 0;
    protected float moveAmountX, moveAmountY;
    protected float lastAmountMovedX, lastAmountMovedY;
    protected float lastFrameMovedX;
    protected float dashSpeed = 9f;   // walkSpeed is usually ~2.3f, so this is a big jump
    protected int dashDuration = 14;   // frames the dash lasts (~1/6 sec at 60fps)
    protected int dashCooldown = 30;
    protected float dashDirX = 0;
    protected float dashDirY = 0;
    protected boolean airDashUsed = false;
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
    protected Key JUMP_KEY = Key.SPACE;
    protected Key MOVE_LEFT_KEY = Key.A;
    protected Key MOVE_RIGHT_KEY = Key.D;
    protected Key CROUCH_KEY = Key.S;

    protected Key DASH_KEY = Key.SHIFT;

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

    // hurts the player when they touch a damaging tile
    private void checkHazardTiles() {
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
                if (tile != null && tile.intersects(this)) {
                    if (tile.getTileType() == TileType.SPIKES) {
                        damage(3);
                        return;
                    }
                    if (tile.getTileType() == TileType.LAVA) {
                        if (!isHurtInvincible()) {
                            damage(1);
                            bounce(18f);
                        }
                        return;
                    }
                }
            }
        }
    }

    public boolean isAttacking() {
    return attackTimer > 0;
    }

    // which way the current dash is going (dy > 0 = a down-dash)
    public float getDashDirY() {
        return dashDirY;
    }

    public float getLastAmountMovedY() {
    return lastAmountMovedY;
}

    // The movement keys below are no longer fixed -- the Controls menu can
    // change any of them mid-level -- so they are refreshed from KeyBinds at the
    // top of every frame. A key that has just been rebound is locked straight
    // away, otherwise still holding it from the menu would fire an instant jump
    // or swing the moment the game unpauses.
    // the actions whose keys get locked on use and re-armed on release
    private static final KeyBinds.Action[] REARMED = {
        KeyBinds.Action.JUMP, KeyBinds.Action.DASH, KeyBinds.Action.ATTACK
    };

    private static final KeyBinds.Action[] WATCHED = {
        KeyBinds.Action.MOVE_LEFT, KeyBinds.Action.MOVE_RIGHT, KeyBinds.Action.JUMP,
        KeyBinds.Action.CROUCH, KeyBinds.Action.DASH, KeyBinds.Action.ATTACK
    };

    private final EnumMap<KeyBinds.Action, Key[]> lastBinds =
            new EnumMap<KeyBinds.Action, Key[]>(KeyBinds.Action.class);

    private void syncKeyBinds() {
        // The *_KEY fields are kept pointing at each action's main key so any
        // code outside this class still works, but the logic below no longer
        // reads them -- an action can have a second key and a single field
        // cannot hold both. Use actionDown / actionPressed instead.
        MOVE_LEFT_KEY = KeyBinds.get(KeyBinds.Action.MOVE_LEFT);
        MOVE_RIGHT_KEY = KeyBinds.get(KeyBinds.Action.MOVE_RIGHT);
        JUMP_KEY = KeyBinds.get(KeyBinds.Action.JUMP);
        CROUCH_KEY = KeyBinds.get(KeyBinds.Action.CROUCH);
        DASH_KEY = KeyBinds.get(KeyBinds.Action.DASH);
        ATTACK_KEY = KeyBinds.get(KeyBinds.Action.ATTACK);

        // A key that has only just been bound is locked at once, otherwise
        // still holding it from the Controls menu fires an instant jump or
        // swing the moment the game unpauses.
        for (KeyBinds.Action action : WATCHED) {
            Key[] now = KeyBinds.keysFor(action);
            Key[] before = lastBinds.get(action);
            if (!Arrays.equals(before, now)) {
                if (before != null) {
                    for (Key key : now) {
                        keyLocker.lockKey(key);
                    }
                }
                lastBinds.put(action, now);
            }
        }
    }

    /** Any key bound to this action is held. */
    protected boolean actionDown(KeyBinds.Action action) {
        return KeyBinds.isDown(action);
    }

    /** No key bound to this action is held. */
    protected boolean actionUp(KeyBinds.Action action) {
        return KeyBinds.isUp(action);
    }

    /*
     * A key for this action is held AND has not been consumed yet. Checked per
     * key rather than per action so that holding one of an action's keys does
     * not block the other, and so one press is one action.
     */
    protected boolean actionPressed(KeyBinds.Action action) {
        for (Key key : KeyBinds.keysFor(action)) {
            if (Keyboard.isKeyDown(key) && !keyLocker.isKeyLocked(key)) {
                return true;
            }
        }
        return false;
    }

    protected void lockAction(KeyBinds.Action action) {
        for (Key key : KeyBinds.keysFor(action)) {
            keyLocker.lockKey(key);
        }
    }

    public void update() {
        syncKeyBinds();
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

            // attacking now works on top of any movement (walking, jumping, falling)
            if (attackTimer > 0) {
                attackTimer--;
            }
            if (canAttack() && actionPressed(KeyBinds.Action.ATTACK) && attackTimer == 0 && playerState != PlayerState.DASHING) {
                startAttack();
            }

            if (actionPressed(KeyBinds.Action.DASH) && playerState != PlayerState.DASHING && dashCooldownTimer == 0 && !(airGroundState == AirGroundState.AIR && airDashUsed)) {
                lockAction(KeyBinds.Action.DASH);

                attackTimer = 0; // dashing cancels an attack
                jumpHeld = false;

                float dx = 0, dy = 0;
                if (actionDown(KeyBinds.Action.MOVE_LEFT)) dx -= 1;
                if (actionDown(KeyBinds.Action.MOVE_RIGHT)) dx += 1;
                if (actionDown(KeyBinds.Action.JUMP)) dy -= 1;
                if (actionDown(KeyBinds.Action.CROUCH)) dy += 1;

                if (airGroundState == AirGroundState.GROUND && dy > 0) dy = 0;

                if (dx == 0 && dy == 0) dx = (facingDirection == Direction.RIGHT) ? 1 : -1;

                if (dx != 0 && dy != 0) {
                    dx *= 0.707f;
                    dy *= 0.707f;
                }

                dashDirX = dx;
                dashDirY = dy;

                if (dx > 0) facingDirection = Direction.RIGHT;
                else if (dx < 0) facingDirection = Direction.LEFT;
                dashDirection = facingDirection;

                if (dy < 0) lockAction(KeyBinds.Action.JUMP);

                if (airGroundState == AirGroundState.AIR) airDashUsed = true;

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

            float xBeforeMovement = getX();
            lastAmountMovedX = super.moveXHandleCollision(velocityX);
            lastFrameMovedX = getX() - xBeforeMovement;
            
            lastAmountMovedY = super.moveYHandleCollision(moveAmountY);

            checkHazardTiles();

            if (map != null && getBounds().getY1() > map.getEndBoundY()) {
                levelState = LevelState.PLAYER_DEAD;
            }

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
        if (canAttack() && actionPressed(KeyBinds.Action.ATTACK)) {
            startAttack();
        }

        else if (actionDown(KeyBinds.Action.MOVE_LEFT) || actionDown(KeyBinds.Action.MOVE_RIGHT)) {
            playerState = PlayerState.WALKING;
        }

        else if (actionPressed(KeyBinds.Action.JUMP)) {
            lockAction(KeyBinds.Action.JUMP);
            playerState = PlayerState.JUMPING;
        }

        else if (actionDown(KeyBinds.Action.CROUCH)) {
            playerState = PlayerState.CROUCHING;
        }
    }

    // player WALKING state logic
    protected void playerWalking() {
        if (canAttack() && actionPressed(KeyBinds.Action.ATTACK)) {
            startAttack();
            return;
        }

        if (actionDown(KeyBinds.Action.MOVE_LEFT)) {
            moveAmountX -= walkSpeed;
            facingDirection = Direction.LEFT;
        }

        else if (actionDown(KeyBinds.Action.MOVE_RIGHT)) {
            moveAmountX += walkSpeed;
            facingDirection = Direction.RIGHT;
        }

        else if (actionUp(KeyBinds.Action.MOVE_LEFT) && actionUp(KeyBinds.Action.MOVE_RIGHT)) {
            playerState = PlayerState.STANDING;
        }

        if (actionPressed(KeyBinds.Action.JUMP)) {
            lockAction(KeyBinds.Action.JUMP);
            playerState = PlayerState.JUMPING;
        }

        else if (actionDown(KeyBinds.Action.CROUCH)) {
            playerState = PlayerState.CROUCHING;
        }
    }

    protected void playerDashing()
    {
        currentAnimationName = dashDirection == Direction.RIGHT ? "DASH_RIGHT" : "DASH_LEFT";

        // no gravity during the dash
        momentumY = 0;
        jumpForce = 0;

        // starts fast, slows down toward the end (this is what makes it feel punchy)
        float progress = dashTimer / (float) dashDuration;   // goes from 1 down to 0
        float speed = dashSpeed * (0.4f + 0.9f * progress);

        moveAmountX = dashDirX * speed;
        moveAmountY = dashDirY * speed;

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

            // after an up-dash, keep rising a little instead of stopping dead
            if (dashDirY < 0) {
                jumpForce = 3f;
            }

            playerState = airGroundState == AirGroundState.GROUND
                ? PlayerState.STANDING
                : PlayerState.JUMPING;
        }
    }

    protected void startAttack() {
        lockAction(KeyBinds.Action.ATTACK);
        attackTimer = 12;
        
    }

    protected void playerAttacking() {
        moveAmountX = 0;
        attackTimer--;

        if (attackTimer <= 0) {
            playerState = actionDown(KeyBinds.Action.MOVE_LEFT) || actionDown(KeyBinds.Action.MOVE_RIGHT)
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
        if (actionUp(KeyBinds.Action.CROUCH)) {
            playerState = PlayerState.STANDING;
        }

        if (actionPressed(KeyBinds.Action.JUMP)) {
            lockAction(KeyBinds.Action.JUMP);
            playerState = PlayerState.JUMPING;
        }
    }

    // player JUMPING state logic
    protected void playerJumping() {
        if (previousAirGroundState == AirGroundState.GROUND && airGroundState == AirGroundState.GROUND) {

            currentAnimationName = facingDirection == Direction.RIGHT ? "JUMP_RIGHT" : "JUMP_LEFT";

            airGroundState = AirGroundState.AIR;
            jumpForce = jumpHeight;
            jumpHeld = true;
            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;

                if (jumpForce < 0) {
                    jumpForce = 0;
                }
            }
        }

        else if (airGroundState == AirGroundState.AIR) {

            if (jumpHeld && actionUp(KeyBinds.Action.JUMP)) {
                jumpHeld = false;
                if (jumpForce > 0) {
                    jumpForce = jumpForce * jumpCutMultiplier;
                }
            }

            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;

                if (jumpForce < 0) {
                    jumpForce = 0;
                }
            }

            if (actionDown(KeyBinds.Action.MOVE_LEFT)) {
                moveAmountX -= walkSpeed;
                facingDirection = Direction.LEFT;
                currentAnimationName = "JUMP_LEFT";
            }

            else if (actionDown(KeyBinds.Action.MOVE_RIGHT)) {
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
        jumpHeld = false;
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
        // Unlocked per key, not per action. An action can answer to two keys --
        // jump is Space and W -- and releasing one must not re-arm the other
        // while it is still held down, or holding W and tapping Space would
        // give a second jump in mid-air.
        for (KeyBinds.Action action : REARMED) {
            for (Key key : KeyBinds.keysFor(action)) {
                if (Keyboard.isKeyUp(key)) {
                    keyLocker.unlockKey(key);
                }
            }
        }
    }

    // anything extra the player should do based on interactions can be handled here
    protected void handlePlayerAnimation() {
        if (playerState == PlayerState.STANDING) {
            this.currentAnimationName = getStandingAnimationName();
        }

        else if (playerState == PlayerState.WALKING) {

            boolean hasMovementInput = actionDown(KeyBinds.Action.MOVE_LEFT) || actionDown(KeyBinds.Action.MOVE_RIGHT);
            this.currentAnimationName =
                hasMovementInput && lastFrameMovedX != 0
                ? (facingDirection == Direction.RIGHT ? "WALK_RIGHT" : "WALK_LEFT")
                : getStandingAnimationName();
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

        // show the attack animation over whatever the player is doing
        if (attackTimer > 0 && playerState != PlayerState.DASHING) {
            String side = facingDirection == Direction.RIGHT ? "RIGHT" : "LEFT";
            if (airGroundState == AirGroundState.AIR) {
                this.currentAnimationName = "AIR_ATTACK_" + side;
            } else {
                this.currentAnimationName = "ATTACK_" + side;
            }
        }
    }

    private String getStandingAnimationName() {
        String animationName = facingDirection == Direction.RIGHT ? "STAND_RIGHT" : "STAND_LEFT";
        int centerX = Math.round(getBounds().getX1()) + Math.round(getBounds().getWidth() / 2f);
        int centerY = Math.round(getBounds().getY1()) + Math.round(getBounds().getHeight() / 2f);
        MapTile currentMapTile = map.getTileByPosition(centerX, centerY);

        if (currentMapTile != null && currentMapTile.getTileType() == TileType.WATER) {
            return facingDirection == Direction.RIGHT ? "SWIM_STAND_RIGHT" : "SWIM_STAND_LEFT";
        }
        return animationName;
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
                airDashUsed = false;
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
        if (levelState != LevelState.RUNNING) {
            return;
        }

        levelState = LevelState.LEVEL_COMPLETED;
        currentAnimationName = facingDirection == Direction.RIGHT ? "STAND_RIGHT" : "STAND_LEFT";
        for (PlayerListener listener : listeners) {
            listener.onLevelCompleted();
        }
    }

    // if player has beaten level, this will be the update cycle
    public void updateLevelCompleted() {
        currentAnimationName = facingDirection == Direction.RIGHT ? "STAND_RIGHT" : "STAND_LEFT";
        super.update();
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

    // Swaps the player's sprite sheet at runtime. Does nothing unless a subclass
    // overrides it -- the shop calls this to put a costume on.
    public void setCostume(String imageFileName) {
    }

    // Cosmetics. A player with no cosmetics of its own just ignores these; the
    // Knight stores a hat and draws it on top of whatever frame is showing.
    public void setHat(int hatIndex) {
    }

    public int getHat() {
        return -1;
    }

    // Drawn immediately after the player's own sprite, every time it is drawn.
    // Being a hook rather than a tack-on at the end of draw() means the hat
    // follows the dash trail copies and vanishes with the invincibility blink
    // instead of hovering over a player who is not there.
    protected void drawCosmetics(GraphicsHandler graphicsHandler) {
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
                drawCosmetics(graphicsHandler);
            }
            setX(realX);
            setY(realY);
        }

        super.draw(graphicsHandler);
        drawCosmetics(graphicsHandler);
    }
    // Uncomment this to have game draw player's bounds to make it easier to visualize
    /*
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
        drawBounds(graphicsHandler, new Color(255, 0, 0, 100));
    }
    */
}
