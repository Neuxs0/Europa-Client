package dev.neuxs.europa_client.modules.utils;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.utils.Chat;

@SuppressWarnings("DuplicatedCode")
public class PacketInspector extends Module {

    public PacketInspector(int keybind, boolean defaultEnabled) {
        super("packetinspector", keybind, defaultEnabled);
    }

    public void enable(boolean messaging) {
        setEnabled(true);

        if (messaging) {
            Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Packet Inspector enabled");
        }
    }

    public void disable(boolean messaging) {
        setEnabled(false);

        if (messaging) {
            Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Packet Inspector disabled");
        }
    }

    public void toggle(boolean messaging) {
        if (isEnabled()) {
            disable(messaging);
        } else {
            enable(messaging);
        }
    }

    @Override
    public void onKeyPressed() {
        if (isEnabled()) {
            disable(true);
        } else {
            enable(true);
        }
    }
}
