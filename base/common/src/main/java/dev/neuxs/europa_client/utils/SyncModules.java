package dev.neuxs.europa_client.utils;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;

public class SyncModules {

    public static void Sync() {
        syncFullbright();
    }

    public static void syncFullbright() {
        if (Modules.fullbright.isEnabled()) {
            if (ChunkShader.DEFAULT_BLOCK_SHADER != Modules.fullbright.blockShader ||
                    ChunkShader.WATER_BLOCK_SHADER != Modules.fullbright.waterShader) {
                Modules.fullbright.enable(false);
                Client.LOGGER.info("Reapplied fullbright shaders.");
            }
        } else {
            if (ChunkShader.DEFAULT_BLOCK_SHADER == Modules.fullbright.blockShader ||
                    ChunkShader.WATER_BLOCK_SHADER == Modules.fullbright.waterShader) {
                Modules.fullbright.disable(false);
                Client.LOGGER.info("Reapplied base shaders because fullbright is disabled.");
            }
        }
    }
}
