package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.ScreenManager;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.Rectangle;
import Level.Enemy;
import Level.MapEntityStatus;
import Level.MapTile;
import Level.Player;
import Level.TileType;
import Utils.AirGroundState;
import Utils.Point;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;

public class LavaBoss extends Enemy {
    private static final int SPRITE_SIZE = 512;
    private static final int MAX_HEALTH = 10;
    private static final int HIT_COOLDOWN = 24;
    private static final int START_INVULNERABILITY = 150;
    private static final int HEAD_RETURN_INVULNERABILITY = 120;
    private static final float SPRITE_SCALE = 0.5f;
    private static final float MOVE_SPEED = 2.2f;
    private static final float HOVER_HEIGHT = 420f;
    private static final int SLAM_INTERVAL = 75;
    private static final int WINDUP_DURATION = 90;
    private static final int RECOVERY_DURATION = 24;
    private static final int ENTRANCE_DURATION = 120;
    private static final float ENTRANCE_START_OFFSET = 480f;
    private static final float HAND_SLAM_SPEED = 12f;
    private static final float HAND_RAISE_SPEED = 9f;
    private static final float HAND_SLIDE_SPEED = 8f;
    private static final float HEAD_DROP_SPEED = 7f;
    private static final float HEAD_SWEEP_SPEED = 8f;
    private static final float HEAD_RETURN_SPEED = 7f;
    private static final int MOUTH_X = 82;
    private static final int MOUTH_Y = 164;
    private static final int MOUTH_WIDTH = 94;
    private static final int MOUTH_HEIGHT = 42;
    private static final float TILE_BOUNCE_HEIGHT = 48f;
    private static final int TILE_BOUNCE_DURATION = 36;
    private static final int TILE_WAVE_RADIUS = 8;
    private static final int TILE_WAVE_DELAY_PER_TILE = 4;
    private static final int HAND_WIDTH = 125;
    private static final int HAND_HEIGHT = 150;
    private static final int OUTLINE_SIZE = 5;
    private static final BufferedImage HAND_SHEET = ImageLoader.load("Lava Boss hands.png");
    private static final BufferedImage LEFT_HAND =
        HAND_SHEET.getSubimage(1110, 170, 160, 175);
    private static final BufferedImage RIGHT_HAND =
        HAND_SHEET.getSubimage(1260, 170, 160, 175);
    private static final BufferedImage FIST_HAND =
        HAND_SHEET.getSubimage(1100, 650, 160, 190);

    private enum AttackState {
        WAITING, WINDUP, SLAMMING, SLIDING, RETURNING, HEAD_DROPPING,
        HEAD_SWEEPING, HEAD_RETURNING, RECOVERING
    }

    private final IdentityHashMap<BufferedImage, BufferedImage> outlinedImages = new IdentityHashMap<>();
    private final ArrayList<TileBounce> tileBounces = new ArrayList<>();
    private int health = MAX_HEALTH;
    private int hitCooldown;
    private int bossInvulnerabilityTimer = START_INVULNERABILITY;
    private int entranceTimer = ENTRANCE_DURATION;
    private final float entranceTargetY;
    private int attackTimer = SLAM_INTERVAL;
    private AttackState attackState = AttackState.WAITING;
    private int attackStateTimer;
    private float leftHandY;
    private float rightHandY;
    private boolean leftHandHitPlayer;
    private boolean rightHandHitPlayer;
    private boolean leftHandAttacksNext = true;
    private boolean leftHandIsAttacking;
    private float slidingHandX;
    private boolean slideHitPlayer;
    private boolean headAttackNext = true;
    private boolean headSweepRightNext = true;
    private boolean headHitPlayer;
    private float headHomeX;
    private float headHomeY;
    private float headSweepStartX;
    private float headSweepTargetX;
    private float headRollAngle;

    public LavaBoss(Point location) {
        super(location.x, location.y, createAnimations(), "IDLE");
        entranceTargetY = location.y;
        setY(entranceTargetY + ENTRANCE_START_OFFSET);
        setIsUpdateOffScreen(true);
    }

