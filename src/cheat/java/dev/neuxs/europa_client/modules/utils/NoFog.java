package dev.neuxs.europa_client.modules.utils;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.utils.Chat;

public class NoFog extends Module {
    public NoFog(int keybind, boolean defaultEnabled) {
        super("NoFog", keybind, defaultEnabled);
    }

    @Override
    public void enable(boolean messaging) {
        setEnabled(true);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "No-Fog enabled");
    }

    @Override
    public void disable(boolean messaging) {
        setEnabled(false);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "No-Fog disabled");
    }
}
