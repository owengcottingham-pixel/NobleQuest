package MapEditor;

import Level.EditorObjects;

import javax.swing.*;


public class MenuBar extends JMenuBar {
    JMenu options, place;
    JCheckBoxMenuItem showNpcs;
    JCheckBoxMenuItem showEnchancedMapTiles;
    JCheckBoxMenuItem showTriggers;

    public MenuBar(TileBuilder tileBuilder) {
        options = new JMenu("Options");
        showNpcs = new JCheckBoxMenuItem("Show NPCs");
        showNpcs.addActionListener(e -> tileBuilder.setShowNPCs(!tileBuilder.getShowNPCs()));
        options.add(showNpcs);
        showEnchancedMapTiles = new JCheckBoxMenuItem("Show Enhanced Map Tiles");
        showEnchancedMapTiles.addActionListener(e -> tileBuilder.setShowEnhancedMapTiles(!tileBuilder.getShowEnhancedMapTiles()));
        options.add(showEnchancedMapTiles);
        showTriggers = new JCheckBoxMenuItem("Show Enemies");
        showTriggers.addActionListener(e -> tileBuilder.setShowEnemies(!tileBuilder.getShowEnemies()));
        options.add(showTriggers);
        add(options);

        place = new JMenu("Place");
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem tiles = new JRadioButtonMenuItem("Tiles (paint the selected tile)", true);
        tiles.addActionListener(e -> choose(tileBuilder, null, null));
        group.add(tiles);
        place.add(tiles);
        place.addSeparator();
        JRadioButtonMenuItem coin = new JRadioButtonMenuItem("Coin");
        coin.addActionListener(e -> choose(tileBuilder, "COIN", null));
        group.add(coin);
        place.add(coin);
        JRadioButtonMenuItem platform = new JRadioButtonMenuItem("Disappearing Platform");
        platform.addActionListener(e -> choose(tileBuilder, "DISAPPEARING_PLATFORM", null));
        group.add(platform);
        place.add(platform);
        for (String[] kind : EditorObjects.PLACEABLE) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(kind[0]);
            item.addActionListener(e -> choose(tileBuilder, null, kind[1]));
            group.add(item);
            place.add(item);
        }
        place.addSeparator();
        JMenuItem help = new JMenuItem("Right-click a placed object to remove it");
        help.setEnabled(false);
        place.add(help);
        add(place);
    }

    private void choose(TileBuilder tileBuilder, String original, String object) {
        tileBuilder.setPlaceCoin("COIN".equals(original));
        tileBuilder.setPlaceDisappearingPlatform("DISAPPEARING_PLATFORM".equals(original));
        tileBuilder.setPlaceObject(object);
        if ((original != null || object != null) && !tileBuilder.getShowEnhancedMapTiles()) {
            tileBuilder.setShowEnhancedMapTiles(true);
            showEnchancedMapTiles.setSelected(true);
        }
    }
}