    private static HashMap<String, Frame[]> createAnimations() {
        BufferedImage sheet = ImageLoader.load("Lava Boss.png");
        Frame closedJawFrame = createHeadFrame(sheet, 0, 0);
        Frame openJawFrame = createHeadFrame(sheet, 0, 1);

        HashMap<String, Frame[]> animations = new HashMap<>();
        animations.put("IDLE", new Frame[] {closedJawFrame});
        animations.put("HEAD_SWEEP", new Frame[] {
            closedJawFrame.copy(),
            openJawFrame
        });
        return animations;
    }

    private static Frame createHeadFrame(BufferedImage sheet, int row, int column) {
        BufferedImage image = sheet.getSubimage(
            column * SPRITE_SIZE,
            row * SPRITE_SIZE,
            SPRITE_SIZE,
            SPRITE_SIZE
        );
        return new FrameBuilder(image, 8)
            .withScale(SPRITE_SCALE)
            .withBounds(100, 110, 312, 330)
            .build();
    }

    @Override
    public void update(Player player) {
        if (mapEntityStatus == MapEntityStatus.REMOVED) {
            return;
        }

        updateTileBounces(player);
        if (health <= 0) {
            if (tileBounces.isEmpty()) {
                mapEntityStatus = MapEntityStatus.REMOVED;
            }
            return;
        }

        if (hitCooldown > 0) {
            hitCooldown--;
        }
        if (entranceTimer > 0) {
            riseIntoArena();
            super.update(player);
            return;
        }
        if (bossInvulnerabilityTimer > 0) {
            bossInvulnerabilityTimer--;
        }
        if (attackState == AttackState.WAITING) {
            hoverAbovePlayer(player);
        }
        updateHandAttack(player);
        setCurrentAnimationName(
            attackState == AttackState.HEAD_DROPPING || attackState == AttackState.HEAD_SWEEPING
                ? "HEAD_SWEEP"
                : "IDLE"
        );
        if (player.isAttacking() && intersects(player)) {
            takeHit(player);
        }
        if (health > 0 && mapEntityStatus != MapEntityStatus.REMOVED) {
            super.update(player);
        }
    }

    @Override
    public void touchedPlayer(Player player) {
    }

    private void riseIntoArena() {
        entranceTimer--;
        float progress = 1f - entranceTimer / (float) ENTRANCE_DURATION;
        float easedProgress = progress * progress * (3f - 2f * progress);
        setY(entranceTargetY + ENTRANCE_START_OFFSET * (1f - easedProgress));
    }

    private void hoverAbovePlayer(Player player) {
        if (map == null) {
            return;
        }

        float tileWidth = map.getTileset().getScaledSpriteWidth();
        float playerCenterX = player.getBounds().getX1() + player.getBounds().getWidth() / 2f;
        float targetX = playerCenterX - getWidth() / 2f;
        targetX = Math.max(tileWidth, Math.min(targetX, map.getWidthPixels() - getWidth() - tileWidth));
        moveTowards(targetX, getY(), MOVE_SPEED, MOVE_SPEED);

        float groundY = findGroundY(getX() + getWidth() / 2f);
        float targetY = Math.max(0, groundY - HOVER_HEIGHT);
        moveTowards(getX(), targetY, MOVE_SPEED, MOVE_SPEED);
    }

