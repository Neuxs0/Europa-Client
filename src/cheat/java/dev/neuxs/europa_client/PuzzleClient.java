package dev.neuxs.europa_client;

import dev.neuxs.europa_client.variant.CheatVariant;
import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.client.ClientModInit;

public class PuzzleClient implements ClientModInit {
    @Override
    public void onClientInit() {
        Client.init(new CheatVariant());
    }
}
