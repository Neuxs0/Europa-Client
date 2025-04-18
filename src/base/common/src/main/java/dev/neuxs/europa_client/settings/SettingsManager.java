package dev.neuxs.europa_client.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.modules.Modules;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"BusyWait", "unused"})
public class SettingsManager {
    private static String filePath = "config/europaclient/base-modules-settings.json";
    private static boolean autoSaveEnabled = true;
    private static volatile long lastAutoSaveTime = 0;
    private static volatile boolean internalUpdate = false;
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
            internalUpdate = true;
            File parent = file.getParentFile();

            if (!parent.exists()) {
                boolean created = parent.mkdirs();
                if (!created) {
                    Client.LOGGER.error("Failed to create config directory: {}", parent.getAbsolutePath());
                }
            }

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(jsonOutput);
            }

            lastAutoSaveTime = file.lastModified();
        } catch (IOException e) {
            Client.LOGGER.error("Error saving settings: {}", e.getMessage());
        } finally {
            internalUpdate = false;
        }
        Client.LOGGER.info("Settings automatically saved to {}", filePath);
    }

    @SuppressWarnings("unchecked")
    public static void loadSettings() {
        File file = new File(filePath);
        if (!file.exists()) {
            saveSettings();
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Map<String, Object> settingsMap = gson.fromJson(reader, Map.class);

            if (settingsMap == null) {
                return;
            }

            setReloading(true);

            // Load Settings
            SettingsLoader.loadModules(settingsMap);

            Client.LOGGER.info("Settings loaded from {}", filePath);
        } catch (IOException e) {
            Client.LOGGER.error("Error loading settings: {}", e.getMessage());
        } finally {
            setReloading(false);
        }
    }

    public static void startFileWatcher() {
        new Thread(() -> {
            try {
                Path dir = Paths.get("config");

                if (!Files.exists(dir)) {
                    Files.createDirectories(dir);
                }
                WatchService watchService = FileSystems.getDefault().newWatchService();

                dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        Path changed = (Path) event.context();
                        if (changed.toString().equals("modules_settings.json")) {
                            File settingsFile = new File(filePath);
                            long currentModTime = settingsFile.lastModified();

                            if (!internalUpdate && currentModTime != lastAutoSaveTime) {
                                Client.LOGGER.info("External modification detected. Reloading settings...");
                                loadSettings();
                                lastAutoSaveTime = currentModTime;
                            }
                            Thread.sleep(100);
                        }
                    }
                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            } catch (Exception e) {
                Client.LOGGER.error("Error watching file: {}", e.getMessage());
            }
        }, "Settings-FileWatcher").start();
    }
}