    private void updateHandAttack(Player player) {
        if (map == null) {
            return;
        }

        float handWidth = HAND_WIDTH * SPRITE_SCALE;
        float restY = getY() + 90;
        float leftX = getHandX(true, handWidth);
        float rightX = getHandX(false, handWidth);
        float handHeight = HAND_HEIGHT * SPRITE_SCALE;
        float leftGroundY = findGroundY(leftX + handWidth / 2f) - handHeight;
        float rightGroundY = findGroundY(rightX + handWidth / 2f) - HAND_HEIGHT * SPRITE_SCALE;

        if (attackState == AttackState.WAITING) {
            attackTimer--;
            leftHandY = restY;
            rightHandY = restY;
            if (attackTimer <= 0) {
                if (isEnraged() && headAttackNext) {
                    beginHeadAttack();
                    headAttackNext = false;
                } else {
                    attackState = AttackState.WINDUP;
                    attackStateTimer = isEnraged() ? WINDUP_DURATION / 2 : WINDUP_DURATION;
                    if (isEnraged()) {
                        headAttackNext = true;
                    }
                }
            }
            return;
        }

        if (attackState == AttackState.HEAD_DROPPING) {
            float nextX = moveTowardsValue(getX(), headSweepStartX, HEAD_DROP_SPEED);
            float groundY = findGroundY(nextX + getWidth() / 2f);
            float targetY = groundY - MOUTH_Y - MOUTH_HEIGHT;
            setLocation(nextX, moveTowardsValue(getY(), targetY, HEAD_DROP_SPEED));
            if (getX() == headSweepStartX && getY() == targetY) {
                attackState = AttackState.HEAD_SWEEPING;
            }
            return;
        }

        if (attackState == AttackState.HEAD_SWEEPING) {
            float targetY = findGroundY(getX() + getWidth() / 2f) - MOUTH_Y - MOUTH_HEIGHT;
            float nextX = moveTowardsValue(getX(), headSweepTargetX, HEAD_SWEEP_SPEED);
            float nextY = moveTowardsValue(getY(), targetY, HEAD_DROP_SPEED);
            headRollAngle += (nextX - getX()) / (getWidth() / 2f);
            setLocation(nextX, nextY);

            Rectangle mouthBounds = getRotatedMouthBounds();
            if (!headHitPlayer && mouthBounds.intersects(player.getBounds())) {
                player.damage(1);
                headHitPlayer = true;
            }
            if (getX() == headSweepTargetX) {
                attackState = AttackState.HEAD_RETURNING;
            }
            return;
        }

        if (attackState == AttackState.HEAD_RETURNING) {
            float nextX = moveTowardsValue(getX(), headHomeX, HEAD_RETURN_SPEED);
            float nextY = moveTowardsValue(getY(), headHomeY, HEAD_RETURN_SPEED);
            setLocation(nextX, nextY);
            if (getX() == headHomeX && getY() == headHomeY) {
                headRollAngle = 0;
                attackState = AttackState.WAITING;
                attackTimer = RECOVERY_DURATION;
                bossInvulnerabilityTimer = HEAD_RETURN_INVULNERABILITY;
            }
            return;
        }

        if (attackState == AttackState.WINDUP) {
            leftHandY = restY;
            rightHandY = restY;
            if (--attackStateTimer <= 0) {
                attackState = AttackState.SLAMMING;
                leftHandIsAttacking = leftHandAttacksNext;
                leftHandAttacksNext = !leftHandAttacksNext;
                if (leftHandIsAttacking) {
                    leftHandHitPlayer = false;
                } else {
                    rightHandHitPlayer = false;
                }
            }
            return;
        }

        if (attackState == AttackState.SLAMMING) {
            if (leftHandIsAttacking) {
                leftHandY = Math.min(leftGroundY, leftHandY + HAND_SLAM_SPEED);
                rightHandY = restY;
                if (leftHandY >= leftGroundY && !leftHandHitPlayer) {
                    markHandImpact(true);
                    if (!isEnraged()) {
                        startTileBounce(leftX + handWidth / 2f);
                    }
                }
            } else {
                rightHandY = Math.min(rightGroundY, rightHandY + HAND_SLAM_SPEED);
                leftHandY = restY;
                if (rightHandY >= rightGroundY && !rightHandHitPlayer) {
                    markHandImpact(false);
                    if (!isEnraged()) {
                        startTileBounce(rightX + handWidth / 2f);
                    }
                }
            }
            boolean attackingHandHitGround = leftHandIsAttacking
                ? leftHandY >= leftGroundY
                : rightHandY >= rightGroundY;
            if (attackingHandHitGround) {
                if (isEnraged()) {
                    attackState = AttackState.SLIDING;
                    slidingHandX = leftHandIsAttacking ? leftX : rightX;
                    slideHitPlayer = false;
                } else {
                    attackState = AttackState.RECOVERING;
                    attackStateTimer = RECOVERY_DURATION;
                }
            }
            return;
        }

        if (attackState == AttackState.SLIDING) {
            float tileWidth = map.getTileset().getScaledSpriteWidth();
            float minX = tileWidth;
            float maxX = Math.max(minX, map.getWidthPixels() - tileWidth - handWidth);
            float targetX = leftHandIsAttacking ? maxX : minX;
            float distance = targetX - slidingHandX;
            slidingHandX += Math.signum(distance) * Math.min(Math.abs(distance), HAND_SLIDE_SPEED);
            float slideY = findGroundY(slidingHandX + handWidth / 2f) - handHeight;
            if (leftHandIsAttacking) {
                leftHandY = slideY;
                rightHandY = restY;
            } else {
                rightHandY = slideY;
                leftHandY = restY;
            }

            Rectangle slidingHandBounds = new Rectangle(
                slidingHandX,
                slideY,
                Math.round(handWidth),
                Math.round(handHeight)
            );
            if (!slideHitPlayer && slidingHandBounds.intersects(player.getBounds())) {
                player.damage(1);
                slideHitPlayer = true;
            }

            if (slidingHandX == targetX) {
                attackState = AttackState.RETURNING;
            }
            return;
        }

        if (attackState == AttackState.RETURNING) {
            float targetX = getHandX(leftHandIsAttacking, handWidth);
            float distance = targetX - slidingHandX;
            slidingHandX += Math.signum(distance) * Math.min(Math.abs(distance), HAND_SLIDE_SPEED);
            float targetY = Math.max(restY, leftHandIsAttacking ? leftHandY : rightHandY);
            float handY = targetY - HAND_RAISE_SPEED;
            if (leftHandIsAttacking) {
                leftHandY = Math.max(restY, handY);
                rightHandY = restY;
            } else {
                rightHandY = Math.max(restY, handY);
                leftHandY = restY;
            }
            if (slidingHandX == targetX
                && (leftHandIsAttacking ? leftHandY : rightHandY) <= restY) {
                leftHandY = restY;
                rightHandY = restY;
                attackState = AttackState.WAITING;
                attackTimer = RECOVERY_DURATION;
            }
            return;
        }

        if (leftHandIsAttacking) {
            leftHandY = Math.max(restY, leftHandY - HAND_RAISE_SPEED);
            rightHandY = restY;
        } else {
            rightHandY = Math.max(restY, rightHandY - HAND_RAISE_SPEED);
            leftHandY = restY;
        }
        boolean attackingHandReturned = leftHandIsAttacking
            ? leftHandY <= restY
            : rightHandY <= restY;
        if (--attackStateTimer <= 0 || attackingHandReturned) {
            leftHandY = restY;
            rightHandY = restY;
            attackState = AttackState.WAITING;
            attackTimer = isEnraged() ? SLAM_INTERVAL / 2 : SLAM_INTERVAL;
        }
    }

