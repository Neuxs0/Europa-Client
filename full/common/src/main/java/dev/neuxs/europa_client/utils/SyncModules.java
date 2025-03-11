package dev.neuxs.europa_client.utils;

import dev.neuxs.europa_client.modules.cheats.NoClip;
import finalforeach.cosmicreach.gamestates.InGame;

public class SyncModules {
    public SyncModules() {}

    public static void Sync() {
        // Sync no-clip
        if (!InGame.getLocalPlayer().getEntity().noClip) {
            if (NoClip.isEnabled()) {
                NoClip.disable();
                NoClip.toggle(false);
            }
        }
    }
}
