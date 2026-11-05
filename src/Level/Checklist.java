package Level;

import Engine.GraphicsHandler;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Predicate;


public class Checklist {
    public interface KeyLabel {
        String get();
    }

    private static class Task {
        final String text;
        final KeyLabel key;
        final Predicate<Player> show, done;
        boolean shown;
        int age;            // frames since it showed up (slides in)
        int doneAge = -1;   // frames since it was ticked (-1 = not yet)

        Task(String text, KeyLabel key, Predicate<Player> show, Predicate<Player> done) {
            this.text = text;
            this.key = key;
            this.show = show;
            this.done = done;
        }
    }

    private static final HashSet<String> ticked = new HashSet<>();   // remembered for the session

    private static final Font TITLE_FONT = new Font("Serif", Font.BOLD, 15);
    private static final Font TASK_FONT = new Font("SansSerif", Font.BOLD, 13);
    private static final Font KEY_FONT = new Font("SansSerif", Font.BOLD, 11);
    private static final Color PANEL = new Color(30, 24, 49, 205);
    private static final Color GOLD = new Color(214, 170, 92);
    private static final Color TEXT = new Color(251, 236, 196);
    private static final Color DONE = new Color(120, 220, 120);
    private static final int LINGER = 100;      // frames a ticked task stays before it fades out
    private static final int FADE = 30;

    private final ArrayList<Task> tasks = new ArrayList<>();
    private int allDoneAge = -1;
    private boolean tickedThisTime = false;     // only say "all done" if you finished it just now

    // text = what to do; key = the key to press (null for none); show / done = tests on the player
    public Checklist add(String text, KeyLabel key, Predicate<Player> show, Predicate<Player> done) {
        tasks.add(new Task(text, key, show, done));
        return this;
    }

    public void update(Player player) {
        boolean any = false, allDone = true;
        for (Task t : tasks) {
            if (ticked.contains(t.text)) {
                if (t.doneAge < 0) t.doneAge = LINGER + FADE;   // done in an earlier life: don't show it
                continue;
            }
            allDone = false;
            if (!t.shown && t.show.test(player)) {
                t.shown = true;
            }
            if (t.shown) {
                any = true;
                t.age++;
                if (t.done.test(player)) {
                    ticked.add(t.text);
                    t.doneAge = 0;
                    tickedThisTime = true;
                }
            }
        }
        for (Task t : tasks) {
            if (t.doneAge >= 0 && t.doneAge < LINGER + FADE) t.doneAge++;
        }
        if (allDone && !tasks.isEmpty() && tickedThisTime) {
            if (allDoneAge < 0) allDoneAge = 0;
            else allDoneAge++;
        }
    }

    private boolean visible(Task t) {
        return t.shown && (t.doneAge < 0 || t.doneAge < LINGER + FADE);
    }

    public void draw(GraphicsHandler graphicsHandler) {
        ArrayList<Task> rows = new ArrayList<>();
        for (Task t : tasks) {
            if (visible(t)) rows.add(t);
        }
        boolean showAllDone = allDoneAge >= 0 && allDoneAge < 150;
        if (rows.isEmpty() && !showAllDone) return;

        Graphics2D g = graphicsHandler.getGraphics();
        Object aa = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics tm = g.getFontMetrics(TASK_FONT), km = g.getFontMetrics(KEY_FONT), hm = g.getFontMetrics(TITLE_FONT);

        int rowH = 22, pad = 10;
        int width = 200;
        for (Task t : rows) {
            int w = 26 + tm.stringWidth(t.text) + (t.key != null ? 12 + km.stringWidth(t.key.get()) + 10 : 0);
            width = Math.max(width, w + pad * 2);
        }
        int height = pad * 2 + hm.getHeight() + 4 + Math.max(1, rows.size()) * rowH;
        int x = 20, y = 68;

        g.setColor(PANEL);
        g.fillRoundRect(x, y, width, height, 10, 10);
        g.setColor(GOLD);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, width, height, 10, 10);
        g.setFont(TITLE_FONT);
        g.drawString(rows.isEmpty() ? "ALL DONE!" : "TO DO", x + pad, y + pad + hm.getAscent());

        int ry = y + pad + hm.getHeight() + 4;
        for (Task t : rows) {
            float fade = t.doneAge > LINGER ? 1 - (t.doneAge - LINGER) / (float) FADE : 1;
            int slide = t.age < 10 ? (10 - t.age) * 6 : 0;          // slides in from the left
            int alpha = Math.max(0, Math.min(255, Math.round(255 * fade)));
            int cx = x + pad + slide, cy = ry + 4;
            boolean done = t.doneAge >= 0;

            // the box, and a tick once it's done
            g.setColor(new Color(251, 236, 196, alpha));
            g.setStroke(new BasicStroke(2));
            g.drawRect(cx, cy, 13, 13);
            if (done) {
                g.setColor(new Color(DONE.getRed(), DONE.getGreen(), DONE.getBlue(), alpha));
                g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawPolyline(new int[] { cx + 2, cx + 6, cx + 15 }, new int[] { cy + 7, cy + 11, cy - 2 }, 3);
            }
            // the task, crossed out once it's done
            g.setFont(TASK_FONT);
            Color text = done ? new Color(170, 160, 140, alpha) : new Color(TEXT.getRed(), TEXT.getGreen(), TEXT.getBlue(), alpha);
            g.setColor(text);
            int tx = cx + 22, ty = ry + 4 + tm.getAscent() - 1;
            g.drawString(t.text, tx, ty);
            if (done) {
                g.setStroke(new BasicStroke(1.5f));
                g.drawLine(tx, ty - tm.getAscent() / 3, tx + tm.stringWidth(t.text), ty - tm.getAscent() / 3);
            }
            // the key, in a little key-cap
            if (t.key != null) {
                String k = t.key.get();
                int kx = tx + tm.stringWidth(t.text) + 10, kw = km.stringWidth(k) + 8;
                g.setColor(new Color(60, 50, 86, alpha));
                g.fillRoundRect(kx, ry + 3, kw, 16, 5, 5);
                g.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), alpha));
                g.setStroke(new BasicStroke(1));
                g.drawRoundRect(kx, ry + 3, kw, 16, 5, 5);
                g.setFont(KEY_FONT);
                g.drawString(k, kx + 4, ry + 3 + km.getAscent() + 1);
            }
            ry += rowH;
        }
        if (rows.isEmpty()) {
            g.setFont(TASK_FONT);
            g.setColor(DONE);
            g.drawString("You know all the moves.", x + pad, ry + tm.getAscent());
        }
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                aa != null ? aa : RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
    }
}
