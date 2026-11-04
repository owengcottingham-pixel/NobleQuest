
package Game;

import Engine.DefaultScreen;
import Engine.GraphicsHandler;
import Engine.Screen;

import Screens.CreditsScreen;
import Screens.MenuScreen;
import Screens.PlayLevelScreen;
import Screens.StoryIntroScreen;

public class ScreenCoordinator extends Screen {

    protected Screen currentScreen = new DefaultScreen();

    protected GameState gameState;
    protected GameState previousGameState;

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public void initialize() {
        gameState = GameState.MENU;
    }

    @Override
    public void update() {

        do {

            if (previousGameState != gameState) {

                switch (gameState) {

                    case MENU:
                        currentScreen = new MenuScreen(this);
                        break;

                    case STORY_INTRO:
                        currentScreen = (Screen) new StoryIntroScreen(this);
                        break;

                    case LEVEL:
                        currentScreen = new PlayLevelScreen(this);
                        break;

                    case CREDITS:
                        currentScreen = new CreditsScreen(this);
                        break;
                }

                currentScreen.initialize();
            }

            previousGameState = gameState;

            currentScreen.update();

        } while (previousGameState != gameState);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        currentScreen.draw(graphicsHandler);
    }
}
