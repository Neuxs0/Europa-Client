package dev.neuxs.europa_client.utils;

import com.badlogic.gdx.Gdx;
import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.gamestates.ChatMenu;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;

public class InputManager {
    public InputManager() {}

    public static void Keybinds() {
        if (!(GameState.currentGameState instanceof ChatMenu)) {
            if (isFirstFrameKeyDown(Modules.fullbright.keyBind)) {
                Modules.fullbright.toggle(InGame.getWorld(), true);
            } else if (isFirstFrameKeyDown(Modules.noClip.keyBind)) {
                Modules.noClip.toggle(true);
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