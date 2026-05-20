package dev.neuxs.europa_client.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.util.SaveLocation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"BusyWait", "unused"})
public class SettingsManager {
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Gson GSON = new Gson();
    private static String cheatConfigDir = "config/europaclient";
    private static String cheatFileName = "cheat-settings.json";
    private static String utilityFileName = "utility-settings.json";
    private static String uiFileName = "ui-settings.json";
    private static String clientFileName = "settings.json";
    private static Path cheatFilePath = Paths.get(cheatConfigDir, cheatFileName);
    private static Path utilityFilePath = Paths.get(cheatConfigDir, utilityFileName);
    private static Path uiFilePath = Paths.get(cheatConfigDir, uiFileName);
    private static Path clientFilePath = Paths.get(cheatConfigDir, clientFileName);
    private static boolean autoSaveEnabled = true;
    private static volatile boolean internalUpdate = false;
    private static volatile boolean reloading = false;
    private static volatile long lastCheatSaveTime = 0;
    private static volatile long lastUtilitySaveTime = 0;
    private static volatile long lastUiSaveTime = 0;
    private static volatile long lastClientSaveTime = 0;
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    public static synchronized void saveSettings() {
        refreshConfigPathsFromGameDirectory();

        Map<String, Object> cheatSettingsMap = new HashMap<>();
        Map<String, Object> utilitySettingsMap = new HashMap<>();
        Map<String, Object> uiSettingsMap = new HashMap<>();

        for (Module module : Modules.cheatModuleList) {
            if (module != null) {
                cheatSettingsMap.put(module.getId(), module.exportSettings());
            }
        }

        for (Module module : Modules.utilModuleList) {
            if (module != null) {
                utilitySettingsMap.put(module.getId(), module.exportSettings());
            }
        }

        for (Module module : Modules.uiModuleList) {
            if (module != null) {
                uiSettingsMap.put(module.getId(), module.exportSettings());
            }
        }

        File cheatFile = cheatFilePath.toFile();
        File utilityFile = utilityFilePath.toFile();
        File uiFile = uiFilePath.toFile();
        File parentDir = cheatFilePath.getParent().toFile();

        try {
            internalUpdate = true;

            if (!parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    Client.LOGGER.error("Failed to create config directory: {}", parentDir.getAbsolutePath());
                    return;
                } else {
                    Client.LOGGER.info("Created config directory: {}", parentDir.getAbsolutePath());
                }
            }

            try (FileWriter writer = new FileWriter(cheatFile)) {
                PRETTY_GSON.toJson(cheatSettingsMap, writer);
                Client.LOGGER.info("Saved cheat settings to {}", cheatFilePath);
            } catch (IOException e) {
                Client.LOGGER.error("Error saving cheat settings to {}: {}", cheatFilePath, e.getMessage(), e);
            }

            try (FileWriter writer = new FileWriter(utilityFile)) {
                PRETTY_GSON.toJson(utilitySettingsMap, writer);
                Client.LOGGER.info("Saved utility settings to {}", utilityFilePath);
            } catch (IOException e) {
                Client.LOGGER.error("Error saving utility settings to {}: {}", utilityFilePath, e.getMessage(), e);
            }

            try (FileWriter writer = new FileWriter(uiFile)) {
                PRETTY_GSON.toJson(uiSettingsMap, writer);
                Client.LOGGER.info("Saved UI settings to {}", uiFilePath);
            } catch (IOException e) {
                Client.LOGGER.error("Error saving UI settings to {}: {}", uiFilePath, e.getMessage(), e);
            }

            writeClientSettingsFile();

            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            if (cheatFile.exists()) lastCheatSaveTime = cheatFile.lastModified();
            if (utilityFile.exists()) lastUtilitySaveTime = utilityFile.lastModified();
            if (uiFile.exists()) lastUiSaveTime = uiFile.lastModified();

        } catch (Exception e) {
            Client.LOGGER.error("An unexpected error occurred during settings save: {}", e.getMessage(), e);
        } finally {
            internalUpdate = false;
        }
    }

    public static synchronized void loadSettings() {
        refreshConfigPathsFromGameDirectory();

        File cheatFile = cheatFilePath.toFile();
        File utilityFile = utilityFilePath.toFile();
        File uiFile = uiFilePath.toFile();
        File clientFile = clientFilePath.toFile();
        Map<String, Object> cheatSettingsMap = new HashMap<>();
        Map<String, Object> utilitySettingsMap = new HashMap<>();
        Map<String, Object> uiSettingsMap = new HashMap<>();
        boolean shouldSaveAfterLoad = !cheatFile.exists() || !utilityFile.exists() || !uiFile.exists() || !clientFile.exists();

        if (cheatFile.exists()) {
            try (FileReader reader = new FileReader(cheatFile)) {
                Map<String, Object> loadedCheatMap = GSON.fromJson(reader, MAP_TYPE);
                if (loadedCheatMap != null) {
                    cheatSettingsMap = loadedCheatMap;
                }
                lastCheatSaveTime = cheatFile.lastModified();
                Client.LOGGER.info("Loaded cheat settings from {}", cheatFilePath);
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                Client.LOGGER.error("Error loading cheat settings from {}: {}", cheatFilePath, e.getMessage(), e);
            }
        } else {
            Client.LOGGER.warn("Cheat settings file not found. Defaults will be saved to {}", cheatFilePath);
        }

        if (utilityFile.exists()) {
            try (FileReader reader = new FileReader(utilityFile)) {
                Map<String, Object> loadedUtilityMap = GSON.fromJson(reader, MAP_TYPE);
                if (loadedUtilityMap != null) {
                    utilitySettingsMap = loadedUtilityMap;
                }
                lastUtilitySaveTime = utilityFile.lastModified();
                Client.LOGGER.info("Loaded utility settings from {}", utilityFilePath);
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                Client.LOGGER.error("Error loading utility settings from {}: {}", utilityFilePath, e.getMessage(), e);
            }
        } else {
            Client.LOGGER.warn("Utility settings file not found. Defaults will be saved to {}", utilityFilePath);
        }

        if (uiFile.exists()) {
            try (FileReader reader = new FileReader(uiFile)) {
                Map<String, Object> loadedUiMap = GSON.fromJson(reader, MAP_TYPE);
                if (loadedUiMap != null) {
                    uiSettingsMap = loadedUiMap;
                }
                lastUiSaveTime = uiFile.lastModified();
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                Client.LOGGER.error("Error loading UI settings from {}: {}", uiFilePath, e.getMessage(), e);
            }
        } else {
            Client.LOGGER.warn("UI settings file not found. Defaults will be saved to {}", uiFilePath);
            copyModuleSettings(utilitySettingsMap, uiSettingsMap, Modules.uiModuleList);
        }

        shouldSaveAfterLoad = shouldSaveAfterLoad
                || isMissingModuleSettings(cheatSettingsMap, Modules.cheatModuleList)
                || isMissingModuleSettings(utilitySettingsMap, Modules.utilModuleList)
                || isMissingModuleSettings(uiSettingsMap, Modules.uiModuleList);

        loadClientSettings();

        setReloading(true);
        try {
            SettingsLoader.loadModules(cheatSettingsMap);
            SettingsLoader.loadModules(utilitySettingsMap);
            SettingsLoader.loadModules(uiSettingsMap);

            Client.LOGGER.info("Applied loaded settings.");

        } catch (Exception e) {
            Client.LOGGER.error("Error applying loaded settings: {}", e.getMessage(), e);
        } finally {
            setReloading(false);
        }

        if (shouldSaveAfterLoad) {
            Client.LOGGER.info("Saving missing settings file(s).");
            saveSettings();
        }
    }

    public static synchronized void saveClientSettings() {
        refreshConfigPathsFromGameDirectory();

        try {
            internalUpdate = true;
            ensureConfigDirectory();
            writeClientSettingsFile();
        } catch (Exception e) {
            Client.LOGGER.error("An unexpected error occurred during client settings save: {}", e.getMessage(), e);
        } finally {
            internalUpdate = false;
        }
    }

    public static synchronized void loadClientSettings() {
        refreshConfigPathsFromGameDirectory();

        File clientFile = clientFilePath.toFile();
        if (!clientFile.exists()) {
            Client.LOGGER.warn("Client settings file not found. Defaults will be saved to {}", clientFilePath.toAbsolutePath());
            return;
        }

        try (FileReader reader = new FileReader(clientFile)) {
            Map<String, Object> loadedClientMap = GSON.fromJson(reader, MAP_TYPE);
            if (loadedClientMap == null) {
                loadedClientMap = new HashMap<>();
            }

            setReloading(true);
            try {
                ClientSettings.importSettings(loadedClientMap);
            } finally {
                setReloading(false);
            }

            lastClientSaveTime = clientFile.lastModified();
            Client.LOGGER.info("Loaded client settings from {}", clientFilePath.toAbsolutePath());
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            Client.LOGGER.error("Error loading client settings from {}: {}", clientFilePath.toAbsolutePath(), e.getMessage(), e);
        }
    }

    public static void reloadClientSettingsIfChanged() {
        refreshConfigPathsFromGameDirectory();

        if (internalUpdate) {
            return;
        }

        File clientFile = clientFilePath.toFile();
        if (!clientFile.exists()) {
            return;
        }

        long currentModTime = clientFile.lastModified();
        if (currentModTime != 0 && currentModTime != lastClientSaveTime) {
            Client.LOGGER.info("Runtime client settings change detected. Reloading {}...", clientFilePath.toAbsolutePath());
            loadClientSettings();
        }
    }

    private static void ensureConfigDirectory() throws IOException {
        Path parentPath = cheatFilePath.getParent();
        if (parentPath != null && !Files.exists(parentPath)) {
            Files.createDirectories(parentPath);
            Client.LOGGER.info("Created config directory: {}", parentPath.toAbsolutePath());
        }
    }

    private static void writeClientSettingsFile() throws IOException {
        File clientFile = clientFilePath.toFile();
        try (FileWriter writer = new FileWriter(clientFile)) {
            PRETTY_GSON.toJson(ClientSettings.exportSettings(), writer);
            Client.LOGGER.info("Saved client settings to {}", clientFilePath.toAbsolutePath());
        }
        lastClientSaveTime = clientFile.lastModified();
    }

    private static synchronized void refreshConfigPathsFromGameDirectory() {
        try {
            Path configDir = Paths.get(SaveLocation.getSaveFolderLocation(), "config", "europaclient");
            String resolvedConfigDir = configDir.toString();
            if (resolvedConfigDir.equals(cheatConfigDir)) {
                return;
            }

            cheatConfigDir = resolvedConfigDir;
            cheatFilePath = configDir.resolve(cheatFileName);
            utilityFilePath = configDir.resolve(utilityFileName);
            uiFilePath = configDir.resolve(uiFileName);
            clientFilePath = configDir.resolve(clientFileName);
        } catch (Exception e) {
        }
    }

    private static boolean isMissingModuleSettings(Map<String, Object> settingsMap, Iterable<Module> modules) {
        for (Module module : modules) {
            if (module != null && !settingsMap.containsKey(module.getId())) {
                return true;
            }
        }
        return false;
    }

    private static void copyModuleSettings(
            Map<String, Object> source,
            Map<String, Object> destination,
            Iterable<Module> modules
    ) {
        for (Module module : modules) {
            if (module != null && source.containsKey(module.getId()) && !destination.containsKey(module.getId())) {
                destination.put(module.getId(), source.get(module.getId()));
            }
        }
    }

    public static void startFileWatcher() {
        refreshConfigPathsFromGameDirectory();

        Thread watcherThread = new Thread(() -> {
            Path dirToWatch = Paths.get(cheatConfigDir);
            WatchService watchService = null;

            try {
                if (!Files.exists(dirToWatch)) {
                    Client.LOGGER.info("Config directory {} not found for watcher, attempting to create.", dirToWatch);
                    try {
                        Files.createDirectories(dirToWatch);
                        Client.LOGGER.info("Created config directory for watcher: {}", dirToWatch);
                    } catch (IOException e) {
                        Client.LOGGER.error("Failed to create config directory for watcher: {}. Watcher will not start.", dirToWatch, e);
                        return;
                    }
                } else if (!Files.isDirectory(dirToWatch)) {
                    Client.LOGGER.error("Config path {} is not a directory. Watcher will not start.", dirToWatch);
                    return;
                }


                watchService = FileSystems.getDefault().newWatchService();
                dirToWatch.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
                Client.LOGGER.info("Started settings file watcher on directory: {}", dirToWatch);

                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key;
                    try {
                        key = watchService.poll(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Client.LOGGER.info("Settings file watcher interrupted. Shutting down.");
                        Thread.currentThread().interrupt();
                        break;
                    } catch (ClosedWatchServiceException e) {
                        Client.LOGGER.info("Watch service closed. Shutting down watcher.");
                        break;
                    }

                    if (key == null) {
                        continue;
                    }


                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        Object context = event.context();
                        if (context instanceof Path changedPath) {
                            String changedFileName = changedPath.getFileName().toString();

                            boolean isCheatFile = changedFileName.equals(cheatFileName);
                            boolean isUtilityFile = changedFileName.equals(utilityFileName);
                            boolean isUiFile = changedFileName.equals(uiFileName);
                            boolean isClientFile = changedFileName.equals(clientFileName);

                            if (isCheatFile || isUtilityFile || isUiFile || isClientFile) {
                                if (internalUpdate) {
                                    Client.LOGGER.debug("Ignoring internal modification of {}", changedFileName);
                                    continue;
                                }

                                try { Thread.sleep(150); } catch (InterruptedException ignored) {}

                                File changedFile = dirToWatch.resolve(changedPath).toFile();
                                if (!changedFile.exists()) continue;

                                long currentModTime = changedFile.lastModified();
                                long lastKnownSaveTime = isCheatFile
                                        ? lastCheatSaveTime
                                        : isUtilityFile ? lastUtilitySaveTime : isUiFile ? lastUiSaveTime : lastClientSaveTime;

                                if (currentModTime != lastKnownSaveTime && currentModTime != 0) {
                                    Client.LOGGER.info("External modification detected for {}. Reloading settings...", changedFileName);
                                    loadSettings();
                                } else {
                                    Client.LOGGER.debug("Modification time for {} hasn't changed significantly or was internal. No reload needed.", changedFileName);
                                }
                            }
                        } else {
                            Client.LOGGER.warn("Watch event context was not a Path: {}", context);
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        Client.LOGGER.warn("Watch key became invalid. Restarting watcher might be necessary if issues persist.");
                        break;
                    }
                }
            } catch (IOException e) {
                Client.LOGGER.error("Error initializing or running file watcher: {}", e.getMessage(), e);
            } catch (Exception e) {
                Client.LOGGER.error("Unexpected error in file watcher thread: {}", e.getMessage(), e);
            } finally {
                if (watchService != null) {
                    try {
                        watchService.close();
                        Client.LOGGER.info("Closed settings file watch service.");
                    } catch (IOException e) {
                        Client.LOGGER.error("Error closing file watch service: {}", e.getMessage(), e);
                    }
                }
            }
        }, "Settings-FileWatcher");
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    public static void autoSaveIfEnabled() {
        if (autoSaveEnabled) {
            saveSettings();
        }
    }

    public static boolean isReloading() {
        return reloading;
    }
    private static void setReloading(boolean flag) {
        reloading = flag;
    }
    public static void setCheatFilePath(String path) {
        cheatFilePath = Paths.get(path);
        cheatFileName = cheatFilePath.getFileName().toString();
        cheatConfigDir = cheatFilePath.getParent().toString();
        utilityFilePath = Paths.get(cheatConfigDir, utilityFileName);
        uiFilePath = Paths.get(cheatConfigDir, uiFileName);
        clientFilePath = Paths.get(cheatConfigDir, clientFileName);
    }
    public static void setUtilityFilePath(String path) {
        utilityFilePath = Paths.get(path);
        utilityFileName = utilityFilePath.getFileName().toString();
    }
    public static void setUiFilePath(String path) {
        uiFilePath = Paths.get(path);
        uiFileName = uiFilePath.getFileName().toString();
    }
    public static void setClientFilePath(String path) {
        clientFilePath = Paths.get(path);
        clientFileName = clientFilePath.getFileName().toString();
    }
    public static void setAutoSaveEnabled(boolean enabled) {
        autoSaveEnabled = enabled;
    }

    public static boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }
}
