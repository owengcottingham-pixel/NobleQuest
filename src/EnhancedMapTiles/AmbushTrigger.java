package EnhancedMapTiles;

import Builders.FrameBuilder;
import Level.Enemy;
import Level.EnhancedMapTile;
import Level.MapEntityStatus;
import Level.Player;
import Level.TileType;
import Utils.Point;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

// An invisible zone. When the player walks into it, the enemies you gave it appear.
// Once every one of those enemies is dead, every gate you gave it opens.
// location = top-left tile of the zone, width/height are in tiles.
public class AmbushTrigger extends EnhancedMapTile {
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Gate> gates = new ArrayList<>();
    private boolean triggered = false;
    private boolean opened = false;

    public AmbushTrigger(Point location, int widthInTiles, int heightInTiles) {
        super(location.x, location.y,
                new FrameBuilder(new BufferedImage(16 * widthInTiles, 16 * heightInTiles, BufferedImage.TYPE_INT_ARGB))
                        .withScale(3)
                        .build(),
                TileType.PASSABLE);
        setIsUpdateOffScreen(true);
    }

    // enemies added here stay hidden until the trap goes off
    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    public void addGate(Gate gate) {
        gates.add(gate);
    }

    @Override
    public void update(Player player) {
        super.update(player);

        // player stepped in: spawn the enemies
        if (!triggered && intersects(player)) {
            triggered = true;
            for (Enemy enemy : enemies) {
                map.addEnemy(enemy);
            }
        }

        // all enemies dead: open the gates
        if (triggered && !opened) {
            for (Enemy enemy : enemies) {
                if (enemy.getMapEntityStatus() != MapEntityStatus.REMOVED) {
                    return;
                }
            }
            for (Gate gate : gates) {
                gate.open();
            }
            opened = true;
        }
    }
}