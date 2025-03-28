package dev.neuxs.europa_client.managers.font;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import dev.neuxs.europa_client.Client;
import finalforeach.cosmicreach.CosmicReachFont;

import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class FontManager {

    public final Map<String, BitmapFont> fontMap;

    public FontManager() {
        this.fontMap = new ConcurrentHashMap<>();

        try {
            BitmapFont cosmicReachFont = CosmicReachFont.getFont();
            if (cosmicReachFont != null) {
                fontMap.put("cosmicreach", cosmicReachFont);
            } else {
                Client.LOGGER.error("Failed to load Cosmic Reach's font: CosmicReachFont.getFont() returned null.");
            }
        } catch (Exception e) {
            Client.LOGGER.error("Exception occurred while loading Cosmic Reach's font: {}", e.getMessage(), e);
        }

        // TODO: Custom fonts
    }

    public BitmapFont getFont(String name) {
        if (name == null || name.trim().isEmpty()) return null;

        BitmapFont font = fontMap.get(name.toLowerCase());
        if (font == null) Client.LOGGER.error("Font not found or not loaded.");

        return font;
    }
    public String getFontName(BitmapFont font) {
        if (font == null) return null;

        for (Map.Entry<String, BitmapFont> entry : fontMap.entrySet()) {
            if (entry.getValue() == font) {
                return entry.getKey();
            }
        }

        Client.LOGGER.error("Font object not found in FontRenderer map.");
        return null;
    }
    public boolean isFontAvailable(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        return fontMap.containsKey(name.toLowerCase());
    }
    public Set<String> getAvailableFontNames() {
        return Collections.unmodifiableSet(fontMap.keySet());
    }
}