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

public class BossArenaTrigger extends EnhancedMapTile {
    private final ArrayList<PumpkinGate> gates = new ArrayList<>();
    private Enemy boss;
    private int entranceDelay = 35;   // frames between the gates slamming and the boss showing up
    private int timer = 0;
    private boolean triggered = false;
    private boolean bossSpawned = false;
    private boolean finished = false;

    public BossArenaTrigger(Point location, int widthInTiles, int heightInTiles) {
        super(location.x, location.y,
                new FrameBuilder(new BufferedImage(16 * widthInTiles, 16 * heightInTiles, BufferedImage.TYPE_INT_ARGB))
                        .withScale(3)
                        .build(),
                TileType.PASSABLE);
        setIsUpdateOffScreen(true);
    }

    public void addGate(PumpkinGate gate) {
        gates.add(gate);
    }

    public void setBoss(Enemy boss) {
        this.boss = boss;
    }

    @Override
    public void update(Player player) {
        super.update(player);

        if (!triggered && intersects(player)) {
            triggered = true;
            timer = entranceDelay;
            for (PumpkinGate gate : gates) {
                gate.close();
            }
        }

        if (triggered && !bossSpawned && --timer <= 0) {
            bossSpawned = true;
            if (boss != null) {
                map.addEnemy(boss);
            }
        }

        if (bossSpawned && !finished && (boss == null || boss.getMapEntityStatus() == MapEntityStatus.REMOVED)) {
            finished = true;
            for (PumpkinGate gate : gates) {
                gate.open();
            }
        }
    }
}
