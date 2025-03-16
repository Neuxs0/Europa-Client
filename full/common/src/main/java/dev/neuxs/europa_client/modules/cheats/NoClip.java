package dev.neuxs.europa_client.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.utils.Chat;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;

public class NoClip extends Module {
    private float speed = 1.0f;

    public NoClip(int keybind, boolean defaultEnabled) {
        super(keybind, defaultEnabled);
    }

    public void setNoClip(Player player, boolean noClip) {
        player.getEntity().setNoClip(noClip);
        if (noClip) player.getEntity().velocity.setZero();
    }

    public void setSpeed(float newSpeed) {
        this.speed = Math.max(0.1f, Math.min(newSpeed, 10.0f));
        Client.clientChat.addMessage(null, "No-clip speed set to " + this.speed);
    }

    public float getSpeed() {
        return this.speed;
    }

    public void toggle(boolean messaging) {
        this.toggle();
        setNoClip(InGame.getLocalPlayer(), this.isEnabled());
        if (this.isEnabled() && messaging)
            Client.clientChat.addMessage(null, Chat.getClientPrefix() + "No-clip enabled");
        else
            Client.clientChat.addMessage(null, Chat.getClientPrefix() + "No-clip disabled");
    }

    public void enable(boolean messaging) {
        setNoClip(InGame.getLocalPlayer(), true);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "No-clip enabled");
    }

    public void disable(boolean messaging) {
        setNoClip(InGame.getLocalPlayer(), false);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "No-clip disabled");
    }
}
