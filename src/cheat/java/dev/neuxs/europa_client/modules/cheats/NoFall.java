package dev.neuxs.europa_client.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.utils.Chat;

public class NoFall extends Module {
    public NoFall(int keybind, boolean defaultEnabled) {
        super("NoFall", keybind, defaultEnabled);
    }

    @Override
    public void enable(boolean messaging) {
        setEnabled(true);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "No-Fall enabled");
    }

    @Override
    public void disable(boolean messaging) {
        setEnabled(false);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "No-Fall disabled");
    }
}
