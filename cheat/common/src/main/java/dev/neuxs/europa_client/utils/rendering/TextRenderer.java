// TODO:
//  Manage fonts via FontRenderer
//  Change fontName to font.getName(font)
//  Change setFont(String) to setFont(font)

package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.utils.ColorUtils;
import finalforeach.cosmicreach.CosmicReachFont;

@SuppressWarnings("unused")
public class TextRenderer {
    private String text;
    private float x;
    private float y;
    private String fontName;
    private Color color;
    private int alignment;
    private float wrapWidth;
    private boolean wrap;
    private static final String defaultFontName = "cosmicreach";
    private static final Color defaultColor = ColorUtils.color(255, 255, 255, 255);
    private static final int defaultAlignment = Align.left;
    private static final float defaultWrapWidth = 0f;
    private static final boolean defaultWrap = false;
    private static final int[] validAlignments = {
            Align.top, Align.topLeft, Align.topRight,
            Align.center, Align.left, Align.right,
            Align.bottom, Align.bottomLeft, Align.bottomRight
    };
    private static final String[] fontNames = {
            "cosmicreach"
    };

    public TextRenderer() {
        this.text = "";
        this.x = 0f;
        this.y = 0f;
        this.fontName = defaultFontName;
        this.color = defaultColor.cpy();
        this.alignment = defaultAlignment;
        this.wrapWidth = defaultWrapWidth;
        this.wrap = defaultWrap;
    }

    public TextRenderer(String text, float x, float y, Color color) {
        this();
        setText(text);
        setPosition(x, y);
        setColor(color);
    }

    public TextRenderer(String text, float x, float y, String fontName, Color color, int alignment, float wrapWidth, boolean wrap) {
        setText(text);
        setPosition(x, y);
        setFont(fontName);
        setColor(color);
        setAlignment(alignment);
        setWrapWidth(wrapWidth);
        setWrap(wrap);
    }

    public void render(SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        if (this.text == null || this.text.isEmpty() || this.color == null || this.color.a <= 0) return;

        BitmapFont font = getFontInternal(this.fontName);
        if (font == null) {
            Client.LOGGER.error("Cannot render text, font '{}' is unavailable.", this.fontName);
            return;
        }

        Color originalFontColor = font.getColor();

        glyphLayout.setText(font, this.text, 0, this.text.length(), this.color, this.wrapWidth, this.alignment, this.wrap, null);

        font.setColor(this.color);
        font.draw(spriteBatch, glyphLayout, this.x, this.y);
        font.setColor(originalFontColor);
    }

    public Vector2 getDimensions(GlyphLayout glyphLayout) {
        if (this.text == null || this.text.isEmpty()) return new Vector2(0, 0);

        BitmapFont font = getFontInternal(this.fontName);
        if (font == null) {
            Client.LOGGER.warn("Cannot get dimensions, font '{}' is unavailable.", this.fontName);
            return new Vector2(0, 0);
        }

        glyphLayout.setText(font, this.text, 0, this.text.length(), this.color, this.wrapWidth, this.alignment, this.wrap, null);

        return new Vector2(glyphLayout.width, glyphLayout.height);
    }

    private static BitmapFont getFontInternal(String fontName) {
        String effectiveFontName = (fontName == null || fontName.trim().isEmpty()) ? defaultFontName : fontName;

        if (effectiveFontName.equals("cosmicreach")) {
            try {
                BitmapFont font = CosmicReachFont.getFont();
                if (font == null) Client.LOGGER.error("CosmicReachFont.getFont() returned null for default font!");
                return font;
            } catch (Exception e) {
                Client.LOGGER.error("Failed to get default CosmicReachFont: {}", e.getMessage(), e);
                return null;
            }
        } else {
            // Custom fonts are not implemented yet
            Client.LOGGER.error("Font '{}' is not supported.", effectiveFontName);
            return null;
        }
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
    public String getFontName() {
        return fontName;
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
    public void setFont(String fontName) {
        boolean isValid = false;
        for (String validFont : fontNames) {
            if (validFont.equalsIgnoreCase(fontName)) {
                isValid = true;
                break;
            }
        }
        this.fontName = (fontName != null && !fontName.trim().isEmpty()) && isValid ? fontName.toLowerCase() : defaultFontName;
    }
    public void setColor(Color color) {
        this.color = (color != null) ? color.cpy() : defaultColor;
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