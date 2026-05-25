package dev.neuxs.europa_client.modules.utils;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.settings.Setting;
import dev.neuxs.europa_client.utils.Chat;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.settings.GraphicsSettings;
import finalforeach.cosmicreach.ui.UI;

import java.util.Map;

@SuppressWarnings("unchecked")
public class Zoom extends Module {
    private static final float MIN_ZOOM = 1.0f;
    private static final float MIN_FOV = 1.0f;
    private static final float DEFAULT_ZOOM = 3.0f;
    private static final float NUMERIC_STEP = 0.5f;
    private static final float EQUAL_STEP = 1.1892071f; // 2^(1/4)

    private float zoomAmount = DEFAULT_ZOOM;
    private float savedZoomAmount = DEFAULT_ZOOM;

    public Zoom(int keybind, boolean defaultEnabled) {
        super("Zoom", keybind, defaultEnabled, false);
        customSettings.put("showHand", new Setting<>("showHand", false)
                .withDisplayName("Show Hand"));
        customSettings.put("showHotbar", new Setting<>("showHotbar", true)
                .withDisplayName("Show Hotbar"));
        customSettings.put("smoothCamera", new Setting<>("smoothCamera", true)
                .withDisplayName("Smooth Camera"));
        customSettings.put("saveZoom", new Setting<>("saveZoom", false)
                .withDisplayName("Save Zoom"));
        customSettings.put("equalZoom", new Setting<>("equalZoom", true)
                .withDisplayName("Equal Zoom"));
    }

    @Override
    public void enable(boolean messaging) {
        zoomAmount = shouldSaveZoom() ? savedZoomAmount : DEFAULT_ZOOM;
        setEnabled(true);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Zoom enabled");
    }

    @Override
    public void disable(boolean messaging) {
        if (shouldSaveZoom()) {
            savedZoomAmount = zoomAmount;
        }
        setEnabled(false);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Zoom disabled");
    }

    public boolean handleScroll(float amountY) {
        if (!isEnabled() || !(GameState.currentGameState instanceof InGame) || UI.uiNeedMouse) {
            return false;
        }

        if (amountY < 0f) {
            zoomIn();
        } else if (amountY > 0f) {
            zoomOut();
        } else {
            return false;
        }

        if (shouldSaveZoom()) {
            savedZoomAmount = zoomAmount;
        }
        return true;
    }

    public float getZoomedFov(float currentFov) {
        if (!isEnabled()) {
            return currentFov;
        }
        float baseFov = getConfiguredFov(currentFov);
        return getZoomedFov(currentFov, baseFov);
    }

    public float getZoomedFov(float currentFov, float baseFov) {
        if (!isEnabled()) {
            return currentFov;
        }
        return Math.max(MIN_FOV, currentFov - baseFov + baseFov / zoomAmount);
    }

    public boolean shouldShowHand() {
        return !isEnabled() || getBoolean("showHand", false);
    }

    public boolean shouldShowHotbar() {
        return !isEnabled() || isUiMouseNeeded() || getBoolean("showHotbar", true);
    }

    public boolean shouldSmoothCamera() {
        return isEnabled() && getBoolean("smoothCamera", true);
    }

    public boolean shouldPersistZoomAmount() {
        return shouldSaveZoom();
    }

    @Override
    public Map<String, Object> exportSettings() {
        Map<String, Object> obj = super.exportSettings();
        obj.put("savedZoomAmount", savedZoomAmount);
        return obj;
    }

    @Override
    public void importSettings(Map<String, Object> data) {
        super.importSettings(data);
        Object saved = data.get("savedZoomAmount");
        if (saved instanceof Number number) {
            savedZoomAmount = clampZoom(number.floatValue());
            if (!isEnabled()) {
                zoomAmount = savedZoomAmount;
            }
        }
        savedZoomAmount = clampZoom(savedZoomAmount);
        zoomAmount = clampZoom(zoomAmount);
    }

    private void zoomIn() {
        if (usesEqualZoom()) {
            zoomAmount *= EQUAL_STEP;
        } else {
            zoomAmount += NUMERIC_STEP;
        }
        zoomAmount = clampZoom(zoomAmount);
    }

    private void zoomOut() {
        if (usesEqualZoom()) {
            zoomAmount /= EQUAL_STEP;
        } else {
            zoomAmount -= NUMERIC_STEP;
        }
        zoomAmount = clampZoom(zoomAmount);
    }

    private boolean usesEqualZoom() {
        return getBoolean("equalZoom", true);
    }

    private boolean shouldSaveZoom() {
        return getBoolean("saveZoom", false);
    }

    private boolean getBoolean(String key, boolean fallback) {
        Setting<Boolean> setting = (Setting<Boolean>) customSettings.get(key);
        return setting == null ? fallback : setting.getValue();
    }

    private boolean isUiMouseNeeded() {
        try {
            return UI.uiNeedMouse;
        } catch (RuntimeException | LinkageError e) {
            return false;
        }
    }

    private float clampZoom(float value) {
        return Math.max(MIN_ZOOM, Math.min(getMaxZoom(), value));
    }

    private float getMaxZoom() {
        return Math.max(MIN_ZOOM, getConfiguredFov(DEFAULT_ZOOM) / MIN_FOV);
    }

    private float getConfiguredFov(float fallback) {
        try {
            return GraphicsSettings.fieldOfView.getValue();
        } catch (RuntimeException | LinkageError e) {
            return fallback;
        }
    }
}
