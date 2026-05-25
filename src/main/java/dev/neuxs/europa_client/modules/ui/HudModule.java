package dev.neuxs.europa_client.modules.ui;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.settings.Setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "unchecked"})
public abstract class HudModule extends Module {
    private static final String HUD_DATA_KEY = "hud";
    private static final String HUD_X_KEY = "x";
    private static final String HUD_Y_KEY = "y";
    private static final String HUD_SCALE_KEY = "scale";
    private static final String HUD_LOCKED_KEY = "locked";
    private static final float UNSET_POSITION = -1f;
    private static final float DEFAULT_MARGIN = 8f;
    private static final float MIN_HUD_SCALE = 0.5f;
    private static final float MAX_HUD_SCALE = 4f;

    private float hudX = UNSET_POSITION;
    private float hudY = UNSET_POSITION;
    private float hudScale = 1f;
    private boolean hudLocked = false;

    public HudModule(String id, int defaultKeybind, boolean defaultEnabled) {
        super(id, defaultKeybind, defaultEnabled, false);
    }

    public abstract void renderHud(Viewport viewport);

    public abstract Vector2 getHudSize(Viewport viewport);

    public String getHudDisplayName() {
        return getId();
    }

    public Rectangle getHudBounds(Viewport viewport) {
        Vector2 size = getHudSize(viewport);
        Vector2 position = getHudPosition(viewport, size);
        return new Rectangle(position.x, position.y, size.x, size.y);
    }

    public Vector2 getHudPosition(Viewport viewport) {
        return getHudPosition(viewport, getHudSize(viewport));
    }

    protected Vector2 getHudPosition(Viewport viewport, Vector2 size) {
        Vector2 requested = isHudPositionSet()
                ? new Vector2(hudX, hudY)
                : getDefaultHudPosition(viewport, size);
        return clampHudPosition(requested.x, requested.y, size, viewport);
    }

    public void setHudPosition(float x, float y, Viewport viewport) {
        Vector2 clamped = clampHudPosition(x, y, getHudSize(viewport), viewport);
        hudX = clamped.x;
        hudY = clamped.y;
    }

    public float getHudScale() {
        return hudScale;
    }

    public void setHudScale(float hudScale) {
        this.hudScale = MathUtils.clamp(hudScale, MIN_HUD_SCALE, MAX_HUD_SCALE);
    }

    public boolean isHudLocked() {
        return hudLocked;
    }

    public void setHudLocked(boolean hudLocked) {
        this.hudLocked = hudLocked;
    }

    public boolean hasHudSettings() {
        return !customSettings.isEmpty();
    }

    public List<Setting<?>> getHudSettings() {
        return new ArrayList<>(customSettings.values());
    }

    public boolean canBeHiddenInHudEditor() {
        return true;
    }

    protected Vector2 getDefaultHudPosition(Viewport viewport, Vector2 size) {
        float viewportHeight = viewport == null ? 0f : viewport.getWorldHeight();
        float height = size == null ? 0f : size.y;
        return new Vector2(DEFAULT_MARGIN, Math.max(0f, viewportHeight - DEFAULT_MARGIN - height));
    }

    private boolean isHudPositionSet() {
        return hudX >= 0f && hudY >= 0f;
    }

    private Vector2 clampHudPosition(float x, float y, Vector2 size, Viewport viewport) {
        float width = size == null ? 0f : Math.max(0f, size.x);
        float height = size == null ? 0f : Math.max(0f, size.y);
        float viewportWidth = viewport == null ? width : Math.max(width, viewport.getWorldWidth());
        float viewportHeight = viewport == null ? height : Math.max(height, viewport.getWorldHeight());

        return new Vector2(
                MathUtils.clamp(x, 0f, Math.max(0f, viewportWidth - width)),
                MathUtils.clamp(y, 0f, Math.max(0f, viewportHeight - height))
        );
    }

    @Override
    public Map<String, Object> exportSettings() {
        Map<String, Object> exported = super.exportSettings();
        Map<String, Object> hudData = new HashMap<>();
        hudData.put(HUD_X_KEY, hudX);
        hudData.put(HUD_Y_KEY, hudY);
        hudData.put(HUD_SCALE_KEY, hudScale);
        hudData.put(HUD_LOCKED_KEY, hudLocked);
        exported.put(HUD_DATA_KEY, hudData);
        return exported;
    }

    @Override
    public void importSettings(Map<String, Object> data) {
        super.importSettings(data);

        Object rawHudData = data.get(HUD_DATA_KEY);
        if (!(rawHudData instanceof Map<?, ?> hudData)) {
            return;
        }

        hudX = readFloat(hudData.get(HUD_X_KEY), hudX);
        hudY = readFloat(hudData.get(HUD_Y_KEY), hudY);
        setHudScale(readFloat(hudData.get(HUD_SCALE_KEY), hudScale));
        Object locked = hudData.get(HUD_LOCKED_KEY);
        if (locked instanceof Boolean bool) {
            hudLocked = bool;
        }
    }

    private float readFloat(Object value, float fallback) {
        if (value instanceof Number number) {
            return number.floatValue();
        }
        if (value instanceof String text) {
            try {
                return Float.parseFloat(text);
            } catch (NumberFormatException ignored) {
            }
        }
        return fallback;
    }
}
