package dev.neuxs.europa_client.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.settings.Setting;
import dev.neuxs.europa_client.utils.Chat;

public class Speed extends Module {

    public Speed(int keybind, boolean defaultEnabled) {
        super("speed", keybind, defaultEnabled);
        customSettings.put("speed", new Setting<>("speed", 1.5f, value -> value >= 1.0f));
    }

    public float getSpeed() {
        @SuppressWarnings("unchecked")
        Setting<Float> speedSetting = (Setting<Float>) customSettings.get("speed");
        return speedSetting.getValue();
    }

    public void setSpeed(float newSpeed) {
        @SuppressWarnings("unchecked")
        Setting<Float> speedSetting = (Setting<Float>) customSettings.get("speed");
        speedSetting.setValue(newSpeed);
        Client.clientChat.addMessage(null, "Player speed set to " + speedSetting.getValue());
    }

    public void enable(boolean messaging) {
        if (!isEnabled()) {
            setEnabled(true);
            if (messaging) {
                Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Speed enabled");
            }
        }
    }

    public void disable(boolean messaging) {
        if (isEnabled()) {
            setEnabled(false);
            if (messaging) {
                Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Speed disabled");
            }
        }
    }

    public void toggle(boolean messaging) {
        if (isEnabled()) {
            disable(messaging);
        } else {
            enable(messaging);
        }
    }

    @Override
    public void onKeyPressed() {
        toggle(true);
    }
}
