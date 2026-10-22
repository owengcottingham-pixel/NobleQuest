package Screens;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import Engine.Config;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import Engine.ScreenManager;
import java.awt.image.BufferedImage;

import Engine.GraphicsHandler;
import Engine.Screen;
import Engine.ImageLoader;
import Game.GameState;
import Game.ScreenCoordinator;
import GameObject.SpriteSheet;
import Level.Map;
import Level.NPC;
import NPCs.DwarfShopkeeper;
import Level.Player;
import Level.PlayerListener;
import Maps.LavaBossLevel;
import Maps.Level1;
import Maps.Level2;
import Maps.TestMap;
import Maps.Level1;
import Players.Knight;
import NPCs.WanderingMonk;
import Utils.Point;
import Utils.NumberDisplay;

public class PlayLevelScreen extends Screen implements PlayerListener {

    // level-complete transition
    private static final int FADE_OUT_TIME = 60;
    private static final int LEVEL_TRANSITION_TIME = 180;

    private static final int INTRO_HOLD_TIME = 90;
    private static final int INTRO_FADE_TIME = 60;

    // pause menu
    private static final String[] PAUSE_OPTIONS = { "Resume", "Restart Level", "Main Menu" };
    private int pauseSelection = 0;
    private final KeyLocker keyLocker = new KeyLocker();
    private boolean showIntroOnInit = false;
    private int introTimer;

    protected ScreenCoordinator screenCoordinator;
    protected int currentLevel = 1;
    protected Map map;
    protected Player player;
    protected PlayLevelScreenState playLevelScreenState;
    protected int screenTimer;
    protected LevelClearedScreen levelClearedScreen;
    protected LevelLoseScreen levelLoseScreen;
    protected boolean levelCompletedStateChangeStart;
    protected NumberDisplay coinDisplay;
    protected BufferedImage coinIcon;

    // coin counter: a fixed 5-slot field pinned to the top right corner.
    // reads the real window size, so it stays in the corner if the window changes again.
    private static final int COIN_SLOTS = 5;
    private static final int HUD_MARGIN = 20;
    private static final int COIN_ICON_SIZE = 28;
    private static final int COIN_ICON_GAP = 10;
    private static final int HUD_TOP = 14;

    public PlayLevelScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        // define/setup map
        this.map = createCurrentMap();

        
        Point spawn = map.getPlayerStartPosition();
        if (WanderingMonk.hasCheckpoint(map.getClass().getSimpleName())) {
            spawn = WanderingMonk.getCheckpoint();
        }
        player = new Knight(spawn.x, spawn.y);

        player.setMap(map);
        player.addListener(this);

        // Set up the coin display.
        coinIcon = ImageLoader.load("Coin.png");

        coinDisplay = new NumberDisplay(
            new SpriteSheet(ImageLoader.load("Digits.png"), 8, 10),
            3
        );

        // Set up the level completion and death screens.
        levelClearedScreen = new LevelClearedScreen();
        levelLoseScreen = new LevelLoseScreen(this);

        dwarf = findDwarf();          // null on levels without a stall
        shopMenu.close();             // never carry an open menu across a reload


        levelCompletedStateChangeStart = false;
        screenTimer = 0;
        pauseSelection = 0;

