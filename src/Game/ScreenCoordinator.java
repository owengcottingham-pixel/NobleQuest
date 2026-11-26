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

    private StoryIntroScreen level2IntroScreen;

    private boolean level2IntroPlaying = false;
    private boolean level2IntroShown = false;

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public void initialize() {

        gameState = GameState.MENU;

        level2IntroPlaying = false;
        level2IntroShown = false;
        level2IntroScreen = null;
    }

    @Override
    public void update() {

        // Play the Level 2 story while gameplay is frozen.
        if (level2IntroPlaying) {

            level2IntroScreen.update();

            if (level2IntroScreen.isFinished()) {

                level2IntroPlaying = false;
                level2IntroScreen = null;
            }

            return;
        }

        do {

            if (previousGameState != gameState) {

                switch (gameState) {

                    case MENU:

                        currentScreen = new MenuScreen(this);

                        level2IntroPlaying = false;
                        level2IntroShown = false;
                        level2IntroScreen = null;

                        break;

                    case STORY_INTRO:

                        currentScreen =
                            new StoryIntroScreen(this);

                        break;

                    case LEVEL:

                        currentScreen =
                            new PlayLevelScreen(this);

                        level2IntroShown = false;

                        break;

                    case CREDITS:

                        currentScreen =
                            new CreditsScreen(this);

                        break;
                }

                currentScreen.initialize();
            }

            previousGameState = gameState;

            currentScreen.update();

            // Detect the transition into Level 2.
            if (gameState == GameState.LEVEL
                    && currentScreen instanceof PlayLevelScreen
                    && !level2IntroShown) {

                PlayLevelScreen playScreen =
                    (PlayLevelScreen) currentScreen;

                Object state =
                    playScreen.getPlayLevelScreenState();

                if ("LEVEL_INTRO".equals(String.valueOf(state))) {

                    level2IntroScreen =
                        new StoryIntroScreen(this, true);

                    level2IntroScreen.initialize();

                    level2IntroPlaying = true;
                    level2IntroShown = true;

                    return;
                }
            }

        } while (previousGameState != gameState);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        if (level2IntroPlaying
                && level2IntroScreen != null) {

            level2IntroScreen.draw(graphicsHandler);

        } else {

            currentScreen.draw(graphicsHandler);
        }
    }
}