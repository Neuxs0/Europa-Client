package dev.neuxs.europa_client.settings;

import dev.neuxs.europa_client.Client;

import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class ClientSettings {
    public static final String DEFAULT_COMMAND_PREFIX = "#";

    public static final Setting<String> COMMAND_PREFIX = new Setting<>(
            "commandPrefix",
            DEFAULT_COMMAND_PREFIX,
            value -> value != null && !value.isBlank() && !value.contains(" ")
    )
            .withDisplayName("Command Prefix")
            .withDescription("Prefix used to run Europa Client commands");

    public static final Setting<Boolean> COMMANDS_ENABLED = new Setting<>(
            "commandsEnabled",
            true
    )
            .withDisplayName("Commands")
            .withDescription("Allow Europa Client commands in chat");

    public static final Setting<String> CLIENT_CHAT_PREFIX = new Setting<>(
            "clientChatPrefix",
            "[Europa Client] ",
            value -> value != null
    )
            .withDisplayName("Chat Prefix")
            .withDescription("Prefix shown before Europa Client chat messages");

    public static final Setting<Boolean> GUI_BACKGROUND_DIM = new Setting<>(
            "guiBackgroundDim",
            true
    )
            .withDisplayName("Dim Background")
            .withDescription("Dim the game behind the Europa Client menu");

    public static final Setting<Boolean> GUI_BACKGROUND_BLUR = new Setting<>(
            "guiBackgroundBlur",
            true
    )
            .withDisplayName("Blur Background")
            .withDescription("Blur the game behind the Europa Client menu");

    public static final Setting<Float> GUI_BACKGROUND_BLUR_STRENGTH = new Setting<>(
            "guiBackgroundBlurStrength",
            0.5f
    )
            .withDisplayName("Blur Strength")
            .withDescription("Strength of the game background blur")
            .withRange(0.25f, 8f)
            .withStep(0.1f);

    private static final Map<String, Setting<?>> SETTINGS = new LinkedHashMap<>();

    static {
        register(COMMANDS_ENABLED);
        register(COMMAND_PREFIX);
        register(CLIENT_CHAT_PREFIX);
        register(GUI_BACKGROUND_DIM);
        register(GUI_BACKGROUND_BLUR);
        register(GUI_BACKGROUND_BLUR_STRENGTH);
    }

    private static void register(Setting<?> setting) {
        SETTINGS.put(setting.getName(), setting);
    }

    public static Map<String, Setting<?>> getSettings() {
        return Collections.unmodifiableMap(SETTINGS);
    }

    public static Map<String, Object> exportSettings() {
        Map<String, Object> exported = new LinkedHashMap<>();
        for (Map.Entry<String, Setting<?>> entry : SETTINGS.entrySet()) {
            exported.put(entry.getKey(), entry.getValue().getValue());
        }
        return exported;
    }

    public static void importSettings(Map<String, Object> data) {
        if (data == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Setting<?> setting = SETTINGS.get(entry.getKey());
            if (setting == null) {
                continue;
            }

            try {
                setting.setValueFromObject(entry.getValue());
            } catch (RuntimeException e) {
                Client.LOGGER.error("Failed to apply client setting {}: {}", entry.getKey(), e.getMessage(), e);
            }
        }
    }

    public static void resetAll() {
        for (Setting<?> setting : SETTINGS.values()) {
            setting.resetToDefault();
        }
    }

    public static boolean areCommandsEnabled() {
        return COMMANDS_ENABLED.getValue();
    }

    public static String getCommandPrefix() {
        String prefix = COMMAND_PREFIX.getValue();
        return prefix == null || prefix.isBlank() ? DEFAULT_COMMAND_PREFIX : prefix.trim();
    }

    public static List<String> getCommandPrefixes() {
        List<String> prefixes = new ArrayList<>();
        prefixes.add(getCommandPrefix());

        if (!prefixes.contains(DEFAULT_COMMAND_PREFIX)) {
            prefixes.add(DEFAULT_COMMAND_PREFIX);
        }

        return prefixes;
    }

    public static String getMatchingCommandPrefix(String messageText) {
        if (messageText == null || messageText.isEmpty()) {
            return "";
        }

        String normalizedMessage = messageText.trim();
        if (normalizedMessage.startsWith("/")) {
            normalizedMessage = normalizedMessage.substring(1);
        }

        String matchedPrefix = "";
        for (String prefix : getCommandPrefixes()) {
            if (prefix != null && !prefix.isEmpty() && normalizedMessage.startsWith(prefix)
                    && prefix.length() > matchedPrefix.length()) {
                matchedPrefix = prefix;
            }
        }

        return matchedPrefix;
    }

    public static boolean isCommandMessage(String messageText) {
        return !getMatchingCommandPrefix(messageText).isEmpty();
    }

    public static String getClientChatPrefix() {
        String prefix = CLIENT_CHAT_PREFIX.getValue();
        return prefix == null ? "" : prefix;
    }

    public static boolean isGuiBackgroundDimEnabled() {
        return GUI_BACKGROUND_DIM.getValue();
    }

    public static boolean isGuiBackgroundBlurEnabled() {
        return GUI_BACKGROUND_BLUR.getValue();
    }

    public static float getGuiBackgroundBlurStrength() {
        Float strength = GUI_BACKGROUND_BLUR_STRENGTH.getValue();
        return strength == null ? GUI_BACKGROUND_BLUR_STRENGTH.getDefaultValue() : strength;
    }
}