    private boolean isEnraged() {
        return health <= MAX_HEALTH / 2;
    }

    private void beginHeadAttack() {
        headHomeX = getX();
        headHomeY = getY();
        float tileWidth = map.getTileset().getScaledSpriteWidth();
        float minX = tileWidth;
        float maxX = Math.max(minX, map.getWidthPixels() - tileWidth - getWidth());
        if (headSweepRightNext) {
            headSweepStartX = minX;
            headSweepTargetX = maxX;
        } else {
            headSweepStartX = maxX;
            headSweepTargetX = minX;
        }
        headSweepRightNext = !headSweepRightNext;
        headHitPlayer = false;
        headRollAngle = 0;
        attackState = AttackState.HEAD_DROPPING;
    }

    private Rectangle getRotatedMouthBounds() {
        double cos = Math.abs(Math.cos(headRollAngle));
        double sin = Math.abs(Math.sin(headRollAngle));
        float centerOffsetX = MOUTH_X + MOUTH_WIDTH / 2f - getWidth() / 2f;
        float centerOffsetY = MOUTH_Y + MOUTH_HEIGHT / 2f - getHeight() / 2f;
        float rotatedCenterX = getX() + getWidth() / 2f
            + (float) (centerOffsetX * Math.cos(headRollAngle) - centerOffsetY * Math.sin(headRollAngle));
        float rotatedCenterY = getY() + getHeight() / 2f
            + (float) (centerOffsetX * Math.sin(headRollAngle) + centerOffsetY * Math.cos(headRollAngle));
        int boundsWidth = (int) Math.ceil(cos * MOUTH_WIDTH + sin * MOUTH_HEIGHT);
        int boundsHeight = (int) Math.ceil(sin * MOUTH_WIDTH + cos * MOUTH_HEIGHT);
        return new Rectangle(
            rotatedCenterX - boundsWidth / 2f,
            rotatedCenterY - boundsHeight / 2f,
            boundsWidth,
            boundsHeight
        );
    }

    private float moveTowardsValue(float current, float target, float speed) {
        float distance = target - current;
        return current + Math.signum(distance) * Math.min(Math.abs(distance), speed);
    }

