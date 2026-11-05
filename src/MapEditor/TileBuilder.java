package MapEditor;

import Engine.GraphicsHandler;
import EnhancedMapTiles.Coin;
import EnhancedMapTiles.DisappearingPlatform;
import GameObject.Rectangle;
import Level.*;
import Utils.Colors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;

public class TileBuilder extends JPanel {
    private Map map;
    private MapTile hoveredMapTile;
    private SelectedTileIndexHolder controlPanelHolder;
    private GraphicsHandler graphicsHandler = new GraphicsHandler();
    private JLabel hoveredTileIndexLabel;
    private boolean showNPCs;
    private boolean showEnhancedMapTiles;
    private boolean showEnemies;
    private boolean placeDisappearingPlatform;
    private boolean placeCoin;
    private String placeObject;     // one of Level.EditorObjects.PLACEABLE's keywords, or null

    public TileBuilder(SelectedTileIndexHolder controlPanelHolder, JLabel hoveredTileIndexLabel) {
        setBackground(Colors.MAGENTA);
        setLocation(0, 0);
        setPreferredSize(new Dimension(585, 562));
        this.controlPanelHolder = controlPanelHolder;
        this.hoveredTileIndexLabel = hoveredTileIndexLabel;
        addMouseListener(new MouseListener() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredMapTile = null;
                hoveredTileIndexLabel.setText("");
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    removeObjectAt(e.getPoint());      // right-click: take away a placed object
                    return;
                }
                tileSelected(e.getPoint());
            }

            @Override
            public void mouseClicked(MouseEvent e) { }

            @Override
            public void mouseReleased(MouseEvent e) { }

