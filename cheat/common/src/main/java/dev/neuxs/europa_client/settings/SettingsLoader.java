package dev.neuxs.europa_client.settings;

import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.modules.Modules;

import java.util.Map;

@SuppressWarnings("unchecked")
public class SettingsLoader {
    public static void loadModules(Map<String, Object> settingsMap) {
        for (Module module : Modules.moduleList) {
            if (settingsMap.containsKey(module.getId())) {
                Object obj = settingsMap.get(module.getId());
                if (obj instanceof Map) {
                    Map<String, Object> moduleData = (Map<String, Object>) obj;
                    module.importSettings(moduleData);
                }
            }
        }
    }
}
