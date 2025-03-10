package dev.neuxs.europa_client.commands.utils;

import com.badlogic.gdx.Gdx;
import dev.neuxs.europa_client.commands.ClientCommand;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.gamestates.*;
import finalforeach.cosmicreach.io.ChunkSaver;
import finalforeach.cosmicreach.networking.client.ClientNetworkManager;

public class QuitGameCommand extends ClientCommand {

    @Override
    public void run(IChat chat) {
        Gdx.app.postRunnable(() -> {
            if (Gdx.input.isCursorCatched()) Gdx.input.setCursorCatched(false);

            if (GameSingletons.isHost) {
                ChunkSaver.saveWorld(InGame.getWorld());
            } else {
                try {
                    ClientNetworkManager.CLIENT.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            GameState.switchToGameState(InGame.IN_GAME);
            GameState.switchToGameState(new MainMenu());
            System.exit(0);
        });
    }

    @Override
    public String getDescription() {
        return "Closes the game quickly and safely.";
    }
}
