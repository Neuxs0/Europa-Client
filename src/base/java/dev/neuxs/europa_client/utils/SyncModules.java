package dev.neuxs.europa_client.utils;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Modules;

public class SyncModules {

    public static void Sync() {
        syncFullbright();
    }

    public static void syncFullbright() {
        if (Modules.fullbright.isEnabled()) {
            Client.LOGGER.debug("Fullbright is enabled.");
        } else {
            Client.LOGGER.debug("Fullbright is disabled.");
        }
    }
}
