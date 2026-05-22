package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import finalforeach.cosmicreach.CosmicReachFont;
import finalforeach.cosmicreach.ui.FontRenderer;

@SuppressWarnings("unused")
public class TextRenderer extends Renderer {
    private static final float DEFAULT_WRAP_SCALE_THRESHOLD = 0.5f;
    private static final float DEFAULT_MIN_SCALE = 0.35f;

    private final GlyphLayout measurementLayout = new GlyphLayout();
    private final Vector2 measurementDimensions = new Vector2();
    private float scale = 1f;
    private String sourceText = "";

    public TextRenderer() {
        setRenderType(RenderUtil.RenderType.SPRITE);
    }

    @Override
    public float getTextWidth(Viewport viewport) {
        return super.getTextWidth(viewport) * scale;
    }

    @Override
    public float getTextHeight(Viewport viewport) {
        return super.getTextHeight(viewport) * scale;
    }

    @Override
    public void renderSprite(Viewport viewport, SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        String text = getText();
        BitmapFont font = getFont();
        if (font == null) {
            Client.LOGGER.trace("Skipping render: font is null for text: {}", text);
        }
        if (spriteBatch == null) {
            Client.LOGGER.error("Skipping render: SpriteBatch is null for text: {}", text);
            return;
        }
        if (glyphLayout == null) {
            Client.LOGGER.error("Skipping render: GlyphLayout is null for text: {}", text);
            return;
        }

        float posX = getPosX();
        float posY = getPosY();
        Color color = getTextColor();
        if (text == null || text.isEmpty() || color == null || color.a <= 0) {
            return;
        }

        spriteBatch.setColor(color);

        Matrix4 originalTransform = null;
        if (scale != 1f) {
            originalTransform = spriteBatch.getTransformMatrix().cpy();
            Matrix4 scaledTransform = originalTransform.cpy()
                    .translate(posX, posY, 0f)
                    .scale(scale, scale, 1f)
                    .translate(-posX, -posY, 0f);
            spriteBatch.setTransformMatrix(scaledTransform);
        }

        if (font instanceof CosmicReachFont) {
            FontRenderer.drawText(spriteBatch, viewport, text, posX, posY, true); // TODO: Make my own renderer for CR's font
        } else {
            glyphLayout.setText(font, text, 0, text.length(), color, 0.0F, 8, false, null);
            if (font != null) font.draw(spriteBatch, glyphLayout, posX, posY);
        }

        if (originalTransform != null) {
            spriteBatch.setTransformMatrix(originalTransform);
        }
    }

    @Override
    public void setText(String text) {
        sourceText = text == null ? "" : text;
        setRenderedText(sourceText);
        setScale(1f);
    }

    public void fitToBox(Viewport viewport, float maxWidth, float maxHeight) {
        fitToBox(viewport, maxWidth, maxHeight, DEFAULT_WRAP_SCALE_THRESHOLD, DEFAULT_MIN_SCALE);
    }

    public void fitToBox(Viewport viewport, float maxWidth, float maxHeight, float wrapScaleThreshold, float minScale) {
        setRenderedText(sourceText);
        setScale(1f);

        if (viewport == null || sourceText.isEmpty() || maxWidth <= 0f || maxHeight <= 0f) {
            return;
        }

        float baseWidth = measureText(viewport, sourceText).x;
        float baseHeight = measureText(viewport, sourceText).y;
        if (baseWidth <= 0f || baseHeight <= 0f) {
            return;
        }

        float scaleToFit = Math.min(maxWidth / baseWidth, maxHeight / baseHeight);
        if (scaleToFit >= 1f) {
            return;
        }

        if (scaleToFit >= wrapScaleThreshold) {
            setScale(scaleToFit);
            return;
        }

        String wrappedText = wrapText(viewport, sourceText, maxWidth / wrapScaleThreshold);
        setRenderedText(wrappedText);

        Vector2 wrappedDimensions = measureText(viewport, wrappedText);
        if (wrappedDimensions.x <= 0f || wrappedDimensions.y <= 0f) {
            return;
        }

        float wrappedScale = Math.min(1f, Math.min(maxWidth / wrappedDimensions.x, maxHeight / wrappedDimensions.y));
        setScale(Math.max(minScale, wrappedScale));
    }

    public void setScale(float scale) {
        this.scale = Math.max(0.1f, scale);
    }

    public float getScale() {
        return scale;
    }

    private void setRenderedText(String text) {
        super.setText(text == null ? "" : text);
    }

    private Vector2 measureText(Viewport viewport, String text) {
        measurementDimensions.set(0f, 0f);
        BitmapFont font = getFont();
        if (font == null || viewport == null || text == null || text.isEmpty()) {
            return measurementDimensions;
        }

        if (font instanceof CosmicReachFont) {
            return FontRenderer.getTextDimensions(viewport, text, measurementDimensions);
        }

        synchronized (measurementLayout) {
            measurementLayout.setText(font, text);
            measurementDimensions.set(measurementLayout.width, measurementLayout.height);
        }
        return measurementDimensions;
    }

    private String wrapText(Viewport viewport, String text, float maxLineWidth) {
        if (text == null || text.isEmpty() || maxLineWidth <= 0f) {
            return text == null ? "" : text;
        }

        String[] paragraphs = text.split("\\R", -1);
        StringBuilder wrapped = new StringBuilder(text.length() + 8);
        for (int i = 0; i < paragraphs.length; i++) {
            if (i > 0) {
                wrapped.append('\n');
            }
            wrapParagraph(viewport, paragraphs[i], maxLineWidth, wrapped);
        }
        return wrapped.toString();
    }

    private void wrapParagraph(Viewport viewport, String paragraph, float maxLineWidth, StringBuilder wrapped) {
        String trimmed = paragraph.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        StringBuilder line = new StringBuilder();
        for (String word : trimmed.split("\\s+")) {
            if (word.isEmpty()) {
                continue;
            }

            if (measureText(viewport, word).x > maxLineWidth) {
                appendLine(wrapped, line);
                line.setLength(0);
                appendWrappedWord(viewport, word, maxLineWidth, wrapped);
                continue;
            }

            String candidate = line.length() == 0 ? word : line + " " + word;
            if (measureText(viewport, candidate).x <= maxLineWidth) {
                line.setLength(0);
                line.append(candidate);
            } else {
                appendLine(wrapped, line);
                line.setLength(0);
                line.append(word);
            }
        }

        appendLine(wrapped, line);
    }

    private void appendWrappedWord(Viewport viewport, String word, float maxLineWidth, StringBuilder wrapped) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            String candidate = line.toString() + word.charAt(i);
            if (line.length() > 0 && measureText(viewport, candidate).x > maxLineWidth) {
                appendLine(wrapped, line);
                line.setLength(0);
            }
            line.append(word.charAt(i));
        }
        appendLine(wrapped, line);
    }

    private void appendLine(StringBuilder wrapped, StringBuilder line) {
        if (line.length() == 0) {
            return;
        }
        if (wrapped.length() > 0) {
            wrapped.append('\n');
        }
        wrapped.append(line);
    }
}
