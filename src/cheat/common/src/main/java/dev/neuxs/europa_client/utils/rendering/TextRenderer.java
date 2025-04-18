package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.managers.FontManager;
import finalforeach.cosmicreach.CosmicReachFont;
import finalforeach.cosmicreach.ui.FontRenderer;

@SuppressWarnings("unused")
public class TextRenderer extends Renderer {
    private static final FontManager fontManager = new FontManager();
    private String text;
    private BitmapFont font;

    public TextRenderer() {
        this.text = "";
        this.font = fontManager.getFont(fontManager.cosmicReachFontKey);
        if (this.font == null) {
            Client.LOGGER.error("Default Cosmic Reach font is null during TextRenderer initialization!");
        }
        setRenderType(RenderUtil.RenderType.SPRITE);
    }

    @Override
    public void renderSprite(Viewport viewport, SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        if (this.font == null) {
            Client.LOGGER.trace("Skipping render: font is null for text: {}", this.text);
        }
        if (spriteBatch == null) {
            Client.LOGGER.error("Skipping render: SpriteBatch is null for text: {}", this.text);
            return;
        }
        if (glyphLayout == null) {
            Client.LOGGER.error("Skipping render: GlyphLayout is null for text: {}", this.text);
            return;
        }

        float posX = getPosX();
        float posY = getPosY();
        Color color = getTextColor();
        if (this.text == null || this.text.isEmpty() || color == null || color.a <= 0) {
            return;
        }

        spriteBatch.setColor(color);

        if (this.font instanceof CosmicReachFont) {
            FontRenderer.drawText(spriteBatch, viewport, this.text, posX, posY, true); // TODO: Make my own renderer for CR's font
        } else {
            glyphLayout.setText(font, this.text, 0, this.text.length(), color, 0.0F, 8, false, null);
            font.draw(spriteBatch, glyphLayout, posX, posY);
        }
    }

    public String getText() {
        return text;
    }
    public BitmapFont getFont() {
        return font;
    }
    public String getFontName() {
        return fontManager.getFontName(this.font);
    }
    public float getHeight(Viewport viewport) {
        if (font == null || text == null || text.isEmpty() || viewport == null) return 0f;
        return fontManager.getTextDimensions(viewport, font, text).y;
    }
    public float getWidth(Viewport viewport) {
        if (font == null || text == null || text.isEmpty() || viewport == null) return 0f;
        return fontManager.getTextDimensions(viewport, font, text).x;
    }

    public void setText(String text) {
        this.text = (text != null) ? text : "";
    }
    public void setFont(BitmapFont font) {
        if (font != null && fontManager.getFontName(font) != null) {
            this.font = font;
        } else {
            this.font = fontManager.getFont(fontManager.cosmicReachFontKey);
            if (font != null) {
                Client.LOGGER.warn("Attempted to set an unknown BitmapFont instance in TextRenderer. Reverting to default.");
            }
        }
        if (this.font == null) {
            Client.LOGGER.error("Font is null in TextRenderer even after attempting to set/fallback!");
        }
    }
    public void setFont(String fontName) {
        BitmapFont resolvedFont = null;
        if (fontName != null && !fontName.trim().isEmpty()) {
            resolvedFont = fontManager.getFont(fontName.toLowerCase());
        }

        if (resolvedFont != null) {
            this.font = resolvedFont;
        } else {
            this.font = fontManager.getFont(fontManager.cosmicReachFontKey);
            if (fontName != null) {
                Client.LOGGER.warn("Font name '{}' not found in FontManager. Reverting TextRenderer to default.", fontName);
            }
        }

        if (this.font == null) {
            Client.LOGGER.error("Font is null in TextRenderer even after attempting to set/fallback by name!");
        }
    }
}
