package dev.neuxs.europa_client.modules;

import com.badlogic.gdx.Input;
import dev.neuxs.europa_client.modules.cheats.NoClip;
import dev.neuxs.europa_client.modules.cheats.Reach;
import dev.neuxs.europa_client.modules.cheats.Speed;
import dev.neuxs.europa_client.modules.ui.FpsCounter;
import dev.neuxs.europa_client.modules.ui.PingCounter;
import dev.neuxs.europa_client.modules.ui.TpsCounter;
import dev.neuxs.europa_client.modules.ui.VanillaHealthbarHud;
import dev.neuxs.europa_client.modules.ui.VanillaHotbarHud;
import dev.neuxs.europa_client.modules.utils.Fullbright;
import dev.neuxs.europa_client.modules.utils.NoFog;
import dev.neuxs.europa_client.modules.utils.PacketInspector;

import java.util.ArrayList;
import java.util.List;

public class Modules {
    public static Fullbright fullbright;
    public static NoFog noFog;
    public static PacketInspector packetInspector;

    public static NoClip noClip;
    public static Speed speed;
    public static Reach reach;

    public static FpsCounter fpsCounter;
    public static TpsCounter tpsCounter;
    public static PingCounter pingCounter;
    public static VanillaHotbarHud vanillaHotbar;
    public static VanillaHealthbarHud vanillaHealthbar;

    public static List<Module> moduleList = new ArrayList<>();
    public static List<Module> utilModuleList = new ArrayList<>();
    public static List<Module> cheatModuleList = new ArrayList<>();
    public static List<Module> uiModuleList = new ArrayList<>();
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
        utilModuleList.clear();
        cheatModuleList.clear();
        uiModuleList.clear();

        int unknown = Input.Keys.UNKNOWN;

        fullbright = new Fullbright(unknown, false);
        utilModuleList.add(fullbright);

        noFog = new NoFog(unknown, false);
        utilModuleList.add(noFog);

        packetInspector = new PacketInspector(unknown, false);
        utilModuleList.add(packetInspector);

        moduleList.addAll(utilModuleList);

        // Cheats
        noClip = new NoClip(unknown, false);
        cheatModuleList.add(noClip);

        speed = new Speed(unknown, false);
        cheatModuleList.add(speed);

        reach = new Reach(unknown, false);
        cheatModuleList.add(reach);

        moduleList.addAll(cheatModuleList);

        // UI
        vanillaHotbar = new VanillaHotbarHud(unknown, true);
        uiModuleList.add(vanillaHotbar);

        vanillaHealthbar = new VanillaHealthbarHud(unknown, true);
        uiModuleList.add(vanillaHealthbar);

        fpsCounter = new FpsCounter(unknown, false);
        uiModuleList.add(fpsCounter);

        tpsCounter = new TpsCounter(unknown, false);
        uiModuleList.add(tpsCounter);

        pingCounter = new PingCounter(unknown, false);
        uiModuleList.add(pingCounter);

        moduleList.addAll(uiModuleList);
    }

    public static Module getModuleById(String id) {
        for (Module m : cheatModuleList) {
            if (m.getId().equals(id)) return m;
        }
        for (Module m : utilModuleList) {
            if (m.getId().equals(id)) return m;
        }
        for (Module m : uiModuleList) {
            if (m.getId().equals(id)) return m;
        }
        return null;
    }
}
