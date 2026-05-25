package dev.neuxs.europa_client.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.modules.Modules;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings({"unused", "unchecked"})
public class ProfileManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();
    private static final int SCHEMA_VERSION = 1;
    private static final Path CONFIG_DIR = Paths.get("config", "europaclient");
    private static final Path PROFILES_ROOT_DIR = CONFIG_DIR.resolve("profiles");
    private static final Path PROFILE_STATE_DIR = CONFIG_DIR.resolve("profile-state");
    private static String activeProfileName = "";

    public static synchronized void initialize() {
        try {
            ensureDirectories();
            loadState();
            List<String> profiles = listProfiles();
            if (profiles.isEmpty()) {
                saveProfile("Default", allSections());
                setActiveProfileName("Default");
            } else if (activeProfileName.isBlank() || !profileExists(activeProfileName)) {
                setActiveProfileName(profiles.get(0));
            }
        } catch (IOException e) {
            Client.LOGGER.error("Failed to initialize profiles: {}", e.getMessage(), e);
        }
    }

    public static synchronized String saveProfile(String profileName, EnumSet<ProfileSection> sections) throws IOException {
        ensureDirectories();
        EnumSet<ProfileSection> selectedSections = normalizeSections(sections);
        String safeName = sanitizeProfileName(profileName);
        Path profilePath = getProfilePath(safeName);
        Map<String, Object> existing = Files.exists(profilePath) ? readMap(profilePath) : new LinkedHashMap<>();
        Map<String, Object> captured = captureProfileData(safeName, selectedSections);

        String now = Instant.now().toString();
        Map<String, Object> merged = new LinkedHashMap<>();
        merged.put("schemaVersion", SCHEMA_VERSION);
        merged.put("name", safeName);
        merged.put("variant", getCurrentVariantName());
        merged.put("createdAt", stringValue(existing.get("createdAt"), now));
        merged.put("updatedAt", now);

        for (ProfileSection section : ProfileSection.values()) {
            String key = section.getJsonKey();
            if (selectedSections.contains(section)) {
                merged.put(key, captured.get(key));
            } else if (existing.containsKey(key)) {
                merged.put(key, existing.get(key));
            }
        }

        merged.put("sections", getPresentSectionNames(merged));
        writeMap(profilePath, merged);
        setActiveProfileName(safeName);
        Client.LOGGER.info("Saved profile '{}' to {}", safeName, profilePath);
        return safeName;
    }

    public static synchronized void applyProfile(String profileName, EnumSet<ProfileSection> sections) throws IOException {
        ensureDirectories();
        String safeName = sanitizeProfileName(profileName);
        Path profilePath = getProfilePath(safeName);
        if (!Files.exists(profilePath)) {
            throw new IOException("Profile not found: " + safeName);
        }

        Map<String, Object> profile = readMap(profilePath);
        validateProfileVariant(profile, safeName);
        EnumSet<ProfileSection> selectedSections = normalizeSections(sections);
        boolean previousAutoSave = SettingsManager.isAutoSaveEnabled();
        SettingsManager.setAutoSaveEnabled(false);
        try {
            applyProfileData(profile, selectedSections);
        } finally {
            SettingsManager.setAutoSaveEnabled(previousAutoSave);
        }

        SettingsManager.saveSettings();
        setActiveProfileName(safeName);
        Client.LOGGER.info("Loaded profile '{}' from {}", safeName, profilePath);
    }

    public static synchronized void deleteProfile(String profileName) throws IOException {
        ensureDirectories();
        String safeName = sanitizeProfileName(profileName);
        Path profilePath = getProfilePath(safeName);
        if (!Files.exists(profilePath)) {
            throw new IOException("Profile not found: " + safeName);
        }

        Files.delete(profilePath);
        if (safeName.equals(activeProfileName)) {
            List<String> profiles = listProfiles();
            setActiveProfileName(profiles.isEmpty() ? "" : profiles.get(0));
        }
        Client.LOGGER.info("Deleted profile '{}'", safeName);
    }

    public static synchronized List<String> listProfiles() {
        try {
            ensureDirectories();
            List<String> profiles = new ArrayList<>();
            try (var stream = Files.list(getProfilesDir())) {
                stream.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
                        .forEach(path -> profiles.add(readProfileDisplayName(path)));
            }
            Collections.sort(profiles, String.CASE_INSENSITIVE_ORDER);
            return profiles;
        } catch (IOException e) {
            Client.LOGGER.error("Failed to list profiles: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public static synchronized boolean profileExists(String profileName) {
        try {
            return Files.exists(getProfilePath(sanitizeProfileName(profileName)));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static synchronized String getActiveProfileName() {
        return activeProfileName == null || activeProfileName.isBlank() ? "None" : activeProfileName;
    }

    public static EnumSet<ProfileSection> allSections() {
        return EnumSet.allOf(ProfileSection.class);
    }

    private static void applyProfileData(Map<String, Object> profile, EnumSet<ProfileSection> sections) {
        if (sections.contains(ProfileSection.MODULES)
                && profile.get(ProfileSection.MODULES.getJsonKey()) instanceof Map<?, ?> enabledData) {
            applyModuleEnabled((Map<String, Object>) enabledData);
        }

        if (sections.contains(ProfileSection.MODULE_SETTINGS)
                && profile.get(ProfileSection.MODULE_SETTINGS.getJsonKey()) instanceof Map<?, ?> settingsData) {
            applyModuleSettings((Map<String, Object>) settingsData);
        }
    }

    private static void applyModuleEnabled(Map<String, Object> enabledData) {
        for (Map.Entry<String, Object> entry : enabledData.entrySet()) {
            Module module = Modules.getModuleById(entry.getKey());
            if (module == null || !(entry.getValue() instanceof Boolean enabled)) {
                continue;
            }

            if (module.isEnabled() == enabled) {
                continue;
            }

            try {
                if (enabled) {
                    module.enable(false);
                } else {
                    module.disable(false);
                }
                if (module.isEnabled() != enabled) {
                    module.setEnabled(enabled);
                }
            } catch (Exception e) {
                Client.LOGGER.error("Failed to apply enabled state for module {}: {}", module.getId(), e.getMessage(), e);
            }
        }
    }

    private static void applyModuleSettings(Map<String, Object> settingsData) {
        for (Map.Entry<String, Object> entry : settingsData.entrySet()) {
            Module module = Modules.getModuleById(entry.getKey());
            if (module == null || !(entry.getValue() instanceof Map<?, ?> moduleData)) {
                continue;
            }

            Map<String, Object> importedSettings = new LinkedHashMap<>((Map<String, Object>) moduleData);
            importedSettings.remove("enabled");
            try {
                module.importSettings(importedSettings);
            } catch (Exception e) {
                Client.LOGGER.error("Failed to apply settings for module {}: {}", module.getId(), e.getMessage(), e);
            }
        }
    }

    private static Map<String, Object> captureProfileData(String profileName, EnumSet<ProfileSection> sections) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("schemaVersion", SCHEMA_VERSION);
        profile.put("name", profileName);
        profile.put("variant", getCurrentVariantName());
        profile.put("updatedAt", Instant.now().toString());

        if (sections.contains(ProfileSection.MODULES)) {
            profile.put(ProfileSection.MODULES.getJsonKey(), captureModuleEnabled());
        }
        if (sections.contains(ProfileSection.MODULE_SETTINGS)) {
            profile.put(ProfileSection.MODULE_SETTINGS.getJsonKey(), captureModuleSettings());
        }

        profile.put("sections", getPresentSectionNames(profile));
        return profile;
    }

    private static Map<String, Object> captureModuleEnabled() {
        Map<String, Object> moduleEnabled = new LinkedHashMap<>();
        for (Module module : Modules.moduleList) {
            if (module != null) {
                moduleEnabled.put(module.getId(), module.isEnabled());
            }
        }
        return moduleEnabled;
    }

    private static Map<String, Object> captureModuleSettings() {
        Map<String, Object> moduleSettings = new LinkedHashMap<>();
        for (Module module : Modules.moduleList) {
            if (module == null) {
                continue;
            }

            Map<String, Object> exported = new LinkedHashMap<>(module.exportSettings());
            exported.remove("enabled");
            moduleSettings.put(module.getId(), exported);
        }
        return moduleSettings;
    }

    private static List<String> getPresentSectionNames(Map<String, Object> profile) {
        List<String> sections = new ArrayList<>();
        for (ProfileSection section : ProfileSection.values()) {
            if (profile.containsKey(section.getJsonKey())) {
                sections.add(section.name().toLowerCase(Locale.ROOT));
            }
        }
        return sections;
    }

    private static EnumSet<ProfileSection> normalizeSections(EnumSet<ProfileSection> sections) {
        if (sections == null || sections.isEmpty()) {
            return allSections();
        }
        return EnumSet.copyOf(sections);
    }

    private static void loadState() {
        Path stateFile = getStateFile();
        if (!Files.exists(stateFile)) {
            activeProfileName = "";
            return;
        }

        try {
            Map<String, Object> state = readMap(stateFile);
            activeProfileName = stringValue(state.get("activeProfile"), "");
        } catch (IOException | JsonSyntaxException e) {
            Client.LOGGER.error("Failed to load profile state: {}", e.getMessage(), e);
            activeProfileName = "";
        }
    }

    private static void setActiveProfileName(String profileName) throws IOException {
        activeProfileName = profileName == null ? "" : profileName;
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("variant", getCurrentVariantName());
        state.put("activeProfile", activeProfileName);
        writeMap(getStateFile(), state);
    }

    private static String readProfileDisplayName(Path path) {
        try {
            Map<String, Object> profile = readMap(path);
            return sanitizeProfileName(stringValue(profile.get("name"), stripJsonExtension(path.getFileName().toString())));
        } catch (IOException | JsonSyntaxException | IllegalArgumentException e) {
            return stripJsonExtension(path.getFileName().toString());
        }
    }

    private static Path getProfilePath(String profileName) {
        return getProfilesDir().resolve(sanitizeFileName(profileName) + ".json");
    }

    private static Map<String, Object> readMap(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            Map<String, Object> map = GSON.fromJson(reader, MAP_TYPE);
            return map == null ? new LinkedHashMap<>() : map;
        }
    }

    private static void writeMap(Path path, Map<String, Object> data) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(data, writer);
        }
    }

    private static void ensureDirectories() throws IOException {
        Files.createDirectories(getProfilesDir());
        Files.createDirectories(PROFILE_STATE_DIR);
    }

    private static void validateProfileVariant(Map<String, Object> profile, String profileName) throws IOException {
        Object profileVariant = profile.get("variant");
        if (!(profileVariant instanceof String storedVariant) || storedVariant.isBlank()) {
            return;
        }

        String currentVariant = getCurrentVariantName();
        if (!storedVariant.equals(currentVariant)) {
            throw new IOException("Profile '" + profileName + "' is for " + storedVariant + ", not " + currentVariant);
        }
    }

    private static Path getProfilesDir() {
        return PROFILES_ROOT_DIR.resolve(getCurrentVariantFileName());
    }

    private static Path getStateFile() {
        return PROFILE_STATE_DIR.resolve(getCurrentVariantFileName() + ".json");
    }

    private static String getCurrentVariantName() {
        String variantName = Client.getClientType();
        return variantName == null || variantName.isBlank() ? "Unknown" : variantName;
    }

    private static String getCurrentVariantFileName() {
        return sanitizeFileName(getCurrentVariantName()).toLowerCase(Locale.ROOT);
    }

    private static String sanitizeProfileName(String profileName) {
        String sanitized = profileName == null ? "" : profileName.trim();
        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("Profile name cannot be empty");
        }
        sanitized = sanitized.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (sanitized.isBlank()) {
            throw new IllegalArgumentException("Profile name cannot be empty");
        }
        return sanitized;
    }

    private static String sanitizeFileName(String value) {
        String sanitized = value == null ? "profile" : value.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
        sanitized = sanitized.replaceAll("\\s+", "_");
        return sanitized.isBlank() ? "profile" : sanitized;
    }

    private static String stripJsonExtension(String fileName) {
        if (fileName != null && fileName.endsWith(".json")) {
            return fileName.substring(0, fileName.length() - 5);
        }
        return fileName == null ? "Profile" : fileName;
    }

    private static String stringValue(Object value, String fallback) {
        return value instanceof String stringValue && !stringValue.isBlank() ? stringValue : fallback;
    }
}
