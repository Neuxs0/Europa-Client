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
import finalforeach.cosmicreach.gamestates.InGame;

public class Fullbright extends Module {
    public ChunkShader blockShader;
    public ChunkShader waterShader;

    public Fullbright(int keybind, boolean defaultEnabled) {
        super("fullbright", keybind, defaultEnabled);
    }

    public void enable(boolean messaging) {
        setEnabled(true);
        World world = InGame.getWorld();

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
        this.blockShader = customChunkShader;
        this.waterShader = customWaterShader;
        GameShader.reloadAllShaders();

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
        GameSingletons.meshGenThread.meshChunks(GameSingletons.zoneRenderer);

        if (messaging) {
            Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Fullbright enabled");
        }
    }

    public void disable(boolean messaging) {
        setEnabled(false);
        World world = InGame.getWorld();

        ChunkShader.initChunkShaders();
        GameShader.reloadAllShaders();

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
        GameSingletons.meshGenThread.meshChunks(GameSingletons.zoneRenderer);

        if (messaging) {
            Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Fullbright disabled");
        }
    }

    public void toggle(boolean messaging) {
        if (isEnabled()) {
            disable(messaging);
        } else {
            enable(messaging);
        }
    }

    @Override
    public void onKeyPressed() {
        if (isEnabled()) {
            disable(true);
        } else {
            enable(true);
        }
    }
}
