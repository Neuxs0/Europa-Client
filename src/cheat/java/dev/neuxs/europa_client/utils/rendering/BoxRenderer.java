package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;

@SuppressWarnings("unused")
public class BoxRenderer extends Renderer {
    private int roundedCornerSegments;

    public BoxRenderer() {
        this.roundedCornerSegments = 20; // Higher = smoother & less performance.
        setRenderType(RenderUtil.RenderType.SHAPE);
        setShapeType(ShapeRenderer.ShapeType.Filled);
    }

    @Override
    public void renderShape(Viewport viewport, ShapeRenderer shapeRenderer) {
        if (viewport == null) {
            Client.LOGGER.error("Skipping render: Viewport is null!");
            return;
        }
        if (shapeRenderer == null) {
            Client.LOGGER.error("Skipping render: ShapeRenderer is null!");
            return;
        }

        float posX = getPosX();
        float posY = getPosY();
        float width = getWidth();
        float height = getHeight();
        float borderWidth = getBorderWidth();
        float borderRadius = getBorderRadius();
        Color fillColor = getFillColor();
        Color borderColor = getBorderColor();
        Color shadowColor = getShadowColor();
        float effectiveRadius = Math.max(0, Math.min(borderRadius, Math.min(width, height) / 2f));

        if (width <= 0 || height <= 0) return;

        boolean hasFill = fillColor != null && fillColor.a > 0;
        boolean hasBorder = isBorder() && borderWidth > 0 && borderColor != null && borderColor.a > 0;
        boolean hasShadow = isShadow() && shadowColor != null && shadowColor.a > 0;

        if (hasShadow) {
            shapeRenderer.setColor(shadowColor);
            drawRoundedRect(shapeRenderer, posX + getShadowOffsetX(), posY + getShadowOffsetY(), width, height, effectiveRadius);
        }

        if (hasBorder) {
            shapeRenderer.setColor(borderColor);
            drawRoundedRect(shapeRenderer, posX, posY, width, height, effectiveRadius);
        }

        if (hasFill) {
            shapeRenderer.setColor(fillColor);
            if (hasBorder) {
                float innerWidth = Math.max(0, width - borderWidth * 2);
                float innerHeight = Math.max(0, height - borderWidth * 2);
                float innerRadius = Math.max(0, effectiveRadius - borderWidth);

                if (innerWidth > 0 && innerHeight > 0) {
                    drawRoundedRect(shapeRenderer, posX + borderWidth, posY + borderWidth, innerWidth, innerHeight, innerRadius);
                }
            } else {
                drawRoundedRect(shapeRenderer, posX, posY, width, height, effectiveRadius);
            }
        }
    }

    private void drawRoundedRect(ShapeRenderer shapeRenderer, float x, float y, float width, float height, float radius) {
        radius = Math.max(0, Math.min(radius, Math.min(width, height) / 2f));

        if (radius <= 0.01f) {
            shapeRenderer.rect(x, y, width, height);
            return;
        }

        int segments = MathUtils.clamp((int)(6 * (float)Math.cbrt(radius)), 4, roundedCornerSegments * 2);

        shapeRenderer.arc(x + radius, y + radius, radius, 180f, 90f, segments); // Bottom-left
        shapeRenderer.arc(x + radius, y + height - radius, radius, 90f, 90f, segments); // Top-left
        shapeRenderer.arc(x + width - radius, y + height - radius, radius, 0f, 90f, segments); // Top-right
        shapeRenderer.arc(x + width - radius, y + radius, radius, 270f, 90f, segments); // Bottom-right
        if (width > 2 * radius) {
            shapeRenderer.rect(x + radius, y, width - 2 * radius, radius); // Bottom edge connector
            shapeRenderer.rect(x + radius, y + height - radius, width - 2 * radius, radius); // Top edge connector
        }

        if (height > 2 * radius) {
            shapeRenderer.rect(x, y + radius, radius, height - 2 * radius); // Left edge connector
            shapeRenderer.rect(x + width - radius, y + radius, radius, height - 2 * radius); // Right edge connector
        }

        if (width > 2 * radius && height > 2 * radius) {
            shapeRenderer.rect(x + radius, y + radius, width - 2 * radius, height - 2 * radius); // Center area
        }
    }

    public int getRoundedCornerSegments() {
        return roundedCornerSegments;
    }
    public void setRoundedCornerSegments(int roundedCornerSegments) {
        if (roundedCornerSegments > 0) this.roundedCornerSegments = roundedCornerSegments;
    }
}
