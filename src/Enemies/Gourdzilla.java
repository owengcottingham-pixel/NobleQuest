package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.ScreenManager;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.Rectangle;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.EnhancedMapTile;
import Level.MapEntity;
import Level.MapEntityStatus;
import Level.MapTile;
import Level.Player;
import Level.TileType;
import Utils.Direction;
import Utils.Point;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;

// GOURDZILLA - the pumpkin boss.
public class Gourdzilla extends Enemy {

    private enum State { ENTRANCE, ENTRANCE_LAND, INTRO, IDLE, CHASE, WINDUP, ROLL, BOUNCE, DIZZY, HURT, SPIT, CROUCH, AIRBORNE, LAND,
                         ROOTS_SHAKE, ROOTS_HOP, ROOTS_SLAM, TRANSFORM, DYING, DEAD }

    private static final float DROP_HEIGHT = 440;  
    private static final int INTRO_TIME = 36; 

    private static final State[] PATTERN = { State.CHASE, State.WINDUP, State.ROOTS_SHAKE, State.SPIT, State.CROUCH };

    // tuning (frames are at 60fps)
    private int maxHealth = 6;
    private int health = maxHealth;
    private float gravity = 0.5f;
    private float terminalVelocity = 10f;
    private int pumpkinlingCount = 5;

    private State state = State.ENTRANCE;
    private float landY;                                      
    private int timer = 0;
    private int patternIndex = 0;
    private boolean enraged = false;
    private Direction facing = Direction.LEFT;
    private float velocityY = 0;
    private float jumpVelocityX = 0;
    private float bounceVelocityX = 0;
    private int hitCooldown = 0;
    private Player target;                                   
    private ArrayList<Pumpkinling> pumpkinlings = new ArrayList<>();
    private int popsLeft = 0;                            
    private int popTimer = 0;


    public Gourdzilla(Point location) {
        super(location.x, location.y - DROP_HEIGHT, new SpriteSheet(ImageLoader.load("Gourdzilla.png"), 48, 48), "JUMP_FALL_LEFT");
        landY = location.y;
        initialize();
        setIsUpdateOffScreen(true);  
        startState(State.ENTRANCE, 0);
    }

    public boolean isDefeated() {
        return state == State.DYING || state == State.DEAD;
    }

  
    @Override
    public void update(Player player) {
        target = player;

        if (state == State.ENTRANCE) {
            velocityY = Math.min(velocityY + gravity, 16f);
            moveY(velocityY);
            if (getY() >= landY) {
                setY(landY);
                velocityY = 0;
                thud();
                startState(State.ENTRANCE_LAND, 22);
            }
            currentAnimationName = animationFor();
            super.update(player);
            return;
        }

        float moveX = 0;
        if (hitCooldown > 0) hitCooldown--;
        timer--;

        switch (state) {
            case ENTRANCE_LAND:                    
                if (timer <= 0) startState(State.INTRO, INTRO_TIME);   
                break;
            case INTRO:
            case TRANSFORM:
                if (timer <= 0) startState(State.IDLE, idleTime());
                break;
            case IDLE:
                facePlayer(player);
                if (timer <= 0) nextAttack(player);
                break;
            case CHASE:
                facePlayer(player);
                moveX = dir() * (enraged ? 1.9f : 1.3f);
                if (timer <= 0) startState(State.IDLE, idleTime());
                break;
            case WINDUP:
                if (timer <= 0) startState(State.ROLL, 300);
                break;
            case ROLL:
                moveX = dir() * (enraged ? 8.5f : 6.5f);
                if (timer <= 0) startState(State.IDLE, idleTime());  
                break;
            case BOUNCE:
                moveX = bounceVelocityX;
                if (timer <= 0) startState(State.DIZZY, enraged ? 95 : 140);   
                break;
            case DIZZY:
                if (timer <= 0) startState(State.IDLE, idleTime());
                break;
            case HURT:
                if (timer <= 0) {
                    if (!enraged && health <= maxHealth / 2) {
                        enraged = true;                                // the 50% HP transformation
                        startState(State.TRANSFORM, 40);
                    } else {
                        startState(State.IDLE, idleTime());
                    }
                }
                break;
            case SPIT:
                if (timer == 12 || (enraged && timer == 36)) spitSeeds(player);
                if (timer <= 0) startState(State.IDLE, idleTime());
                break;
            case CROUCH:
                if (timer <= 0) {
                    startState(State.AIRBORNE, 0);
                    velocityY = -11.5f;
                    float dx = player.getX() - getX();
                    jumpVelocityX = Math.max(-6f, Math.min(6f, dx / 48f));
                }
                break;
            case AIRBORNE:
                moveX = jumpVelocityX;
                break;
            case LAND:
                if (timer <= 0) startState(State.IDLE, idleTime());
                break;
            case ROOTS_SHAKE:                       
                if (timer <= 0) {
                    startState(State.ROOTS_HOP, 60);
                    velocityY = -6f;              
                }
                break;
            case ROOTS_HOP:                        
                if (timer <= 0) startState(State.IDLE, idleTime());  
                break;
            case ROOTS_SLAM:                      
                if (timer <= 0) startState(State.IDLE, idleTime());
                break;
            case DYING:
                if (timer == 12) {                 
                    landingDust();
                    popsLeft = pumpkinlingCount;
                    popTimer = 1;
                }
                if (timer <= 0) startState(State.DEAD, 120);
                break;
            case DEAD:
               
                if (timer <= 0 && popsLeft == 0 && pumpkinlingsLeft() == 0) mapEntityStatus = MapEntityStatus.REMOVED;
                break;
        }

        if (popsLeft > 0 && --popTimer <= 0) {
            popOutPumpkinling(pumpkinlingCount - popsLeft);
            popsLeft--;
            popTimer = 7;
        }

        // sword hits only land while he's dizzy
        if (player.isAttacking() && inSwordRange(player)) {
            takeHit(player);
        }

        velocityY = Math.min(velocityY + gravity, terminalVelocity);
        moveYHandleCollision(velocityY);
        moveXHandleCollision(moveX);

        currentAnimationName = animationFor();
        super.update(player);
    }

