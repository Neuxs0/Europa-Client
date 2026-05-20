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
    private static boolean initialized;

    static {
        initModules();
    }

    public static void initModules() {
        if (initialized) {
            return;
        }
        initialized = true;
        moduleList.clear();

        int unknown = Input.Keys.UNKNOWN;

        fullbright = new Fullbright(unknown, false);
        moduleList.add(fullbright);

        packetInspector = new PacketInspector(unknown, false);
        moduleList.add(packetInspector);
    }
}
