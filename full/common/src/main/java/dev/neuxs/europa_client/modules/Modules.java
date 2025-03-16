package dev.neuxs.europa_client.modules;

import com.badlogic.gdx.Input;
import dev.neuxs.europa_client.modules.cheats.NoClip;
import dev.neuxs.europa_client.modules.cheats.Reach;
import dev.neuxs.europa_client.modules.cheats.Speed;
import dev.neuxs.europa_client.modules.utils.Fullbright;

public class Modules {
    // Utils
    public static Fullbright fullbright;

    // Cheats
    public static NoClip noClip;
    public static Speed speed;
    public static Reach reach;

    public static void initModules() {
        // Utils
        int fullbrightKeybind = Input.Keys.UNKNOWN;
        fullbright = new Fullbright(fullbrightKeybind, false);

        // Cheats
        int noClipKeybind = Input.Keys.UNKNOWN;
        noClip = new NoClip(noClipKeybind, false);
        int speedKeybind = Input.Keys.UNKNOWN;
        speed = new Speed(speedKeybind, false);
        int reachKeybind = Input.Keys.UNKNOWN;
        reach = new Reach(reachKeybind, false);
    }
}
