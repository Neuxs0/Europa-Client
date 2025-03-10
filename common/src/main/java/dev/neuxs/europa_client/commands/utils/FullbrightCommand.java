package dev.neuxs.europa_client.commands.utils;

import dev.neuxs.europa_client.commands.ClientCommand;
import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Region;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.GameSingletons;

import static dev.neuxs.europa_client.utils.ChatFormater.getClientPrefix;

public class FullbrightCommand extends ClientCommand {
    public static boolean enabled = false;

    public static void toggleFullbright(World world) {
        enabled = !enabled;

        if (enabled) {
            // Create custom fullbright chunk shaders.
            ChunkShader customChunkShader = new ChunkShader(
                    Identifier.of("europa_client", "shaders/chunk.vert.glsl"),
                    Identifier.of("europa_client", "shaders/chunk.frag.glsl")
            );
            ChunkShader customWaterShader = new ChunkShader(
                    Identifier.of("europa_client", "shaders/chunk-water.vert.glsl"),
                    Identifier.of("europa_client", "shaders/chunk-water.frag.glsl")
            );
            // Replace the default shaders.
            ChunkShader.DEFAULT_BLOCK_SHADER = customChunkShader;
            ChunkShader.WATER_BLOCK_SHADER = customWaterShader;
        } else {
            // Revert to the default shader implementations.
            ChunkShader.initChunkShaders();
        }

        // Reload all shaders so that the new shader sources are compiled.
        GameShader.reloadAllShaders();

        // Iterate over every zone, region, and chunk to force remeshing.
        for (Zone zone : world.getZones()) {
            for (Region region : zone.getRegions()) {
                for (Chunk chunk : region.getChunks()) {
                    // Use true to indicate immediate update.
                    chunk.flagForRemeshing(true);
                }
            }
        }

        // Force the mesh generation thread to process all queued chunk remesh requests immediately.
        GameSingletons.meshGenThread.meshChunks(GameSingletons.zoneRenderer);
    }

    @Override
    public void run(IChat chat) {
        toggleFullbright(InGame.getWorld());
        if (enabled) {
            chat.addMessage(null, getClientPrefix() + "Fullbright Enabled and chunks updated immediately");
        } else {
            chat.addMessage(null, getClientPrefix() + "Fullbright Disabled and chunks updated immediately");
        }
    }

    @Override
    public String getDescription() {
        return "Toggles fullbright and forces immediate chunk regeneration.";
    }
}