        if (showIntroOnInit) {
            playLevelScreenState = PlayLevelScreenState.LEVEL_INTRO;
            introTimer = INTRO_HOLD_TIME + INTRO_FADE_TIME;
            showIntroOnInit = false;
        } else {
            playLevelScreenState = PlayLevelScreenState.RUNNING;
        }
    }

    private boolean showControls = true;
    private boolean hWasDown = false;

    private static final String[][] CONTROLS = {
            { "A / D", "Move" },
            { "W", "Jump" },
            { "S", "Crouch" },
            { "J", "Sword" },
            { "Left Click", "Crossbow (aim with mouse)" },
            { "Shift", "Dash" },
            { "Space", "Talk" },
            { "E", "Shop / Wishing Well" },
            { "Esc", "Pause" },
            { "H", "Hide controls" }
    };
    private static final Font CONTROLS_TITLE_FONT = new Font("Serif", Font.BOLD, 13);
    private static final Font CONTROLS_FONT = new Font("Arial", Font.BOLD, 11);

        private final ShopMenu shopMenu = new ShopMenu();
        private DwarfShopkeeper dwarf;

    private Map createCurrentMap() {
        switch (currentLevel) {
            case 1:
                return new Level1();
            case 2:
                return new Level2();
            case 3:
                return new LavaBossLevel();
            default:
                throw new IllegalStateException("Unknown level: " + currentLevel);
        }
    }

    // The stall's shopkeeper, if this level has one. Looked up once per level
    // rather than every frame.
    private DwarfShopkeeper findDwarf() {
        for (NPC npc : map.getNPCs()) {
            if (npc instanceof DwarfShopkeeper) {
                return (DwarfShopkeeper) npc;
            }
        }
        return null;
    }

    @Override
    public void update() {
        switch (playLevelScreenState) {

            case RUNNING:
                updateKeyLocks();
                if (Keyboard.isKeyDown(Key.H)) {
                    if (!hWasDown) {
                        showControls = !showControls;
                    }
                    hWasDown = true;
                } else {
                    hWasDown = false;
                }

                if (shopMenu.isOpen()) {
                    // the world is frozen while trading -- no player, no map, so
                    // you cannot walk off and leave the menu hanging in mid air
                    shopMenu.update(player);
                    if (dwarf != null) {
                        dwarf.setShopOpen(shopMenu.isOpen());
                    }
                    keyLocker.lockKey(Key.ESC);
                    break;
                }

                if (Keyboard.isKeyDown(Key.ESC) && !keyLocker.isKeyLocked(Key.ESC)) {
                    openPauseMenu();
                    break;
                }

                player.update();
                map.update(player);

                if (dwarf != null && dwarf.consumeShopRequest()) {
                    shopMenu.open();
                    dwarf.setShopOpen(true);
                }
                break;

            case PAUSED:
                updatePauseMenu();
                break;

            case LEVEL_INTRO:
                introTimer--;
                if (introTimer <= 0) {
                    playLevelScreenState = PlayLevelScreenState.RUNNING;
                }
                break;

            case LEVEL_COMPLETED:
                if (levelCompletedStateChangeStart) {
                    screenTimer = LEVEL_TRANSITION_TIME;
                    levelCompletedStateChangeStart = false;
                } else {
                    levelClearedScreen.update();
                    screenTimer--;

                    if (screenTimer <= 0) {
                        if (currentLevel == 1) {
                            // Level 1 is complete. Start Level 2.
                            currentLevel = 2;
                            WanderingMonk.clearCheckpoint();
                            showIntroOnInit = true;
                            initialize();
                        } else if (currentLevel == 2) {
                            // Level 2 is complete. Start the Lava Boss level.
                            currentLevel = 3;
                            WanderingMonk.clearCheckpoint();
                            showIntroOnInit = true;
                            initialize();
                        } else {
                            // The Lava Boss level is complete. Return to the menu.
                            goBackToMenu();
                        }
                    }
                }
                break;

            case LEVEL_LOSE:
                levelLoseScreen.update();
                break;
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        switch (playLevelScreenState) {

            case RUNNING:
                map.draw(graphicsHandler);
                player.draw(graphicsHandler);
                drawCoinCounter(graphicsHandler);
                drawDashBar(graphicsHandler);
                drawHealthBar(graphicsHandler);
                drawControls(graphicsHandler);
                shopMenu.draw(graphicsHandler, player);
                break;

            case PAUSED:
                map.draw(graphicsHandler);
                player.draw(graphicsHandler);
                drawCoinCounter(graphicsHandler);
                drawDashBar(graphicsHandler);
                drawHealthBar(graphicsHandler);
                drawControls(graphicsHandler);
                drawPauseMenu(graphicsHandler);
                break;

            case LEVEL_INTRO:
                drawLevelIntro(graphicsHandler);
                break;

            case LEVEL_COMPLETED:
                drawLevelCompleted(graphicsHandler);
                break;

            case LEVEL_LOSE:
                levelLoseScreen.draw(graphicsHandler);
                break;
        }
    }

    // pause menu

    private void updateKeyLocks() {
        if (Keyboard.isKeyUp(Key.ESC)) keyLocker.unlockKey(Key.ESC);
        if (Keyboard.isKeyUp(Key.ENTER)) keyLocker.unlockKey(Key.ENTER);
        if (Keyboard.isKeyUp(Key.UP)) keyLocker.unlockKey(Key.UP);
        if (Keyboard.isKeyUp(Key.DOWN)) keyLocker.unlockKey(Key.DOWN);
        if (Keyboard.isKeyUp(Key.W)) keyLocker.unlockKey(Key.W);
        if (Keyboard.isKeyUp(Key.S)) keyLocker.unlockKey(Key.S);
    }

    private void openPauseMenu() {
        playLevelScreenState = PlayLevelScreenState.PAUSED;
        pauseSelection = 0;
        keyLocker.lockKey(Key.ESC);
        keyLocker.lockKey(Key.ENTER);
        keyLocker.lockKey(Key.UP);
        keyLocker.lockKey(Key.DOWN);
        keyLocker.lockKey(Key.W);
        keyLocker.lockKey(Key.S);
    }

    private void resumeFromPause() {
        playLevelScreenState = PlayLevelScreenState.RUNNING;
        keyLocker.lockKey(Key.ESC);
    }

    private void updatePauseMenu() {
        updateKeyLocks();

        boolean up = (Keyboard.isKeyDown(Key.UP) && !keyLocker.isKeyLocked(Key.UP))
                  || (Keyboard.isKeyDown(Key.W) && !keyLocker.isKeyLocked(Key.W));
        boolean down = (Keyboard.isKeyDown(Key.DOWN) && !keyLocker.isKeyLocked(Key.DOWN))
                    || (Keyboard.isKeyDown(Key.S) && !keyLocker.isKeyLocked(Key.S));

        if (up) {
            pauseSelection = (pauseSelection + PAUSE_OPTIONS.length - 1) % PAUSE_OPTIONS.length;
            keyLocker.lockKey(Key.UP);
            keyLocker.lockKey(Key.W);
        } else if (down) {
            pauseSelection = (pauseSelection + 1) % PAUSE_OPTIONS.length;
            keyLocker.lockKey(Key.DOWN);
            keyLocker.lockKey(Key.S);
        }

        if (Keyboard.isKeyDown(Key.ESC) && !keyLocker.isKeyLocked(Key.ESC)) {
            resumeFromPause();
        } else if (Keyboard.isKeyDown(Key.ENTER) && !keyLocker.isKeyLocked(Key.ENTER)) {
            keyLocker.lockKey(Key.ENTER);
            switch (pauseSelection) {
                case 0:
                    resumeFromPause();
                    break;
                case 1:
                    WanderingMonk.clearCheckpoint();
                    resetLevel();
                    break;
                case 2:
                    goBackToMenu();
                    break;
            }
        }
    }

    private void drawPauseMenu(GraphicsHandler graphicsHandler) {
        int w = ScreenManager.getScreenWidth();
        int h = ScreenManager.getScreenHeight();

        // dim the frozen game
        graphicsHandler.drawFilledRectangle(0, 0, w, h, new Color(0, 0, 0, 170));

        // panel
        int panelW = 420;
        int panelH = 330;
        int panelX = (w - panelW) / 2;
        int panelY = (h - panelH) / 2;
        graphicsHandler.drawFilledRectangleWithBorder(
            panelX, panelY, panelW, panelH,
            new Color(25, 25, 35, 235), Color.white, 3
        );

        drawCenteredText(graphicsHandler, "PAUSED", panelY + 30,
            new Font("Arial", Font.BOLD, 44), Color.white);

        Font optionFont = new Font("Arial", Font.PLAIN, 28);
        int optionY = panelY + 120;
        for (int i = 0; i < PAUSE_OPTIONS.length; i++) {
            boolean selected = (i == pauseSelection);
            String label = selected ? "> " + PAUSE_OPTIONS[i] + " <" : PAUSE_OPTIONS[i];
            drawCenteredText(graphicsHandler, label, optionY + i * 50, optionFont,
                selected ? new Color(255, 215, 80) : new Color(200, 200, 200));
        }

        drawCenteredText(graphicsHandler, "Up/Down to select   Enter to confirm   Esc to resume",
            panelY + panelH - 40, new Font("Arial", Font.PLAIN, 14), new Color(170, 170, 170));
    }

    // level transition

    private void drawLevelCompleted(GraphicsHandler graphicsHandler) {
        int w = ScreenManager.getScreenWidth();
        int h = ScreenManager.getScreenHeight();
        int elapsed = LEVEL_TRANSITION_TIME - screenTimer;

        if (elapsed < FADE_OUT_TIME) {
            // keep showing the finished level while it fades to black
            map.draw(graphicsHandler);
            player.draw(graphicsHandler);
            int alpha = Math.min(255, elapsed * 255 / FADE_OUT_TIME);
            graphicsHandler.drawFilledRectangle(0, 0, w, h, new Color(0, 0, 0, alpha));
        } else {
            graphicsHandler.drawFilledRectangle(0, 0, w, h, Color.black);

            // fade the text in over the first half second of the black screen
            int textAlpha = Math.min(255, (elapsed - FADE_OUT_TIME) * 255 / 30);
            drawCenteredText(graphicsHandler, "Level " + currentLevel + " Cleared!", h / 2 - 40,
                new Font("Arial", Font.BOLD, 44), new Color(255, 255, 255, textAlpha));
            if (currentLevel == 1) {
                drawCenteredText(graphicsHandler, "Get ready for Level 2...", h / 2 + 30,
                    new Font("Arial", Font.PLAIN, 22), new Color(200, 200, 200, textAlpha));
            }
        }
    }

    private void drawLevelIntro(GraphicsHandler graphicsHandler) {
        int w = ScreenManager.getScreenWidth();
        int h = ScreenManager.getScreenHeight();

        // the new level is already loaded underneath the title card
        map.draw(graphicsHandler);
        player.draw(graphicsHandler);
        drawCoinCounter(graphicsHandler);
        drawDashBar(graphicsHandler);
        drawHealthBar(graphicsHandler);

        int elapsed = INTRO_HOLD_TIME + INTRO_FADE_TIME - introTimer;
        int alpha = 255;
        if (elapsed > INTRO_HOLD_TIME) {
            alpha = 255 - (elapsed - INTRO_HOLD_TIME) * 255 / INTRO_FADE_TIME;
        }
        alpha = Math.max(0, Math.min(255, alpha));

        graphicsHandler.drawFilledRectangle(0, 0, w, h, new Color(0, 0, 0, alpha));
        drawCenteredText(graphicsHandler, "Level " + currentLevel, h / 2 - 30,
            new Font("Arial", Font.BOLD, 56), new Color(255, 255, 255, alpha));
    }

    private void drawCenteredText(GraphicsHandler graphicsHandler, String text, int y, Font font, Color color) {
        Graphics2D g = graphicsHandler.getGraphics();
        FontMetrics fm = g.getFontMetrics(font);
        int x = (ScreenManager.getScreenWidth() - fm.stringWidth(text)) / 2;
        graphicsHandler.drawString(text, x, y + fm.getAscent(), font, color);
    }

    public PlayLevelScreenState getPlayLevelScreenState() {
        return playLevelScreenState;
    }

    // Coin icon plus a reserved 5-digit field, right edge a fixed margin from the
    // window edge. The field is reserved up front, so the icon and slot 0 never move
    // no matter how many digits the count has.
    private void drawCoinCounter(GraphicsHandler graphicsHandler) {
        int fieldWidth = coinDisplay.fieldWidth(COIN_SLOTS);
        int blockWidth = COIN_ICON_SIZE + COIN_ICON_GAP + fieldWidth;
        int blockX = Config.GAME_WINDOW_WIDTH - HUD_MARGIN - blockWidth;

        graphicsHandler.drawImage(coinIcon, blockX, HUD_TOP, COIN_ICON_SIZE, COIN_ICON_SIZE);

        int digitsX = blockX + COIN_ICON_SIZE + COIN_ICON_GAP;
        int digitsY = HUD_TOP + (COIN_ICON_SIZE - coinDisplay.slotHeight()) / 2;
        coinDisplay.drawInSlots(graphicsHandler, player.getCoins(), digitsX, digitsY, COIN_SLOTS);
    }

    private void drawDashBar(GraphicsHandler graphicsHandler)
    {
        int x = 20;
        int y = 20;
        int width = 100;
        int height = 12;

        float fill = 1f;

        if (player.getDashCooldownTimer() > 0) {
            fill = 1f - (
                (float) player.getDashCooldownTimer()
                / player.getDashCooldown()
            );
        }

        fill = Math.max(0f, Math.min(1f, fill));

        int fillWidth = (int) (width * fill);

        graphicsHandler.drawFilledRectangle(
            x, y, width, height,
            new Color(40, 40, 40)
        );

        Color fillColor = fill >= 1f
            ? new Color(80, 220, 100)
            : new Color(230, 150, 50);

        if (fillWidth > 0) {
            graphicsHandler.drawFilledRectangle(
                x, y, fillWidth, height, fillColor
            );
        }

        graphicsHandler.drawRectangle(
            x, y, width, height, Color.white, 2
        );
    }

    private void drawHealthBar(GraphicsHandler graphicsHandler) {
        int x = 20;
        int y = 45;
        int width = 120;
        int height = 14;

        float fill = (float) player.getHealth()
            / player.getMaxHealth();

        fill = Math.max(0f, Math.min(1f, fill));

        int fillWidth = (int) (width * fill);

        graphicsHandler.drawFilledRectangle(
            x, y, width, height,
            new Color(40, 40, 40)
        );

        Color color = fill > 0.5f
            ? new Color(80, 200, 90)
            : fill > 0.25f
                ? new Color(230, 170, 40)
                : new Color(210, 50, 45);

        if (fillWidth > 0) {
            graphicsHandler.drawFilledRectangle(
                x, y, fillWidth, height, color
            );
        }

        graphicsHandler.drawRectangle(
            x, y, width, height, Color.white, 2
        );
    }

    private void drawControls(GraphicsHandler graphicsHandler) {
    if (!showControls) {
        return;
    }
    int x = 20;
    int y = 68;
    int rowHeight = 14;
    int keyColumn = 68;
    int width = 165;
    int height = 26 + CONTROLS.length * rowHeight;

    graphicsHandler.drawFilledRectangle(x, y, width, height, new Color(30, 24, 49, 200));
    graphicsHandler.drawRectangle(x, y, width, height, new Color(214, 170, 92), 2);
    graphicsHandler.drawString("CONTROLS", x + 8, y + 16, CONTROLS_TITLE_FONT, new Color(214, 170, 92));

    for (int i = 0; i < CONTROLS.length; i++) {
        int rowY = y + 32 + i * rowHeight;
        graphicsHandler.drawString(CONTROLS[i][0], x + 8, rowY, CONTROLS_FONT, new Color(255, 214, 120));
        graphicsHandler.drawString(CONTROLS[i][1], x + 8 + keyColumn, rowY, CONTROLS_FONT, new Color(251, 230, 160));
    }
    }

    @Override
    public void onLevelCompleted() {
        if (playLevelScreenState != PlayLevelScreenState.LEVEL_COMPLETED) {
            playLevelScreenState = PlayLevelScreenState.LEVEL_COMPLETED;
            levelCompletedStateChangeStart = true;
        }
    }

    @Override
    public void onDeath() {
        if (playLevelScreenState != PlayLevelScreenState.LEVEL_LOSE) {
            playLevelScreenState = PlayLevelScreenState.LEVEL_LOSE;
        }
    }

    public void resetLevel() {
        // Restart the current level, not always Level 1.
        initialize();
    }

    public void goBackToMenu() {
        WanderingMonk.clearCheckpoint();
        screenCoordinator.setGameState(GameState.MENU);
    }

    private enum PlayLevelScreenState {
        RUNNING,
        PAUSED,
        LEVEL_INTRO,
        LEVEL_COMPLETED,
        LEVEL_LOSE
    }
}