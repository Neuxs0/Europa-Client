package dev.neuxs.europa_client.modules;

import com.badlogic.gdx.Input;
import dev.neuxs.europa_client.modules.utils.Fullbright;

public class Modules {
    // Utils
    public static Fullbright fullbright;

    public static void initModules() {
        // Utils
        int fullbrightKeybind = Input.Keys.UNKNOWN;
        fullbright = new Fullbright(fullbrightKeybind, false);
    }
}
