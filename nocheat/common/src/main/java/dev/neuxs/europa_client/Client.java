package dev.neuxs.europa_client;

import dev.neuxs.europa_client.commands.ClientCommandRegistry;
import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.chat.IChat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
    public static final String MOD_ID = "europa_client";
    public static final String MOD_NAME = "Europa Client No-Cheat";
    public static Logger LOGGER = LoggerFactory.getLogger("EuropaClient");
    public static String VERSION = "1.2.0";
    public static IChat clientChat = Chat.MAIN_CLIENT_CHAT;

    public static void init() {
        LOGGER.info("Europa Client Initializing...");

        Modules.initModules();
        ClientCommandRegistry.registerClientCommands();

        LOGGER.info("Europa Client Initialized!");
    }
}
