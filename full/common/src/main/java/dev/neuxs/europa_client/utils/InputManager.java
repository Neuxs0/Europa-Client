package dev.neuxs.europa_client.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.ModuleInit;
import dev.neuxs.europa_client.modules.cheats.NoClip;
import dev.neuxs.europa_client.modules.utils.Fullbright;
import finalforeach.cosmicreach.gamestates.ChatMenu;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;

public class InputManager {
    public InputManager() {}

    public static void Keybinds() {
        if (!(GameState.currentGameState instanceof ChatMenu)) {
            if (isFirstFrameKeyDown(ModuleInit.fullbrightKeybind)) {
                Fullbright.toggle(InGame.getWorld(), true);
            } else if (isFirstFrameKeyDown(ModuleInit.noClipKeybind)) {
                NoClip.toggle(true);
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