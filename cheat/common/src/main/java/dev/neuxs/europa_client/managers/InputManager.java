package dev.neuxs.europa_client.managers;

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

        for (Module module : Modules.utilModuleList) {
            int key = module.getKeybind();
            if (key == 0) {
                continue;
            }
            if (isFirstFrameKeyDown(key)) {
                module.onKeyPressed(true);
            }
        }

        for (Module module : Modules.cheatModuleList) {
            int key = module.getKeybind();
            if (key == 0) {
                continue;
            }
            if (isFirstFrameKeyDown(key)) {
                module.onKeyPressed(true);
            }
        }
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

    public static boolean isMouseButtonDown(int keycode) {
        return Gdx.input.isButtonPressed(keycode);
    }

    public static boolean isMouseButtonUp(int keycode) {
        return !Gdx.input.isButtonPressed(keycode);
    }

    public static boolean isFirstFrameMouseButtonDown(int keycode) {
        return Gdx.input.isButtonJustPressed(keycode);
    }
}
