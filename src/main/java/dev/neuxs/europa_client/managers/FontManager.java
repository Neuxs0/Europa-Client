package dev.neuxs.europa_client.managers;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import finalforeach.cosmicreach.CosmicReachFont;
import finalforeach.cosmicreach.ui.FontRenderer;

import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class FontManager {
    private final GlyphLayout glyphLayout = new GlyphLayout();

    public final Map<String, BitmapFont> fontMap;
    public final String cosmicReachFontKey = "cosmicreach";

    public FontManager() {
        this.fontMap = new ConcurrentHashMap<>();
        loadCosmicReachFont();

        // TODO: Implement custom font loading
    }

    private void loadCosmicReachFont() {
        try {
            BitmapFont cosmicReachFont = CosmicReachFont.getFont();
            if (cosmicReachFont != null) {
                fontMap.put(cosmicReachFontKey, cosmicReachFont);
            } else {
                Client.LOGGER.error("Failed to get Cosmic Reach's font: CosmicReachFont.getFont() returned null during initialization.");
            }
        } catch (Exception e) {
            Client.LOGGER.error("Exception occurred while initializing Cosmic Reach's font: {}", e.getMessage(), e);
        }
    }

    public BitmapFont getFont(String name) {
        if (name == null || name.trim().isEmpty()) {
            Client.LOGGER.warn("Attempted to get font with null or empty name.");
            return null;
        }

        if (cosmicReachFontKey.equalsIgnoreCase(name)) {
            return CosmicReachFont.getFont();
        }

        return fontMap.get(name.toLowerCase());
    }

    public String getFontName(BitmapFont font) {
        if (font == null) {
            return null;
        }

        if (font instanceof CosmicReachFont) {
            return cosmicReachFontKey;
        }

        for (Map.Entry<String, BitmapFont> entry : fontMap.entrySet()) {
            if (entry.getValue() == font) {
                return entry.getKey();
            }
        }

        return null;
    }

    public boolean isFontAvailable(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (cosmicReachFontKey.equalsIgnoreCase(name)) {
            return true;
        }
        return fontMap.containsKey(name.toLowerCase());
    }

    public Set<String> getAvailableFontNames() {
        return Collections.unmodifiableSet(fontMap.keySet());
    }

    public Vector2 getTextDimensions(Viewport uiViewport, BitmapFont font, String text) {
        Vector2 dimensions = new Vector2(0f, 0f);

        if (font == null || text == null || text.isEmpty()) {
            return dimensions;
        }

        if (font instanceof CosmicReachFont) {
            return FontRenderer.getTextDimensions(uiViewport, text, dimensions);
        } else {
            String fontName = getFontName(font);
            if (fontMap.containsKey(fontName)) {
                synchronized (glyphLayout) {
                    glyphLayout.setText(font, text);
                    dimensions.set(glyphLayout.width, glyphLayout.height);
                }
            } else {
                Client.LOGGER.warn("getTextDimensions called with an unrecognized non-CosmicReach font.");
            }
            return dimensions;
        }
    }

    public void dispose() {
        Client.LOGGER.info("Disposing FontManager resources...");
        for (Map.Entry<String, BitmapFont> entry : fontMap.entrySet()) {
            if (!cosmicReachFontKey.equals(entry.getKey())) {
                Client.LOGGER.debug("Disposing custom font: {}", entry.getKey());
                entry.getValue().dispose();
            }
        }
        fontMap.clear();
        Client.LOGGER.info("FontManager disposed.");
    }
}
