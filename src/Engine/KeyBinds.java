package Engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.EnumSet;

/*
 * Every key the player can press during a level, in one place.
 *
 * Before this, the keys lived wherever they were used -- the movement keys as
 * fields on Player, E on both the dwarf and the wishing well, SPACE inside NPC,
 * H and ESC inside PlayLevelScreen -- and the on-screen CONTROLS box was a
 * hand-written list that had already drifted out of date. Everything reads from
 * here now, so a rebind reaches the game and the help box together.
 *
 * An action can have TWO keys: a main one and an optional second. Jump answers
 * to both Space and W out of the box. That is not the same as two actions
 * sharing a key, which is still refused -- one key does one thing, but one
 * thing may have more than one key.
 *
 * Bindings are saved to keybinds.cfg next to the game. If it cannot be read or
 * written the defaults are used and the game carries on; losing a key mapping
 * is not worth a crash.
 */
public final class KeyBinds {

    public enum Action {
        MOVE_LEFT("Move Left", Key.A),
        MOVE_RIGHT("Move Right", Key.D),
        JUMP("Jump", Key.SPACE, Key.W),
        CROUCH("Crouch", Key.S),
        DASH("Dash", Key.SHIFT),
        ATTACK("Sword", Key.J),
        INTERACT("Talk / Shop / Well", Key.E),
        PAUSE("Pause", Key.ESC),
        TOGGLE_CONTROLS("Hide Controls", Key.H);

        public final String label;
        public final Key defaultKey;
        public final Key defaultAlt;      // null when the action has only one key

        Action(String label, Key defaultKey) {
            this(label, defaultKey, null);
        }

        Action(String label, Key defaultKey, Key defaultAlt) {
            this.label = label;
            this.defaultKey = defaultKey;
            this.defaultAlt = defaultAlt;
        }
    }

    // ESC and ENTER drive every menu in the game, so they are never handed out
    // as gameplay bindings -- binding jump to ESC would leave you unable to
    // leave the menu you bound it in.
    public static boolean isReserved(Key key) {
        return key == Key.ESC || key == Key.ENTER;
    }

    private static final String FILE_NAME = "keybinds.cfg";
    private static final EnumMap<Action, Key> MAIN = new EnumMap<Action, Key>(Action.class);
    private static final EnumMap<Action, Key> ALT = new EnumMap<Action, Key>(Action.class);

    static {
        restoreDefaults();
        load();
        resolveDuplicates();
    }

    private KeyBinds() {
    }

    public static Key get(Action action) {
        Key key = MAIN.get(action);
        return key != null ? key : action.defaultKey;
    }

    /** The action's second key, or null if it has only one. */
    public static Key getAlt(Action action) {
        return ALT.get(action);
    }

    /** Every key that triggers this action: one entry, or two. */
    public static Key[] keysFor(Action action) {
        Key alt = getAlt(action);
        return alt == null ? new Key[] { get(action) }
                           : new Key[] { get(action), alt };
    }

