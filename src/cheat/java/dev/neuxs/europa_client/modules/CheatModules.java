package dev.neuxs.europa_client.modules;

import com.badlogic.gdx.Input;
import dev.neuxs.europa_client.modules.cheats.Fly;
import dev.neuxs.europa_client.modules.cheats.NoClip;
import dev.neuxs.europa_client.modules.cheats.NoFall;
import dev.neuxs.europa_client.modules.cheats.Reach;
import dev.neuxs.europa_client.modules.cheats.Speed;
import dev.neuxs.europa_client.modules.cheats.Xray;

import java.util.ArrayList;
import java.util.List;

public final class CheatModules {
    public static NoClip noClip;
    public static Fly fly;
    public static Speed speed;
    public static Reach reach;
    public static Xray xray;
    public static NoFall noFall;

    public static final List<Module> cheatModuleList = new ArrayList<>();

    private CheatModules() {
    }

    public static void register() {
        cheatModuleList.clear();

        int unknown = Input.Keys.UNKNOWN;

        noClip = new NoClip(unknown, false);
        Modules.registerModule(noClip, cheatModuleList);

        fly = new Fly(unknown, false);
        Modules.registerModule(fly, cheatModuleList);

        speed = new Speed(unknown, false);
        Modules.registerModule(speed, cheatModuleList);

        reach = new Reach(unknown, false);
        Modules.registerModule(reach, cheatModuleList);

        xray = new Xray(unknown, false);
        Modules.registerModule(xray, cheatModuleList);

        noFall = new NoFall(unknown, false);
        Modules.registerModule(noFall, cheatModuleList);
    }
}
