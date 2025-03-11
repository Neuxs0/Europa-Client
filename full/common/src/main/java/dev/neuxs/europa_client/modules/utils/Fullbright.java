package dev.neuxs.europa_client.modules.utils;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.utils.Chat;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Region;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.Zone;

public class Fullbright extends Module {
    public int keybind;

    private static boolean enabled;

    public Fullbright(int keybind, boolean defaultEnabled) {
        super(keybind, defaultEnabled);
    }

    public static void toggle(World world, boolean messaging) {
        enabled = !enabled;

        if (enabled) {
            ChunkShader customChunkShader = new ChunkShader(
                    Identifier.of("europa_client", "shaders/chunk.vert.glsl"),
                    Identifier.of("europa_client", "shaders/chunk.frag.glsl")
            );
            ChunkShader customWaterShader = new ChunkShader(
                    Identifier.of("europa_client", "shaders/chunk-water.vert.glsl"),
                    Identifier.of("europa_client", "shaders/chunk-water.frag.glsl")
            );

            ChunkShader.DEFAULT_BLOCK_SHADER = customChunkShader;
            ChunkShader.WATER_BLOCK_SHADER = customWaterShader;
        } else {
            // Revert back to the default shader implementations.
            ChunkShader.initChunkShaders();
        }

        GameShader.reloadAllShaders();

        // For each chunk, force a complete rebuild of its mesh group.
        for (Zone zone : world.getZones()) {
            for (Region region : zone.getRegions()) {
                for (Chunk chunk : region.getChunks()) {
                    if (chunk.getMeshGroup() != null) {
                        chunk.getMeshGroup().dispose();
                    }

                    chunk.setMeshGroup(null);
                    GameSingletons.zoneRenderer.addChunk(chunk);
                    chunk.flagForRemeshing(true);
                }
            }
        }

        // Force the mesh generation thread to process all remesh requests.
        GameSingletons.meshGenThread.meshChunks(GameSingletons.zoneRenderer);

        if (enabled && messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Fullbright enabled");
        else Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Fullbright disabled");
    }

    public static void enable() {
        enabled = true;
    }

    public static void disable() {
        enabled = false;
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
