package dev.neuxs.europa_client.modules;

public abstract class Module {
    public int keyBind;
    private boolean enabled;

    public Module(int keyBind, boolean defaultEnabled) {
        this.keyBind = keyBind;
        this.enabled = defaultEnabled;
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
