package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.ScreenManager;
import EnhancedMapTiles.LavaEscapeWall;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.Rectangle;
import Level.Enemy;
import Level.MapEntityStatus;
import Level.MapTile;
import Level.Player;
import Level.TileType;
import Tilesets.LavaBossTileset;
import Utils.AirGroundState;
import Utils.Point;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class LavaBoss extends Enemy {
    private static final int SPRITE_SIZE = 512;
    private static final int STAGE_TWO_SPRITE_SIZE = 1000;
    private static final int STAGE_THREE_AIR_TIME = 420;
    private static final int STAGE_THREE_SLAM_COUNT = 6;
    private static final int STAGE_THREE_SLAM_DELAY = 12;
    private static final int MAX_HEALTH = 20;
    private static final int HIT_COOLDOWN = 24;
    private static final int START_INVULNERABILITY = 150;
    private static final int HEAD_RETURN_INVULNERABILITY = 120;
    private static final float SPRITE_SCALE = 0.5f;
    private static final float MOVE_SPEED = 2.2f;
    private static final float HOVER_HEIGHT = 420f;
    private static final int SLAM_INTERVAL = 120;
    private static final int STAGE_TWO_ATTACK_INTERVAL = 150;
    private static final int STAGE_THREE_ATTACK_INTERVAL = 135;
    private static final int FINAL_STAGE_SHAKE_DURATION = 90;
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
    private static final int TRANSFORMATION_DURATION = 75;
    private static final float ROCK_HAND_SPEED = 9f;
    private static final int ROCK_WALL_GRAB_PAUSE = 18;
    private static final int LAVA_TURN_DURATION = 30;
    private static final int LAVA_POUR_DURATION = 90;
    private static final int LAVA_POUR_FRAME_DURATION = 10;
    private static final float ROCK_GRAVITY = 0.35f;
    private static final int ROCK_RADIUS = 20;
    private static final int LAVA_STREAM_WIDTH = 46;
    private static final BufferedImage HAND_SHEET = ImageLoader.load("Lava Boss hands.png");
    private static final BufferedImage LEFT_HAND =
        HAND_SHEET.getSubimage(1110, 170, 160, 175);
    private static final BufferedImage RIGHT_HAND =
        HAND_SHEET.getSubimage(1260, 170, 160, 175);
    private static final BufferedImage FIST_HAND =
        HAND_SHEET.getSubimage(1100, 650, 160, 190);
    private static final BufferedImage STAGE_THREE_SHEET = ImageLoader.load("Lava Boss stage 3.png");
    private static final BufferedImage STAGE_THREE_CROWNED_HEAD =
        cropStageThreeSprite(STAGE_THREE_SHEET, 0, 0);
    private static final BufferedImage STAGE_THREE_BARE_HEAD =
        cropStageThreeSprite(STAGE_THREE_SHEET, 0, 1);
    private static final BufferedImage STAGE_THREE_CROWN_HAND =
        cropStageThreeSprite(STAGE_THREE_SHEET, 1, 0);
    private static final BufferedImage STAGE_THREE_CROWN =
        cropStageThreeSprite(STAGE_THREE_SHEET, 1, 1);

    private enum AttackState {
        WAITING, WINDUP, SLAMMING, SLIDING, RETURNING, HEAD_DROPPING,
        HEAD_SWEEPING, HEAD_RETURNING, RECOVERING, ROCK_GOING_TO_WALL,
        ROCK_GRABBING, ROCK_RETURNING,
        LAVA_TURNING, LAVA_POURING, CROWN_GRABBING, CROWN_HOLDING,
        CROWN_AIRBORNE, CROWN_FALLING, CROWN_RETRIEVING, CROWN_REPLACING
    }

    private enum BossStage {
        STAGE_ONE, STAGE_TWO, STAGE_THREE, STAGE_FOUR
    }

    private enum StageOneAttack {
        GROUND_POUND, SLIDING_FIST, ROLLING_HEAD
    }

    private final IdentityHashMap<BufferedImage, BufferedImage> outlinedImages = new IdentityHashMap<>();
    private final ArrayList<TileBounce> tileBounces = new ArrayList<>();
    private final ArrayList<PourLavaTile> pourLavaTiles = new ArrayList<>();
    private int health = MAX_HEALTH;
    private int hitCooldown;
    private int bossInvulnerabilityTimer = START_INVULNERABILITY;
    private int entranceTimer = ENTRANCE_DURATION;
    private final float entranceTargetY;
    private int attackTimer = SLAM_INTERVAL;
    private AttackState attackState = AttackState.WAITING;
    private int attackStateTimer;
    private BossStage bossStage = BossStage.STAGE_ONE;
    private StageOneAttack stageOneAttack = StageOneAttack.GROUND_POUND;
    private int transformationTimer;
    private boolean stageTwoNextRockAttack = true;
    private boolean stageThreeNextRockAttack = true;
    private boolean stageTwoFacingRight;
    private boolean rockGrabFacingRight;
    private float rockX;
    private float rockY;
    private float rockHandX;
    private float rockHandY;
    private float rockHandHomeX;
    private float rockHandHomeY;
    private float rockWallX;
    private float rockWallY;
    private float rockVelocityX;
    private float rockVelocityY;
    private boolean rockActive;
    private boolean rockHeld;
    private float lavaTargetX;
    private boolean lavaHitPlayer;
    private float leftHandY;
    private float rightHandY;
    private boolean leftHandHitPlayer;
    private boolean rightHandHitPlayer;
    private boolean leftHandAttacksNext = true;
    private boolean leftHandIsAttacking;
    private float slidingHandX;
    private boolean slideHitPlayer;
    private boolean headSweepRightNext = true;
    private boolean headHitPlayer;
    private float headHomeX;
    private float headHomeY;
    private float headSweepStartX;
    private float headSweepTargetX;
    private float headRollAngle;
    private boolean crownRemoved;
    private boolean crownHeld;
    private boolean crownHitPlayer;
    private float crownX;
    private float crownY;
    private float crownHomeX;
    private float crownHomeY;
    private float crownTargetX;
    private float crownHandX;
    private float crownHandY;
    private float crownHandHomeX;
    private float crownHandHomeY;
    private float crownVelocityY;
    private float crownRollAngle;
    private int crownAirTimer;
    private int crownStateTimer;
    private int stageThreeSlamCount;
    private int stageThreeSlamDelay;
    private boolean stageThreeSlamActive;
    private boolean stageThreeSlamReturning;
    private boolean stageThreeSlamLeft;
    private final float arenaRightBound;
    private boolean finalStageSequenceStarted;
    private int finalStageShakeTimer;
    private float finalStageStartX;
    private float finalStageStartY;
    private float finalStageFallSpeed;

    public LavaBoss(Point location, float arenaRightBound) {
        super(location.x, location.y, createAnimations(), "IDLE");
        entranceTargetY = location.y;
        this.arenaRightBound = arenaRightBound;
        setY(entranceTargetY + ENTRANCE_START_OFFSET);
        setIsUpdateOffScreen(true);
    }

    private static HashMap<String, Frame[]> createAnimations() {
        BufferedImage sheet = ImageLoader.load("Lava Boss.png");
        Frame closedJawFrame = createHeadFrame(sheet, 0, 0);
        Frame openJawFrame = createHeadFrame(sheet, 0, 1);
        BufferedImage stageTwoSheet = ImageLoader.load("Lava Boss stage 2 .png");

        HashMap<String, Frame[]> animations = new HashMap<>();
        animations.put("IDLE", new Frame[] {closedJawFrame});
        animations.put("HEAD_SWEEP", new Frame[] {
            closedJawFrame.copy(),
            openJawFrame
        });
        animations.put("STAGE2_IDLE", new Frame[] {
            createStageTwoFrame(stageTwoSheet, 0, 0)
        });
        animations.put("STAGE2_SIDE", new Frame[] {
            createStageTwoFrame(stageTwoSheet, 0, 1)
        });
        animations.put("STAGE2_POUR_1", new Frame[] {createStageTwoFrame(stageTwoSheet, 0, 1)});
        animations.put("STAGE2_POUR_2", new Frame[] {createStageTwoFrame(stageTwoSheet, 1, 0)});
        animations.put("STAGE2_POUR_3", new Frame[] {createStageTwoFrame(stageTwoSheet, 1, 1)});
        animations.put("STAGE3_IDLE", new Frame[] {
            createStageThreeHeadFrame(STAGE_THREE_CROWNED_HEAD)
        });
        animations.put("STAGE3_BARE", new Frame[] {
            createStageThreeHeadFrame(STAGE_THREE_BARE_HEAD)
        });
        return animations;
    }

    private static BufferedImage cropStageThreeSprite(BufferedImage sheet, int row, int column) {
        BufferedImage source = sheet.getSubimage(
            column * STAGE_TWO_SPRITE_SIZE,
            row * STAGE_TWO_SPRITE_SIZE,
            STAGE_TWO_SPRITE_SIZE,
            STAGE_TWO_SPRITE_SIZE
        );
        int minX = source.getWidth();
        int minY = source.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                if ((source.getRGB(x, y) >>> 24) == 0) {
                    continue;
                }
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        if (maxX < minX || maxY < minY) {
            throw new IllegalStateException("Stage-three boss panel contains no visible artwork");
        }
        return source.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private static Frame createStageThreeHeadFrame(BufferedImage cropped) {
        int outputWidth = 268;
        int outputHeight = Math.round(outputWidth * cropped.getHeight() / (float) cropped.getWidth());
        BufferedImage image = new BufferedImage(SPRITE_SIZE, SPRITE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageGraphics = image.createGraphics();
        imageGraphics.setRenderingHint(
            java.awt.RenderingHints.KEY_INTERPOLATION,
            java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );
        imageGraphics.drawImage(cropped, 122, 140, outputWidth, outputHeight, null);
        imageGraphics.dispose();
        return new FrameBuilder(image, 10)
            .withScale(SPRITE_SCALE)
            .withBounds(100, 110, 312, 330)
            .build();
    }

    private static Frame createStageTwoFrame(BufferedImage sheet, int row, int column) {
        BufferedImage source = sheet.getSubimage(
            column * STAGE_TWO_SPRITE_SIZE,
            row * STAGE_TWO_SPRITE_SIZE,
            STAGE_TWO_SPRITE_SIZE,
            STAGE_TWO_SPRITE_SIZE
        );
        int minX = source.getWidth();
        int minY = source.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                if ((source.getRGB(x, y) >>> 24) == 0) {
                    continue;
                }
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        if (maxX < minX || maxY < minY) {
            throw new IllegalStateException("Stage-two boss frame contains no visible artwork");
        }

        BufferedImage cropped = source.getSubimage(
            minX,
            minY,
            maxX - minX + 1,
            maxY - minY + 1
        );
        int outputWidth = 268;
        int outputHeight = Math.round(outputWidth * cropped.getHeight() / (float) cropped.getWidth());
        BufferedImage image = new BufferedImage(SPRITE_SIZE, SPRITE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageGraphics = image.createGraphics();
        imageGraphics.setRenderingHint(
            java.awt.RenderingHints.KEY_INTERPOLATION,
            java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );
        imageGraphics.drawImage(cropped, 122, 140, outputWidth, outputHeight, null);
        imageGraphics.dispose();

        return new FrameBuilder(image, 10)
            .withScale(SPRITE_SCALE)
            .withBounds(100, 110, 312, 330)
            .build();
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
            rockActive = false;
            if (tileBounces.isEmpty()) {
                mapEntityStatus = MapEntityStatus.REMOVED;
            }
            return;
        }
        updateRockProjectile(player);

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
        updateBossStage();
        if (bossStage == BossStage.STAGE_FOUR) {
            updateFinalStageSequence();
        } else if (bossStage == BossStage.STAGE_ONE) {
            if (attackState == AttackState.WAITING) {
                hoverAbovePlayer(player);
            }
            updateHandAttack(player);
            setCurrentAnimationName(
                attackState == AttackState.HEAD_DROPPING || attackState == AttackState.HEAD_SWEEPING
                    ? "HEAD_SWEEP"
                    : "IDLE"
            );
        } else if (bossStage == BossStage.STAGE_THREE) {
            if (transformationTimer > 0) {
                transformationTimer--;
            } else {
                if (attackState == AttackState.WAITING) {
                    hoverAbovePlayer(player);
                }
                stageTwoFacingRight = player.getBounds().getX1() + player.getBounds().getWidth() / 2f
                    > getX() + getWidth() / 2f;
                updateStageTwoAttacks(player);
            }
            setCurrentAnimationName(crownRemoved ? "STAGE3_BARE" : "STAGE3_IDLE");
        } else if (transformationTimer > 0) {
            transformationTimer--;
            setCurrentAnimationName("STAGE2_IDLE");
        } else {
            if (attackState == AttackState.WAITING) {
                hoverAbovePlayer(player);
            }
            stageTwoFacingRight = player.getBounds().getX1() + player.getBounds().getWidth() / 2f
                > getX() + getWidth() / 2f;
            updateStageTwoAttacks(player);
            if (attackState == AttackState.LAVA_TURNING) {
                setCurrentAnimationName("STAGE2_SIDE");
            } else if (attackState == AttackState.LAVA_POURING) {
                updateLavaPourAnimation();
            } else {
                setCurrentAnimationName("STAGE2_IDLE");
            }
        }
        if (bossStage != BossStage.STAGE_FOUR && player.isAttacking() && intersects(player)) {
            takeHit(player);
        }
        if (health > 0 && mapEntityStatus != MapEntityStatus.REMOVED) {
            super.update(player);
        }
    }

    private void updateBossStage() {
        BossStage nextStage = getStageForHealth(health);
        if (nextStage == bossStage) {
            return;
        }

        bossStage = nextStage;
        attackState = AttackState.WAITING;
        headRollAngle = 0;
        rockActive = false;
        rockHeld = false;
        crownRemoved = false;
        crownHeld = false;
        stageThreeSlamActive = false;
        stopTileBounces();
        restorePourLavaTiles();
        if (bossStage == BossStage.STAGE_TWO) {
            transformationTimer = TRANSFORMATION_DURATION;
            attackTimer = TRANSFORMATION_DURATION;
            bossInvulnerabilityTimer = TRANSFORMATION_DURATION;
        } else if (bossStage == BossStage.STAGE_THREE) {
            transformationTimer = TRANSFORMATION_DURATION;
            attackTimer = TRANSFORMATION_DURATION;
            bossInvulnerabilityTimer = TRANSFORMATION_DURATION;
        } else {
            finalStageSequenceStarted = true;
            finalStageShakeTimer = FINAL_STAGE_SHAKE_DURATION;
            finalStageStartX = getX();
            finalStageStartY = getY();
        }
    }

    public boolean isFinalStageSequenceStarted() {
        return finalStageSequenceStarted;
    }

    private void updateFinalStageSequence() {
        if (finalStageShakeTimer > 0) {
            finalStageShakeTimer--;
            setLocation(
                finalStageStartX + ThreadLocalRandom.current().nextInt(-6, 7),
                finalStageStartY + ThreadLocalRandom.current().nextInt(-4, 5)
            );
            return;
        }

        finalStageFallSpeed = Math.min(20f, finalStageFallSpeed + 0.8f);
        setLocation(finalStageStartX, getY() + finalStageFallSpeed);
        if (getY() > map.getHeightPixels() + getHeight()) {
            float wallX = map.getCamera().getX() + map.getTileset().getScaledSpriteWidth();
            map.addEnhancedMapTile(new LavaEscapeWall(new Point(wallX, 0), map.getHeight()));
            mapEntityStatus = MapEntityStatus.REMOVED;
        }
    }

    private BossStage getStageForHealth(int currentHealth) {
        if (currentHealth > MAX_HEALTH * 3 / 4) {
            return BossStage.STAGE_ONE;
        }
        if (currentHealth > MAX_HEALTH / 2) {
            return BossStage.STAGE_TWO;
        }
        if (currentHealth > MAX_HEALTH / 4) {
            return BossStage.STAGE_THREE;
        }
        return BossStage.STAGE_FOUR;
    }

    private int getStageTwoAttackInterval() {
        switch (bossStage) {
            case STAGE_TWO:
                return STAGE_TWO_ATTACK_INTERVAL;
            case STAGE_THREE:
                return STAGE_THREE_ATTACK_INTERVAL;
            default:
                return SLAM_INTERVAL;
        }
    }

    private void updateStageTwoAttacks(Player player) {
        if (map == null) {
            return;
        }

        if (attackState == AttackState.WAITING) {
            if (attackTimer > 0) {
                attackTimer--;
            }
            if (attackTimer <= 0) {
                if (bossStage == BossStage.STAGE_THREE) {
                    if (stageThreeNextRockAttack) {
                        beginRockAttack();
                    } else {
                        beginCrownAttack(player);
                    }
                    stageThreeNextRockAttack = !stageThreeNextRockAttack;
                } else if (stageTwoNextRockAttack) {
                    beginRockAttack();
                } else {
                    attackState = AttackState.LAVA_TURNING;
                    attackStateTimer = LAVA_TURN_DURATION;
                    lavaTargetX = clampArenaX(
                        player.getBounds().getX1() + player.getBounds().getWidth() / 2f,
                        LAVA_STREAM_WIDTH / 2f
                    );
                    lavaHitPlayer = false;
                }
                if (bossStage != BossStage.STAGE_THREE) {
                    stageTwoNextRockAttack = !stageTwoNextRockAttack;
                }
            }
            return;
        }

        if (attackState == AttackState.CROWN_GRABBING
            || attackState == AttackState.CROWN_HOLDING
            || attackState == AttackState.CROWN_AIRBORNE
            || attackState == AttackState.CROWN_FALLING
            || attackState == AttackState.CROWN_RETRIEVING
            || attackState == AttackState.CROWN_REPLACING) {
            updateCrownAttack(player);
            return;
        }

        if (attackState == AttackState.ROCK_GOING_TO_WALL) {
            rockHandY = moveTowardsValue(rockHandY, rockWallY, ROCK_HAND_SPEED);
            rockHandX = moveTowardsValue(rockHandX, rockWallX, ROCK_HAND_SPEED);
            if (rockHandX == rockWallX && rockHandY == rockWallY) {
                rockHeld = true;
                rockX = rockHandX;
                rockY = rockHandY + HAND_HEIGHT * SPRITE_SCALE * 0.2f;
                attackState = AttackState.ROCK_GRABBING;
                attackStateTimer = ROCK_WALL_GRAB_PAUSE;
            }

            return;
        }

        if (attackState == AttackState.ROCK_GRABBING) {
            if (--attackStateTimer <= 0) {
                attackState = AttackState.ROCK_RETURNING;
            }
            return;
        }

        if (attackState == AttackState.ROCK_RETURNING) {
            rockHandX = moveTowardsValue(rockHandX, rockHandHomeX, ROCK_HAND_SPEED);
            rockHandY = moveTowardsValue(rockHandY, rockHandHomeY, ROCK_HAND_SPEED);
            rockX = rockHandX;
            rockY = rockHandY + HAND_HEIGHT * SPRITE_SCALE * 0.2f;
            if (rockHandX == rockHandHomeX && rockHandY == rockHandHomeY) {
                float targetX = clampArenaX(
                    player.getBounds().getX1() + player.getBounds().getWidth() / 2f,
                    ROCK_RADIUS
                );
                float targetY = player.getBounds().getY1() + player.getBounds().getHeight() / 2f;
                float flightFrames = Math.max(24f, Math.min(65f, Math.abs(targetX - rockX) / 8f));
                rockVelocityX = (targetX - rockX) / flightFrames;
                rockVelocityY = (targetY - rockY - 0.5f * ROCK_GRAVITY * flightFrames * flightFrames)
                    / flightFrames;
                rockHeld = false;
                rockActive = true;
                attackState = AttackState.WAITING;
                attackTimer = getStageTwoAttackInterval();
            }
            return;
        }

        if (attackState == AttackState.LAVA_TURNING) {
            float targetX = lavaTargetX - getWidth() / 2f;
            float targetY = getLavaPourTargetY();
            moveTowards(targetX, targetY, MOVE_SPEED * 2f, 8f);
            if (--attackStateTimer <= 0) {
                turnPourTargetIntoLava();
                attackState = AttackState.LAVA_POURING;
                attackStateTimer = LAVA_POUR_DURATION;
            }
            return;
        }

        if (attackState == AttackState.LAVA_POURING) {
            moveTowards(lavaTargetX - getWidth() / 2f, getLavaPourTargetY(), MOVE_SPEED * 2f, 8f);
            damagePlayerInLavaStream(player);
            if (--attackStateTimer <= 0) {
                attackState = AttackState.WAITING;
                attackTimer = getStageTwoAttackInterval();
            }
        }
    }

    private void beginRockAttack() {
        attackState = AttackState.ROCK_GOING_TO_WALL;
        rockGrabFacingRight = stageTwoFacingRight;
        rockHandHomeX = clampArenaX(
            getX() + getWidth() / 2f
                + (rockGrabFacingRight ? getWidth() * 0.62f : -getWidth() * 0.62f),
            Math.max(ROCK_RADIUS, HAND_WIDTH * SPRITE_SCALE / 2f)
        );
        rockHandHomeY = getY() + getHeight() * 0.72f;
        rockHandX = rockHandHomeX;
        rockHandY = rockHandHomeY;
        float tileWidth = map.getTileset().getScaledSpriteWidth();
        rockWallX = rockHandHomeX < getArenaRightBound() / 2f
            ? tileWidth + ROCK_RADIUS
            : getArenaRightBound() - tileWidth - ROCK_RADIUS;
        rockWallY = findGroundY(rockWallX)
            - ROCK_RADIUS
            - HAND_HEIGHT * SPRITE_SCALE * 0.2f;
        rockActive = false;
        rockHeld = false;
    }

    private void beginCrownAttack(Player player) {
        float handWidth = HAND_WIDTH * SPRITE_SCALE;
        crownHomeX = getX() + getWidth() / 2f;
        crownHomeY = getY() + getHeight() * 0.12f;
        crownX = crownHomeX;
        crownY = crownHomeY;
        crownTargetX = clampArenaX(
            player.getBounds().getX1() + player.getBounds().getWidth() / 2f,
            getCrownDisplayWidth() / 2f
        );
        crownHandHomeX = getHandX(true, handWidth);
        crownHandHomeY = getY() + 90;
        crownHandX = crownHandHomeX;
        crownHandY = crownHandHomeY;
        crownHeld = false;
        crownHitPlayer = false;
        crownRollAngle = 0;
        crownAirTimer = STAGE_THREE_AIR_TIME;
        crownStateTimer = 0;
        stageThreeSlamCount = 0;
        stageThreeSlamDelay = 20;
        stageThreeSlamActive = false;
        stageThreeSlamReturning = false;
        attackState = AttackState.CROWN_GRABBING;
    }

    private void updateCrownAttack(Player player) {
        float handWidth = HAND_WIDTH * SPRITE_SCALE;
        float handHeight = HAND_HEIGHT * SPRITE_SCALE;
        float restY = getY() + 90;
        float leftGroundY = findGroundY(getHandX(true, handWidth) + handWidth / 2f) - handHeight;
        float rightGroundY = findGroundY(getHandX(false, handWidth) + handWidth / 2f) - handHeight;
        if (attackState != AttackState.CROWN_AIRBORNE && attackState != AttackState.CROWN_FALLING) {
            leftHandY = restY;
            rightHandY = restY;
        }

        if (attackState == AttackState.CROWN_GRABBING) {
            float targetX = crownHomeX - handWidth * 0.45f;
            float targetY = crownHomeY - handHeight * 0.2f;
            crownHandX = moveTowardsValue(crownHandX, targetX, ROCK_HAND_SPEED);
            crownHandY = moveTowardsValue(crownHandY, targetY, ROCK_HAND_SPEED);
            if (crownHandX == targetX && crownHandY == targetY) {
                crownHeld = true;
                crownRemoved = true;
                attackState = AttackState.CROWN_HOLDING;
                crownStateTimer = 24;
            }
            return;
        }
        if (attackState == AttackState.CROWN_HOLDING) {
            if (--crownStateTimer <= 0) {
                crownHeld = false;
                crownY = crownHomeY - 30;
                crownVelocityY = -8f;
                crownAirTimer = STAGE_THREE_AIR_TIME;
                attackState = AttackState.CROWN_AIRBORNE;
            }
            return;
        }
        if (attackState == AttackState.CROWN_AIRBORNE) {
            int elapsed = STAGE_THREE_AIR_TIME - crownAirTimer;
            crownAirTimer--;
            float progress = elapsed / (float) STAGE_THREE_AIR_TIME;
            crownX = crownHomeX + (crownTargetX - crownHomeX) * progress;
            if (elapsed < 30) {
                crownY = crownHomeY - 30f - elapsed * 5f;
            } else {
                crownY = crownHomeY - 180f + (float) Math.sin(elapsed * 0.09f) * 12f;
            }
            crownRollAngle += 0.28f;
            updateStageThreeSlams(player, restY, leftGroundY, rightGroundY, handWidth, handHeight);
            if (crownAirTimer <= 0) {
                stageThreeSlamActive = false;
                leftHandY = restY;
                rightHandY = restY;
                attackState = AttackState.CROWN_FALLING;
                crownVelocityY = 0;
            }
            return;
        }
        if (attackState == AttackState.CROWN_FALLING) {
            crownVelocityY += 0.65f;
            crownY += crownVelocityY;
            crownRollAngle += 0.28f;
            float crownHeight = getCrownDisplayHeight();
            float groundY = findGroundY(crownTargetX);
            Rectangle crownBounds = new Rectangle(
                crownX - getCrownDisplayWidth() / 2f,
                crownY - crownHeight / 2f,
                Math.round(getCrownDisplayWidth()),
                Math.round(crownHeight)
            );
            if (!crownHitPlayer && crownBounds.intersects(player.getBounds())) {
                player.damage(1);
                crownHitPlayer = true;
            }
            if (crownY + crownHeight / 2f >= groundY) {
                crownY = groundY - crownHeight / 2f;
                crownRollAngle = 0;
                turnCrownLandingIntoLava();
                crownHandX = crownHandHomeX;
                crownHandY = crownHandHomeY;
                attackState = AttackState.CROWN_RETRIEVING;
            }
            return;
        }
        if (attackState == AttackState.CROWN_RETRIEVING) {
            float targetX = crownX - handWidth * 0.45f;
            float targetY = crownY - handHeight * 0.2f;
            crownHandX = moveTowardsValue(crownHandX, targetX, ROCK_HAND_SPEED);
            crownHandY = moveTowardsValue(crownHandY, targetY, ROCK_HAND_SPEED);
            if (crownHandX == targetX && crownHandY == targetY) {
                crownHeld = true;
                attackState = AttackState.CROWN_REPLACING;
            }
            return;
        }
        if (attackState == AttackState.CROWN_REPLACING) {
            float targetX = crownHomeX - handWidth * 0.45f;
            float targetY = crownHomeY - handHeight * 0.2f;
            crownHandX = moveTowardsValue(crownHandX, targetX, ROCK_HAND_SPEED);
            crownHandY = moveTowardsValue(crownHandY, targetY, ROCK_HAND_SPEED);
            if (crownHandX == targetX && crownHandY == targetY) {
                crownHeld = false;
                crownRemoved = false;
                attackState = AttackState.WAITING;
                attackTimer = getStageTwoAttackInterval();
            }
        }
    }

    private void updateStageThreeSlams(
        Player player,
        float restY,
        float leftGroundY,
        float rightGroundY,
        float handWidth,
        float handHeight
    ) {
        if (stageThreeSlamCount >= STAGE_THREE_SLAM_COUNT) {
            leftHandY = restY;
            rightHandY = restY;
            return;
        }
        if (!stageThreeSlamActive) {
            if (stageThreeSlamDelay > 0) {
                stageThreeSlamDelay--;
                return;
            }
            stageThreeSlamActive = true;
            stageThreeSlamReturning = false;
            stageThreeSlamLeft = stageThreeSlamCount % 2 == 0;
            if (stageThreeSlamLeft) {
                leftHandHitPlayer = false;
            } else {
                rightHandHitPlayer = false;
            }
        }

        float x = getHandX(stageThreeSlamLeft, handWidth);
        float groundY = stageThreeSlamLeft ? leftGroundY : rightGroundY;
        float handY = stageThreeSlamLeft ? leftHandY : rightHandY;
        if (!stageThreeSlamReturning) {
            handY = Math.min(groundY, handY + 20f);
            if (handY >= groundY) {
                stageThreeSlamReturning = true;
                if (stageThreeSlamLeft) {
                    markHandImpact(true);
                } else {
                    markHandImpact(false);
                }
                Rectangle handBounds = new Rectangle(x, handY, Math.round(handWidth), Math.round(handHeight));
                if (handBounds.intersects(player.getBounds())) {
                    player.damage(1);
                }
                startTileBounce(x + handWidth / 2f);
            }
        } else {
            handY = Math.max(restY, handY - 20f);
            if (handY <= restY) {
                stageThreeSlamActive = false;
                stageThreeSlamCount++;
                stageThreeSlamDelay = STAGE_THREE_SLAM_DELAY;
                stageThreeSlamReturning = false;
            }
        }
        if (stageThreeSlamLeft) {
            leftHandY = handY;
            rightHandY = restY;
        } else {
            rightHandY = handY;
            leftHandY = restY;
        }
    }

    private float getCrownDisplayWidth() {
        return 84f;
    }

    private float getCrownDisplayHeight() {
        return getCrownDisplayWidth() * STAGE_THREE_CROWN.getHeight() / STAGE_THREE_CROWN.getWidth();
    }

    private void turnPourTargetIntoLava() {
        restorePourLavaTiles();

        int tileWidth = map.getTileset().getScaledSpriteWidth();
        int tileHeight = map.getTileset().getScaledSpriteHeight();
        if (map.getWidth() == 0 || map.getHeight() == 0) {
            return;
        }

        int maxArenaColumn = getLastArenaTileColumn(tileWidth);
        int targetColumn = Math.max(0, Math.min((int) (lavaTargetX / tileWidth), maxArenaColumn));
        int row = Math.max(0, Math.min((int) (findGroundY(lavaTargetX) / tileHeight), map.getHeight() - 1));
        for (int column = Math.max(0, targetColumn - 1);
             column <= Math.min(targetColumn + 1, maxArenaColumn);
             column++) {
            if (column >= map.getWidth()) {
                continue;
            }

            MapTile originalTile = map.getMapTile(column, row);
            if (originalTile == null) {
                continue;
            }
            pourLavaTiles.add(new PourLavaTile(column, row, originalTile));

            MapTile lavaTile = map.getTileset().getTile(LavaBossTileset.LAVA_TILE_INDEX)
                .build(column * tileWidth, row * tileHeight);
            lavaTile.setMap(map);
            map.setMapTile(column, row, lavaTile);
        }
    }

    private void restorePourLavaTiles() {
        if (map == null) {
            return;
        }
        for (PourLavaTile pourLavaTile : pourLavaTiles) {
            map.setMapTile(pourLavaTile.column, pourLavaTile.row, pourLavaTile.originalTile);
        }
        pourLavaTiles.clear();
    }

    private void turnCrownLandingIntoLava() {
        restorePourLavaTiles();

        int tileWidth = map.getTileset().getScaledSpriteWidth();
        int tileHeight = map.getTileset().getScaledSpriteHeight();
        int maxArenaColumn = getLastArenaTileColumn(tileWidth);
        int row = Math.max(0, Math.min((int) (findGroundY(crownTargetX) / tileHeight), map.getHeight() - 1));
        int targetColumn = Math.max(0, Math.min((int) (crownTargetX / tileWidth), maxArenaColumn));
        for (int column = Math.max(0, targetColumn - 3);
             column <= Math.min(targetColumn + 3, maxArenaColumn);
             column++) {
            if (column >= map.getWidth()) {
                continue;
            }

            MapTile originalTile = map.getMapTile(column, row);
            if (originalTile == null) {
                continue;
            }

            pourLavaTiles.add(new PourLavaTile(column, row, originalTile));
            MapTile lavaTile = map.getTileset().getTile(LavaBossTileset.LAVA_TILE_INDEX)
                .build(column * tileWidth, row * tileHeight);
            lavaTile.setMap(map);
            map.setMapTile(column, row, lavaTile);
        }
    }

    private void updateLavaPourAnimation() {
        if (attackStateTimer == LAVA_POUR_DURATION) {
            return;
        }
        int elapsed = LAVA_POUR_DURATION - attackStateTimer - 1;
        if (elapsed < LAVA_POUR_FRAME_DURATION) {
            setCurrentAnimationName("STAGE2_POUR_1");
        } else if (elapsed < LAVA_POUR_FRAME_DURATION * 2) {
            setCurrentAnimationName("STAGE2_POUR_2");
        } else {
            setCurrentAnimationName("STAGE2_POUR_3");
        }
    }

    private float getLavaPourTargetY() {
        float groundY = findGroundY(lavaTargetX);
        float boundsBottomOffset = getBounds().getY1() - getY() + getBounds().getHeight();
        return groundY - boundsBottomOffset;
    }

    private void updateRockProjectile(Player player) {
        if (!rockActive || map == null) {
            return;
        }

        rockVelocityY += ROCK_GRAVITY;
        rockX += rockVelocityX;
        rockY += rockVelocityY;
        Rectangle rockBounds = new Rectangle(
            rockX - ROCK_RADIUS,
            rockY - ROCK_RADIUS,
            ROCK_RADIUS * 2,
            ROCK_RADIUS * 2
        );
        if (rockX - ROCK_RADIUS < 0 || rockX + ROCK_RADIUS > getArenaRightBound()) {
            rockActive = false;
            return;
        }
        if (rockBounds.intersects(player.getBounds())) {
            player.damage(1);
            rockActive = false;
            return;
        }
        if (rockY + ROCK_RADIUS >= findGroundY(rockX)) {
            rockActive = false;
        }
    }

    private void damagePlayerInLavaStream(Player player) {
        float groundY = findGroundY(lavaTargetX);
        float streamTop = getBounds().getY1() + getBounds().getHeight() * 0.55f;
        Rectangle streamBounds = new Rectangle(
            lavaTargetX - LAVA_STREAM_WIDTH / 2f,
            streamTop,
            LAVA_STREAM_WIDTH,
            Math.max(0, Math.round(groundY - streamTop))
        );
        if (!lavaHitPlayer && streamBounds.intersects(player.getBounds())) {
            player.damage(1);
            lavaHitPlayer = true;
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
        targetX = Math.max(
            tileWidth,
            Math.min(targetX, getArenaRightBound() - getWidth() - tileWidth)
        );
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
                int attackChoice = ThreadLocalRandom.current().nextInt(StageOneAttack.values().length);
                stageOneAttack = StageOneAttack.values()[attackChoice];
                if (stageOneAttack == StageOneAttack.ROLLING_HEAD) {
                    beginHeadAttack();
                } else {
                    attackState = AttackState.WINDUP;
                    attackStateTimer = WINDUP_DURATION;
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
                attackTimer = SLAM_INTERVAL;
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
                    if (stageOneAttack == StageOneAttack.GROUND_POUND) {
                        startTileBounce(leftX + handWidth / 2f);
                    }
                }
            } else {
                rightHandY = Math.min(rightGroundY, rightHandY + HAND_SLAM_SPEED);
                leftHandY = restY;
                if (rightHandY >= rightGroundY && !rightHandHitPlayer) {
                    markHandImpact(false);
                    if (stageOneAttack == StageOneAttack.GROUND_POUND) {
                        startTileBounce(rightX + handWidth / 2f);
                    }
                }
            }
            boolean attackingHandHitGround = leftHandIsAttacking
                ? leftHandY >= leftGroundY
                : rightHandY >= rightGroundY;
            if (attackingHandHitGround) {
                if (stageOneAttack == StageOneAttack.SLIDING_FIST) {
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
            float maxX = Math.max(minX, getArenaRightBound() - tileWidth - handWidth);
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
            attackTimer = SLAM_INTERVAL;
        }
    }

    private void beginHeadAttack() {
        headHomeX = getX();
        headHomeY = getY();
        float tileWidth = map.getTileset().getScaledSpriteWidth();
        float minX = tileWidth;
        float maxX = Math.max(minX, getArenaRightBound() - tileWidth - getWidth());
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
        float maxX = Math.max(minX, getArenaRightBound() - tileWidth - handWidth);
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
        int maxArenaColumn = getLastArenaTileColumn(tileWidth);
        int impactColumn = Math.max(0, Math.min((int) (impactX / tileWidth), maxArenaColumn));

        for (int distance = 0; distance <= TILE_WAVE_RADIUS; distance++) {
            for (int direction : new int[] {-1, 1}) {
                if (distance == 0 && direction == 1) {
                    continue;
                }
                int column = impactColumn + direction * distance;
                if (column < 0 || column > maxArenaColumn || column >= map.getWidth()) {
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

    @Override
    public boolean canLandOnJumpThroughPlatforms() {
        return false;
    }

    private float findGroundY(float x) {
        float tileWidth = map.getTileset().getScaledSpriteWidth();
        int column = Math.max(
            0,
            Math.min((int) (clampArenaX(x, 1f) / tileWidth), getLastArenaTileColumn(tileWidth))
        );

        float groundY = map.getHeightPixels();
        for (int row = map.getHeight() - 1; row >= 0; row--) {
            MapTile tile = map.getMapTile(column, row);
            if (tile != null && (tile.getTileType() == TileType.NOT_PASSABLE
                || tile.getTileType() == TileType.PILLAR)) {
                groundY = tile.getBounds().getY1();
                break;
            }
        }
        return groundY;
    }

    private float getArenaRightBound() {
        return map == null ? arenaRightBound : Math.min(arenaRightBound, map.getWidthPixels());
    }

    private float clampArenaX(float x, float margin) {
        float minX = map.getTileset().getScaledSpriteWidth();
        float maxX = Math.max(minX, getArenaRightBound() - margin);
        return Math.max(minX, Math.min(x, maxX));
    }

    private int getLastArenaTileColumn(float tileWidth) {
        return Math.min(
            map.getWidth() - 1,
            (int) Math.ceil(getArenaRightBound() / tileWidth) - 1
        );
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

    private static final class PourLavaTile {
        private final int column;
        private final int row;
        private final MapTile originalTile;

        private PourLavaTile(int column, int row, MapTile originalTile) {
            this.column = column;
            this.row = row;
            this.originalTile = originalTile;
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
        if (bossStage == BossStage.STAGE_FOUR
            || health <= 0
            || hitCooldown > 0
            || isHeadAwayFromStart()
            || bossInvulnerabilityTimer > 0) {
            return;
        }

        health--;
        hitCooldown = HIT_COOLDOWN;
        if (getStageForHealth(health) != bossStage) {
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
            || (stageOneAttack == StageOneAttack.SLIDING_FIST
                && attackState == AttackState.RECOVERING);
        if (movingLeftHand) {
            if (leftHandIsAttacking) {
                leftX = slidingHandX;
            } else {
                rightX = slidingHandX;
            }
        }
        boolean stageThreeCrownHandActive = bossStage == BossStage.STAGE_THREE
            && (attackState == AttackState.CROWN_GRABBING
                || attackState == AttackState.CROWN_HOLDING
                || attackState == AttackState.CROWN_RETRIEVING
                || attackState == AttackState.CROWN_REPLACING);
        if (stageThreeCrownHandActive) {
            leftX = crownHandX;
            leftHandY = crownHandY;
        }
        if (attackState == AttackState.WAITING) {
            leftHandY = restY;
            rightHandY = restY;
        }
        if (attackState == AttackState.HEAD_SWEEPING) {
            drawRotatedOutlinedHead(graphicsHandler, bossX, bossY);
        } else {
            drawOutlinedImage(
                graphicsHandler,
                currentFrame.getImage(),
                bossX,
                bossY,
                getWidth(),
                getHeight(),
                bossStage != BossStage.STAGE_ONE && stageTwoFacingRight
            );
        }
        boolean rockHandIsMoving = attackState == AttackState.ROCK_GOING_TO_WALL
            || attackState == AttackState.ROCK_GRABBING
            || attackState == AttackState.ROCK_RETURNING;
        boolean showingStageOneHands = bossStage == BossStage.STAGE_ONE
            && attackState != AttackState.HEAD_DROPPING
            && attackState != AttackState.HEAD_SWEEPING
            && attackState != AttackState.HEAD_RETURNING;
        boolean showingHands = bossStage != BossStage.STAGE_ONE || showingStageOneHands;
        boolean showLeftHand = showingHands
            && !(bossStage != BossStage.STAGE_ONE && rockHandIsMoving && !rockGrabFacingRight);
        boolean showRightHand = showingHands
            && !(bossStage != BossStage.STAGE_ONE && rockHandIsMoving && rockGrabFacingRight);
        boolean showingFist = attackState == AttackState.SLAMMING
            || attackState == AttackState.SLIDING
            || attackState == AttackState.RETURNING
            || attackState == AttackState.RECOVERING;
        boolean stageThreeBothFists = bossStage == BossStage.STAGE_THREE
            && (attackState == AttackState.CROWN_AIRBORNE
                || attackState == AttackState.CROWN_FALLING);
        BufferedImage leftHandImage = crownHeld && stageThreeCrownHandActive
            ? STAGE_THREE_CROWN_HAND
            : ((showingFist && leftHandIsAttacking) || stageThreeBothFists ? FIST_HAND : LEFT_HAND);
        BufferedImage rightHandImage = (showingFist && !leftHandIsAttacking) || stageThreeBothFists
            ? FIST_HAND
            : RIGHT_HAND;
        if (showLeftHand) {
            drawOutlinedImage(
                graphicsHandler,
                leftHandImage,
                Math.round(leftX - map.getCamera().getX()),
                Math.round(leftHandY - map.getCamera().getY()),
                handWidth,
                handHeight,
                false
            );
        }
        if (showRightHand) {
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

        if (attackState == AttackState.ROCK_GOING_TO_WALL
            || attackState == AttackState.ROCK_GRABBING
            || attackState == AttackState.ROCK_RETURNING) {
            drawRockGrabHand(graphicsHandler);
        }
        if (rockHeld || rockActive) {
            drawRock(graphicsHandler);
        }
        if (bossStage == BossStage.STAGE_THREE) {
            if (attackState == AttackState.CROWN_AIRBORNE || attackState == AttackState.CROWN_FALLING) {
                drawCrownLandingMarker(graphicsHandler);
                drawSpinningCrown(graphicsHandler);
            } else if (attackState == AttackState.CROWN_RETRIEVING) {
                drawSpinningCrown(graphicsHandler);
            }
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
        for (int stageBoundary = 1; stageBoundary < 4; stageBoundary++) {
            int boundaryX = barX + barWidth * stageBoundary / 4;
            graphicsHandler.drawFilledRectangle(
                boundaryX - 1, barY - 2, 2, barHeight + 4, Color.WHITE
            );
        }
        graphicsHandler.drawStringWithOutline(
            "LAVA BOSS - STAGE " + getStageNumber() + "/4",
            barX,
            barY - 8,
            new Font("Arial", Font.BOLD, 18),
            new Color(255, 214, 120),
            Color.black,
            3
        );
    }

    private int getStageNumber() {
        switch (bossStage) {
            case STAGE_TWO:
                return 2;
            case STAGE_THREE:
                return 3;
            case STAGE_FOUR:
                return 4;
            default:
                return 1;
        }
    }

    private void drawRock(GraphicsHandler graphicsHandler) {
        float cameraX = map.getCamera().getX();
        float cameraY = map.getCamera().getY();
        int x = Math.round(rockX - cameraX);
        int y = Math.round(rockY - cameraY);
        int radius = ROCK_RADIUS;
        Graphics2D rockGraphics = (Graphics2D) graphicsHandler.getGraphics().create();
        Polygon rock = new Polygon(
            new int[] {x - radius, x - radius / 2, x + radius / 2, x + radius, x + radius / 2, x - radius / 2},
            new int[] {y - radius / 3, y - radius, y - radius, y - radius / 3, y + radius, y + radius},
            6
        );
        rockGraphics.setColor(new Color(65, 53, 48));
        rockGraphics.fillPolygon(rock);
        rockGraphics.setColor(new Color(125, 91, 67));
        rockGraphics.setStroke(new BasicStroke(3));
        rockGraphics.drawPolygon(rock);
        rockGraphics.drawLine(x - radius / 3, y - radius / 2, x, y);
        rockGraphics.drawLine(x, y, x + radius / 3, y - radius / 3);
        rockGraphics.dispose();
    }

    private void drawRockGrabHand(GraphicsHandler graphicsHandler) {
        float cameraX = map.getCamera().getX();
        float cameraY = map.getCamera().getY();
        int handWidth = Math.round(HAND_WIDTH * SPRITE_SCALE);
        int handHeight = Math.round(HAND_HEIGHT * SPRITE_SCALE);
        int handX = Math.round(rockHandX - handWidth / 2f - cameraX);
        int handY = Math.round(rockHandY - handHeight * 0.3f - cameraY);

        drawOutlinedImage(
            graphicsHandler,
            rockGrabFacingRight ? RIGHT_HAND : LEFT_HAND,
            handX,
            handY,
            handWidth,
            handHeight,
            !rockGrabFacingRight
        );
    }

    private void drawCrownLandingMarker(GraphicsHandler graphicsHandler) {
        float cameraX = map.getCamera().getX();
        float cameraY = map.getCamera().getY();
        int markerX = Math.round(crownTargetX - cameraX);
        int groundY = Math.round(findGroundY(crownTargetX) - cameraY);
        Graphics2D markerGraphics = (Graphics2D) graphicsHandler.getGraphics().create();
        markerGraphics.setColor(new Color(255, 242, 190, 210));
        markerGraphics.setStroke(new BasicStroke(3));
        markerGraphics.drawLine(markerX, groundY - 52, markerX, groundY - 8);
        markerGraphics.drawOval(markerX - 10, groundY - 18, 20, 10);
        markerGraphics.drawLine(markerX - 14, groundY - 6, markerX + 14, groundY - 6);
        markerGraphics.dispose();
    }

    private void drawSpinningCrown(GraphicsHandler graphicsHandler) {
        BufferedImage image = STAGE_THREE_CROWN;
        BufferedImage outlinedImage = outlinedImages.computeIfAbsent(image, LavaBoss::createOutlinedImage);
        int width = Math.round(getCrownDisplayWidth());
        int height = Math.round(getCrownDisplayHeight());
        int x = Math.round(crownX - map.getCamera().getX());
        int y = Math.round(crownY - map.getCamera().getY());
        Graphics2D crownGraphics = (Graphics2D) graphicsHandler.getGraphics().create();
        crownGraphics.translate(x, y);
        crownGraphics.rotate(crownRollAngle);
        crownGraphics.drawImage(outlinedImage, -width / 2, -height / 2, width, height, null);
        crownGraphics.dispose();
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