    private void takeHit(Player player) {
        if (state != State.DIZZY || hitCooldown > 0) {
            return;
        }
        health--;
        hitCooldown = 30;
        if (health <= 0) {
            player.addCoins(25);
            clearArena();
            startState(State.DYING, 6 * 12);
        } else {
            startState(State.HURT, 22);
        }
    }

    @Override
    public boolean hitByBolt(Player player) {
        takeHit(player);
        return true;
    }

    private void nextAttack(Player player) {
        State next = PATTERN[patternIndex % PATTERN.length];
        patternIndex++;
        facePlayer(player);
        switch (next) {
            case CHASE:       startState(State.CHASE, enraged ? 70 : 90); break;
            case WINDUP:      startState(State.WINDUP, enraged ? 18 : 26); break;
            case SPIT:        startState(State.SPIT, enraged ? 50 : 26); break;
            case ROOTS_SHAKE: startState(State.ROOTS_SHAKE, enraged ? 18 : 28); break;
            default:          startState(State.CROUCH, enraged ? 10 : 16); break;
        }
    }

    private void startState(State s, int frames) {
        state = s;
        timer = frames;
    }

    private int idleTime() {
        return enraged ? 24 : 40;
    }

    private int warnTime() {
        return enraged ? 38 : 50;   // how long the cracks glow before the roots burst out
    }

    private int spikeTime() {
        return enraged ? 34 : 44;   // how long the roots stay up
    }

    private int dir() {
        return facing == Direction.LEFT ? -1 : 1;
    }

    private void facePlayer(Player player) {
        facing = player.getX() < getX() ? Direction.LEFT : Direction.RIGHT;
    }

    private boolean inSwordRange(Player player) {
        Rectangle b = getBounds(), p = player.getBounds();
        float bossCenter = b.getX1() + b.getWidth() / 2f;
        float playerCenter = p.getX1() + p.getWidth() / 2f;
        if (Math.abs((b.getY1() + b.getHeight() / 2f) - (p.getY1() + p.getHeight() / 2f)) > 110) {
            return false;
        }
        float reach = b.getWidth() / 2f + 75;
        float dx = bossCenter - playerCenter;
        return player.getFacingDirection() == Direction.RIGHT ? (dx > -30 && dx < reach) : (dx < 30 && dx > -reach);
    }

    // attacks
    private void spitSeeds(Player player) {
        facePlayer(player);
        Rectangle b = getBounds(), p = player.getBounds();
        float mouthX = facing == Direction.LEFT ? b.getX1() - 24 : b.getX2() - 24;
        float mouthY = b.getY1() + 18;
        float landY = b.getY2() + 1 - 33;                  
        float knightX = p.getX1() + p.getWidth() / 2f - 24;  

        for (float spread : new float[] { -90, 0, 90 }) {
            float dx = knightX + spread - mouthX;
            float dy = landY - mouthY;
            int frames = Math.round(Math.max(36, Math.min(66, 30 + Math.abs(dx) / 14)));
            float vx = dx / frames;
            float vy = (dy - GourdProjectile.SEED_GRAVITY * frames * (frames + 1) / 2f) / frames;
            map.addEnemy(new GourdProjectile(new Point(mouthX, mouthY),
                    enraged ? GourdProjectile.Kind.FLAMING_SEED : GourdProjectile.Kind.SEED, vx, vy));
        }
    }

