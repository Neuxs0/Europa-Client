package dev.neuxs.europa_client;

import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.client.ClientModInit;

public class PuzzleClient implements ClientModInit {
    @Override
    public void onClientInit() {
        Client.init();
    }
}
