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
    private static volatile long lastAutoSaveTime = 0;
    private static volatile boolean internalUpdate = false;
    // New flag to indicate a settings reload in progress.
    private static volatile boolean reloading = false;

    public static boolean isReloading() {
        return reloading;
    }

    public static void setReloading(boolean flag) {
        reloading = flag;
    }

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

        for (Module module : Modules.moduleList) {
            settingsMap.put(module.getId(), module.exportSettings());
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(settingsMap);
        File file = new File(filePath);

        try {
            // Mark this as an internal update so the watcher can ignore these changes
            internalUpdate = true;
            // Ensure the config directory exists
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(jsonOutput);
            }
            // Update lastAutoSaveTime using the file's lastModified timestamp
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
        File file = new File(filePath);
        if (!file.exists()) {
            // If the file doesn't exist yet, create a default one.
            saveSettings();
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Map<String, Object> settingsMap = gson.fromJson(reader, Map.class);
            if (settingsMap == null) {
                return;
            }
            // Disable auto-saving during the reload
            setReloading(true);
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
        } finally {
            setReloading(false);
        }
    }

    /**
     * Starts a file watcher thread that monitors the settings file.
     * If a modification or creation is detected that did not originate from an internal update,
     * loadSettings() is triggered automatically.
     */
    public static void startFileWatcher() {
        new Thread(() -> {
            try {
                Path dir = Paths.get("config");
                // Ensure the directory exists
                if (!Files.exists(dir)) {
                    Files.createDirectories(dir);
                }
                WatchService watchService = FileSystems.getDefault().newWatchService();
                // Register for both MODIFY and CREATE events.
                dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        Path changed = (Path) event.context();
                        if (changed.toString().equals("modules_settings.json")) {
                            // Give a short delay to allow the write to finish.
                            Thread.sleep(100);
                            File settingsFile = new File(filePath);
                            long currentModTime = settingsFile.lastModified();
                            // Only reload if the change wasn't internal and the time has changed.
                            if (!internalUpdate && currentModTime != lastAutoSaveTime) {
                                System.out.println("External modification detected. Reloading settings...");
                                loadSettings();
                                lastAutoSaveTime = currentModTime; // update our marker
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