    private void stopTileBounces() {
        for (TileBounce bounce : tileBounces) {
            bounce.tile.setY(bounce.startY);
        }
        tileBounces.clear();
    }

    private float getHandX(boolean leftHand, float handWidth) {
        float x = leftHand ? getX() - handWidth - 20 : getX() + getWidth() + 20;
        float tileWidth = map.getTileset().getScaledSpriteWidth();
        float minX = tileWidth;
        float maxX = Math.max(minX, map.getWidthPixels() - tileWidth - handWidth);
        return Math.max(minX, Math.min(x, maxX));
    }

    private void markHandImpact(boolean leftHand) {
        if (leftHand) {
            leftHandHitPlayer = true;
        } else {
            rightHandHitPlayer = true;
        }
    }

    private void startTileBounce(float impactX) {
        if (map == null) {
            return;
        }

        int tileWidth = map.getTileset().getScaledSpriteWidth();
        int impactColumn = Math.max(0, Math.min((int) (impactX / tileWidth), map.getWidth() - 1));

        for (int distance = 0; distance <= TILE_WAVE_RADIUS; distance++) {
            for (int direction : new int[] {-1, 1}) {
                if (distance == 0 && direction == 1) {
                    continue;
                }
                int column = impactColumn + direction * distance;
                if (column < 0 || column >= map.getWidth()) {
                    continue;
                }

                MapTile floorTile = null;
                for (int row = map.getHeight() - 1; row >= 0; row--) {
                    MapTile tile = map.getMapTile(column, row);
                    if (tile == null || (tile.getTileType() != TileType.NOT_PASSABLE
                        && tile.getTileType() != TileType.PILLAR)) {
                        continue;
                    }
                    floorTile = tile;
                    break;
                }
                if (floorTile != null && !isTileBouncing(floorTile)) {
                    tileBounces.add(new TileBounce(
                        floorTile,
                        distance * TILE_WAVE_DELAY_PER_TILE
                    ));
                }
            }
        }
    }

    private boolean isTileBouncing(MapTile tile) {
        for (TileBounce bounce : tileBounces) {
            if (bounce.tile == tile) {
                return true;
            }
        }
        return false;
    }

    private void updateTileBounces(Player player) {
        for (int i = tileBounces.size() - 1; i >= 0; i--) {
            TileBounce bounce = tileBounces.get(i);
            if (bounce.delay > 0) {
                bounce.delay--;
                continue;
            }
            bounce.elapsed++;
            float progress = Math.min(1f, bounce.elapsed / (float) TILE_BOUNCE_DURATION);
            float lift = (float) Math.sin(Math.PI * progress) * TILE_BOUNCE_HEIGHT;
            bounce.tile.setY(bounce.startY - lift);

            Rectangle tileBounds = bounce.tile.getBounds();
            Rectangle playerBounds = player.getBounds();
            boolean standingOnTile = player.getAirGroundState() == AirGroundState.GROUND
                && playerBounds.getX1() <= tileBounds.getX2()
                && playerBounds.getX2() >= tileBounds.getX1()
                && Math.abs(playerBounds.getY2() + 1 - bounce.startY) <= 8;
            if (!bounce.playerBounced && lift > 0 && standingOnTile) {
                player.bounce(23f);
                bounce.playerBounced = true;
            }

            if (progress >= 1f) {
                bounce.tile.setY(bounce.startY);
                tileBounces.remove(i);
            }
        }
    }

    private void moveTowards(float targetX, float targetY, float maxMoveX, float maxMoveY) {
        float dx = targetX - getX();
        float dy = targetY - getY();
        if (Math.abs(dx) > maxMoveX) {
            dx = Math.signum(dx) * maxMoveX;
        }
        if (Math.abs(dy) > maxMoveY) {
            dy = Math.signum(dy) * maxMoveY;
        }
        moveXHandleCollision(dx);
        moveYHandleCollision(dy);
    }

    private float findGroundY(float x) {
        float tileWidth = map.getTileset().getScaledSpriteWidth();
        int column = Math.max(0, Math.min((int) (x / tileWidth), map.getWidth() - 1));

        for (int row = map.getHeight() - 1; row >= 0; row--) {
            MapTile tile = map.getMapTile(column, row);
            if (tile != null && (tile.getTileType() == TileType.NOT_PASSABLE
                || tile.getTileType() == TileType.PILLAR)) {
                return tile.getBounds().getY1();
            }
        }
        return map.getHeightPixels();
    }