            @Override
            public void mouseEntered(MouseEvent e) { }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                tileHovered(e.getPoint());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                tileHovered(e.getPoint());
                if (!SwingUtilities.isRightMouseButton(e)) {
                    tileSelected(e.getPoint());
                }
            }
        });
    }

    public void setMap(Map map) {
        this.map = map;
        setPreferredSize(new Dimension(map.getWidthPixels(), map.getHeightPixels()));
        repaint();
    }

    public void draw() {
        for (MapTile tile : map.getMapTiles()) {
            tile.draw(graphicsHandler);
        }

        if (showEnhancedMapTiles) {
            for (EnhancedMapTile enhancedMapTile : map.getEnhancedMapTiles()) {
                enhancedMapTile.draw(graphicsHandler);
            }
        }

        if (showNPCs) {
            for (NPC npc : map.getNPCs()) {
                npc.draw(graphicsHandler);
            }
        }

        if (showEnemies) {
            for (Enemy enemy : map.getEnemies()) {
                enemy.draw(graphicsHandler);
            }
        }

        if (hoveredMapTile != null) {
            graphicsHandler.drawRectangle(
                    Math.round(hoveredMapTile.getX()) + 2,
                    Math.round(hoveredMapTile.getY()) + 2,
                    hoveredMapTile.getWidth() - 5,
                    hoveredMapTile.getHeight() - 5,
                    Color.YELLOW,
                    5
            );
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        graphicsHandler.setGraphics((Graphics2D) g);
        draw();
    }

    public void tileSelected(Point selectedPoint) {
        int selectedTileIndex = getSelectedTileIndex(selectedPoint);
        if (selectedTileIndex == -1) {
            return;
        }

        MapTile selectedMapTile = map.getMapTiles()[selectedTileIndex];

        if (placeDisappearingPlatform) {
            // Avoid placing duplicates on the same tile.
            if (!hasDisappearingPlatformAt(selectedMapTile.getX(), selectedMapTile.getY())) {
                DisappearingPlatform platform = new DisappearingPlatform(
                    selectedMapTile.getX(),
                    selectedMapTile.getY(),
                    3,
                    new Rectangle(0, 6, 16, 4)
                );
                map.addEditorEnhancedMapTile(platform);
            }
            repaint();
            return;
        }

        if (placeCoin) {
            if (!hasCoinAt(selectedMapTile.getX(), selectedMapTile.getY())) {
                map.addEditorEnhancedMapTile(new Coin(selectedMapTile.getX(), selectedMapTile.getY()));
            }
            repaint();
            return;
        }

        if (placeObject != null) {
            int tileX = Math.round(selectedMapTile.getX()) / map.getTileset().getScaledSpriteWidth();
            int tileY = Math.round(selectedMapTile.getY()) / map.getTileset().getScaledSpriteHeight();
            if (!hasObjectAt(placeObject, tileX, tileY)) {
                EnhancedMapTile object = EditorObjects.place(placeObject, tileX, tileY, map);
                if (object != null) {
                    map.addEditorEnhancedMapTile(object);
                }
            }
            repaint();
            return;
        }

        MapTile newMapTile = map.getTileset().getTile(controlPanelHolder.getSelectedTileIndex())
            .build(selectedMapTile.getX(), selectedMapTile.getY());
        newMapTile.setMap(map);
        map.getMapTiles()[selectedTileIndex] = newMapTile;
        repaint();
    }

    private boolean hasDisappearingPlatformAt(float x, float y) {
        for (EnhancedMapTile tile : map.getEditorEnhancedMapTiles()) {
            if (tile instanceof DisappearingPlatform
                    && Math.round(tile.getX()) == Math.round(x)
                    && Math.round(tile.getY()) == Math.round(y)) {
                return true;
            }
        }
        return false;
    }

    // is there already one of these (same keyword) placed at this tile?
    private boolean hasObjectAt(String what, int tileX, int tileY) {
        String keyword = what.split(" ")[0];
        for (EnhancedMapTile tile : map.getEditorEnhancedMapTiles()) {
            if (tile instanceof EditorObject) {
                String[] line = ((EditorObject) tile).editorLine().split(" ");
                if (line[0].equals(keyword) && (int) Math.floor(Float.parseFloat(line[1])) == tileX
                        && (int) Math.floor(Float.parseFloat(line[2])) == tileY) {
                    return true;
                }
            }
        }
        return false;
    }

    // right-click: removes the placed object (spikes, spring, coin...) under the mouse
    private void removeObjectAt(Point point) {
        java.util.ArrayList<EnhancedMapTile> placed = map.getEditorEnhancedMapTiles();
        for (int i = placed.size() - 1; i >= 0; i--) {
            EnhancedMapTile tile = placed.get(i);
            Rectangle b = tile.getBounds();
            boolean under = point.x >= Math.min(b.getX1(), tile.getX()) - 4 && point.x <= Math.max(b.getX2(), tile.getX() + 48) + 4
                    && point.y >= Math.min(b.getY1(), tile.getY()) - 4 && point.y <= Math.max(b.getY2(), tile.getY() + 48) + 4;
            if (!under && tile instanceof EditorObject) {
                // or its home tile (a crusher or a sinking pillar may be drawn partway through moving)
                String[] line = ((EditorObject) tile).editorLine().split(" ");
                int tileX = point.x / map.getTileset().getScaledSpriteWidth(), tileY = point.y / map.getTileset().getScaledSpriteHeight();
                under = (int) Math.floor(Float.parseFloat(line[1])) == tileX && (int) Math.floor(Float.parseFloat(line[2])) == tileY;
            }
            if (under) {
                map.removeEditorEnhancedMapTile(tile);
                repaint();
                return;
            }
        }
    }

    public void tileHovered(Point hoveredPoint) {
        this.hoveredMapTile = getHoveredTile(hoveredPoint);
        if (this.hoveredMapTile != null) {
            int hoveredIndexX = Math.round(this.hoveredMapTile.getX()) / map.getTileset().getScaledSpriteWidth();
            int hoveredIndexY = Math.round(this.hoveredMapTile.getY()) / map.getTileset().getScaledSpriteHeight();
            hoveredTileIndexLabel.setText("X: " + hoveredIndexX + ", Y: " + hoveredIndexY);
            repaint();
        }
    }

    private boolean hasCoinAt(float x, float y) {
        for (EnhancedMapTile tile : map.getEditorEnhancedMapTiles()) {
            if (tile instanceof Coin
                    && Math.round(tile.getX()) == Math.round(x)
                    && Math.round(tile.getY()) == Math.round(y)) {
                return true;
            }
        }
        return false;
    }

    protected MapTile getHoveredTile(Point mousePoint) {
        for (MapTile mapTile : map.getMapTiles()) {
            if (isPointInTile(mousePoint, mapTile)) {
                return mapTile;
            }
        }
        return null;
    }

    protected int getSelectedTileIndex(Point mousePoint) {
        MapTile[] mapTiles = map.getMapTiles();
        for (int i = 0; i < mapTiles.length; i++) {
            if (isPointInTile(mousePoint, mapTiles[i])) {
                return i;
            }
        }
        return -1;
    }

    protected boolean isPointInTile(Point point, MapTile tile) {
        return (point.x >= tile.getX() && point.x <= tile.getX() + tile.getWidth() &&
                point.y >= tile.getY() && point.y <= tile.getY() + tile.getHeight());
    }

    public boolean getShowNPCs() {
        return showNPCs;
    }

    public void setShowNPCs(boolean showNPCs) {
        this.showNPCs = showNPCs;
        repaint();
    }

    public boolean getShowEnhancedMapTiles() {
        return showEnhancedMapTiles;
    }

    public void setShowEnhancedMapTiles(boolean showEnhancedMapTiles) {
        this.showEnhancedMapTiles = showEnhancedMapTiles;
        repaint();
    }


    public boolean getPlaceDisappearingPlatform() {
        return placeDisappearingPlatform;
    }

    public void setPlaceDisappearingPlatform(boolean placeDisappearingPlatform) {
        this.placeDisappearingPlatform = placeDisappearingPlatform;
        updatePlacementCursor();
    }

    public void setPlaceObject(String what) {
        this.placeObject = what;
        updatePlacementCursor();
    }

    public boolean getPlaceCoin() {
        return placeCoin;
    }

    public void setPlaceCoin(boolean placeCoin) {
        this.placeCoin = placeCoin;
        updatePlacementCursor();
    }

    private void updatePlacementCursor() {
        setCursor(new Cursor(placeDisappearingPlatform || placeCoin || placeObject != null
            ? Cursor.CROSSHAIR_CURSOR
            : Cursor.DEFAULT_CURSOR));
    }

    public boolean getShowEnemies() {
        return showEnemies;
    }

    public void setShowEnemies(boolean showEnemies) {
        this.showEnemies = showEnemies;
        repaint();
    }
}
