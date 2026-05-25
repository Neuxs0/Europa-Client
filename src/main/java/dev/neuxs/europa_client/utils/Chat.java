package dev.neuxs.europa_client.utils;

import dev.neuxs.europa_client.settings.ClientSettings;

public class Chat {
    public static String getClientPrefix() {
        return ClientSettings.getClientChatPrefix();
    }
}
