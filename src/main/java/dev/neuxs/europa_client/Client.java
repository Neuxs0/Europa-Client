package dev.neuxs.europa_client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import dev.crmodders.cosmicquilt.api.entrypoint.ModInitializer;
import dev.neuxs.europa_client.utils.InputManager;
import org.quiltmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.neuxs.europa_client.commands.ClientCommandRegistry.registerClientCommands;
import static dev.neuxs.europa_client.modules.ModuleInit.initCheats;
import static dev.neuxs.europa_client.modules.cheats.NoClip.toggleNoClip;

public class Client implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("EuropaClient");

	@Override
	public void onInitialize(ModContainer mod) {
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