    private void shockwaves() {
        Rectangle b = getBounds();
        float y = b.getY2() - 47;
        float speed = enraged ? 6f : 4.5f;
        map.addEnemy(new GourdProjectile(new Point(b.getX1() - 40, y), GourdProjectile.Kind.SHOCKWAVE, -speed, 0));
        map.addEnemy(new GourdProjectile(new Point(b.getX2() - 8, y), GourdProjectile.Kind.SHOCKWAVE, speed, 0));
    }

    private void thud() {
        Rectangle b = getBounds();
        float groundY = b.getY2() + 1;
        GourdProjectile.puff(map, b.getX1() - 24, groundY, -2.4f);
        GourdProjectile.puff(map, b.getX1() + 8, groundY, -1.0f);
        GourdProjectile.puff(map, b.getX2() - 8, groundY, 1.0f);
        GourdProjectile.puff(map, b.getX2() + 24, groundY, 2.4f);
    }

    private void landingDust() {
        Rectangle b = getBounds();
        GourdProjectile.puff(map, b.getX1() + 4, b.getY2() + 1, -1.4f);
        GourdProjectile.puff(map, b.getX2() - 4, b.getY2() + 1, 1.4f);
    }

    private void rootCracks() {
        if (target == null) return;
        int tile = map.getTileset().getScaledSpriteWidth();
        float groundY = getBounds().getY2() + 1;
        Rectangle p = target.getBounds();
        int underKnight = Math.round((p.getX1() + p.getWidth() / 2f) / tile) * tile;  
        int[] offsets = { 0, -4, 4, -8, 8 };                                          
        int wanted = enraged ? 3 : 1;
        for (int i = 0, made = 0; i < offsets.length && made < wanted; i++) {
            if (rootCrack(underKnight + offsets[i] * tile, groundY, tile)) made++;
        }
    }

    private boolean rootCrack(int centerX, float groundY, int tile) {
        boolean made = false;
        for (int x = centerX - tile; x < centerX + tile; x += tile) {
            if (isOpenFloor(x + tile / 2f, groundY)) {
                boolean flipped = (x / tile) % 2 == 0;  
                map.addEnemy(GourdProjectile.rootCrack(new Point(x, groundY - tile), warnTime(), spikeTime(), flipped));
                made = true;
            }
        }
        return made;
    }

    private boolean isOpenFloor(float x, float groundY) {
        MapTile air = map.getTileByPosition(x, groundY - 10);
        MapTile ground = map.getTileByPosition(x, groundY + 10);
        if (air == null || ground == null) return false;
        if (air.getTileType() == TileType.NOT_PASSABLE) return false;
        if (ground.getTileType() != TileType.NOT_PASSABLE && ground.getTileType() != TileType.JUMP_THROUGH_PLATFORM) return false;
        for (EnhancedMapTile t : map.getEnhancedMapTiles()) {
            Rectangle r = t.getBounds();
            if (t.getTileType() == TileType.NOT_PASSABLE && x >= r.getX1() && x <= r.getX2()
                    && groundY - 10 >= r.getY1() && groundY - 10 <= r.getY2()) {
                return false;
            }
        }
        return true;
    }

    private void clearArena() {
        for (Enemy e : new ArrayList<>(map.getEnemies())) {  
            if (e instanceof GourdProjectile) {
                ((GourdProjectile) e).vanish();
            }
        }
    }


    private void popOutPumpkinling(int i) {
        Rectangle b = getBounds();
        float centerX = b.getX1() + b.getWidth() / 2f;
        float groundY = b.getY2() + 1;
        float openSide = centerX < map.getWidthPixels() / 2f ? 1 : -1;
        float vx = openSide * (-1.5f + 1.6f * i);  
        float vy = -8.5f - (i % 2) * 2.5f;
        Pumpkinling p = new Pumpkinling(new Point(centerX - 24, groundY - 54), vx, vy, 1.0f + 0.12f * i);
        pumpkinlings.add(p);
        GourdProjectile.puff(map, centerX, groundY, vx * 0.3f);
        GourdProjectile.addOnTop(map, p);          
    }

