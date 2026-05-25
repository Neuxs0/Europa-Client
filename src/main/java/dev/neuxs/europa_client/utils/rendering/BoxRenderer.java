package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;

@SuppressWarnings("unused")
public class BoxRenderer extends Renderer {
    private int roundedCornerSegments;
    private boolean topLeftRounded = true;
    private boolean topRightRounded = true;
    private boolean bottomLeftRounded = true;
    private boolean bottomRightRounded = true;

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
            drawSquareCornerPatches(posX + getShadowOffsetX(), posY + getShadowOffsetY(), width, height, effectiveRadius, shadowColor);
        }

        if (hasBorder) {
            drawRoundedRect(posX, posY, width, height, effectiveRadius, borderColor);
            drawSquareCornerPatches(posX, posY, width, height, effectiveRadius, borderColor);
        }

        if (hasFill) {
            if (hasBorder) {
                float innerWidth = Math.max(0, width - borderWidth * 2);
                float innerHeight = Math.max(0, height - borderWidth * 2);
                float innerRadius = Math.max(0, effectiveRadius - borderWidth);

                if (innerWidth > 0 && innerHeight > 0) {
                    drawRoundedRect(posX + borderWidth, posY + borderWidth, innerWidth, innerHeight, innerRadius, fillColor);
                    drawSquareCornerPatches(posX + borderWidth, posY + borderWidth, innerWidth, innerHeight, innerRadius, fillColor);
                }
            } else {
                drawRoundedRect(posX, posY, width, height, effectiveRadius, fillColor);
                drawSquareCornerPatches(posX, posY, width, height, effectiveRadius, fillColor);
            }
        }
    }

    private void drawRoundedRect(float x, float y, float width, float height, float radius, Color color) {
        SdfRenderer.get().drawRoundedRect(x, y, width, height, radius, color);
    }

    private void drawSquareCornerPatches(float x, float y, float width, float height, float radius, Color color) {
        if (radius <= 0f || areAllCornersRounded()) {
            return;
        }

        if (!bottomLeftRounded) {
            drawRoundedRect(x, y, radius, radius, 0f, color);
        }
        if (!bottomRightRounded) {
            drawRoundedRect(x + width - radius, y, radius, radius, 0f, color);
        }
        if (!topLeftRounded) {
            drawRoundedRect(x, y + height - radius, radius, radius, 0f, color);
        }
        if (!topRightRounded) {
            drawRoundedRect(x + width - radius, y + height - radius, radius, radius, 0f, color);
        }
    }

    private boolean areAllCornersRounded() {
        return topLeftRounded && topRightRounded && bottomLeftRounded && bottomRightRounded;
    }

    public int getRoundedCornerSegments() {
        return roundedCornerSegments;
    }
    public void setRoundedCornerSegments(int roundedCornerSegments) {
        if (roundedCornerSegments > 0) this.roundedCornerSegments = roundedCornerSegments;
    }

    public void setTopLeftRounded(boolean topLeftRounded) {
        this.topLeftRounded = topLeftRounded;
    }

    public void setTopRightRounded(boolean topRightRounded) {
        this.topRightRounded = topRightRounded;
    }

    public void setBottomLeftRounded(boolean bottomLeftRounded) {
        this.bottomLeftRounded = bottomLeftRounded;
    }

    public void setBottomRightRounded(boolean bottomRightRounded) {
        this.bottomRightRounded = bottomRightRounded;
    }
}
