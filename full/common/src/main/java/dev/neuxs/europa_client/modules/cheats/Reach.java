package dev.neuxs.europa_client.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.settings.Setting;
import dev.neuxs.europa_client.utils.Chat;

public class Reach extends Module {

    public Reach(int keybind, boolean defaultEnabled) {
        super("reach", keybind, defaultEnabled);
        customSettings.put("distance", new Setting<>("distance", 6.0f, value -> value >= 1.0f));
    }

    public float getReachDistance() {
        @SuppressWarnings("unchecked")
        Setting<Float> distanceSetting = (Setting<Float>) customSettings.get("distance");
        return distanceSetting.getValue();
    }

    public void setReachDistance(float newDistance) {
        @SuppressWarnings("unchecked")
        Setting<Float> distanceSetting = (Setting<Float>) customSettings.get("distance");
        distanceSetting.setValue(newDistance);
        Client.clientChat.addMessage(null, "Reach set to " + distanceSetting.getValue());
    }

    public void enable(boolean messaging) {
        if (!isEnabled()) {
            setEnabled(true);
            if (messaging) {
                Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Reach enabled");
            }
        }
    }

    public void disable(boolean messaging) {
        if (isEnabled()) {
            setEnabled(false);
            if (messaging) {
                Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Reach disabled");
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