    private int pumpkinlingsLeft() {
        int left = 0;
        for (Pumpkinling p : pumpkinlings) {
            if (!p.isPopped()) left++;
        }
        return left;
    }

    // collisions
    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (hasCollided && state == State.ROLL) {
            Rectangle b = getBounds();
            float wallX = direction == Direction.LEFT ? b.getX1() : b.getX2();
            float away = direction == Direction.LEFT ? 1 : -1;
            GourdProjectile.puff(map, wallX, b.getY2() + 1, away * 0.6f);
            GourdProjectile.puff(map, wallX + away * 40, b.getY2() + 1, away * 1.8f);
            facing = away > 0 ? Direction.RIGHT : Direction.LEFT;  
            bounceVelocityX = away * (enraged ? 4.5f : 3.5f);       
            velocityY = -6f;                                        
            startState(State.BOUNCE, 60);
        } else if (hasCollided && state == State.BOUNCE) {
            bounceVelocityX = 0; 
        }
    }

    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        if (hasCollided && direction == Direction.DOWN) {
            velocityY = 0;
            if (state == State.AIRBORNE) {
                startState(State.LAND, 14);
                landingDust();
                shockwaves();
            } else if (state == State.ROOTS_HOP) {
                startState(State.ROOTS_SLAM, warnTime() + 10);
                landingDust();
                rootCracks();
            } else if (state == State.BOUNCE) {
                landingDust();
                startState(State.DIZZY, enraged ? 95 : 140);
            }
        }
    }

    @Override
    public void touchedPlayer(Player player) {
        switch (state) {
            case ENTRANCE: case ENTRANCE_LAND: case INTRO:
            case DIZZY: case HURT: case TRANSFORM: case DYING: case DEAD:
                return;                                         
            case ROLL: case AIRBORNE:
                player.damage(2);
                break;
            default:
                player.damage(1);
        }
    }

    // animation
    private String animationFor() {
        String name;
        switch (state) {
            case ENTRANCE: name = "JUMP_FALL"; break;
            case ENTRANCE_LAND: name = "JUMP_LAND"; break;
            case INTRO: name = "ROAR"; break;
            case TRANSFORM: name = "ROAR"; break;
            case CHASE: name = "WALK"; break;
            case WINDUP: name = "WINDUP"; break;
            case ROLL: name = "ROLL"; break;
            case DIZZY: name = "DIZZY"; break;
            case HURT: name = "HURT"; break;
            case SPIT: name = "SPIT"; break;
            case CROUCH: name = "JUMP_CROUCH"; break;
            case AIRBORNE: name = velocityY < -3 ? "JUMP_LAUNCH" : velocityY < 1 ? "JUMP_RISE" : "JUMP_FALL"; break;
            case LAND: name = "JUMP_LAND"; break;
            case ROOTS_SHAKE: name = "JUMP_CROUCH"; break;
            case ROOTS_HOP: name = velocityY < 0 ? "JUMP_LAUNCH" : "JUMP_FALL"; break;
            case ROOTS_SLAM: name = timer > warnTime() - 4 ? "JUMP_LAND" : "IDLE"; break;
            case DYING: case DEAD: name = "DEATH"; break;
            default: name = "IDLE";
        }
        // phase 1 inro roar uses the normal sheet; the transformation roar is on the enraged sheet
        boolean useEnragedSheet = enraged || state == State.TRANSFORM;
        return (useEnragedSheet ? "E_" : "") + name + (facing == Direction.LEFT ? "_LEFT" : "_RIGHT");
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        HashMap<String, Frame[]> a = new HashMap<>();
        SpriteSheet enragedSheet = new SpriteSheet(ImageLoader.load("GourdzillaEnraged.png"), 48, 48);
        for (int phase = 0; phase < 2; phase++) {
            SpriteSheet s = phase == 0 ? spriteSheet : enragedSheet;
            String p = phase == 0 ? "" : "E_";
            for (String side : new String[] { "LEFT", "RIGHT" }) {
                boolean right = side.equals("RIGHT");
                //                       row   frames  delay  (sheet row from boss_sheet.json)
                a.put(p + "IDLE_" + side,   anim(s, 0, 0, 4, 9, right, false));
                a.put(p + "WALK_" + side,   anim(s, 1, 0, 4, 7, right, false));
                a.put(p + "WINDUP_" + side, anim(s, 2, 0, 4, 6, right, false));
                a.put(p + "ROLL_" + side,   anim(s, 3, 0, 8, 3, right, true));
                a.put(p + "SPIT_" + side,   anim(s, 4, 0, 4, 6, right, false));
                a.put(p + "JUMP_CROUCH_" + side, anim(s, 5, 0, 1, 0, right, false));
                a.put(p + "JUMP_LAUNCH_" + side, anim(s, 5, 1, 1, 0, right, false));
                a.put(p + "JUMP_RISE_" + side,   anim(s, 5, 2, 1, 0, right, false));
                a.put(p + "JUMP_FALL_" + side,   anim(s, 5, 3, 1, 0, right, false));
                a.put(p + "JUMP_LAND_" + side,   anim(s, 5, 4, 1, 0, right, false));
                a.put(p + "DIZZY_" + side,  anim(s, 6, 0, 4, 7, right, false));
                a.put(p + "HURT_" + side,   anim(s, 7, 0, 2, 5, right, false));
                a.put(p + "ROAR_" + side,   anim(s, 8, 0, 4, 9, right, false));
                Frame[] death = anim(s, 9, 0, 6, 12, right, false);
                death[5] = frame(s, 9, 5, 0, right, false);           // hold the squashed pile
                a.put(p + "DEATH_" + side, death);
            }
        }
        return a;
    }

    private Frame[] anim(SpriteSheet s, int row, int startCol, int count, int delay, boolean right, boolean rolling) {
        Frame[] frames = new Frame[count];
        for (int i = 0; i < count; i++) {
            frames[i] = frame(s, row, startCol + i, delay, right, rolling);
        }
        return frames;
    }

    private Frame frame(SpriteSheet s, int row, int col, int delay, boolean right, boolean rolling) {

        FrameBuilder b = new FrameBuilder(s.getSprite(row, col), delay).withScale(3);
        if (right) b.withImageEffect(ImageEffect.FLIP_HORIZONTAL);   // the art faces left
        b.withBounds(10, 19, 28, 29);  
        return b.build();
    }


    private void drawLandingShadow(GraphicsHandler graphicsHandler) {
        Rectangle b = getBounds();
        float fallen = 1 - Math.max(0, landY - getY()) / DROP_HEIGHT;         
        int w = Math.round(36 + 60 * fallen) / 6 * 6;                            
        int centerX = Math.round(b.getX1() + b.getWidth() / 2f - map.getCamera().getX());
        int floor = Math.round(landY + getHeight() - map.getCamera().getY());   
        Color shade = new Color(0, 0, 0, 80 + Math.round(100 * fallen));
        graphicsHandler.drawFilledRectangle(centerX - w / 2 + 6, floor, w - 12, 3, shade);
        graphicsHandler.drawFilledRectangle(centerX - w / 2, floor + 3, w, 6, shade);
        graphicsHandler.drawFilledRectangle(centerX - w / 2 + 6, floor + 9, w - 12, 3, shade);
    }
    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (state == State.ENTRANCE) {
            drawLandingShadow(graphicsHandler);
        }

        int shake = state == State.ROOTS_SHAKE ? ((timer / 2) % 2 == 0 ? 3 : -3) : 0;
        graphicsHandler.drawImage(currentFrame.getImage(),
                Math.round(getCalibratedXLocation()) + shake, Math.round(getCalibratedYLocation()),
                currentFrame.getWidth(), currentFrame.getHeight(), currentFrame.getImageEffect());

        if (state == State.ENTRANCE || state == State.ENTRANCE_LAND || state == State.DEAD) return;

        int w = 360, h = 14;
        int x = ScreenManager.getScreenWidth() / 2 - w / 2, y = 30;
        float shown = state == State.INTRO ? 1 - Math.max(0, timer) / (float) INTRO_TIME : 1;  
        int fill = Math.round(w * shown * Math.max(0, health) / (float) maxHealth);
        graphicsHandler.drawFilledRectangle(x - 3, y - 3, w + 6, h + 6, new Color(20, 12, 8));
        graphicsHandler.drawFilledRectangle(x, y, w, h, new Color(60, 30, 20));
        if (fill > 0) {
            graphicsHandler.drawFilledRectangle(x, y, fill, h, enraged ? new Color(232, 70, 30) : new Color(242, 144, 46));
        }
        graphicsHandler.drawStringWithOutline(enraged ? "GOURDZILLA  (ENRAGED)" : "GOURDZILLA", x, y - 8,
                new Font("Arial", Font.BOLD, 18), new Color(255, 214, 120), Color.black, 3);
    }
}
