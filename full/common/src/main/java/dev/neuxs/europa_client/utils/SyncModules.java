package dev.neuxs.europa_client.utils;

import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.entities.player.Player;

public class SyncModules {
    public static void Sync() {
        Player localPlayer = InGame.getLocalPlayer();

        // Sync no-clip
        if (localPlayer != null) {
            boolean worldNoClipState = localPlayer.getEntity().isNoClip();
            if (Modules.noClip.isEnabled() != worldNoClipState) {
                Modules.noClip.setEnabled(worldNoClipState);

                System.out.println("Synced no-clip setting from world: " + worldNoClipState);
            }
        }
    }
}
