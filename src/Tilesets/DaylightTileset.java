package Tilesets;

import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import Level.TileType;
import Level.Tileset;

import java.util.ArrayList;


public class DaylightTileset extends Tileset {

    // index -> name, collision type
    private static final String[] TILES = {
            // grass-topped sandstone (the meadow)
            /*  0 */ "top_l", "NOT_PASSABLE",
            /*  1 */ "top", "NOT_PASSABLE",
            /*  2 */ "top_r", "NOT_PASSABLE",
            /*  3 */ "top_single", "NOT_PASSABLE",     // top of a 1-wide column
            /*  4 */ "face_l", "NOT_PASSABLE",
            /*  5 */ "rock", "NOT_PASSABLE",
            /*  6 */ "rock_crack", "NOT_PASSABLE",
            /*  7 */ "face_r", "NOT_PASSABLE",
            /*  8 */ "bottom_l", "NOT_PASSABLE",
            /*  9 */ "bottom", "NOT_PASSABLE",
            /* 10 */ "bottom_r", "NOT_PASSABLE",
            /* 11 */ "column", "NOT_PASSABLE",         // middle of a 1-wide column
            /* 12 */ "column_bottom", "NOT_PASSABLE",
            /* 13 */ "rock_moss", "NOT_PASSABLE",
            /* 14 */ "back_wall", "PASSABLE",          // darker sandstone you can walk in front of
            /* 15 */ "top_2", "NOT_PASSABLE",
            // hard jagged slate (the spike area)
            /* 16 */ "k_top_l", "NOT_PASSABLE",
            /* 17 */ "k_top", "NOT_PASSABLE",
            /* 18 */ "k_top_r", "NOT_PASSABLE",
            /* 19 */ "k_top_single", "NOT_PASSABLE",
            /* 20 */ "k_face_l", "NOT_PASSABLE",
            /* 21 */ "k_rock", "NOT_PASSABLE",
            /* 22 */ "k_rock_crack", "NOT_PASSABLE",
            /* 23 */ "k_face_r", "NOT_PASSABLE",
            /* 24 */ "k_bottom_l", "NOT_PASSABLE",
            /* 25 */ "k_bottom", "NOT_PASSABLE",
            /* 26 */ "k_bottom_r", "NOT_PASSABLE",
            /* 27 */ "k_column", "NOT_PASSABLE",
            /* 28 */ "k_column_bottom", "NOT_PASSABLE",
            /* 29 */ "k_rock_2", "NOT_PASSABLE",
            /* 30 */ "k_back_wall", "PASSABLE",        // the dark rock behind you inside the chute
            /* 31 */ "k_top_2", "NOT_PASSABLE",
            // meadow decoration -- see-through, you walk in front of it
            /* 32 */ "bush_l", "PASSABLE",
            /* 33 */ "bush_r", "PASSABLE",
            /* 34 */ "tall_grass", "PASSABLE",
            /* 35 */ "fence_l", "PASSABLE",
            /* 36 */ "fence_m", "PASSABLE",
            /* 37 */ "fence_r", "PASSABLE",
            /* 38 */ "sign_arrow", "PASSABLE",
            /* 39 */ "rock_small", "PASSABLE",
            /* 40 */ "tree_tl", "PASSABLE",            // a round oak, 2 wide x 3 tall: 40 41 / 42 43 / 44 45
            /* 41 */ "tree_tr", "PASSABLE",
            /* 42 */ "tree_ml", "PASSABLE",
            /* 43 */ "tree_mr", "PASSABLE",
            /* 44 */ "tree_bl", "PASSABLE",
            /* 45 */ "tree_br", "PASSABLE",
            /* 46 */ "mushroom", "PASSABLE",
            /* 47 */ "sign_spring", "PASSABLE",
            // smooth slate, where spikes sit on top of it or hang underneath it
            /* 48 */ "k_top_flat", "NOT_PASSABLE",
            /* 49 */ "k_top_l_flat", "NOT_PASSABLE",
            /* 50 */ "k_top_r_flat", "NOT_PASSABLE",
            /* 51 */ "k_bottom_flat", "NOT_PASSABLE",
            /* 52 */ "k_bottom_l_flat", "NOT_PASSABLE",
            /* 53 */ "k_bottom_r_flat", "NOT_PASSABLE",
            // the reflection world under the lake: you stand on the underside of the water
            /* 54 */ "mirror_surface", "NOT_PASSABLE",
            /* 55 */ "mirror_water", "NOT_PASSABLE",
            /* 56 */ "kelp", "NOT_PASSABLE",           // a wall of kelp, only in the reflection
    };

    public DaylightTileset() {
        super(ImageLoader.load("DaylightTileset.png"), 16, 16, 3);
    }

    @Override
    public ArrayList<MapTileBuilder> defineTiles() {
        ArrayList<MapTileBuilder> mapTiles = new ArrayList<>();
        for (int i = 0; i < TILES.length / 2; i++) {
            TileType type = TileType.valueOf(TILES[i * 2 + 1]);
            FrameBuilder frame = new FrameBuilder(getSubImage(i / 8, i % 8)).withScale(tileScale);
            mapTiles.add(new MapTileBuilder(frame.build()).withTileType(type));
        }
        return mapTiles;
    }
}
