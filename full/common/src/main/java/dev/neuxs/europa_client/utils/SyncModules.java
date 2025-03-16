package dev.neuxs.europa_client.utils;

import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.gamestates.InGame;

public class SyncModules {
    public SyncModules() {}

    public static void Sync() {
        // Sync no-clip
        if (InGame.getLocalPlayer().getEntity().isNoClip() != Modules.noClip.isEnabled()) {
            Modules.noClip.enable(false);
        }
    }
}
