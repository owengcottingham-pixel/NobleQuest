package Tilesets;

import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import Level.TileType;
import Level.Tileset;
import Utils.SlopeTileLayoutUtils;

import java.util.ArrayList;


public class DarkForestTileset extends Tileset {

    // index -> name, collision type
    private static final String[] TILES = {
            /*  0 */ "slope_up", "SLOPE",            // ground rising to the right
            /*  1 */ "slope_down", "SLOPE",          // ground falling to the right
            /*  2 */ "thorns_down", "SPIKES",
            /*  3 */ "thorns_right", "SPIKES",       // on a wall to their left
            /*  4 */ "thorns_left", "SPIKES",        // on a wall to their right
            /*  5 */ "leaves", "PASSABLE",
            /*  6 */ "leaves_flowers", "PASSABLE",
            /*  7 */ "trunk", "PASSABLE",
            /*  8 */ "trunk_eyes_a", "PASSABLE",     // a hollow with eyes in it; blinks (animated with 9)
            /*  9 */ "trunk_eyes_b", "PASSABLE",
            /* 10 */ "canopy_edge_b", "PASSABLE",    // see-through at the bottom (the sky shows)
            /* 11 */ "canopy_edge_l", "PASSABLE",    // see-through on the left
            /* 12 */ "canopy_corner", "PASSABLE",
            /* 13 */ "canopy_inner", "PASSABLE",
            /* 14 */ "sign_danger", "PASSABLE",
            /* 15 */ "sign_dash", "PASSABLE",
            /* 16 */ "thorns_up", "SPIKES",         // same as the arena's thorns, with the old spike tiles' smaller hitbox
            /* 17 */ "fern", "PASSABLE",
            /* 18 */ "bluebells", "PASSABLE",
            /* 19 */ "mossy_rock", "PASSABLE",
            /* 20 */ "oak_tl", "PASSABLE",           // a big oak, 2 wide x 3 tall: 20 21 / 22 23 / 24 25
            /* 21 */ "oak_tr", "PASSABLE",
            /* 22 */ "oak_ml", "PASSABLE",
            /* 23 */ "oak_mr", "PASSABLE",
            /* 24 */ "oak_bl", "PASSABLE",
            /* 25 */ "oak_br", "PASSABLE",
            /* 26 */ "creeping_vine", "PASSABLE",    // see-through: purple thorn vines on daylight stone
    };

    public DarkForestTileset() {
        super(ImageLoader.load("DarkForestTileset.png"), 16, 16, 3);
    }

    @Override
    public ArrayList<MapTileBuilder> defineTiles() {
        ArrayList<MapTileBuilder> mapTiles = new ArrayList<>();
        for (int i = 0; i < TILES.length / 2; i++) {
            String name = TILES[i * 2];
            TileType type = TileType.valueOf(TILES[i * 2 + 1]);
            int row = i / 8, col = i % 8;

            if (name.equals("trunk_eyes_a") || name.equals("trunk_eyes_b")) {
                boolean open = name.endsWith("a");
                int other = open ? 9 : 8;
                // open for a long while, shut for a blink
                Frame[] frames = new Frame[] {
                        new FrameBuilder(getSubImage(row, col), open ? 150 : 10).withScale(tileScale).build(),
                        new FrameBuilder(getSubImage(other / 8, other % 8), open ? 10 : 150).withScale(tileScale).build()
                };
                mapTiles.add(new MapTileBuilder(frames).withTileType(type));
                continue;
            }

            FrameBuilder frame = new FrameBuilder(getSubImage(row, col)).withScale(tileScale);
            if (type == TileType.SPIKES) {
                // just the thorns, not the empty air beside them
                switch (name) {
                    // same hitboxes as the stone/grass spike tiles they replace in the forest
                    case "thorns_up": frame.withBounds(1, 10, 14, 6); break;
                    case "thorns_down": frame.withBounds(1, 0, 14, 6); break;
                    case "thorns_right": frame.withBounds(0, 1, 6, 14); break;
                    default: frame.withBounds(10, 1, 6, 14); break;
                }
            }
            MapTileBuilder builder = new MapTileBuilder(frame.build()).withTileType(type);
            if (name.equals("slope_up")) {
                builder.withTileLayout(SlopeTileLayoutUtils.createLeft45SlopeLayout(spriteWidth, (int) tileScale));
            } else if (name.equals("slope_down")) {
                builder.withTileLayout(SlopeTileLayoutUtils.createRight45SlopeLayout(spriteWidth, (int) tileScale));
            }
            mapTiles.add(builder);
        }
        return mapTiles;
    }
}
