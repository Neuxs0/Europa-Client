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
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"BusyWait", "unused"})
public class SettingsManager {
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    private static final ModuleSettingsSection UTILITY_SECTION = new ModuleSettingsSection(
            "utility",
            "utility",
            "utility-settings.json",
            () -> Modules.utilModuleList
    );
    private static final ModuleSettingsSection UI_SECTION = new ModuleSettingsSection(
            "ui",
            "UI",
            "ui-settings.json",
            () -> Modules.uiModuleList
    );

    private static String configDir = "config/europaclient";
    private static String clientFileName = "settings.json";
    private static Path clientFilePath = Paths.get(configDir, clientFileName);
    private static final Map<String, Path> moduleFilePaths = new HashMap<>();
    private static final Map<String, Long> lastModuleSaveTimes = new HashMap<>();
    private static boolean autoSaveEnabled = true;
    private static volatile boolean internalUpdate = false;
    private static volatile boolean reloading = false;
    private static volatile long lastClientSaveTime = 0;

    static {
        resetDefaultModuleFilePaths(Paths.get(configDir));
    }

    public static synchronized void saveSettings() {
        refreshConfigPathsFromGameDirectory();

        try {
            internalUpdate = true;
            ensureConfigDirectory();

            for (ModuleSettingsSection section : getModuleSettingsSections()) {
                Map<String, Object> settingsMap = new HashMap<>();
                for (Module module : section.modules().get()) {
                    if (module != null) {
                        settingsMap.put(module.getId(), module.exportSettings());
                    }
                }
                writeModuleSettingsFile(section, settingsMap);
            }

            writeClientSettingsFile();
        } catch (Exception e) {
            Client.LOGGER.error("An unexpected error occurred during settings save: {}", e.getMessage(), e);
        } finally {
            internalUpdate = false;
        }
    }

    public static synchronized void loadSettings() {
        refreshConfigPathsFromGameDirectory();

        Map<String, Map<String, Object>> sectionSettings = new HashMap<>();
        boolean shouldSaveAfterLoad = !clientFilePath.toFile().exists();

        for (ModuleSettingsSection section : getModuleSettingsSections()) {
            Path filePath = getModuleFilePath(section);
            File file = filePath.toFile();
            Map<String, Object> settingsMap = new HashMap<>();

            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    Map<String, Object> loadedMap = GSON.fromJson(reader, MAP_TYPE);
                    if (loadedMap != null) {
                        settingsMap = loadedMap;
                    }
                    lastModuleSaveTimes.put(section.id(), file.lastModified());
                    Client.LOGGER.info("Loaded {} settings from {}", section.displayName(), filePath);
                } catch (IOException | com.google.gson.JsonSyntaxException e) {
                    Client.LOGGER.error("Error loading {} settings from {}: {}", section.displayName(), filePath, e.getMessage(), e);
                }
            } else {
                Client.LOGGER.warn("{} settings file not found. Defaults will be saved to {}", section.displayName(), filePath);
                shouldSaveAfterLoad = true;
            }

