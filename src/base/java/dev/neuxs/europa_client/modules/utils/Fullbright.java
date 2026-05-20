package dev.neuxs.europa_client.modules.utils;

import com.badlogic.gdx.Gdx;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.utils.Chat;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Region;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.gamestates.InGame;

import java.util.Map;

@SuppressWarnings("DuplicatedCode")
public class Fullbright extends Module {
    public ChunkShader blockShader;
    public ChunkShader waterShader;
    private int toggleRequestId;

    public Fullbright(int keybind, boolean defaultEnabled) {
        super("fullbright", keybind, defaultEnabled);
    }

    public void enable(boolean messaging) {
        setEnabled(true);
        int requestId = ++toggleRequestId;
        runOnRenderThread(() -> {
            if (requestId != toggleRequestId) return;
            if (!isEnabled()) return;

            if (this.blockShader == null) {
                this.blockShader = new ChunkShader(
                        Identifier.of("europa_client", "shaders/chunk.vert.glsl"),
                        Identifier.of("europa_client", "shaders/chunk.frag.glsl")
                );
            }
            if (this.waterShader == null) {
                this.waterShader = new ChunkShader(
                        Identifier.of("europa_client", "shaders/chunk-water.vert.glsl"),
                        Identifier.of("europa_client", "shaders/chunk-water.frag.glsl")
                );
            }

            GameShader.reloadAllShaders();
            remeshWorld();
        });

        if (messaging) {
            Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Fullbright enabled");
        }
    }

    public void disable(boolean messaging) {
        setEnabled(false);
        int requestId = ++toggleRequestId;
        runOnRenderThread(() -> {
            if (requestId != toggleRequestId) return;
            if (isEnabled()) return;

            ChunkShader.initChunkShaders();
            GameShader.reloadAllShaders();
            remeshWorld();
        });

        if (messaging) {
            Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Fullbright disabled");
        }
    }

    @Override
    public void importSettings(Map<String, Object> data) {
        boolean wasEnabled = isEnabled();
        super.importSettings(data);

        if (isEnabled() != wasEnabled) {
            if (isEnabled()) enable(false);
            else disable(false);
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

    private void runOnRenderThread(Runnable runnable) {
        if (Gdx.app == null) {
            Client.LOGGER.error("Cannot toggle Fullbright before the render application is ready.");
            return;
        }

        Gdx.app.postRunnable(runnable);
    }

    private void remeshWorld() {
        World world = InGame.getWorld();
        if (world == null) return;

        for (Zone zone : world.getZones()) {
            if (zone == null) continue;
            for (Region region : zone.getRegions()) {
                if (region == null) continue;
                for (Chunk chunk : region.getChunks()) {
                    if (chunk == null) continue;
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
    }
}
