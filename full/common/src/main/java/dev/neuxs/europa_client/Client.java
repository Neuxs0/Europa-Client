package dev.neuxs.europa_client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import dev.neuxs.europa_client.commands.ClientCommandRegistry;
import dev.neuxs.europa_client.modules.ModuleInit;
import dev.neuxs.europa_client.modules.cheats.NoClip;
import dev.neuxs.europa_client.utils.InputManager;
import dev.neuxs.europa_client.utils.SyncModules;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.gamestates.InGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
    public static Logger LOGGER = LoggerFactory.getLogger("EuropaClient");
    public static String VERSION = "1.2.0";
    public static boolean playerDied = false;
    public static IChat clientChat = Chat.MAIN_CLIENT_CHAT;

    public static void init() {
        ModuleInit.initModules();
        ClientCommandRegistry.registerClientCommands();

        LOGGER.info("Europa Client Initialized!");
    }

    public static void render() {
        SyncModules.Sync();
        InputManager.Keybinds();
    }
}
