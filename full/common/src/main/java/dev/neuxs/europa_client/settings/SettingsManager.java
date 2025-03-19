package dev.neuxs.europa_client.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.modules.Modules;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class SettingsManager {
    private static String filePath = "config/modules_settings.json";
    private static boolean autoSaveEnabled = true;

    // Used to track if a file modification was due to an internal auto-save.
    private static volatile long lastAutoSaveTime = 0;
    private static volatile boolean internalUpdate = false;

    public static void setFilePath(String path) {
        filePath = path;
    }

    public static void setAutoSaveEnabled(boolean enabled) {
        autoSaveEnabled = enabled;
    }

    public static boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    public static void autoSaveIfEnabled() {
        if (autoSaveEnabled) {
            saveSettings();
        }
    }

    public static void saveSettings() {
        Map<String, Object> settingsMap = new HashMap<>();
        // Loop over every registered module.
        for (Module module : Modules.moduleList) {
            settingsMap.put(module.getId(), module.exportSettings());
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(settingsMap);

        try {
            internalUpdate = true;
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(jsonOutput);
            }
            // Update the "lastAutoSaveTime" using the file's last modified timestamp.
            File file = new File(filePath);
            lastAutoSaveTime = file.lastModified();
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
            e.printStackTrace();
        } finally {
            internalUpdate = false;
        }
        System.out.println("Settings automatically saved to " + filePath);
    }

    @SuppressWarnings("unchecked")
    public static void loadSettings() {
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            Map<String, Object> settingsMap = gson.fromJson(reader, Map.class);
            if (settingsMap == null) {
                return;
            }
            // For every registered module, import its settings if present.
            for (Module module : Modules.moduleList) {
                if (settingsMap.containsKey(module.getId())) {
                    Object obj = settingsMap.get(module.getId());
                    if (obj instanceof Map) {
                        Map<String, Object> moduleData = (Map<String, Object>) obj;
                        module.importSettings(moduleData);
                    }
                }
            }
            System.out.println("Settings loaded from " + filePath);
        } catch (IOException e) {
            System.err.println("Error loading settings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Starts a file watcher thread that monitors the settings file.
     * If a modification is detected that did not originate from an internal update,
     * loadSettings() is triggered automatically.
     */
    public static void startFileWatcher() {
        new Thread(() -> {
            try {
                Path dir = Paths.get("config");
                WatchService watchService = FileSystems.getDefault().newWatchService();
                // Ensure the directory exists
                if (!Files.exists(dir)) {
                    Files.createDirectories(dir);
                }
                dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            Path changed = (Path) event.context();
                            if (changed.toString().equals("modules_settings.json")) {
                                // Give a short delay for writing to finish
                                Thread.sleep(100);
                                File file = new File(filePath);
                                long currentModTime = file.lastModified();
                                // If the change was not internal and the modification time is different.
                                if (!internalUpdate && currentModTime != lastAutoSaveTime) {
                                    System.out.println("External modification detected. Reloading settings...");
                                    loadSettings();
                                    lastAutoSaveTime = currentModTime; // update our marker
                                }
                            }
                        }
                    }
                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Settings-FileWatcher").start();
    }
}
