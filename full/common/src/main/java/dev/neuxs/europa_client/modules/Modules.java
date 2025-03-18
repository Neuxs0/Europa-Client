package dev.neuxs.europa_client.modules;

import com.badlogic.gdx.Input;
import dev.neuxs.europa_client.modules.cheats.NoClip;
import dev.neuxs.europa_client.modules.cheats.Reach;
import dev.neuxs.europa_client.modules.cheats.Speed;
import dev.neuxs.europa_client.modules.utils.Fullbright;
import java.util.ArrayList;
import java.util.List;

public class Modules {
    public static Fullbright fullbright;
    public static NoClip noClip;
    public static Speed speed;
    public static Reach reach;

    // Central list of all modules.
    public static List<Module> moduleList = new ArrayList<>();

    public static void initModules() {
        int unknown = Input.Keys.UNKNOWN; // 0 if not set

        fullbright = new Fullbright(unknown, false);
        moduleList.add(fullbright);

        noClip = new NoClip(unknown, false);
        moduleList.add(noClip);

        speed = new Speed(unknown, false);
        moduleList.add(speed);

        reach = new Reach(unknown, false);
        moduleList.add(reach);
    }
}
