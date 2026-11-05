package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.ForestChunk;
import Enemies.GourdProjectile;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.Rectangle;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.AirGroundState;
import Utils.Effects;
import Utils.Point;

import java.util.HashMap;


public class Catapult extends EnhancedMapTile {
    private enum State { READY, LOADED, FLYING, COOLDOWN }

    private State state = State.READY;
    private int timer = 0;
    private int flightTime = 0;
    private Player flyer;

    // the flight path 
    private static final float FLIGHT_GRAVITY = 0.5f, MAX_FALL = 6f;
    private float pathX, pathY, pathVy;

    private float launchForce = 19f;
    private float launchSpeed = 10f;
    private int fuseTime = 20;

    public Catapult(Point location) {
        super(location.x, location.y, frames(), TileType.PASSABLE);
        initialize();
        setIsUpdateOffScreen(true);  
    }

    public Catapult launch(float force, float speed) {
        this.launchForce = force;
        this.launchSpeed = speed;
        return this;
    }

    private static HashMap<String, Frame[]> frames() {
        SpriteSheet s = new SpriteSheet(ImageLoader.load("Catapult.png"), 32, 32);
        HashMap<String, Frame[]> a = new HashMap<>();
        a.put("DEFAULT", new Frame[] { frame(s, 0, 0) }); 
        a.put("IDLE", new Frame[] { frame(s, 0, 0) });
        a.put("LOADED", new Frame[] { frame(s, 1, 4), frame(s, 2, 4) });
        a.put("FIRE", new Frame[] { frame(s, 3, 0) });
        return a;
    }

    private static Frame frame(SpriteSheet s, int cell, int delay) {
        return new FrameBuilder(s.getSprite(0, cell), delay).withScale(4).withBounds(4, 4, 24, 24).build();
    }

    // where the knight sits until it fires
    private float muzzleX() { return getX() + 5 * 4; }
    private float muzzleY() { return getY() + 11 * 4; }

    private void holdAtMuzzle(Player player) {
        Rectangle p = player.getBounds();
        player.setX(player.getX() + (muzzleX() - (p.getX1() + p.getWidth() / 2f)));
        player.setY(player.getY() + (muzzleY() - p.getY2()));
    }

    @Override
    public void update(Player player) {
        super.update(player);
        switch (state) {
            case READY:
                currentAnimationName = "IDLE";
                if (intersects(player)) {
                    state = State.LOADED;
                    timer = fuseTime;
                    flyer = player;
                    holdAtMuzzle(player);
                    Effects.shake(1.5f, fuseTime);
                }
                break;

            case LOADED:
                currentAnimationName = "LOADED";
                holdAtMuzzle(player);
                if (--timer <= 0) {
                    fire(player);
                }
                break;

            case FLYING:
                flightTime++;
                pathX += launchSpeed;
                pathVy = Math.min(pathVy + FLIGHT_GRAVITY, MAX_FALL);
                pathY += pathVy;
                boolean landed = false, blocked = false;
                if (pathVy > 0 && flightTime > 4 && player.getAirGroundState() == AirGroundState.GROUND) {
                    landed = true;      // came down on something by himself
                } else {
                    // put him back on the arc, whatever he did this frame
                    float wantX = pathX - player.getX(), wantY = pathY - player.getY();
                    float gotX = player.moveXHandleCollision(wantX);
                    float gotY = player.moveYHandleCollision(wantY);
                    if (wantY > 0 && gotY < wantY - 1) landed = flightTime > 4;          // hit the ground
                    if (Math.abs(gotX - wantX) > 2 || (wantY < 0 && gotY > wantY + 1)) blocked = true;   // a wall or a ceiling
                }
                if (flightTime % 3 == 0) {
                    GourdProjectile.puff(map, player.getBounds().getX1(), player.getBounds().getY2() - 10, -0.6f);
                }
                if (flightTime > 20) currentAnimationName = "IDLE";
                if (landed || blocked || flightTime > 240) {
                    state = State.COOLDOWN;
                    timer = 50;
                    if (landed) {
                        Effects.shake(4, 10);
                    }
                }
                break;

            case COOLDOWN:
                currentAnimationName = "IDLE";
                if (--timer <= 0 && !intersects(player)) {
                    state = State.READY;
                }
                break;
        }
        // if the knight died mid-shot, stop steering him
        if (state == State.FLYING && flyer != null && flyerDead(flyer)) {
            state = State.COOLDOWN;
            timer = 50;
        }
    }

    private boolean flyerDead(Player player) {
        return player.getHealth() <= 0;
    }

    private void fire(Player player) {
        state = State.FLYING;
        flightTime = 0;
        currentAnimationName = "FIRE";
        player.bounce(launchForce);       // jumping state
        pathX = player.getX();
        pathY = player.getY();
        pathVy = -launchForce;
        Effects.shake(9, 18);
        float mx = muzzleX(), my = muzzleY();
        GourdProjectile.puff(map, mx, my + 10, 1.5f);
        GourdProjectile.puff(map, mx - 30, my + 30, -1f);
        ForestChunk.burst(map, mx, my, new int[] { ForestChunk.BARK, ForestChunk.LEAF, ForestChunk.SEED }, 12, 6f);
    }
}
