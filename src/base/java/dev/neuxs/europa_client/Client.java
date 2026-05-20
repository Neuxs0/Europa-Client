package dev.neuxs.europa_client;

import dev.neuxs.europa_client.commands.ClientCommandRegistry;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.InputManager;
import dev.neuxs.europa_client.utils.SyncModules;
import dev.neuxs.europa_client.utils.ClientLogger;
import dev.neuxs.europa_client.settings.SettingsManager;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.chat.IChat;

public class Client {
    public static final String MOD_ID = "europa_client";
    public static final String MOD_NAME = "Europa Client";
    public static ClientLogger LOGGER = new ClientLogger("EuropaClient");
    public static String VERSION = "2.0.0";
    public static IChat clientChat = Chat.MAIN_CLIENT_CHAT;

    public static void init() {
        LOGGER.info("Europa Client Initializing...");

        Modules.initModules();
        SettingsManager.loadSettings();
        SettingsManager.startFileWatcher();
        ClientCommandRegistry.registerClientCommands();

        LOGGER.info("Europa Client Initialized!");
    }

    public static void render() {
        SyncModules.Sync();
        InputManager.Keybinds();
    }
}
