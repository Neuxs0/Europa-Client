package dev.neuxs.europa_client.modules;

import com.badlogic.gdx.Input;
import dev.neuxs.europa_client.modules.utils.Fullbright;
import dev.neuxs.europa_client.modules.utils.PacketInspector;

import java.util.ArrayList;
import java.util.List;

public class Modules {
    public static Fullbright fullbright;
    public static PacketInspector packetInspector;

    public static List<Module> moduleList = new ArrayList<>();

    public static void initModules() {
        int unknown = Input.Keys.UNKNOWN;

        fullbright = new Fullbright(unknown, false);
        moduleList.add(fullbright);

        packetInspector = new PacketInspector(unknown, false);
        moduleList.add(packetInspector);
    }
}
