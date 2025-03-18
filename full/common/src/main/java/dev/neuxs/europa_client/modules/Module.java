package dev.neuxs.europa_client.modules;

import dev.neuxs.europa_client.settings.Setting;
import java.util.HashMap;
import java.util.Map;

public abstract class Module {
    // Unique identifier (e.g., "noclip", "speed", "reach").
    protected final String id;
    // Store each module's enabled state and keybind.
    protected final Setting<Boolean> enabled;
    protected final Setting<Integer> keybind;
    // Map of module-specific custom settings.
    protected final Map<String, Setting<?>> customSettings;

    public Module(String id, int defaultKeybind, boolean defaultEnabled) {
        this.id = id;
        this.enabled = new Setting<>("enabled", defaultEnabled);
        this.keybind = new Setting<>("keybind", defaultKeybind);
        this.customSettings = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled.getValue();
    }

    public void setEnabled(boolean value) {
        enabled.setValue(value);
    }

    public int getKeybind() {
        return keybind.getValue();
    }

    public void setKeybind(int key) {
        keybind.setValue(key);
    }

    // Default action when a module’s keybind is pressed.
    public void onKeyPressed() {
        toggle();
    }

    public void toggle() {
        setEnabled(!isEnabled());
    }

    /**
     * Exports settings in the following structure:
     * {
     *   "enabled": boolean,
     *   "keybind": integer,
     *   "settings": {
     *       ... extra settings ...
     *   }
     * }
     */
    public Map<String, Object> exportSettings() {
        Map<String, Object> obj = new HashMap<>();
        obj.put("enabled", enabled.getValue());
        obj.put("keybind", keybind.getValue());
        Map<String, Object> extra = new HashMap<>();
        for (Map.Entry<String, Setting<?>> entry : customSettings.entrySet()) {
            extra.put(entry.getKey(), entry.getValue().getValue());
        }
        obj.put("settings", extra);
        return obj;
    }

    /**
     * Imports module settings from a Map.
     */
    @SuppressWarnings("unchecked")
    public void importSettings(Map<String, Object> data) {
        if (data.containsKey("enabled")) {
            Object e = data.get("enabled");
            if (e instanceof Boolean) {
                enabled.setValue((Boolean) e);
            }
        }
        if (data.containsKey("keybind")) {
            Object k = data.get("keybind");
            if (k instanceof Number) {
                keybind.setValue(((Number) k).intValue());
            }
        }
        if (data.containsKey("settings")) {
            Map<String, Object> custom = (Map<String, Object>) data.get("settings");
            for (String settingKey : custom.keySet()) {
                if (customSettings.containsKey(settingKey)) {
                    Setting s = customSettings.get(settingKey);
                    Object val = custom.get(settingKey);
                    Object converted = convertValue(s.getDefaultValue(), val);
                    s.setValue(converted);
                }
            }
        }
    }

    /**
     * Converts the loaded value to the proper type.
     */
    private Object convertValue(Object defaultValue, Object value) {
        if (defaultValue instanceof Float) {
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
        } else if (defaultValue instanceof Integer) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        } else if (defaultValue instanceof Boolean) {
            return value;
        } else if (defaultValue instanceof String) {
            return value.toString();
        }
        return value;
    }
}
