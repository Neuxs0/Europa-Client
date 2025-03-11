package dev.neuxs.europa_client.modules;

public class Module {
    public int keyBind;

    private static boolean enabled;


    public Module(int keybind, boolean defaultEnabled) {
        this.keyBind = keybind;
        enabled = defaultEnabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static void enable() {
        enabled = true;
    }

    public static void disable() {
        enabled = false;
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
