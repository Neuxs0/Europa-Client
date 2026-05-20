package dev.neuxs.europa_client.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.settings.Setting;
import dev.neuxs.europa_client.utils.Chat;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;

@SuppressWarnings("unchecked")
public class NoClip extends Module {

    public NoClip(int keybind, boolean defaultEnabled) {
        super("NoClip", keybind, defaultEnabled);
        customSettings.put("speed", new Setting<>("speed", 1.0f, value -> value >= 1.0f)
                .withRange(1.0f, 4.0f));
    }

    @Override
    public void enable(boolean messaging) {
        if (!isEnabled()) {
            setEnabled(true);
            setNoClip(InGame.getLocalPlayer(), true);
            if (messaging) {
                Client.clientChat.addMessage(null, Chat.getClientPrefix() + "No-Clip enabled");
            }
        }
    }

    @Override
    public void disable(boolean messaging) {
        if (isEnabled()) {
            setEnabled(false);
            setNoClip(InGame.getLocalPlayer(), false);
            if (messaging) {
                Client.clientChat.addMessage(null, Chat.getClientPrefix() + "No-Clip disabled");
            }
        }
    }

    public void setNoClip(Player player, boolean noClip) {
        player.getEntity().setNoClip(noClip);
        if (noClip) {
            player.getEntity().velocity.setZero();
        }
    }
    public void setSpeed(float newSpeed) {
        Setting<Float> speedSetting = (Setting<Float>) customSettings.get("speed");
        speedSetting.setValue(newSpeed);
        Client.clientChat.addMessage(null, "No-clip speed set to " + speedSetting.getValue());
    }

    public float getSpeed() {
        Setting<Float> speedSetting = (Setting<Float>) customSettings.get("speed");
        return speedSetting.getValue();
    }
}
