package dev.neuxs.europa_client.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.settings.Setting;
import dev.neuxs.europa_client.utils.Chat;

@SuppressWarnings("unchecked")
public class Reach extends Module {

    public Reach(int keybind, boolean defaultEnabled) {
        super("Reach", keybind, defaultEnabled);
        customSettings.put("distance", new Setting<>("distance", 6.0f, value -> value >= 1.0f));
    }

    @Override
    public void enable(boolean messaging) {
        setEnabled(true);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Reach enabled");
    }

    @Override
    public void disable(boolean messaging) {
        setEnabled(false);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Reach disabled");
    }

    public float getReachDistance() {
        Setting<Float> distanceSetting = (Setting<Float>) customSettings.get("distance");
        return distanceSetting.getValue();
    }

    public void setReachDistance(float newDistance) {
        Setting<Float> distanceSetting = (Setting<Float>) customSettings.get("distance");
        distanceSetting.setValue(newDistance);
        Client.clientChat.addMessage(null, "Reach set to " + distanceSetting.getValue());
    }
}
