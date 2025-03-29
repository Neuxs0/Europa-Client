package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client; // Assuming Client logger is accessible or pass one
import dev.neuxs.europa_client.managers.font.FontManager;
import dev.neuxs.europa_client.utils.ColorUtils;
// Import the game's FontRenderer
import finalforeach.cosmicreach.ui.FontRenderer;
import finalforeach.cosmicreach.CosmicReachFont; // Import for instanceof check

@SuppressWarnings("unused")
public class TextRenderer {
    private static final FontManager fontManager = FontManager.getInstance();
    private String text;
    private float x;
    private float y;
    private BitmapFont font;
    private Color color;
    private int alignment;
    private float wrapWidth;
    private boolean wrap;
    private static final Color defaultColor = ColorUtils.color(255, 255, 255, 255);
    private static final int defaultAlignment = Align.left;
    private static final float defaultWrapWidth = 0f;
    private static final boolean defaultWrap = false;
    private static final int[] validAlignments = {
            Align.top, Align.topLeft, Align.topRight,
            Align.center, Align.left, Align.right,
            Align.bottom, Align.bottomLeft, Align.bottomRight
    };

    public TextRenderer() {
        this.text = "";
        this.x = 0f;
        this.y = 0f;
        this.font = fontManager.getCosmicReachFont();
        if (this.font == null) {
            Client.LOGGER.error("Default Cosmic Reach font is null during TextRenderer initialization!");
        }
        this.color = defaultColor.cpy();
        this.alignment = defaultAlignment;
        this.wrapWidth = defaultWrapWidth;
        this.wrap = defaultWrap;
    }

    public void render(SpriteBatch spriteBatch, GlyphLayout glyphLayout, Viewport viewport) {
        if (this.text == null || this.text.isEmpty() || this.color == null || this.color.a <= 0 || this.font == null || viewport == null) {
            if(this.font == null) {
                Client.LOGGER.trace("Skipping render: font is null for text: {}", this.text);
            }
            if(viewport == null) {
                Client.LOGGER.trace("Skipping render: viewport is null for text: {}", this.text);
            }
            return;
        }

        Color previousBatchColor = spriteBatch.getColor().cpy();
        spriteBatch.setColor(this.color);

        if (this.font instanceof CosmicReachFont) {
            // This renderer knows how to handle the game's specific texture atlas and glyph setup.
            // IMPORTANT: FontRenderer.drawText draws relative to the bottom-left 'y' coordinate
            // and does *not* inherently support GlyphLayout's alignment or wrapping features.
            // Positioning must be handled by setting 'x' and 'y' correctly before calling render.
            // It might render text visually upside-down depending on the coordinate system,
            // requiring adjustment of the 'y' coordinate calculation.
            FontRenderer.drawText(spriteBatch, viewport, this.text, this.x, this.y, true);
        } else {
            glyphLayout.setText(font, this.text, 0, this.text.length(), this.color, this.wrapWidth, this.alignment, this.wrap, null);
            font.draw(spriteBatch, glyphLayout, this.x, this.y);
        }

        spriteBatch.setColor(previousBatchColor);
    }

    public String getText() {
        return text;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public BitmapFont getFont() {
        return font;
    }
    public String getFontName() {
        return fontManager.getFontName(this.font);
    }
    public Color getColor() {
        return color.cpy();
    }
    public int getAlignment() {
        return alignment;
    }
    public float getWrapWidth() {
        return wrapWidth;
    }
    public boolean isWrap() {
        return wrap;
    }
    public float getHeight(Viewport viewport) {
        if (font == null || text == null || text.isEmpty() || viewport == null) return 0f;
        // Delegate dimension calculation to FontManager
        return fontManager.getTextDimensions(viewport, font, text).y;
    }
    public float getWidth(Viewport viewport) {
        if (font == null || text == null || text.isEmpty() || viewport == null) return 0f;
        // Delegate dimension calculation to FontManager
        return fontManager.getTextDimensions(viewport, font, text).x;
    }

    public void setText(String text) {
        this.text = (text != null) ? text : "";
    }
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public void setX(float x) {
        this.x = x;
    }
    public void setY(float y) {
        this.y = y;
    }
    public void setFont(BitmapFont font) {
        BitmapFont defaultFontInstance = fontManager.getCosmicReachFont();
        if (font != null && fontManager.getFontName(font) != null) {
            this.font = font;
        } else {
            this.font = defaultFontInstance;
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
            this.font = fontManager.getCosmicReachFont();
            if (fontName != null) {
                Client.LOGGER.warn("Font name '{}' not found in FontManager. Reverting TextRenderer to default.", fontName);
            }
        }

        if (this.font == null) {
            Client.LOGGER.error("Font is null in TextRenderer even after attempting to set/fallback by name!");
        }
    }
    public void setColor(Color color) {
        this.color = (color != null) ? color.cpy() : defaultColor.cpy();
    }
    public void setAlignment(int alignment) {
        boolean isValid = false;
        for (int validAlign : validAlignments) {
            if (validAlign == alignment) {
                isValid = true;
                break;
            }
        }
        this.alignment = isValid ? alignment : defaultAlignment;
    }
    public void setWrapWidth(float wrapWidth) {
        this.wrapWidth = Math.max(0, wrapWidth);
    }
    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }
}