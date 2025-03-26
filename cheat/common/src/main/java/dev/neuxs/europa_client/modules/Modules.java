package dev.neuxs.europa_client.modules;

import com.badlogic.gdx.Input;
import dev.neuxs.europa_client.modules.cheats.NoClip;
import dev.neuxs.europa_client.modules.cheats.Reach;
import dev.neuxs.europa_client.modules.cheats.Speed;
import dev.neuxs.europa_client.modules.utils.Fullbright;
import dev.neuxs.europa_client.modules.utils.PacketInspector;

import java.util.ArrayList;
import java.util.List;

public class Modules {
    public static Fullbright fullbright;
    public static PacketInspector packetInspector;

    public static NoClip noClip;
    public static Speed speed;
    public static Reach reach;

    public static List<Module> moduleList = new ArrayList<>();

    public static void initModules() {
        int unknown = Input.Keys.UNKNOWN;

        fullbright = new Fullbright(unknown, false);
        moduleList.add(fullbright);

        packetInspector = new PacketInspector(unknown, false);
        moduleList.add(packetInspector);


        // Cheats

        noClip = new NoClip(unknown, false);
        moduleList.add(noClip);

        speed = new Speed(unknown, false);
        moduleList.add(speed);

        reach = new Reach(unknown, false);
        moduleList.add(reach);
    }
}
