package dev.neuxs.europa_client.managers.font;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.utils.ColorUtils;
import finalforeach.cosmicreach.CosmicReachFont;
import finalforeach.cosmicreach.FontTexture;
import finalforeach.cosmicreach.ui.FontRenderer;

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
        //  Separate Client's custom fonts and user's custom fonts for better customization
        //  Add file management for user's custom fonts
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

    public Vector2 getTextDimensions(Viewport uiViewport, BitmapFont font, String text) {
        Vector2 a = new Vector2(0f, 0f);
        if (text == null || text.trim().isEmpty()) return a;

        if (getFontName(font).equals("cosmicreach")) return FontRenderer.getTextDimensions(uiViewport, text, a);
        else return a;
    }
}
