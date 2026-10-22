package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.ScreenManager;
import GameObject.Frame;
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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;

public class LavaBoss extends Enemy {
    private static final int SPRITE_SIZE = 512;
    private static final int MAX_HEALTH = 10;
    private static final int HIT_COOLDOWN = 24;
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
    private static final float TILE_BOUNCE_HEIGHT = 48f;
    private static final int TILE_BOUNCE_DURATION = 36;
    private static final int TILE_WAVE_RADIUS = 8;
    private static final int TILE_WAVE_DELAY_PER_TILE = 4;
    private static final int HAND_WIDTH = 125;
    private static final int HAND_HEIGHT = 150;
    private static final int OUTLINE_SIZE = 5;
    private static final BufferedImage LEFT_HAND =
        ImageLoader.loadSubImage("Lava Boss hands .png", 105, 180, HAND_WIDTH, HAND_HEIGHT);
    private static final BufferedImage RIGHT_HAND =
        ImageLoader.loadSubImage("Lava Boss hands .png", 280, 180, HAND_WIDTH, HAND_HEIGHT);

    private enum AttackState { WAITING, WINDUP, SLAMMING, RECOVERING }

    private final IdentityHashMap<BufferedImage, BufferedImage> outlinedImages = new IdentityHashMap<>();
    private final ArrayList<TileBounce> tileBounces = new ArrayList<>();
    private int health = MAX_HEALTH;
    private int hitCooldown;
    private int entranceTimer = ENTRANCE_DURATION;
    private final float entranceTargetY;
    private int attackTimer = SLAM_INTERVAL;
    private AttackState attackState = AttackState.WAITING;
    private int attackStateTimer;
    private float leftHandY;
    private float rightHandY;
    private boolean leftHandHitPlayer;
    private boolean rightHandHitPlayer;

    public LavaBoss(Point location) {
        super(location.x, location.y, createAnimations(), "IDLE");
        entranceTargetY = location.y;
        setY(entranceTargetY + ENTRANCE_START_OFFSET);
        setIsUpdateOffScreen(true);
    }

    private static HashMap<String, Frame[]> createAnimations() {
        BufferedImage sheet = ImageLoader.load("Lava Boss.png");
        Frame[] idleFrames = new Frame[8];

        for (int frameIndex = 0; frameIndex < idleFrames.length; frameIndex++) {
            int row = frameIndex / 3;
            int column = frameIndex % 3;
            BufferedImage image = sheet.getSubimage(
                column * SPRITE_SIZE,
                row * SPRITE_SIZE,
                SPRITE_SIZE,
                SPRITE_SIZE
            );
            idleFrames[frameIndex] = new FrameBuilder(image, 12)
                .withScale(SPRITE_SCALE)
                .withBounds(100, 110, 312, 330)
                .build();
        }

        HashMap<String, Frame[]> animations = new HashMap<>();
        animations.put("IDLE", idleFrames);
        return animations;
    }

    @Override
    public void update(Player player) {
        if (mapEntityStatus == MapEntityStatus.REMOVED) {
            return;
        }

        if (hitCooldown > 0) {
            hitCooldown--;
        }
        updateTileBounces(player);
        if (entranceTimer > 0) {
            riseIntoArena();
            super.update(player);
            return;
        }
        if (attackState == AttackState.WAITING) {
            hoverAbovePlayer(player);
        }
        updateHandAttack(player);
        if (player.isAttacking() && intersects(player)) {
            takeHit(player);
        }
        if (mapEntityStatus != MapEntityStatus.REMOVED) {
            super.update(player);
        }
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
                attackState = AttackState.WINDUP;
                attackStateTimer = WINDUP_DURATION;
            }
            return;
        }

        if (attackState == AttackState.WINDUP) {
            leftHandY = restY;
            rightHandY = restY;
            if (--attackStateTimer <= 0) {
                attackState = AttackState.SLAMMING;
                leftHandHitPlayer = false;
                rightHandHitPlayer = false;
            }
            return;
        }

        if (attackState == AttackState.SLAMMING) {
            leftHandY = Math.min(leftGroundY, leftHandY + HAND_SLAM_SPEED);
            rightHandY = Math.min(rightGroundY, rightHandY + HAND_SLAM_SPEED);
            if (leftHandY >= leftGroundY && !leftHandHitPlayer) {
                markHandImpact(true);
                startTileBounce(leftX + handWidth / 2f);
            }
            if (rightHandY >= rightGroundY && !rightHandHitPlayer) {
                markHandImpact(false);
                startTileBounce(rightX + handWidth / 2f);
            }
            if (leftHandY >= leftGroundY && rightHandY >= rightGroundY) {
                attackState = AttackState.RECOVERING;
                attackStateTimer = RECOVERY_DURATION;
            }
            return;
        }

        leftHandY = Math.max(restY, leftHandY - HAND_RAISE_SPEED);
        rightHandY = Math.max(restY, rightHandY - HAND_RAISE_SPEED);
        if (--attackStateTimer <= 0 || (leftHandY <= restY && rightHandY <= restY)) {
            leftHandY = restY;
            rightHandY = restY;
            attackState = AttackState.WAITING;
            attackTimer = SLAM_INTERVAL;
        }
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
        if (mapEntityStatus == MapEntityStatus.REMOVED || hitCooldown > 0) {
            return false;
        }
        takeHit(player);
        return true;
    }

    private void takeHit(Player player) {
        if (hitCooldown > 0) {
            return;
        }

        health--;
        hitCooldown = HIT_COOLDOWN;
        if (health <= 0) {
            player.addCoins(25);
            mapEntityStatus = MapEntityStatus.REMOVED;
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (mapEntityStatus == MapEntityStatus.REMOVED) {
            return;
        }

        int bossX = Math.round(getCalibratedXLocation());
        int bossY = Math.round(getCalibratedYLocation());
        int handWidth = Math.round(HAND_WIDTH * SPRITE_SCALE);
        int handHeight = Math.round(HAND_HEIGHT * SPRITE_SCALE);
        float restY = getY() + 90;
        float leftX = getHandX(true, handWidth);
        float rightX = getHandX(false, handWidth);
        if (attackState == AttackState.WAITING) {
            leftHandY = restY;
            rightHandY = restY;
        }
        drawOutlinedImage(graphicsHandler, currentFrame.getImage(), bossX, bossY, getWidth(), getHeight());
        drawOutlinedImage(
            graphicsHandler,
            LEFT_HAND,
            Math.round(leftX - map.getCamera().getX()),
            Math.round(leftHandY - map.getCamera().getY()),
            handWidth,
            handHeight
        );
        drawOutlinedImage(
            graphicsHandler,
            RIGHT_HAND,
            Math.round(rightX - map.getCamera().getX()),
            Math.round(rightHandY - map.getCamera().getY()),
            handWidth,
            handHeight
        );

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

    private void drawOutlinedImage(
        GraphicsHandler graphicsHandler,
        BufferedImage image,
        int x,
        int y,
        int width,
        int height
    ) {
        BufferedImage outlinedImage = outlinedImages.computeIfAbsent(image, LavaBoss::createOutlinedImage);
        int outlineX = Math.round(OUTLINE_SIZE * width / (float) image.getWidth());
        int outlineY = Math.round(OUTLINE_SIZE * height / (float) image.getHeight());
        graphicsHandler.drawImage(
            outlinedImage,
            x - outlineX,
            y - outlineY,
            width + 2 * outlineX,
            height + 2 * outlineY
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
