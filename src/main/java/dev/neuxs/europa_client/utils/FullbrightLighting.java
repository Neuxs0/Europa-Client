package dev.neuxs.europa_client.utils;

import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Region;
import finalforeach.cosmicreach.world.Zone;

public final class FullbrightLighting {
    public static final float MAX_DAYLIGHT_BRIGHTNESS = 0.75F;
    public static final int MAX_SKYLIGHT = 15;
    public static final short XRAY_ORE_MARKER_BLOCKLIGHT = (short) (15 << 8);

    private FullbrightLighting() {
    }

    public static void remeshLoadedChunks() {
        remeshLoadedChunks(false);
    }

    public static void remeshLoadedChunks(boolean immediate) {
        Player player;
        try {
            player = InGame.getLocalPlayer();
        } catch (RuntimeException | LinkageError e) {
            return;
        }
        if (player == null) {
            return;
        }

        Zone zone = player.getZone();
        if (zone == null) {
            return;
        }

        for (Region region : zone.getRegions()) {
            if (region == null) {
                continue;
            }

            for (Chunk chunk : region.getChunks()) {
                if (chunk != null) {
                    chunk.flagForRemeshing(immediate);
                }
            }
        }
    }
}
