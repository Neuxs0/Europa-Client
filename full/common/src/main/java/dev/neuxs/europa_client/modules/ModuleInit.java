package dev.neuxs.europa_client.modules;

import com.badlogic.gdx.Input;
import dev.neuxs.europa_client.modules.cheats.NoClip;
import dev.neuxs.europa_client.modules.utils.Fullbright;

public class ModuleInit {
    public static int fullbrightKeybind = 0;
    public static int noClipKeybind = Input.Keys.V;

    public static void initModules() {
        // Utils
        new Fullbright(fullbrightKeybind, false);

        // Cheats
        new NoClip(noClipKeybind, false);
    }
}