    public static boolean isDown(Action action) {
        for (Key key : keysFor(action)) {
            if (Keyboard.isKeyDown(key)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUp(Action action) {
        return !isDown(action);
    }

    /*
     * Binds the action's main key, and returns whichever action had to give that
     * key up, or null if it was free.
     *
     * Three cases, in order:
     *  - the key is this action's own second key, so the two simply trade places
     *  - another action's main key: it inherits the key this one just vacated
     *  - another action's second key: that second key is dropped, since there is
     *    nothing sensible to move it to
     */
    public static Action set(Action action, Key key) {
        if (key == null || isReserved(key)) {
            return null;
        }
        return assign(action, key);
    }

    private static Action assign(Action action, Key key) {
        Key previous = get(action);

        if (key == getAlt(action)) {
            ALT.put(action, previous);
            MAIN.put(action, key);
            save();
            return null;
        }

        Action displaced = null;
        boolean displacedWasAlt = false;
        for (Action other : Action.values()) {
            if (other == action) {
                continue;
            }
            if (get(other) == key) {
                displaced = other;
                break;
            }
            if (getAlt(other) == key) {
                displaced = other;
                displacedWasAlt = true;
                break;
            }
        }

        MAIN.put(action, key);

        if (displaced != null) {
            if (displacedWasAlt) {
                ALT.remove(displaced);
            } else {
                // Normally the displaced action inherits the key this one gave
                // up. Not if that key is Esc: Pause may sit on Esc, but handing
                // Esc to Sword would leave Sword firing whenever you open a menu
                // and unable to be rebound off it.
                Key inherited = previous;
                if (isReserved(inherited)) {
                    inherited = firstFreeKey(keysInUse(displaced));
                }
                if (inherited != null) {
                    MAIN.put(displaced, inherited);
                }
            }
        }
        save();
        return displaced;
    }

    /** Back to defaults for this action, both keys, swapping anyone in the way. */
    public static Action reset(Action action) {
        if (action.defaultAlt != null) {
            ALT.put(action, action.defaultAlt);
        } else {
            ALT.remove(action);
        }
        Key previous = get(action);
        MAIN.put(action, previous);          // assign() reads the current value
        Action displaced = assign(action, action.defaultKey);
        resolveDuplicates();
        save();
        return displaced;
    }

    public static void restoreDefaults() {
        for (Action action : Action.values()) {
            MAIN.put(action, action.defaultKey);
            if (action.defaultAlt != null) {
                ALT.put(action, action.defaultAlt);
            } else {
                ALT.remove(action);
            }
        }
    }

    public static void restoreDefaultsAndSave() {
        restoreDefaults();
        save();
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

    /** "Space / W" for an action with two keys, "J" for one with a single key. */
    public static String label(Action action) {
        Key alt = getAlt(action);
        return alt == null ? label(get(action))
                           : label(get(action)) + " / " + label(alt);
    }

    // ---- housekeeping ----

    private static EnumSet<Key> keysInUse(Action except) {
        EnumSet<Key> used = EnumSet.noneOf(Key.class);
        for (Action action : Action.values()) {
            if (action == except) {
                continue;
            }
            for (Key key : keysFor(action)) {
                used.add(key);
            }
        }
        return used;
    }

    private static Key firstFreeKey(EnumSet<Key> taken) {
        for (Key candidate : Key.values()) {
            if (!isReserved(candidate) && !taken.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /*
     * Nothing stops someone opening keybinds.cfg in a text editor and putting two
     * actions on one key, so the file is checked rather than trusted: a clashing
     * main key falls back to its own default, or to the first free key if that is
     * taken too, and a clashing second key is simply dropped.
     */
    private static void resolveDuplicates() {
        EnumSet<Key> taken = EnumSet.noneOf(Key.class);
        for (Action action : Action.values()) {
            Key key = get(action);
            if (taken.contains(key)) {
                Key replacement = !taken.contains(action.defaultKey)
                        ? action.defaultKey
                        : firstFreeKey(taken);
                if (replacement != null) {
                    MAIN.put(action, replacement);
                    key = replacement;
                }
            }
            taken.add(key);

            Key alt = getAlt(action);
            if (alt != null) {
                if (alt == key || taken.contains(alt) || isReserved(alt)) {
                    ALT.remove(action);
                } else {
                    taken.add(alt);
                }
            }
        }
    }

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
                    String name = line.substring(0, equals).trim();
                    String value = line.substring(equals + 1).trim();
                    boolean isAlt = name.endsWith(".alt");
                    if (isAlt) {
                        name = name.substring(0, name.length() - ".alt".length());
                    }
                    Action action = Action.valueOf(name);
                    if (value.equals("none")) {
                        if (isAlt) {
                            ALT.remove(action);
                        }
                        continue;
                    }
                    Key key = Key.valueOf(value);
                    if (isReserved(key)) {
                        continue;
                    }
                    if (isAlt) {
                        ALT.put(action, key);
                    } else {
                        MAIN.put(action, key);
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
            writer.println("# <ACTION>=<key> is the main key, <ACTION>.alt=<key> an optional second");
            for (Action action : Action.values()) {
                writer.println(action.name() + "=" + get(action).name());
                Key alt = getAlt(action);
                writer.println(action.name() + ".alt=" + (alt == null ? "none" : alt.name()));
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
