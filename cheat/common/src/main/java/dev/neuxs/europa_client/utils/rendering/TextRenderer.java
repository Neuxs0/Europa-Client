package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import dev.neuxs.europa_client.managers.font.FontManager;
import dev.neuxs.europa_client.utils.ColorUtils;

@SuppressWarnings("unused")
public class TextRenderer {
    private static final FontManager fontManager = new FontManager();

    private String text;
    private float x;
    private float y;
    private BitmapFont font;
    private Color color;
    private int alignment;
    private float wrapWidth;
    private boolean wrap;
    private static final BitmapFont defaultFont = fontManager.getFont("cosmicreach");
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
        this.font = defaultFont;
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

    public TextRenderer(String text, float x, float y, BitmapFont font, Color color, int alignment, float wrapWidth, boolean wrap) {
        setText(text);
        setPosition(x, y);
        setFont(font);
        setColor(color);
        setAlignment(alignment);
        setWrapWidth(wrapWidth);
        setWrap(wrap);
    }

    public void render(SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        if (this.text == null || this.text.isEmpty() || this.color == null || this.color.a <= 0) return;

        glyphLayout.setText(font, this.text, 0, this.text.length(), this.color, this.wrapWidth, this.alignment, this.wrap, null);

        font.setColor(this.color);
        font.draw(spriteBatch, glyphLayout, this.x, this.y);
        font.setColor(defaultColor.cpy());
    }

    public Vector2 getDimensions(GlyphLayout glyphLayout) {
        if (this.text == null || this.text.isEmpty()) return new Vector2(0, 0);
        glyphLayout.setText(font, this.text, 0, this.text.length(), this.color, this.wrapWidth, this.alignment, this.wrap, null);
        return new Vector2(glyphLayout.width, glyphLayout.height);
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
        return fontManager.getFontName(font);
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
    public void setFont(BitmapFont font) {
        this.font = (font != null && fontManager.isFontAvailable(fontManager.getFontName(font))) ? font : defaultFont;
    }
    public void setFont(String font) {
        this.font = (font != null && !font.trim().isEmpty() && fontManager.isFontAvailable(font.toLowerCase())) ?
                     fontManager.getFont(font.toLowerCase()) : defaultFont;
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
