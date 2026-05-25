package dev.neuxs.europa_client.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.utils.Chat;
import dev.neuxs.europa_client.utils.FullbrightLighting;

import java.util.Map;

public class Xray extends Module {
    public Xray(int keybind, boolean defaultEnabled) {
        super("Xray", keybind, defaultEnabled);
    }

    @Override
    public void enable(boolean messaging) {
        setEnabled(true);
        FullbrightLighting.remeshLoadedChunks();
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Xray enabled");
    }

    @Override
    public void disable(boolean messaging) {
        setEnabled(false);
        FullbrightLighting.remeshLoadedChunks();
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Xray disabled");
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
