package dev.neuxs.europa_client;

import dev.crmodders.cosmicquilt.api.entrypoint.ModInitializer;
import org.quiltmc.loader.api.ModContainer;

public class QuiltClient implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		Client.init();
	}
}
