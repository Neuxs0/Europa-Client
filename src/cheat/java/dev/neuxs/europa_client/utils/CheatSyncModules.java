package dev.neuxs.europa_client.utils;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.CheatModules;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;

public final class CheatSyncModules {
    private CheatSyncModules() {
    }

    public static void syncNoClip() {
        if (CheatModules.noClip == null) {
            return;
        }

        Player localPlayer = InGame.getLocalPlayer();

        if (localPlayer != null) {
            boolean worldNoClipState = localPlayer.getEntity().isNoClip();
            if (CheatModules.noClip.isEnabled() != worldNoClipState) {
                CheatModules.noClip.setEnabled(worldNoClipState);

                Client.LOGGER.info("Synced no-clip setting from world: {}", worldNoClipState);
            }
        }
    }
}
