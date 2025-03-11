package dev.neuxs.europa_client.modules;

import com.badlogic.gdx.Input;
import dev.neuxs.europa_client.modules.utils.Fullbright;

public class ModuleInit {
    public static int fullbrightKeybind = Input.Keys.UNKNOWN;

    public static void initModules() {
        // Utils
        new Fullbright(fullbrightKeybind, false);
    }
}
