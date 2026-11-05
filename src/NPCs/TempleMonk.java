package NPCs;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.KeyBinds;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.NPC;
import Level.Player;
import Utils.Point;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

// ...
public class TempleMonk extends NPC {
    private static final float SCALE = 1.5f;
    private static final int SMOKE_TIME = 50;
    private static final int TALK_RANGE = 110;

    private enum Stage { HIDDEN, SMOKE, TALKING, DONE }

    private final String hiddenLine;
    private final String[] pages;
    private String reminder;
    private final Runnable onFinished;
    private final BufferedImage smoke;

    private Stage stage = Stage.HIDDEN;
    private int page = -1;
    private int smokeTimer = 0;
    private boolean interactWasDown = true;
    private boolean near = false;
    private int ticks = 0;
    private Player player;

    public TempleMonk(Point location, String hiddenLine, String[] pages, String reminder, Runnable onFinished) {
        super(location.x, location.y, new SpriteSheet(ImageLoader.load("WanderingMonk.png"), 56, 72), "SHADOW");
        this.hiddenLine = hiddenLine;
        this.pages = pages;
        this.reminder = reminder;
        this.onFinished = onFinished;
        smoke = ImageLoader.load("SmokePuff.png");

        textbox.setFillColor(new Color(30, 24, 49, 235));
        textbox.setBorderColor(new Color(214, 170, 92));
        textbox.setBorderThickness(3);
        textbox.setTextColor(new Color(251, 230, 160));
        textbox.setFont(new Font("Serif", Font.BOLD, 18));
        textbox.setHPadding(14);
        textbox.setVPadding(10);
        textbox.setGap(4);
        textboxOffsetX = -150;
        textboxOffsetY = -130;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public boolean isDone() {
        return stage == Stage.DONE;
    }

    public boolean isTalking() {
        return page >= 0;
    }

    @Override
    public void checkTalkedTo(Player player) {
        // handled in update
    }

    @Override
    public void update(Player player) {
        this.player = player;
        ticks++;
        super.update(player);

        float px = player.getBounds().getX1() + player.getBounds().getWidth() / 2f;
        float mx = getBounds().getX1() + getBounds().getWidth() / 2f;
        near = Math.abs(px - mx) < TALK_RANGE && Math.abs(player.getBounds().getY2() - getBounds().getY2()) < 80;

        boolean down = KeyBinds.isDown(KeyBinds.Action.INTERACT);
        boolean pressed = down && !interactWasDown;
        interactWasDown = down;

        switch (stage) {
            case HIDDEN:
                currentAnimationName = "SHADOW";
                if (page < 0 && near && pressed) {
                    open(hiddenLine, player);
                } else if (page >= 0 && pressed) {
                    page = -1;
                    talkedTo = false;
                    stage = Stage.SMOKE;
                    smokeTimer = SMOKE_TIME;
                }
                break;

            case SMOKE:
                smokeTimer--;
                currentAnimationName = smokeTimer < SMOKE_TIME / 2 ? "IDLE_LIT" : "SHADOW";
                if (smokeTimer <= 0) {
                    stage = Stage.TALKING;
                    page = 0;
                    textbox.setText(pages[0]);
                    talkedTo = true;
                }
                break;

            case TALKING:
                currentAnimationName = "TALK_LIT";
                if (pressed) {
                    page++;
                    if (page >= pages.length) {
                        close(player);
                        stage = Stage.DONE;
                        if (onFinished != null) onFinished.run();
                    } else {
                        textbox.setText(pages[page]);
                    }
                }
                break;

            case DONE:
                currentAnimationName = page >= 0 ? "TALK_LIT" : "IDLE_LIT";
                if (pressed && page >= 0) {
                    close(player);
                } else if (pressed && near) {
                    open(reminder, player);
                }
                break;
        }
    }

    private void open(String text, Player player) {
        page = 0;
        textbox.setText(text);
        talkedTo = true;
        player.setFrozen(true);
    }

    private void close(Player player) {
        page = -1;
        talkedTo = false;
        player.setFrozen(false);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
        Graphics2D g = graphicsHandler.getGraphics();
        int x = Math.round(getCalibratedXLocation());
        int y = Math.round(getCalibratedYLocation());

        if (stage == Stage.HIDDEN) {
            // two eyes in the dark, blinking now and then
            if ((ticks / 6) % 40 != 0) {
                g.setColor(new Color(255, 214, 120));
                g.fillRect(x + 33, y + 27, 4, 3);
                g.fillRect(x + 42, y + 27, 4, 3);
            }
        }
        if (stage == Stage.SMOKE && smoke != null) {
            float t = 1f - smokeTimer / (float) SMOKE_TIME;
            int size = Math.round(84 + 60 * t);
            g.drawImage(smoke, x + 42 - size / 2, y + 54 - size / 2, size, size, null);
        }
        if (near && page < 0 && stage != Stage.SMOKE && (player == null || !player.isFrozen())) {
            String label = "[" + KeyBinds.label(KeyBinds.Action.INTERACT) + "] Talk";
            g.setFont(new Font("Serif", Font.BOLD, 16));
            int w = g.getFontMetrics().stringWidth(label);
            g.setColor(new Color(20, 16, 30, 200));
            g.fillRoundRect(x + 42 - w / 2 - 8, y - 30, w + 16, 24, 10, 10);
            g.setColor(new Color(251, 230, 160));
            g.drawString(label, x + 42 - w / 2, y - 13);
        }
        if (page >= 0 && stage != Stage.SMOKE) {
            String more = "[" + KeyBinds.label(KeyBinds.Action.INTERACT) + "]";
            g.setFont(new Font("Serif", Font.BOLD, 13));
            g.setColor(new Color(214, 170, 92));
            g.drawString(more, x + textboxOffsetX + 8, y + textboxOffsetY - 6);
        }
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        HashMap<String, Frame[]> animations = new HashMap<>();
        animations.put("SHADOW", shadowRow(spriteSheet, new int[] {16, 16, 16, 16}));
        animations.put("IDLE_LIT", row(spriteSheet, 3, new int[] {12, 12, 12, 12}));
        animations.put("TALK_LIT", row(spriteSheet, 4, new int[] {8, 8, 8, 8}));
        return animations;
    }

    private Frame[] row(SpriteSheet sheet, int row, int[] delays) {
        Frame[] frames = new Frame[delays.length];
        for (int i = 0; i < delays.length; i++) {
            frames[i] = new FrameBuilder(sheet.getSprite(row, i), delays[i])
                    .withScale(SCALE)
                    .withBounds(18, 8, 28, 62)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .build();
        }
        return frames;
    }

    // the monk as a dark shape (row 0 without his lantern lit)
    private Frame[] shadowRow(SpriteSheet sheet, int[] delays) {
        Frame[] frames = new Frame[delays.length];
        for (int i = 0; i < delays.length; i++) {
            BufferedImage src = sheet.getSprite(0, i);
            BufferedImage dark = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int yy = 0; yy < src.getHeight(); yy++) {
                for (int xx = 0; xx < src.getWidth(); xx++) {
                    int argb = src.getRGB(xx, yy);
                    if ((argb >>> 24) == 0) continue;
                    dark.setRGB(xx, yy, 0xFF1A1224);
                }
            }
            frames[i] = new FrameBuilder(dark, delays[i])
                    .withScale(SCALE)
                    .withBounds(18, 8, 28, 62)
                    .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                    .build();
        }
        return frames;
    }
}
