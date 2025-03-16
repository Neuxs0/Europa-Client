package dev.neuxs.europa_client.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.utils.Chat;

public class Reach extends Module {
    private float reach = 6.0f; // Default game's reach distance is 6

    public Reach(int keybind, boolean defaultEnabled) {
        super(keybind, defaultEnabled);
    }

    public float getReachDistance() {
        return this.reach;
    }

    public void setReachDistance(float newSpeed) {
        this.reach = Math.max(newSpeed, 1.0f);
        Client.clientChat.addMessage(null, "Reach set to " + this.reach);
    }

    public void toggle(boolean messaging) {
        this.toggle();

        if (this.isEnabled() && messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Reach enabled");
        else Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Reach disabled");
    }

    public void enable(boolean messaging) {
        this.enable();
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Reach enabled");
    }

    public void disable(boolean messaging) {
        this.disable();
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Reach disabled");
    }
}
