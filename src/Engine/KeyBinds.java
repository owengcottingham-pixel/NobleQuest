package Engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.EnumMap;

/*
 * Every key the player can press during a level, in one place.
 *
 * Before this, the keys lived wherever they were used -- the movement keys as
 * fields on Player, E on both the dwarf and the wishing well, SPACE inside NPC,
 * H and ESC inside PlayLevelScreen -- and the on-screen CONTROLS box was a
 * hand-written list that had already drifted out of date (it promised W for
 * jump long after jump moved to SPACE). Everything reads from here now, so a
 * rebind reaches the game and the help box together and neither can go stale.
 *
 * Bindings are saved to keybinds.cfg next to the game, the same way Resources/
 * and MapFiles/ are found, so they survive closing the game. If that file
 * cannot be read or written -- read-only folder, launched from somewhere else --
 * the defaults below are used and the game carries on; losing a key mapping is
 * not worth a crash.
 *
 * Duplicates are allowed on purpose. SPACE is both Jump and Talk out of the box
 * and E is both the shop and the well, which is how the game already played;
 * the menu flags a shared key rather than refusing it.
 */
public final class KeyBinds {

    public enum Action {
        MOVE_LEFT("Move Left", Key.A),
        MOVE_RIGHT("Move Right", Key.D),
        JUMP("Jump", Key.SPACE),
        CROUCH("Crouch", Key.S),
        DASH("Dash", Key.SHIFT),
        ATTACK("Sword", Key.J),
        TALK("Talk", Key.SPACE),
        INTERACT("Shop / Well", Key.E),
        PAUSE("Pause", Key.ESC),
        TOGGLE_CONTROLS("Hide Controls", Key.H);

        public final String label;
        public final Key defaultKey;

        Action(String label, Key defaultKey) {
            this.label = label;
            this.defaultKey = defaultKey;
        }
    }

    // ESC and ENTER drive every menu in the game, so they are never handed out
    // as gameplay bindings -- binding jump to ESC would leave you unable to
    // leave the menu you bound it in.
    public static boolean isReserved(Key key) {
        return key == Key.ESC || key == Key.ENTER;
    }

    private static final String FILE_NAME = "keybinds.cfg";
    private static final EnumMap<Action, Key> BOUND = new EnumMap<Action, Key>(Action.class);

    static {
        restoreDefaults();
        load();
    }

    private KeyBinds() {
    }

    public static Key get(Action action) {
        Key key = BOUND.get(action);
        return key != null ? key : action.defaultKey;
    }

    public static void set(Action action, Key key) {
        if (key == null || isReserved(key)) {
            return;
        }
        BOUND.put(action, key);
        save();
    }

    public static void reset(Action action) {
        BOUND.put(action, action.defaultKey);
        save();
    }

    public static void restoreDefaults() {
        for (Action action : Action.values()) {
            BOUND.put(action, action.defaultKey);
        }
    }

    public static void restoreDefaultsAndSave() {
        restoreDefaults();
        save();
    }

    public static boolean isDown(Action action) {
        return Keyboard.isKeyDown(get(action));
    }

    public static boolean isUp(Action action) {
        return Keyboard.isKeyUp(get(action));
    }

    // true when some other action answers to the same key
    public static boolean isShared(Action action) {
        Key key = get(action);
        for (Action other : Action.values()) {
            if (other != action && get(other) == key) {
                return true;
            }
        }
        return false;
    }

    // what to print on a key cap: "Space", "Left Arrow", "1", "A"
    public static String label(Key key) {
        if (key == null) {
            return "--";
        }
        switch (key) {
            case UP:    return "Up Arrow";
            case DOWN:  return "Down Arrow";
            case LEFT:  return "Left Arrow";
            case RIGHT: return "Right Arrow";
            case ENTER: return "Enter";
            case SHIFT: return "Shift";
            case SPACE: return "Space";
            case ESC:   return "Esc";
            case ONE:   return "1";
            case TWO:   return "2";
            case THREE: return "3";
            case FOUR:  return "4";
            case FIVE:  return "5";
            case SIX:   return "6";
            case SEVEN: return "7";
            case EIGHT: return "8";
            case NINE:  return "9";
            case ZERO:  return "0";
            default:    return key.name();
        }
    }

    public static String label(Action action) {
        return label(get(action));
    }

    // ---- saving ----

    private static void load() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                int equals = line.indexOf('=');
                if (line.isEmpty() || line.startsWith("#") || equals <= 0) {
                    continue;
                }
                // A hand-edited or out-of-date file must not take the game down,
                // so an unreadable line just leaves that action on its default.
                try {
                    Action action = Action.valueOf(line.substring(0, equals).trim());
                    Key key = Key.valueOf(line.substring(equals + 1).trim());
                    if (!isReserved(key)) {
                        BOUND.put(action, key);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (Exception e) {
            System.out.println("Could not read " + FILE_NAME + ", using the default controls. " + e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static void save() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(FILE_NAME);
            writer.println("# NobleQuest key bindings - rewritten by the Controls menu");
            for (Action action : Action.values()) {
                writer.println(action.name() + "=" + get(action).name());
            }
        } catch (Exception e) {
            System.out.println("Could not save " + FILE_NAME + ", the controls will reset next launch. " + e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
