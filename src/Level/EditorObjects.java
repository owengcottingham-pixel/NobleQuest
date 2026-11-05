package Level;

import EnhancedMapTiles.SinkingPillar;
import EnhancedMapTiles.SpikeBed;
import EnhancedMapTiles.SpikeCrusher;
import EnhancedMapTiles.SpinningSpikeBar;
import EnhancedMapTiles.SpringPad;
import Utils.Point;

import java.util.Locale;

// The objects the map editor can place, saved as lines after the tile grid in a map file
public class EditorObjects {
    public static final int TILE = 48;

    // what the editor's Place menu offers: { menu label, keyword }
    public static final String[][] PLACEABLE = {
            { "Spikes (pointing up)", "SPIKES UP" },
            { "Spikes (pointing down)", "SPIKES DOWN" },
            { "Spikes (pointing left)", "SPIKES LEFT" },
            { "Spikes (pointing right)", "SPIKES RIGHT" },
            { "Low Spring", "SPRING LOW" },
            { "Big Spring", "SPRING NORMAL" },
            { "Spiked Ball", "SPIKE_BAR" },
            { "Sinking Pillar", "SINKING_PILLAR" },
            { "Crusher", "CRUSHER" },
    };

    // makes the object a map file line describes (null if the line isn't one of these)
    public static EnhancedMapTile create(String[] t) {
        try {
            switch (t[0]) {
                case "SPIKES": {
                    SpikeBed s = new SpikeBed(at(t[1], t[2]), Integer.parseInt(t[3]), SpikeBed.Facing.valueOf(t[4]));
                    if (t.length > 5) s.popForce(Float.parseFloat(t[5]));
                    return s;
                }
                case "SPRING": {
                    SpringPad s = new SpringPad(at(t[1], t[2]), Integer.parseInt(t[3]), SpringPad.Kind.valueOf(t[4]));
                    if (t.length > 5) s.bounceForce(Float.parseFloat(t[5]));
                    return s;
                }
                case "SPIKE_BAR":
                    return new SpinningSpikeBar(at(t[1], t[2]), Float.parseFloat(t[3]) * TILE, Integer.parseInt(t[4]),
                            Float.parseFloat(t[5]), Float.parseFloat(t[6]));
                case "SINKING_PILLAR":
                    return new SinkingPillar(at(t[1], t[2]), Float.parseFloat(t[3]) * TILE);
                case "CRUSHER":
                    return new SpikeCrusher(at(t[1], t[2]), Float.parseFloat(t[3]) * TILE, Integer.parseInt(t[4]));
                default:
                    return null;
            }
        } catch (RuntimeException e) {
            System.out.println("Couldn't read map object line: " + String.join(" ", t));
            return null;
        }
    }

    // a new object placed with the editor at tile (x, y), with sensible defaults
    public static EnhancedMapTile place(String what, int x, int y, Map map) {
        String[] w = what.split(" ");
        switch (w[0]) {
            case "SPIKES":
                return create(new String[] { "SPIKES", "" + x, "" + y, "1", w[1] });
            case "SPRING":
                return create(new String[] { "SPRING", "" + x, "" + y, "2", w[1] });
            case "SPIKE_BAR":
                return create(new String[] { "SPIKE_BAR", (x + 0.5f) + "", (y + 0.5f) + "", "3.4", "1", "-1.2", "90" });
            case "SINKING_PILLAR":
                return create(new String[] { "SINKING_PILLAR", "" + x, "" + y, "" + floorBelow(map, x, y + 1) });
            case "CRUSHER":
                return create(new String[] { "CRUSHER", "" + x, "" + y, num(landingBelow(map, x, y + 2) / (float) TILE), "0" });
            default:
                return null;
        }
    }

    // the first solid row under (x, y)
    private static int floorBelow(Map map, int x, int y) {
        for (int row = y; row < map.getHeight(); row++) {
            MapTile tile = map.getMapTile(x, row);
            if (tile != null && tile.getTileType() == TileType.NOT_PASSABLE) return row;
        }
        return map.getHeight();
    }

    // where a crusher dropped at column x from row y would land (pixels): a spring under it, or the floor
    private static float landingBelow(Map map, int x, int y) {
        float best = floorBelow(map, x, y) * TILE;
        for (EnhancedMapTile t : map.getEnhancedMapTiles()) {
            if (t instanceof SpringPad && t.getBounds().getX2() > x * TILE && t.getBounds().getX1() < (x + 2) * TILE
                    && t.getBounds().getY1() > y * TILE) {
                best = Math.min(best, t.getBounds().getY1() - 4);
            }
        }
        return best;
    }

    private static Point at(String x, String y) {
        return new Point(Float.parseFloat(x) * TILE, Float.parseFloat(y) * TILE);
    }

    // pixels -> tiles, written without needless decimals (90, 77.125)
    public static String tiles(float pixels) {
        return num(pixels / TILE);
    }

    public static String num(float n) {
        if (Math.abs(n - Math.round(n)) < 0.0005f) return String.valueOf(Math.round(n));
        String s = String.format(Locale.US, "%.3f", n);
        return s.replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
