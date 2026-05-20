package dev.neuxs.europa_client;

import dev.neuxs.europa_client.commands.ClientCommandRegistry;
import dev.neuxs.europa_client.managers.InputManager;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.settings.SettingsManager;
import dev.neuxs.europa_client.utils.ClientLogger;
import dev.neuxs.europa_client.utils.SyncModules;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.chat.IChat;

// TODO GUI: Add Cheat page
// TODO SETTINGS: Add support for custom profiles
// TODO GUI: Add Profiles page
// TODO SETTINGS: Add support for more custom settings
// TODO GUI: Add Settings page
// TODO GUI: Save current page and if side menu is open/closed for re-opening the menu
// TODO GUI: Add background blur (optional to user via settings)
// TODO GUI/UI: Add MSAA

@SuppressWarnings("unused")
public class Client {
    public static final String MOD_ID = "europa_client";
    public static final String MOD_NAME = "Europa Client";
    public static final String CLIENT_TYPE = "Cheat";
    public static ClientLogger LOGGER = new ClientLogger("EuropaClient");
    public static String VERSION = "2.0.0";
    public static IChat clientChat = Chat.MAIN_CLIENT_CHAT;

    public static void init() {
        LOGGER.info("{} Initializing...", MOD_NAME);

        Modules.initModules();
        SettingsManager.loadSettings();
        SettingsManager.startFileWatcher();
        ClientCommandRegistry.registerClientCommands();

        LOGGER.info("{} Initialized!", MOD_NAME);
    }

    // Called every frame
    public static void render() {
        SyncModules.Sync();
        InputManager.getInstance().initialize();
    }
}
