package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;

@SuppressWarnings("unused")
public class CircleRenderer extends Renderer {
    private float radius;
    private int circleSegments;

    public CircleRenderer() {
        this.radius = 0f;
        this.circleSegments = 30; // Higher = smoother & less performance
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
        if (radius <= 0) return;

        float posX = getPosX();
        float posY = getPosY();
        Color fillColor = getFillColor();
        Color borderColor = getBorderColor();
        Color shadowColor = getShadowColor();
        boolean hasShadow = isShadow() && shadowColor != null && shadowColor.a > 0;
        boolean hasFill = fillColor != null && fillColor.a > 0;
        boolean hasBorder = isBorder() && getBorderWidth() > 0f && borderColor != null && borderColor.a > 0;

        if (hasShadow) {
            shapeRenderer.setColor(shadowColor);
            drawCircle(shapeRenderer, posX + getShadowOffsetX(), posY + getShadowOffsetY(), radius);
        }

        if (hasBorder) {
            shapeRenderer.setColor(borderColor);
            drawCircle(shapeRenderer, posX, posY, radius);
        }

        if (hasFill) {
            if (getShapeType() != ShapeRenderer.ShapeType.Filled) setShapeType(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(fillColor);
            if (hasBorder) {
                float innerRadius = Math.max(0f, radius - getBorderWidth());
                if (innerRadius > 0f) drawCircle(shapeRenderer, posX, posY, innerRadius);
            } else {
                drawCircle(shapeRenderer, posX, posY, radius);
            }
        }
    }

    private void drawCircle(ShapeRenderer shapeRenderer, float cx, float cy, float r) {
        int segments = MathUtils.clamp((int)(6 * (float)Math.cbrt(r)), 8, circleSegments * 2);
        shapeRenderer.circle(cx, cy, r, segments);
    }

    public float getRadius() {
        return radius;
    }
    public float getCircleSegments() {
        return this.circleSegments;
    }

    public void setRadius(float radius) {
        this.radius = Math.max(0, radius);
    }
    public void setCircleSegments(int circleSegments) {
        if (circleSegments > 0 && circleSegments != this.circleSegments) this.circleSegments = circleSegments;
    }
}
