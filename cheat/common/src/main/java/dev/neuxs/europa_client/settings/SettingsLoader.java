package dev.neuxs.europa_client.settings;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.modules.Modules;

import java.util.Map;

@SuppressWarnings("unchecked")
public class SettingsLoader {
    public static void loadModules(Map<String, Object> combinedSettingsMap) {
        Client.LOGGER.debug("SettingsLoader received {} entries to process.", combinedSettingsMap.size());

        for (Map.Entry<String, Object> entry : combinedSettingsMap.entrySet()) {
            String moduleId = entry.getKey();
            Object settingsData = entry.getValue();

            Module module = Modules.getModuleById(moduleId);

            if (module != null) {
                if (settingsData instanceof Map) {
                    try {
                        module.importSettings((Map<String, Object>) settingsData);
                    } catch (Exception e) {
                        Client.LOGGER.error("Failed to apply settings for module {}: {}", moduleId, e.getMessage(), e);
                    }
                } else {
                    Client.LOGGER.warn("Settings data for module {} is not in the expected Map format. Skipping.", moduleId);
                }
            } else {
                Client.LOGGER.warn("Module with ID '{}' not found during settings load. Skipping.", moduleId);
            }
        }
    }
}
