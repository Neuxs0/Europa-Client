package dev.neuxs.europa_client.modules.ui;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.modules.Modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HudManager {
    private static boolean inGameHudSuppressed = false;

    private HudManager() {
    }

    public static List<HudModule> getHudModules() {
        List<HudModule> hudModules = new ArrayList<>();
        for (Module module : Modules.uiModuleList) {
            if (module instanceof HudModule hudModule) {
                hudModules.add(hudModule);
            }
        }
        return hudModules;
    }

    public static List<HudModule> getVisibleHudModules() {
        List<HudModule> hudModules = new ArrayList<>();
        for (HudModule module : getHudModules()) {
            if (module.isEnabled()) {
                hudModules.add(module);
            }
        }
        return hudModules;
    }

    public static List<HudModule> getHiddenHudModules() {
        List<HudModule> hudModules = new ArrayList<>();
        for (HudModule module : getHudModules()) {
            if (!module.isEnabled()) {
                hudModules.add(module);
            }
        }
        return hudModules;
    }

    public static HudModule getHudModuleByDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }

        for (HudModule module : getHudModules()) {
            if (displayName.equals(module.getHudDisplayName())) {
                return module;
            }
        }
        return null;
    }

    public static HudModule findTopElementAt(float x, float y, Viewport viewport) {
        List<HudModule> modules = getVisibleHudModules();
        Collections.reverse(modules);
        for (HudModule module : modules) {
            Rectangle bounds = module.getHudBounds(viewport);
            if (bounds.contains(x, y)) {
                return module;
            }
        }
        return null;
    }

    public static void render(Viewport viewport) {
        for (HudModule module : getVisibleHudModules()) {
            module.renderHud(viewport);
        }
    }

    public static boolean isInGameHudSuppressed() {
        return inGameHudSuppressed;
    }

    public static void setInGameHudSuppressed(boolean suppressed) {
        inGameHudSuppressed = suppressed;
    }
}
