package dev.neuxs.europa_client.modules;

import dev.neuxs.europa_client.settings.ClientSettings;
import dev.neuxs.europa_client.settings.Setting;
import dev.neuxs.europa_client.settings.SettingsManager;
import dev.neuxs.europa_client.utils.KeybindUtil;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused", "rawtypes"})
public abstract class Module {
    protected final String id;
    protected final Setting<Boolean> enabled;
    protected final Setting<String> keybind;
    protected final Map<String, Setting<?>> customSettings;

    public Module(String id, int defaultKeybind, boolean defaultEnabled) {
        this(id, defaultKeybind, defaultEnabled, true);
    }

    public Module(String id, int defaultKeybind, boolean defaultEnabled, boolean defaultNotifications) {
        this.id = id;
        this.enabled = new Setting<>("enabled", defaultEnabled);
        this.keybind = new Setting<>("keybind", KeybindUtil.fromSingleKey(defaultKeybind));
        this.customSettings = new HashMap<>();
        this.customSettings.put("notifications", new Setting<>("notifications", defaultNotifications)
                .withDisplayName("Notifications")
                .withDescription("Show enabled and disabled messages for this module"));
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
        java.util.List<Integer> keys = KeybindUtil.parse(keybind.getValue());
        return keys.isEmpty() ? com.badlogic.gdx.Input.Keys.UNKNOWN : keys.get(keys.size() - 1);
    }

    public String getKeybindCombo() {
        return keybind.getValue();
    }

    public Map<String, Setting<?>> getCustomSettings() {
        return customSettings;
    }

    public void setKeybind(int key) {
        keybind.setValue(KeybindUtil.fromSingleKey(key));
    }

    public void setKeybind(String keybind) {
        this.keybind.setValue(keybind == null ? KeybindUtil.UNBOUND : keybind);
    }

    public void onKeyPressed(boolean messaging) {
        toggle(messaging);
    }

    public void enable(boolean messaging) {}

    public void disable(boolean messaging) {}

    public void toggle(boolean messaging) {
        boolean wasEnabled = isEnabled();
        boolean previousAutoSave = SettingsManager.isAutoSaveEnabled();
        boolean notify = shouldNotify(messaging);

        SettingsManager.setAutoSaveEnabled(false);
        try {
            if (wasEnabled) disable(notify);
            else enable(notify);
        } finally {
            SettingsManager.setAutoSaveEnabled(previousAutoSave);
        }

        if (previousAutoSave && !SettingsManager.isReloading() && wasEnabled != isEnabled()) {
            SettingsManager.saveSettings();
        }
    }

    @SuppressWarnings("unchecked")
    public boolean shouldNotify(boolean messaging) {
        if (!messaging || !ClientSettings.areModuleNotificationsEnabled()) {
            return false;
        }
        Setting<Boolean> notifications = (Setting<Boolean>) customSettings.get("notifications");
        return notifications == null || notifications.getValue();
    }

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
                keybind.setValue(KeybindUtil.fromSingleKey(((Number) k).intValue()));
            } else if (k instanceof String) {
                keybind.setValue((String) k);
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
