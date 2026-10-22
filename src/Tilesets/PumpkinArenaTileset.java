package Tilesets;

import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import Level.TileType;
import Level.Tileset;

import java.util.ArrayList;

public class PumpkinArenaTileset extends Tileset {

    // index -> name, collision type
    private static final String[] TILES = {
            /*  0 */ "ground_tl", "NOT_PASSABLE",
            /*  1 */ "ground_t", "NOT_PASSABLE",
            /*  2 */ "ground_tr", "NOT_PASSABLE",
            /*  3 */ "ground_t2", "NOT_PASSABLE",
            /*  4 */ "ground_t3", "NOT_PASSABLE",
            /*  5 */ "dirt", "NOT_PASSABLE",
            /*  6 */ "dirt_roots", "NOT_PASSABLE",
            /*  7 */ "dirt_seeds", "NOT_PASSABLE",
            /*  8 */ "ground_l", "NOT_PASSABLE",
            /*  9 */ "dirt_2", "NOT_PASSABLE",
            /* 10 */ "ground_r", "NOT_PASSABLE",
            /* 11 */ "ground_bl", "NOT_PASSABLE",
            /* 12 */ "ground_b", "NOT_PASSABLE",
            /* 13 */ "ground_br", "NOT_PASSABLE",
            /* 14 */ "grass_tufts", "PASSABLE",
            /* 15 */ "glowshrooms", "PASSABLE",
            /* 16 */ "vine_floor_l", "NOT_PASSABLE",
            /* 17 */ "vine_floor", "NOT_PASSABLE",
            /* 18 */ "vine_floor_r", "NOT_PASSABLE",
            /* 19 */ "vine_net", "PASSABLE",
            /* 20 */ "vine_net_end", "PASSABLE",
            /* 21 */ "vplat_l", "JUMP_THROUGH_PLATFORM",
            /* 22 */ "vplat_m", "JUMP_THROUGH_PLATFORM",
            /* 23 */ "vplat_r", "JUMP_THROUGH_PLATFORM",
            /* 24 */ "bramble_tl", "NOT_PASSABLE",
            /* 25 */ "bramble_t", "NOT_PASSABLE",
            /* 26 */ "bramble_tr", "NOT_PASSABLE",
            /* 27 */ "bramble_l", "NOT_PASSABLE",
            /* 28 */ "bramble_c", "NOT_PASSABLE",
            /* 29 */ "bramble_r", "NOT_PASSABLE",
            /* 30 */ "bramble_b", "NOT_PASSABLE",
            /* 31 */ "thorn_spikes", "SPIKES",
            /* 32 */ "gate_top", "NOT_PASSABLE",
            /* 33 */ "gate_mid", "PASSABLE",
            /* 34 */ "gate_bottom", "PASSABLE",
            /* 35 */ "gate_open_top", "NOT_PASSABLE",
            /* 36 */ "pillar_top", "NOT_PASSABLE",
            /* 37 */ "pillar", "NOT_PASSABLE",
            /* 38 */ "fence_h", "PASSABLE",
            /* 39 */ "fence_post", "PASSABLE",
            /* 40 */ "pumpkin", "PASSABLE",
            /* 41 */ "pumpkin_pile", "PASSABLE",
            /* 42 */ "jackolantern_a", "PASSABLE",
            /* 43 */ "jackolantern_b", "PASSABLE",
            /* 44 */ "campfire_a", "PASSABLE",
            /* 45 */ "campfire_b", "PASSABLE",
            /* 46 */ "sign_arena", "PASSABLE",
            /* 47 */ "banner", "PASSABLE",
            /* 48 */ "stone", "PASSABLE",
            /* 49 */ "stump", "PASSABLE",
            /* 50 */ "lantern_post", "PASSABLE",
            /* 51 */ "wisp_a", "PASSABLE",
            /* 52 */ "wisp_b", "PASSABLE",
            /* 53 */ "hanging_vines", "PASSABLE",
            /* 54 */ "chain", "PASSABLE",
            /* 55 */ "pumpkin_big_lit", "PASSABLE",
            /* 56 */ "tree_top_l", "PASSABLE",
            /* 57 */ "tree_top_r", "PASSABLE",
            /* 58 */ "bush_tl", "PASSABLE",
            /* 59 */ "bush_tr", "PASSABLE",
            /* 60 */ "bg_dark", "PASSABLE",
            /* 61 */ "bg_dark2", "PASSABLE",
            /* 62 */ "bg_eyes", "PASSABLE",
            /* 63 */ "bg_canopy", "NOT_PASSABLE",   // the arena's roof, so you can't jump out the top
            /* 64 */ "tree_mid_l", "PASSABLE",
            /* 65 */ "tree_mid_r", "PASSABLE",
            /* 66 */ "bush_bl", "PASSABLE",
            /* 67 */ "bush_br", "PASSABLE",
            /* 68 */ "bg_trunk", "PASSABLE",
            /* 69 */ "bg_trunk_base", "PASSABLE",
            /* 70 */ "bg_mist", "PASSABLE",
            /* 71 */ "bg_lanterns", "PASSABLE",
            /* 72 */ "tree_base_l", "PASSABLE",
            /* 73 */ "tree_base_r", "PASSABLE",
            /* 74 */ "ground_inner_l", "NOT_PASSABLE",
            /* 75 */ "ground_inner_r", "NOT_PASSABLE",
            /* 76 */ "pit", "PASSABLE",
            /* 77 */ "bg_eyes2", "PASSABLE",
            /* 78 */ "bg_ghost", "PASSABLE",
            /* 79 */ "bg_mist2", "PASSABLE",
    };

    public PumpkinArenaTileset() {
        super(ImageLoader.load("PumpkinArenaTileset.png"), 16, 16, 3);
    }

    @Override
    public ArrayList<MapTileBuilder> defineTiles() {
        ArrayList<MapTileBuilder> mapTiles = new ArrayList<>();
        for (int i = 0; i < TILES.length / 2; i++) {
            String name = TILES[i * 2];
            TileType type = TileType.valueOf(TILES[i * 2 + 1]);
            int row = i / 8, col = i % 8;

            // 2-frame animated tiles: jack-o'-lanterns flicker, campfire burns, wisps bob
            int partner = animationPartner(name);
            if (partner >= 0) {
                Frame[] frames = new Frame[] {
                        new FrameBuilder(getSubImage(row, col), 18).withScale(tileScale).build(),
                        new FrameBuilder(getSubImage(partner / 8, partner % 8), 18).withScale(tileScale).build()
                };
                mapTiles.add(new MapTileBuilder(frames).withTileType(type));
                continue;
            }

            FrameBuilder frame = new FrameBuilder(getSubImage(row, col)).withScale(tileScale);
            if (type == TileType.JUMP_THROUGH_PLATFORM) {
                frame.withBounds(0, 0, 16, 5);        // only the rope band on top is standable
            } else if (type == TileType.SPIKES) {
                frame.withBounds(1, 6, 14, 10);       // the thorns, not the empty air above them
            }
            mapTiles.add(new MapTileBuilder(frame.build()).withTileType(type));
        }
        return mapTiles;
    }

    // the other frame of a 2-frame animated tile, or -1
    private int animationPartner(String name) {
        switch (name) {
            case "jackolantern_a": return 43;
            case "jackolantern_b": return 42;
            case "campfire_a": return 45;
            case "campfire_b": return 44;
            case "wisp_a": return 52;
            case "wisp_b": return 51;
            default: return -1;
        }
    }
}
