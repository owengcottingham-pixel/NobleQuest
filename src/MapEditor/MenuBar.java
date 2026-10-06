package MapEditor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuBar extends JMenuBar {
    JMenu options;
    JCheckBoxMenuItem showNpcs;
    JCheckBoxMenuItem showEnchancedMapTiles;
    JCheckBoxMenuItem showTriggers;
    JCheckBoxMenuItem placeDisappearingPlatform;

    public MenuBar(TileBuilder tileBuilder) {
        options = new JMenu("Options");
        showNpcs = new JCheckBoxMenuItem("Show NPCs");
        showNpcs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tileBuilder.setShowNPCs(!tileBuilder.getShowNPCs());
            }
        });
        options.add(showNpcs);
        showEnchancedMapTiles = new JCheckBoxMenuItem("Show Enhanced Map Tiles");
        showEnchancedMapTiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tileBuilder.setShowEnhancedMapTiles(!tileBuilder.getShowEnhancedMapTiles());
            }
        });
        options.add(showEnchancedMapTiles);

        placeDisappearingPlatform = new JCheckBoxMenuItem("Place Disappearing Platform");
        placeDisappearingPlatform.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tileBuilder.setPlaceDisappearingPlatform(placeDisappearingPlatform.isSelected());
                if (placeDisappearingPlatform.isSelected() && !tileBuilder.getShowEnhancedMapTiles()) {
                    tileBuilder.setShowEnhancedMapTiles(true);
                    showEnchancedMapTiles.setSelected(true);
                }
            }
        });
        options.add(placeDisappearingPlatform);

        showTriggers = new JCheckBoxMenuItem("Show Enemies");
        showTriggers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tileBuilder.setShowEnemies(!tileBuilder.getShowEnemies());
            }
        });
        options.add(showTriggers);
        add(options);
    }
}
