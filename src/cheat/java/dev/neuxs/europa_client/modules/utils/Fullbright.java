package dev.neuxs.europa_client.modules.utils;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.utils.Chat;

import java.util.Map;

public class Fullbright extends Module {
    public Fullbright(int keybind, boolean defaultEnabled) {
        super("Fullbright", keybind, defaultEnabled);
    }

    @Override
    public void enable(boolean messaging) {
        setEnabled(true);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Fullbright enabled");
    }

    @Override
    public void disable(boolean messaging) {
        setEnabled(false);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Fullbright disabled");
    }

    @Override
    public void importSettings(Map<String, Object> data) {
        boolean wasEnabled = isEnabled();
        super.importSettings(data);

        if (isEnabled() != wasEnabled) {
            if (isEnabled()) enable(false);
            else disable(false);
        }
    }
}
