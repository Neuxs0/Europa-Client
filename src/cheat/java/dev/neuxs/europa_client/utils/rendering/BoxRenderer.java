package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
            drawRoundedRect(posX + getShadowOffsetX(), posY + getShadowOffsetY(), width, height, effectiveRadius, shadowColor);
        }

        if (hasBorder) {
            drawRoundedRect(posX, posY, width, height, effectiveRadius, borderColor);
        }

        if (hasFill) {
            if (hasBorder) {
                float innerWidth = Math.max(0, width - borderWidth * 2);
                float innerHeight = Math.max(0, height - borderWidth * 2);
                float innerRadius = Math.max(0, effectiveRadius - borderWidth);

                if (innerWidth > 0 && innerHeight > 0) {
                    drawRoundedRect(posX + borderWidth, posY + borderWidth, innerWidth, innerHeight, innerRadius, fillColor);
                }
            } else {
                drawRoundedRect(posX, posY, width, height, effectiveRadius, fillColor);
            }
        }
    }

    private void drawRoundedRect(float x, float y, float width, float height, float radius, Color color) {
        SdfRenderer.get().drawRoundedRect(x, y, width, height, radius, color);
    }

    public int getRoundedCornerSegments() {
        return roundedCornerSegments;
    }
    public void setRoundedCornerSegments(int roundedCornerSegments) {
        if (roundedCornerSegments > 0) this.roundedCornerSegments = roundedCornerSegments;
    }
}
