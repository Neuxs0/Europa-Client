package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import finalforeach.cosmicreach.CosmicReachFont;
import finalforeach.cosmicreach.ui.FontRenderer;

@SuppressWarnings("unused")
public class TextRenderer extends Renderer {
    private float scale = 1f;

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

    public void setScale(float scale) {
        this.scale = Math.max(0.1f, scale);
    }

    public float getScale() {
        return scale;
    }
}
