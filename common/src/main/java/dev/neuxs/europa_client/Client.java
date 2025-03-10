package dev.neuxs.europa_client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.neuxs.europa_client.commands.ClientCommandRegistry.registerClientCommands;
import static dev.neuxs.europa_client.modules.ModuleInit.initCheats;
import static dev.neuxs.europa_client.modules.cheats.NoClip.toggleNoClip;

public class Client {
    public static Logger LOGGER = LoggerFactory.getLogger("EuropaClient");

    public static void init() {
        initCheats();
        registerClientCommands();

        LOGGER.info("Europa Client Initialized!");
    }

    public static void render() {
//		InputManager.Keybinds();
        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
            toggleNoClip();
        }
    }
}
