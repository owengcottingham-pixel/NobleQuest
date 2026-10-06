package MapEditor;

import Level.Map;
import Maps.Level1;
import Maps.NewLevel;
import Maps.TestMap;
import Maps.TitleScreenMap;

import java.util.ArrayList;

public class EditorMaps {
    public static ArrayList<String> getMapNames() {
        return new ArrayList<String>() {{
            add("TestMap");
            add("Level1");
            add("TitleScreen");
            add("NewLevel");
        }};
    }

    public static Map getMapByName(String mapName) {
        switch(mapName) {
            case "TestMap":
                return new TestMap();
            case "Level1":
                return new Level1();
            case "TitleScreen":
                return new TitleScreenMap();
            case "NewLevel":
                return new NewLevel();
            default:
                throw new RuntimeException("Unrecognized map name");
        }
    }
}
