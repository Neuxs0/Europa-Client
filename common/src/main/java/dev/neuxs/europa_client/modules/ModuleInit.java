package dev.neuxs.europa_client.modules;

import com.badlogic.gdx.Input;
import dev.neuxs.europa_client.modules.cheats.NoClip;

public class ModuleInit {
    public static int noClipKeybind = Input.Keys.V;

    public static void initCheats() {
        new NoClip(noClipKeybind);
    }
}