            if (isMissingModuleSettings(settingsMap, section.modules().get())) {
                shouldSaveAfterLoad = true;
            }
            sectionSettings.put(section.id(), settingsMap);
        }

        loadClientSettings();

        setReloading(true);
        try {
            for (ModuleSettingsSection section : getModuleSettingsSections()) {
                SettingsLoader.loadModules(sectionSettings.getOrDefault(section.id(), Map.of()));
            }

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

    public static void startFileWatcher() {
        refreshConfigPathsFromGameDirectory();

        Thread watcherThread = new Thread(() -> {
            Path dirToWatch = Paths.get(configDir);
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
                        Object context = event.context();
                        if (context instanceof Path changedPath) {
                            handleWatchedFileChange(dirToWatch, changedPath);
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

    public static void setUtilityFilePath(String path) {
        setModuleSectionFilePath("utility", path);
        Path utilityFilePath = Paths.get(path);
        configDir = utilityFilePath.getParent().toString();
        resetDefaultModuleFilePaths(Paths.get(configDir));
        moduleFilePaths.put("utility", utilityFilePath);
        clientFilePath = Paths.get(configDir, clientFileName);
    }

    public static void setUiFilePath(String path) {
        setModuleSectionFilePath("ui", path);
    }

    public static void setClientFilePath(String path) {
        clientFilePath = Paths.get(path);
        clientFileName = clientFilePath.getFileName().toString();
    }

    public static void setModuleSectionFilePath(String sectionId, String path) {
        moduleFilePaths.put(sectionId, Paths.get(path));
    }

    public static void setAutoSaveEnabled(boolean enabled) {
        autoSaveEnabled = enabled;
    }

    public static boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    private static void setReloading(boolean flag) {
        reloading = flag;
    }

    private static void ensureConfigDirectory() throws IOException {
        Path parentPath = clientFilePath.getParent();
        if (parentPath != null && !Files.exists(parentPath)) {
            Files.createDirectories(parentPath);
            Client.LOGGER.info("Created config directory: {}", parentPath.toAbsolutePath());
        }
    }

    private static void writeModuleSettingsFile(ModuleSettingsSection section, Map<String, Object> settingsMap) {
        Path filePath = getModuleFilePath(section);
        File file = filePath.toFile();

        try {
            Path parentPath = filePath.getParent();
            if (parentPath != null && !Files.exists(parentPath)) {
                Files.createDirectories(parentPath);
            }
        } catch (IOException e) {
            Client.LOGGER.error("Error creating config directory for {}: {}", filePath, e.getMessage(), e);
            return;
        }

        try (FileWriter writer = new FileWriter(file)) {
            PRETTY_GSON.toJson(settingsMap, writer);
            Client.LOGGER.info("Saved {} settings to {}", section.displayName(), filePath);
            lastModuleSaveTimes.put(section.id(), file.lastModified());
        } catch (IOException e) {
            Client.LOGGER.error("Error saving {} settings to {}: {}", section.displayName(), filePath, e.getMessage(), e);
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
            Path resolvedPath = Paths.get(SaveLocation.getSaveFolderLocation(), "config", "europaclient");
            String resolvedConfigDir = resolvedPath.toString();
            if (resolvedConfigDir.equals(configDir)) {
                return;
            }

            configDir = resolvedConfigDir;
            resetDefaultModuleFilePaths(resolvedPath);
            clientFilePath = resolvedPath.resolve(clientFileName);
        } catch (Exception e) {
        }
    }

    private static void resetDefaultModuleFilePaths(Path configPath) {
        moduleFilePaths.put("utility", configPath.resolve(UTILITY_SECTION.fileName()));
        moduleFilePaths.put("ui", configPath.resolve(UI_SECTION.fileName()));
        if (Client.getVariant() != null) {
            for (ModuleSettingsSection section : Client.getVariant().moduleSettingsSections()) {
                moduleFilePaths.putIfAbsent(section.id(), configPath.resolve(section.fileName()));
            }
        }
    }

    private static List<ModuleSettingsSection> getModuleSettingsSections() {
        List<ModuleSettingsSection> sections = new ArrayList<>();
        sections.add(UTILITY_SECTION);
        if (Client.getVariant() != null) {
            sections.addAll(Client.getVariant().moduleSettingsSections());
        }
        sections.add(UI_SECTION);
        return sections;
    }

    private static Path getModuleFilePath(ModuleSettingsSection section) {
        return moduleFilePaths.computeIfAbsent(section.id(), id -> Paths.get(configDir, section.fileName()));
    }

    private static boolean isMissingModuleSettings(Map<String, Object> settingsMap, Iterable<Module> modules) {
        for (Module module : modules) {
            if (module != null && !settingsMap.containsKey(module.getId())) {
                return true;
            }
        }
        return false;
    }

    private static void handleWatchedFileChange(Path dirToWatch, Path changedPath) {
        String changedFileName = changedPath.getFileName().toString();
        Map<String, Long> activeFileSaveTimes = new HashMap<>();

        for (ModuleSettingsSection section : getModuleSettingsSections()) {
            activeFileSaveTimes.put(section.fileName(), lastModuleSaveTimes.getOrDefault(section.id(), 0L));
        }
        activeFileSaveTimes.put(clientFileName, lastClientSaveTime);

        Long lastKnownSaveTime = activeFileSaveTimes.get(changedFileName);
        if (lastKnownSaveTime == null) {
            return;
        }

        if (internalUpdate) {
            Client.LOGGER.debug("Ignoring internal modification of {}", changedFileName);
            return;
        }

        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        File changedFile = dirToWatch.resolve(changedPath).toFile();
        if (!changedFile.exists()) {
            return;
        }

        long currentModTime = changedFile.lastModified();
        if (currentModTime != lastKnownSaveTime && currentModTime != 0) {
            Client.LOGGER.info("External modification detected for {}. Reloading settings...", changedFileName);
            loadSettings();
        } else {
            Client.LOGGER.debug("Modification time for {} hasn't changed significantly or was internal. No reload needed.", changedFileName);
        }
    }
}
