package dev.neuxs.europa_client.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.modules.Modules;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsSaver {

    public static void saveSettings(String filePath) {
        Map<String, Object> settingsMap = new HashMap<>();

        for (Module module : Modules.moduleList) {
            settingsMap.put(module.getId(), module.exportSettings());
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(settingsMap);

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(jsonOutput);
            System.out.println("Settings saved to " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
