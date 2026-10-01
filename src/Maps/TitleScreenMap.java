package Maps;

import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Sprite;
import Level.Map;
import Tilesets.CommonTileset;
import Utils.Colors;
import Utils.Point;

// Represents the map that is used as a background for the main menu and credits menu screen
public class TitleScreenMap extends Map {

    private Sprite Knight;

    public TitleScreenMap() {
        super("title_screen_map.txt", new CommonTileset());
        Point KnightLocation = getMapTile(6, 7).getLocation().subtractX(4).subtractY(4);
        Knight = new Sprite(ImageLoader.loadSubImage("Knight.png", Colors.MAGENTA, 0, 0, 124, 84));
        Knight.setScale(1);
        Knight.setLocation(KnightLocation.x, KnightLocation.y);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
        Knight.draw(graphicsHandler);
    }

}
