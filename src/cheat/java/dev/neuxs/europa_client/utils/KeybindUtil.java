package dev.neuxs.europa_client.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class KeybindUtil {
    public static final String UNBOUND = "";

    private static final int[] MODIFIER_KEYS = {
            Input.Keys.CONTROL_LEFT,
            Input.Keys.CONTROL_RIGHT,
            Input.Keys.SHIFT_LEFT,
            Input.Keys.SHIFT_RIGHT,
            Input.Keys.ALT_LEFT,
            Input.Keys.ALT_RIGHT,
            Input.Keys.SYM
    };

    private KeybindUtil() {}

    public static String fromSingleKey(int keycode) {
        return isValidKey(keycode) ? String.valueOf(keycode) : UNBOUND;
    }

    public static String serialize(Collection<Integer> keycodes) {
        if (keycodes == null || keycodes.isEmpty()) {
            return UNBOUND;
        }

        List<Integer> orderedKeys = new ArrayList<>();
        for (int modifier : MODIFIER_KEYS) {
            if (keycodes.contains(modifier) && !orderedKeys.contains(modifier)) {
                orderedKeys.add(modifier);
            }
        }
        for (Integer keycode : keycodes) {
            if (keycode != null && isValidKey(keycode) && !orderedKeys.contains(keycode)) {
                orderedKeys.add(keycode);
            }
        }

        if (orderedKeys.isEmpty()) {
            return UNBOUND;
        }

        StringBuilder value = new StringBuilder();
        for (int i = 0; i < orderedKeys.size(); i++) {
            if (i > 0) {
                value.append(',');
            }
            value.append(orderedKeys.get(i));
        }
        return value.toString();
    }

    public static List<Integer> parse(String keybind) {
        List<Integer> keys = new ArrayList<>();
        if (keybind == null || keybind.isBlank()) {
            return keys;
        }

        String[] parts = keybind.contains(",") ? keybind.split(",") : keybind.split("\\+");
        for (String part : parts) {
            int keycode = parseKey(part.trim());
            if (isValidKey(keycode) && !keys.contains(keycode)) {
                keys.add(keycode);
            }
        }
        return keys;
    }

    public static String format(String keybind) {
        List<Integer> keys = parse(keybind);
        if (keys.isEmpty()) {
            return "None";
        }

        StringBuilder value = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            if (i > 0) {
                value.append('+');
            }
            value.append(formatKey(keys.get(i)));
        }
        return value.toString();
    }

    public static boolean isActive(String keybind) {
        List<Integer> keys = parse(keybind);
        if (keys.isEmpty()) {
            return false;
        }

        int triggerKey = getTriggerKey(keys);
        if (!Gdx.input.isKeyJustPressed(triggerKey)) {
            return false;
        }

        if (!modifiersMatch(keys)) {
            return false;
        }

        for (int key : keys) {
            if (!Gdx.input.isKeyPressed(key)) {
                return false;
            }
        }
        return true;
    }

    public static Set<Integer> captureCurrentCombination(int keycode) {
        LinkedHashSet<Integer> keys = new LinkedHashSet<>();
        for (int modifier : MODIFIER_KEYS) {
            if (Gdx.input.isKeyPressed(modifier)) {
                keys.add(modifier);
            }
        }
        if (isValidKey(keycode)) {
            keys.add(keycode);
        }
        return keys;
    }

    public static boolean isModifierKey(int keycode) {
        for (int modifier : MODIFIER_KEYS) {
            if (modifier == keycode) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsNonModifier(Collection<Integer> keycodes) {
        if (keycodes == null) {
            return false;
        }
        for (Integer keycode : keycodes) {
            if (keycode != null && !isModifierKey(keycode)) {
                return true;
            }
        }
        return false;
    }

    private static int getTriggerKey(List<Integer> keys) {
        for (int i = keys.size() - 1; i >= 0; i--) {
            int key = keys.get(i);
            if (!isModifierKey(key)) {
                return key;
            }
        }
        return keys.get(keys.size() - 1);
    }

    private static boolean modifiersMatch(List<Integer> keys) {
        for (int modifier : MODIFIER_KEYS) {
            if (Gdx.input.isKeyPressed(modifier) != keys.contains(modifier)) {
                return false;
            }
        }
        return true;
    }

    private static int parseKey(String value) {
        if (value == null || value.isBlank()) {
            return Input.Keys.UNKNOWN;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
        }

        return switch (value.toLowerCase()) {
            case "ctrl", "control", "left ctrl", "l-ctrl", "ctrl left", "control_left" -> Input.Keys.CONTROL_LEFT;
            case "right ctrl", "r-ctrl", "ctrl right", "control_right" -> Input.Keys.CONTROL_RIGHT;
            case "shift", "left shift", "l-shift", "shift left", "shift_left" -> Input.Keys.SHIFT_LEFT;
            case "right shift", "r-shift", "shift right", "shift_right" -> Input.Keys.SHIFT_RIGHT;
            case "alt", "left alt", "l-alt", "alt left", "alt_left" -> Input.Keys.ALT_LEFT;
            case "right alt", "r-alt", "alt right", "alt_right" -> Input.Keys.ALT_RIGHT;
            case "sym" -> Input.Keys.SYM;
            default -> {
                try {
                    yield Input.Keys.valueOf(value);
                } catch (RuntimeException e) {
                    yield Input.Keys.UNKNOWN;
                }
            }
        };
    }

    private static String formatKey(int keycode) {
        if (keycode == Input.Keys.CONTROL_LEFT) return "Left Ctrl";
        if (keycode == Input.Keys.CONTROL_RIGHT) return "Right Ctrl";
        if (keycode == Input.Keys.SHIFT_LEFT) return "Left Shift";
        if (keycode == Input.Keys.SHIFT_RIGHT) return "Right Shift";
        if (keycode == Input.Keys.ALT_LEFT) return "Left Alt";
        if (keycode == Input.Keys.ALT_RIGHT) return "Right Alt";
        if (keycode == Input.Keys.SYM) return "Sym";
        return Input.Keys.toString(keycode);
    }

    private static boolean isValidKey(int keycode) {
        return keycode > 0 && keycode != Input.Keys.UNKNOWN;
    }
}
