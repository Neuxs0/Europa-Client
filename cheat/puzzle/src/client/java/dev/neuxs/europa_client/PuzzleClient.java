package dev.neuxs.europa_client;

import com.github.puzzle.core.loader.launch.provider.mod.entrypoint.impls.ClientModInitializer;

public class PuzzleClient implements ClientModInitializer {
    @Override
    public void onInit() {
        Client.init();
    }
}