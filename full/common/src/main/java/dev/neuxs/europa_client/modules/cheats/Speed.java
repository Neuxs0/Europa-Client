package dev.neuxs.europa_client.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.utils.Chat;

public class Speed extends Module {
    private float speed = 1.5f;

    public Speed(int keybind, boolean defaultEnabled) {
        super(keybind, defaultEnabled);
    }

    public float getSpeed() {
        return this.speed;
    }

    public void setSpeed(float newSpeed) {
        this.speed = Math.max(0.1f, Math.min(newSpeed, 10.0f));
        Client.clientChat.addMessage(null, "Player speed set to " + this.speed);
    }

    public void toggle(boolean messaging) {
        this.toggle();

        if (this.isEnabled() && messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Speed enabled");
        else Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Speed disabled");
    }

    public void enable(boolean messaging) {
        this.enable();
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Speed enabled");
    }

    public void disable(boolean messaging) {
        this.disable();
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Speed disabled");
    }
}
