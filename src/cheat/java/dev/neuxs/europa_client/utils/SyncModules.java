package dev.neuxs.europa_client.utils;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.entities.player.Player;

public class SyncModules {

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

                Client.LOGGER.info("Synced no-clip setting from world: {}", worldNoClipState);
            }
        }
    }

    public static void syncFullbright() {
        if (Modules.fullbright.isEnabled()) {
            Client.LOGGER.debug("Fullbright is enabled.");
        } else {
            Client.LOGGER.debug("Fullbright is disabled.");
        }
    }
}
