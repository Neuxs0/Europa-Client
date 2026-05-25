package dev.neuxs.europa_client.modules;

import dev.neuxs.europa_client.SmokeTestSupport;
import dev.neuxs.europa_client.modules.cheats.NoClip;
import dev.neuxs.europa_client.modules.cheats.Reach;
import dev.neuxs.europa_client.modules.cheats.Speed;
import dev.neuxs.europa_client.modules.utils.Freecam;
import dev.neuxs.europa_client.modules.utils.Zoom;
import dev.neuxs.europa_client.variant.CheatVariant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModuleSettingsSmokeTest {
    @BeforeEach
    void resetClientState() {
        SmokeTestSupport.resetForVariant(new CheatVariant());
    }

    @Test
    void numericSettingsRejectValuesBelowMinimum() {
        Speed speed = CheatModules.speed;
        Reach reach = CheatModules.reach;
        Freecam freecam = Modules.freecam;
        NoClip noClip = CheatModules.noClip;

        assertThrows(IllegalArgumentException.class, () -> speed.setSpeed(0.5f));
        assertThrows(IllegalArgumentException.class, () -> speed.setJetpackSpeed(0.5f));
        assertThrows(IllegalArgumentException.class, () -> reach.setReachDistance(0.5f));
        assertThrows(IllegalArgumentException.class, () -> freecam.setSpeed(0.5f));
        assertThrows(IllegalArgumentException.class, () -> noClip.setSpeed(0.5f));
    }

    @Test
    void moduleExportImportPersistsStateKeybindAndCustomSettings() {
        Zoom source = Modules.zoom;
        source.setEnabled(true);
        source.setKeybind("CTRL+Z");
        setting(source, "showHand", Boolean.class).setValue(true);
        source.importSettings(Map.of("savedZoomAmount", 4.0f));

        Zoom target = new Zoom(0, false);
        target.importSettings(source.exportSettings());

        assertEquals(true, target.isEnabled());
        assertEquals("CTRL+Z", target.getKeybindCombo());
        assertEquals(true, setting(target, "showHand", Boolean.class).getValue());
        assertEquals(4.0f, ((Number) target.exportSettings().get("savedZoomAmount")).floatValue());
    }

    @SuppressWarnings("unchecked")
    private static <T> dev.neuxs.europa_client.settings.Setting<T> setting(Module module, String name, Class<T> type) {
        return (dev.neuxs.europa_client.settings.Setting<T>) module.getCustomSettings().get(name);
    }
}
