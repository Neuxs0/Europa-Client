package dev.neuxs.europa_client;

import dev.neuxs.europa_client.commands.ClientCommandRegistry;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.managers.InputManager;
import dev.neuxs.europa_client.settings.ProfileManager;
import dev.neuxs.europa_client.settings.SettingsManager;
import dev.neuxs.europa_client.utils.ClientLogger;
import dev.neuxs.europa_client.variant.ClientVariant;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.chat.IChat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public class Client {
    public static final String MOD_ID = "europa_client";
    public static final String MOD_NAME = "Europa Client";
    public static ClientLogger LOGGER = new ClientLogger("EuropaClient");
    public static String VERSION = "2.0.0";
    public static IChat clientChat = Chat.MAIN_CLIENT_CHAT;
    private static boolean initialized = false;
    private static ClientVariant variant;
    private static boolean attemptedLazyVariantInit = false;

    public static String getClientType() {
        return variant == null ? "Unknown" : variant.clientType();
    }

    public static String getNetworkIdentifier() {
        return MOD_NAME + " " + getClientType() + "/" + VERSION;
    }

    public static ClientVariant getVariant() {
        return variant;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void init(ClientVariant selectedVariant) {
        if (initialized) {
            return;
        }
        variant = selectedVariant;
        initialized = true;

        LOGGER.info("{} Initializing...", MOD_NAME);

        Modules.initModules();
        ProfileManager.initialize();
        SettingsManager.loadSettings();
        SettingsManager.loadClientSettings();
        SettingsManager.startFileWatcher();
        ClientCommandRegistry.registerClientCommands();

        LOGGER.info("{} Initialized!", MOD_NAME);
    }

    // Called every frame
    public static void render() {
        if (!initialized) {
            initFromVariantResource();
        }
        if (!initialized) {
            return;
        }
        SettingsManager.reloadClientSettingsIfChanged();
        if (variant != null) {
            variant.syncModules();
        }
        InputManager.getInstance().initialize();
    }

    private static void initFromVariantResource() {
        if (attemptedLazyVariantInit) {
            return;
        }
        attemptedLazyVariantInit = true;

        try (InputStream stream = Client.class.getClassLoader().getResourceAsStream("europa_client.variant")) {
            if (stream == null) {
                LOGGER.warn("Could not lazily initialize {}: europa_client.variant resource not found.", MOD_NAME);
                return;
            }

            String variantClassName;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                variantClassName = reader.readLine();
            }

            if (variantClassName == null || variantClassName.isBlank()) {
                LOGGER.warn("Could not lazily initialize {}: europa_client.variant is empty.", MOD_NAME);
                return;
            }

            Class<?> variantClass = Class.forName(variantClassName.trim());
            Object selectedVariant = variantClass.getDeclaredConstructor().newInstance();
            if (selectedVariant instanceof ClientVariant clientVariant) {
                init(clientVariant);
            } else {
                LOGGER.error("Could not lazily initialize {}: {} is not a ClientVariant.", MOD_NAME, variantClassName);
            }
        } catch (Exception e) {
            LOGGER.error("Could not lazily initialize {} from europa_client.variant: {}", MOD_NAME, e.getMessage(), e);
        }
    }
}
