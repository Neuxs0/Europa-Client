package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.utils.rendering.Renderer;
import finalforeach.cosmicreach.CosmicReachFont;

public class FontRenderer extends Renderer {
    private static final Vector2 tempDimensions = new Vector2();

    private static BitmapFont getFont(String fontName) {
        if (fontName != null && !fontName.isEmpty() && !fontName.equalsIgnoreCase("cosmicreach")) {
            Client.LOGGER.warn("Custom font '{}' requested, using default 'cosmicreach'.", fontName);
        }
        try {
            BitmapFont font = CosmicReachFont.getFont();
            if (font == null) {
                Client.LOGGER.error("CosmicReachFont.getFont() returned null!");
            }
            return font;
        } catch (Exception e) {
            Client.LOGGER.error("Failed to get CosmicReachFont: {}", e.getMessage(), e);
            return null;
        }
    }

    public static Vector2 getTextDimensions(String fontName, String text) {
        BitmapFont font = getFont(fontName);
        if (font == null || text == null || text.isEmpty()) return tempDimensions.set(0,0);
        glyphLayout.setText(font, text);
        return tempDimensions.set(glyphLayout.width, glyphLayout.height);
    }

    public static void drawText(Matrix4 projectionMatrix, String fontName, String text, float x, float y, Color color, int alignment, float wrapWidth, boolean wrap) {
        if (text == null || text.isEmpty() || color == null || color.a <= 0) {
            return;
        }

        BitmapFont font = getFont(fontName);
        if (font == null) {
            Client.LOGGER.error("Cannot draw text, font is unavailable.");
            return;
        }

        Color originalFontColor = font.getColor().cpy();

        glyphLayout.setText(font, text, 0, text.length(), color, wrapWidth, alignment, wrap, null);

        font.setColor(color);

        batch.setProjectionMatrix(projectionMatrix);
        batch.begin();

        font.draw(batch, glyphLayout, x, y);

        batch.end();

        font.setColor(originalFontColor);
    }
}