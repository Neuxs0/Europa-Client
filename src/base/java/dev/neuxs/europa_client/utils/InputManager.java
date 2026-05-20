package dev.neuxs.europa_client.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.ui.GUI;
import finalforeach.cosmicreach.gamestates.ChatMenu;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;

@SuppressWarnings("unused")
public class InputManager {
    public InputManager() {}

    public static void Keybinds() {
        if (GameState.currentGameState instanceof ChatMenu ||
            GameState.currentGameState instanceof GUI
        ) {
            return;
        }

        if (GameState.currentGameState instanceof InGame && isFirstFrameKeyDown(Input.Keys.GRAVE)) {
            Gdx.input.setCursorCatched(false);
            GameState.switchToGameState(new GUI());
            return;
        }

        for (Module module : Modules.moduleList) {
            int key = module.getKeybind();
            if (!isValidModuleKeybind(key)) {
                continue;
            }
            if (isFirstFrameKeyDown(key)) {
                module.onKeyPressed();
            }
        }
    }

    private static boolean isValidModuleKeybind(int keycode) {
        return keycode > 0 && keycode != Input.Keys.UNKNOWN;
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
