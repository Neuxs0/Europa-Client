package dev.neuxs.europa_client.modules;

import com.badlogic.gdx.Input;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.ui.ConnectedServerHud;
import dev.neuxs.europa_client.modules.ui.FpsCounter;
import dev.neuxs.europa_client.modules.ui.PingCounter;
import dev.neuxs.europa_client.modules.ui.TpsCounter;
import dev.neuxs.europa_client.modules.ui.VanillaHealthbarHud;
import dev.neuxs.europa_client.modules.ui.VanillaHotbarHud;
import dev.neuxs.europa_client.modules.ui.VelocityHud;
import dev.neuxs.europa_client.modules.utils.Fullbright;
import dev.neuxs.europa_client.modules.utils.NoFog;
import dev.neuxs.europa_client.modules.utils.PacketInspector;
import dev.neuxs.europa_client.modules.utils.Zoom;

import java.util.ArrayList;
import java.util.List;

public class Modules {
    public static Fullbright fullbright;
    public static NoFog noFog;
    public static PacketInspector packetInspector;
    public static Zoom zoom;

    public static FpsCounter fpsCounter;
    public static TpsCounter tpsCounter;
    public static PingCounter pingCounter;
    public static VelocityHud velocityHud;
    public static ConnectedServerHud connectedServerHud;
    public static VanillaHotbarHud vanillaHotbar;
    public static VanillaHealthbarHud vanillaHealthbar;

    public static List<Module> moduleList = new ArrayList<>();
    public static List<Module> utilModuleList = new ArrayList<>();
    public static List<Module> uiModuleList = new ArrayList<>();
    private static boolean initialized;
    private static String initializedVariantType;

    public static void initModules() {
        String variantType = Client.getClientType();
        if (initialized && variantType.equals(initializedVariantType)) {
            return;
        }
        initialized = true;
        initializedVariantType = variantType;
        moduleList.clear();
        utilModuleList.clear();
        uiModuleList.clear();

        int unknown = Input.Keys.UNKNOWN;

        registerModule(fullbright = new Fullbright(unknown, false), utilModuleList);

        registerModule(noFog = new NoFog(unknown, false), utilModuleList);

        registerModule(packetInspector = new PacketInspector(unknown, false), utilModuleList);

        registerModule(zoom = new Zoom(unknown, false), utilModuleList);

        if (Client.getVariant() != null) {
            Client.getVariant().registerModules();
        }

        // UI
        registerModule(vanillaHotbar = new VanillaHotbarHud(unknown, true), uiModuleList);

        registerModule(vanillaHealthbar = new VanillaHealthbarHud(unknown, true), uiModuleList);

        registerModule(fpsCounter = new FpsCounter(unknown, false), uiModuleList);

        registerModule(tpsCounter = new TpsCounter(unknown, false), uiModuleList);

        registerModule(pingCounter = new PingCounter(unknown, false), uiModuleList);

        registerModule(velocityHud = new VelocityHud(unknown, false), uiModuleList);

        registerModule(connectedServerHud = new ConnectedServerHud(unknown, false), uiModuleList);
    }

    public static void registerModule(Module module, List<Module> categoryList) {
        categoryList.add(module);
        moduleList.add(module);
    }

    public static Module getModuleById(String id) {
        for (Module m : moduleList) {
            if (m.getId().equals(id)) return m;
        }
        return null;
    }
}
