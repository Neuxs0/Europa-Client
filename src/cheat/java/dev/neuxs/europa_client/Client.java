package dev.neuxs.europa_client;

import dev.neuxs.europa_client.commands.ClientCommandRegistry;
import dev.neuxs.europa_client.managers.InputManager;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.settings.ProfileManager;
import dev.neuxs.europa_client.settings.SettingsManager;
import dev.neuxs.europa_client.utils.ClientLogger;
import dev.neuxs.europa_client.utils.SyncModules;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.chat.IChat;

@SuppressWarnings("unused")
public class Client {
    public static final String MOD_ID = "europa_client";
    public static final String MOD_NAME = "Europa Client";
    public static final String CLIENT_TYPE = "Cheat";
    public static ClientLogger LOGGER = new ClientLogger("EuropaClient");
    public static String VERSION = "2.0.0";
    public static IChat clientChat = Chat.MAIN_CLIENT_CHAT;

    public static String getNetworkIdentifier() {
        return MOD_NAME + " " + CLIENT_TYPE + "/" + VERSION;
    }

    public static void init() {
        LOGGER.info("{} Initializing...", MOD_NAME);

        Modules.initModules();
        SettingsManager.loadSettings();
        ProfileManager.initialize();
        SettingsManager.loadClientSettings();
        SettingsManager.startFileWatcher();
        ClientCommandRegistry.registerClientCommands();

        LOGGER.info("{} Initialized!", MOD_NAME);
    }

    // Called every frame
    public static void render() {
        SettingsManager.reloadClientSettingsIfChanged();
        SyncModules.Sync();
        InputManager.getInstance().initialize();
    }
}
