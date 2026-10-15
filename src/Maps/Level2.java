package Maps;

import Level.Map;
import Tilesets.CommonTileset;

public class Level2 extends Map {

    public Level2() {
        super("Level2.txt", new CommonTileset());
        this.playerStartPosition = getMapTile(1, 11).getLocation();
    }
}
