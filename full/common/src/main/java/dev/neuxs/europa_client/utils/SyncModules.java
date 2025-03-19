package dev.neuxs.europa_client.utils;

import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.util.Identifier;

import java.util.Objects;

public class SyncModules {
    // Somewhere in your initialization code, define a constant base shader.
    public static final ChunkShader BASE_BLOCK_SHADER =
            new ChunkShader(Identifier.of("base", "shaders/chunk.vert.glsl"),
                    Identifier.of("base", "shaders/chunk.frag.glsl"));
    public static final ChunkShader BASE_WATER_SHADER =
            new ChunkShader(Identifier.of("base", "shaders/chunk-water.vert.glsl"),
                    Identifier.of("base", "shaders/chunk-water.frag.glsl"));

    public static void Sync() {
        syncNoClip();
        syncFullbright();
    }

    public static void syncNoClip() {
        Player localPlayer = InGame.getLocalPlayer();

        if (localPlayer != null) {
            boolean worldNoClipState = localPlayer.getEntity().isNoClip();
            if (Modules.noClip.isEnabled() != worldNoClipState) {
                Modules.noClip.setEnabled(worldNoClipState);

                System.out.println("Synced no-clip setting from world: " + worldNoClipState);
            }
        }
    }

    public static void syncFullbright() {
        if (Modules.fullbright.isEnabled()) {
            if (ChunkShader.DEFAULT_BLOCK_SHADER != Modules.fullbright.blockShader ||
                    ChunkShader.WATER_BLOCK_SHADER != Modules.fullbright.waterShader) {
                Modules.fullbright.enable(false);
                System.out.println("Reapplied fullbright shaders.");
            }
        } else {
            if (ChunkShader.DEFAULT_BLOCK_SHADER == Modules.fullbright.blockShader ||
                    ChunkShader.WATER_BLOCK_SHADER == Modules.fullbright.waterShader) {
                Modules.fullbright.disable(false);
                System.out.println("Reapplied base shaders because fullbright is disabled.");
            }
        }
    }
}
