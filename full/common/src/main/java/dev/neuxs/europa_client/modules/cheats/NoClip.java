package dev.neuxs.europa_client.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.settings.Setting;
import dev.neuxs.europa_client.utils.Chat;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;

public class NoClip extends Module {

    public NoClip(int keybind, boolean defaultEnabled) {
        super("noclip", keybind, defaultEnabled);
        customSettings.put("speed", new Setting<>("speed", 1.0f, value -> value >= 1.0f));
    }

    public void setNoClip(Player player, boolean noClip) {
        player.getEntity().setNoClip(noClip);
        if (noClip) {
            player.getEntity().velocity.setZero();
        }
    }

    public void setSpeed(float newSpeed) {
        @SuppressWarnings("unchecked")
        Setting<Float> speedSetting = (Setting<Float>) customSettings.get("speed");
        speedSetting.setValue(newSpeed);
        Client.clientChat.addMessage(null, "No-clip speed set to " + speedSetting.getValue());
    }

    public float getSpeed() {
        @SuppressWarnings("unchecked")
        Setting<Float> speedSetting = (Setting<Float>) customSettings.get("speed");
        return speedSetting.getValue();
    }

    public void enable(boolean messaging) {
        if (!isEnabled()) {
            setEnabled(true);
            setNoClip(InGame.getLocalPlayer(), true);
            if (messaging) {
                Client.clientChat.addMessage(null, Chat.getClientPrefix() + "No-clip enabled");
            }
        }
    }

    public void disable(boolean messaging) {
        if (isEnabled()) {
            setEnabled(false);
            setNoClip(InGame.getLocalPlayer(), false);
            if (messaging) {
                Client.clientChat.addMessage(null, Chat.getClientPrefix() + "No-clip disabled");
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
}
