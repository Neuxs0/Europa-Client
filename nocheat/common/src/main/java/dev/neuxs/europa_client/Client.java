package dev.neuxs.europa_client;

import dev.neuxs.europa_client.commands.ClientCommandRegistry;
import dev.neuxs.europa_client.modules.ModuleInit;
import dev.neuxs.europa_client.utils.InputManager;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.chat.IChat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
    public static Logger LOGGER = LoggerFactory.getLogger("EuropaClient-NoCheat");
    public static String VERSION = "1.2.0";
    public static IChat clientChat = Chat.MAIN_CLIENT_CHAT;

    public static void init() {
        ModuleInit.initModules();
        ClientCommandRegistry.registerClientCommands();

        LOGGER.info("Europa Client Initialized!");
    }

    public static void render() {
        InputManager.Keybinds();
    }
}
