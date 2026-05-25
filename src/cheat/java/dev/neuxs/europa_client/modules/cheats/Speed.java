package dev.neuxs.europa_client.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.settings.Setting;
import dev.neuxs.europa_client.utils.Chat;

@SuppressWarnings("unchecked")
public class Speed extends Module {

    public Speed(int keybind, boolean defaultEnabled) {
        super("Speed", keybind, defaultEnabled);
        customSettings.put("speed", new Setting<>("speed", 1.5f, value -> value >= 1.0f)
                .withRange(1.0f, 6.0f));
        customSettings.put("jetpackSpeed", new Setting<>("jetpackSpeed", 1.5f, value -> value >= 1.0f)
                .withRange(1.0f, 6.0f)
                .withDescription("Movement multiplier while the vanilla jetpack is active"));
    }

    @Override
    public void enable(boolean messaging) {
        setEnabled(true);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Speed enabled");
    }

    @Override
    public void disable(boolean messaging) {
        setEnabled(false);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Speed disabled");
    }

    public float getSpeed() {
        Setting<Float> speedSetting = (Setting<Float>) customSettings.get("speed");
        return speedSetting.getValue();
    }

    public void setSpeed(float newSpeed) {
        Setting<Float> speedSetting = (Setting<Float>) customSettings.get("speed");
        speedSetting.setValue(newSpeed);
        Client.clientChat.addMessage(null, "Player speed set to " + speedSetting.getValue());
    }

    public float getJetpackSpeed() {
        Setting<Float> speedSetting = (Setting<Float>) customSettings.get("jetpackSpeed");
        return speedSetting.getValue();
    }

    public void setJetpackSpeed(float newSpeed) {
        Setting<Float> speedSetting = (Setting<Float>) customSettings.get("jetpackSpeed");
        speedSetting.setValue(newSpeed);
        Client.clientChat.addMessage(null, "Jetpack speed set to " + speedSetting.getValue());
    }
}
