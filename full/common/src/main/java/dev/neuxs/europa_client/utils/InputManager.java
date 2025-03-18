package dev.neuxs.europa_client.utils;

import com.badlogic.gdx.Gdx;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.gamestates.ChatMenu;
import finalforeach.cosmicreach.gamestates.GameState;

public class InputManager {
    public InputManager() {}

    public static void Keybinds() {
        if (GameState.currentGameState instanceof ChatMenu) {
            return;
        }
        for (Module module : Modules.moduleList) {
            int key = module.getKeybind();
            if (key == 0) {
                continue;
            }
            if (isFirstFrameKeyDown(key)) {
                module.onKeyPressed();
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
}
