package dev.neuxs.europa_client.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.utils.Chat;

public class JetpackHeight extends Module {
    private static final float UNLIMITED_HEIGHT_ALLOWANCE = 1_000_000.0f;

    public JetpackHeight(int keybind, boolean defaultEnabled) {
        super("JetpackHeight", keybind, defaultEnabled);
    }

    @Override
    public void enable(boolean messaging) {
        setEnabled(true);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Jetpack height enabled");
    }

    @Override
    public void disable(boolean messaging) {
        setEnabled(false);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Jetpack height disabled");
    }

    public float getHeightAllowance(float vanillaHeightAllowance) {
        if (!isEnabled()) {
            return vanillaHeightAllowance;
        }

        return UNLIMITED_HEIGHT_ALLOWANCE;
    }
}
