package dev.neuxs.europa_client.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.ModuleInit;
import finalforeach.cosmicreach.gamestates.ChatMenu;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;

import static dev.neuxs.europa_client.modules.cheats.NoClip.toggleNoClip;

public class InputManager {
    public InputManager() {}

    public static boolean clickGUIEnabled = false;

    public static void Keybinds() {
        if (!(GameState.currentGameState instanceof ChatMenu)) {
            if (isFirstFrameKeyDown(Input.Keys.SHIFT_RIGHT)) {
                if (!clickGUIEnabled) {
//                    GameState.switchToGameState(new ClickGUI());
                    Client.LOGGER.info("ClickGUI enabled");
                } else {
                    GameState.switchToGameState(new InGame());
                    Client.LOGGER.info("ClickGUI disabled");
                }
                clickGUIEnabled = !clickGUIEnabled;
            }
        }

//        if (GameState.currentGameState instanceof ClickGUI) {
//            if (isFirstFrameKeyDown(Input.Keys.ESCAPE)) {
//                GameState.switchToGameState(new InGame());
//                Client.LOGGER.info("ClickGUI disabled");
//            }
//        }
    }

    public static boolean isKeyDown(int keycode) {
        return Gdx.input.isKeyPressed(keycode);
    }

    public static boolean isKeyUp(int keycode) {
        return !Gdx.input.isKeyPressed(keycode);
    }

    public static boolean isFirstFrameKeyDown(int keycode) {
        return Gdx.input.isKeyJustPressed(keycode);
    }
}