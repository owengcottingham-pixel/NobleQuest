package Maps;

import EnhancedMapTiles.FallingObject;
import Level.EnhancedMapTile;
import Level.Map;
import Tilesets.CommonTileset;

import java.util.ArrayList;

public class NewLevel extends Map {

    public NewLevel() {
        super("new_level.txt", new CommonTileset());
        this.playerStartPosition = getMapTile(3, 194).getLocation();
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        FallingObject fallingObject = new FallingObject(
            getMapTile(19, 192).getX(),
            getMapTile(19, 192).getY()

            
        );
        enhancedMapTiles.add(fallingObject);

        FallingObject secondDripStone = new FallingObject(
            getMapTile(22, 192).getX(),
            getMapTile(22, 192).getY()
        );
        enhancedMapTiles.add(secondDripStone);

        FallingObject TheirdDripStone = new FallingObject(
            getMapTile(45, 181).getX(),
            getMapTile(45, 181).getY()
        );
        enhancedMapTiles.add(TheirdDripStone);


        return enhancedMapTiles;
    } 
    
}