    private static final class TileBounce {
        private final MapTile tile;
        private final float startY;
        private int delay;
        private int elapsed;
        private boolean playerBounced;

        private TileBounce(MapTile tile, int delay) {
            this.tile = tile;
            this.startY = tile.getY();
            this.delay = delay;
        }
    }

    @Override
    public boolean hitByBolt(Player player) {
        if (mapEntityStatus == MapEntityStatus.REMOVED
            || health <= 0
            || hitCooldown > 0
            || isHeadAwayFromStart()
            || bossInvulnerabilityTimer > 0) {
            return false;
        }
        takeHit(player);
        return true;
    }

    private boolean isHeadAwayFromStart() {
        return attackState == AttackState.HEAD_DROPPING
            || attackState == AttackState.HEAD_SWEEPING
            || attackState == AttackState.HEAD_RETURNING;
    }

    private void takeHit(Player player) {
        if (health <= 0 || hitCooldown > 0 || isHeadAwayFromStart() || bossInvulnerabilityTimer > 0) {
            return;
        }

        health--;
        hitCooldown = HIT_COOLDOWN;
        if (health == MAX_HEALTH / 2) {
            stopTileBounces();
        }
        if (health <= 0) {
            health = 0;
            player.addCoins(25);
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (mapEntityStatus == MapEntityStatus.REMOVED || health <= 0) {
            return;
        }

        int bossX = Math.round(getCalibratedXLocation());
        int bossY = Math.round(getCalibratedYLocation());
        int handWidth = Math.round(HAND_WIDTH * SPRITE_SCALE);
        int handHeight = Math.round(HAND_HEIGHT * SPRITE_SCALE);
        float restY = getY() + 90;
        float leftX = getHandX(true, handWidth);
        float rightX = getHandX(false, handWidth);
        boolean movingLeftHand = attackState == AttackState.SLIDING
            || attackState == AttackState.RETURNING
            || (isEnraged() && attackState == AttackState.RECOVERING);
        if (movingLeftHand) {
            if (leftHandIsAttacking) {
                leftX = slidingHandX;
            } else {
                rightX = slidingHandX;
            }
        }
        if (attackState == AttackState.WAITING) {
            leftHandY = restY;
            rightHandY = restY;
        }
        if (attackState == AttackState.HEAD_SWEEPING) {
            drawRotatedOutlinedHead(graphicsHandler, bossX, bossY);
        } else {
            drawOutlinedImage(graphicsHandler, currentFrame.getImage(), bossX, bossY, getWidth(), getHeight());
        }
        boolean showingHands = attackState != AttackState.HEAD_DROPPING
            && attackState != AttackState.HEAD_SWEEPING
            && attackState != AttackState.HEAD_RETURNING;
        boolean showingFist = attackState == AttackState.SLAMMING
            || attackState == AttackState.SLIDING
            || attackState == AttackState.RETURNING
            || attackState == AttackState.RECOVERING;
        BufferedImage leftHandImage = showingFist && leftHandIsAttacking ? FIST_HAND : LEFT_HAND;
        BufferedImage rightHandImage = showingFist && !leftHandIsAttacking ? FIST_HAND : RIGHT_HAND;
        if (showingHands) {
            drawOutlinedImage(
                graphicsHandler,
                leftHandImage,
                Math.round(leftX - map.getCamera().getX()),
                Math.round(leftHandY - map.getCamera().getY()),
                handWidth,
                handHeight,
                false
            );
            drawOutlinedImage(
                graphicsHandler,
                rightHandImage,
                Math.round(rightX - map.getCamera().getX()),
                Math.round(rightHandY - map.getCamera().getY()),
                handWidth,
                handHeight,
                rightHandImage == FIST_HAND
            );
        }

        int barWidth = 360;
        int barHeight = 14;
        int barX = ScreenManager.getScreenWidth() / 2 - barWidth / 2;
        int barY = 30;
        int fillWidth = Math.round(barWidth * health / (float) MAX_HEALTH);
        graphicsHandler.drawFilledRectangle(
            barX - 3, barY - 3, barWidth + 6, barHeight + 6, new Color(20, 12, 8)
        );
        graphicsHandler.drawFilledRectangle(barX, barY, barWidth, barHeight, new Color(60, 30, 20));
        if (fillWidth > 0) {
            graphicsHandler.drawFilledRectangle(barX, barY, fillWidth, barHeight, new Color(232, 70, 30));
        }
        int halfHealthX = barX + barWidth / 2;
        graphicsHandler.drawFilledRectangle(
            halfHealthX - 2, barY - 3, 4, barHeight + 6, new Color(20, 12, 8)
        );
        graphicsHandler.drawFilledRectangle(
            halfHealthX - 1, barY - 2, 2, barHeight + 4, Color.WHITE
        );
        graphicsHandler.drawStringWithOutline(
            "LAVA BOSS",
            barX,
            barY - 8,
            new Font("Arial", Font.BOLD, 18),
            new Color(255, 214, 120),
            Color.black,
            3
        );
    }

    private void drawRotatedOutlinedHead(GraphicsHandler graphicsHandler, int x, int y) {
        BufferedImage image = currentFrame.getImage();
        BufferedImage outlinedImage = outlinedImages.computeIfAbsent(image, LavaBoss::createOutlinedImage);
        int outlineX = Math.round(OUTLINE_SIZE * getWidth() / (float) image.getWidth());
        int outlineY = Math.round(OUTLINE_SIZE * getHeight() / (float) image.getHeight());
        Graphics2D headGraphics = (Graphics2D) graphicsHandler.getGraphics().create();
        headGraphics.translate(x + getWidth() / 2f, y + getHeight() / 2f);
        headGraphics.rotate(headRollAngle);
        headGraphics.drawImage(
            outlinedImage,
            -getWidth() / 2 - outlineX,
            -getHeight() / 2 - outlineY,
            getWidth() + outlineX * 2,
            getHeight() + outlineY * 2,
            null
        );
        headGraphics.dispose();
    }

    private void drawOutlinedImage(
        GraphicsHandler graphicsHandler,
        BufferedImage image,
        int x,
        int y,
        int width,
        int height
    ) {
        drawOutlinedImage(graphicsHandler, image, x, y, width, height, false);
    }

    private void drawOutlinedImage(
        GraphicsHandler graphicsHandler,
        BufferedImage image,
        int x,
        int y,
        int width,
        int height,
        boolean flipHorizontally
    ) {
        BufferedImage outlinedImage = outlinedImages.computeIfAbsent(image, LavaBoss::createOutlinedImage);
        int outlineX = Math.round(OUTLINE_SIZE * width / (float) image.getWidth());
        int outlineY = Math.round(OUTLINE_SIZE * height / (float) image.getHeight());
        graphicsHandler.drawImage(
            outlinedImage,
            x - outlineX,
            y - outlineY,
            width + 2 * outlineX,
            height + 2 * outlineY,
            flipHorizontally ? ImageEffect.FLIP_HORIZONTAL : ImageEffect.NONE
        );
    }

    private static BufferedImage createOutlinedImage(BufferedImage image) {
        int padding = OUTLINE_SIZE;
        BufferedImage silhouette = new BufferedImage(
            image.getWidth(),
            image.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D silhouetteGraphics = silhouette.createGraphics();
        silhouetteGraphics.drawImage(image, 0, 0, null);
        silhouetteGraphics.setComposite(AlphaComposite.SrcIn);
        silhouetteGraphics.setColor(Color.WHITE);
        silhouetteGraphics.fillRect(0, 0, silhouette.getWidth(), silhouette.getHeight());
        silhouetteGraphics.dispose();

        BufferedImage outlinedImage = new BufferedImage(
            image.getWidth() + padding * 2,
            image.getHeight() + padding * 2,
            BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D outlineGraphics = outlinedImage.createGraphics();
        for (int offsetY = -padding; offsetY <= padding; offsetY++) {
            for (int offsetX = -padding; offsetX <= padding; offsetX++) {
                if (offsetX * offsetX + offsetY * offsetY <= padding * padding) {
                    outlineGraphics.drawImage(
                        silhouette,
                        padding + offsetX,
                        padding + offsetY,
                        null
                    );
                }
            }
        }
        outlineGraphics.drawImage(image, padding, padding, null);
        outlineGraphics.dispose();
        return outlinedImage;
    }
}
