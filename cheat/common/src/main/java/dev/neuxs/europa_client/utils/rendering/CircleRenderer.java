package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import dev.neuxs.europa_client.utils.ColorUtils;

@SuppressWarnings({"unused", "DuplicatedCode"})
public class CircleRenderer {
    private float posX;
    private float posY;
    private float radius;
    private boolean border;
    private float borderWidth;
    private boolean dropShadow;
    private float shadowOffsetX;
    private float shadowOffsetY;
    private Color fillColor;
    private Color borderColor;
    private Color dropShadowColor;
    private static final int defaultCircleSegments = 30; // Higher = smoother & less performance
    private final Color defaultFillColor = ColorUtils.color(255, 255, 255, 255);
    private final Color defaultBorderColor = ColorUtils.color(150, 150, 150, 255);
    private final Color defaultDropShadowColor = ColorUtils.color(0, 0, 0, 127);

    public CircleRenderer() {
        super();
        this.posX = 0f;
        this.posY = 0f;
        this.radius = 0f;
        this.borderWidth = 1f;
        this.shadowOffsetX = 2f;
        this.shadowOffsetY = -2f;
        this.border = false;
        this.dropShadow = false;
        this.fillColor = defaultFillColor.cpy();
        this.borderColor = defaultBorderColor.cpy();
        this.dropShadowColor = defaultDropShadowColor.cpy();
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (radius <= 0) return;

        boolean hasFill = this.fillColor != null && this.fillColor.a > 0;
        boolean hasBorder = this.border && this.borderWidth > 0 && this.borderColor != null && this.borderColor.a > 0;
        boolean hasShadow = this.dropShadow && this.dropShadowColor != null && this.dropShadowColor.a > 0;

        if (!hasFill && !hasBorder && !hasShadow) return;

        if (hasShadow) {
            shapeRenderer.setColor(this.dropShadowColor);
            drawCircle(
                    shapeRenderer,
                    posX + shadowOffsetX,
                    posY + shadowOffsetY,
                    radius
            );
        }

        if (hasBorder) {
            shapeRenderer.setColor(this.borderColor);
            drawCircle(
                    shapeRenderer,
                    posX,
                    posY,
                    radius
            );
        }

        if (hasFill) {
            shapeRenderer.setColor(this.fillColor);
            if (hasBorder) {
                float innerRadius = Math.max(0, radius - this.borderWidth);
                if (innerRadius > 0) {
                    drawCircle(
                            shapeRenderer,
                            posX,
                            posY,
                            innerRadius
                    );
                }
            } else {
                drawCircle(
                        shapeRenderer,
                        posX,
                        posY,
                        radius
                );
            }
        }
    }

    private void drawCircle(ShapeRenderer shapeRenderer, float cx, float cy, float r) {
        if (r <= 0) return;
        int segments = MathUtils.clamp((int)(6 * (float)Math.cbrt(r)), 8, defaultCircleSegments * 2);
        shapeRenderer.circle(cx, cy, r, segments);
    }

    public float getPosX() {
        return posX;
    }
    public float getPosY() {
        return posY;
    }
    public float getRadius() { // New Getter
        return radius;
    }
    public float getBorderWidth() {
        return borderWidth;
    }
    public float getShadowOffsetX() {
        return shadowOffsetX;
    }
    public float getShadowOffsetY() {
        return shadowOffsetY;
    }
    public boolean isBorderEnabled() {
        return border;
    }
    public boolean isDropShadowEnabled() {
        return dropShadow;
    }
    public Color getFillColor() {
        return fillColor.cpy();
    }
    public Color getBorderColor() {
        return borderColor.cpy();
    }
    public Color getDropShadowColor() {
        return dropShadowColor.cpy();
    }

    public void setPosition(float centerX, float centerY) {
        this.posX = centerX;
        this.posY = centerY;
    }
    public void setPosX(float posX) {
        this.posX = posX;
    }
    public void setPosY(float posY) {
        this.posY = posY;
    }
    public void setRadius(float radius) {
        this.radius = Math.max(0, radius);
    }
    public void setFillColor(Color fillColor) {
        this.fillColor = (fillColor != null) ? fillColor.cpy() : this.defaultFillColor.cpy();
    }
    public void setBorderColor(Color borderColor) {
        this.borderColor = (borderColor != null) ? borderColor.cpy() : this.defaultBorderColor.cpy();
    }
    public void setBorderWidth(float borderWidth) {
        this.borderWidth = Math.max(0, borderWidth);
    }
    public void setBorderEnabled(boolean border) {
        this.border = border;
    }
    public void setDropShadowEnabled(boolean dropShadow) {
        this.dropShadow = dropShadow;
    }
    public void setDropShadowColor(Color dropShadowColor) {
        this.dropShadowColor = (dropShadowColor != null) ? dropShadowColor.cpy() : this.defaultDropShadowColor.cpy();
    }
    public void setDropShadowOffset(float x, float y) {
        this.shadowOffsetX = x;
        this.shadowOffsetY = y;
    }
    public void setShadowOffsetX(float shadowOffsetX) {
        this.shadowOffsetX = shadowOffsetX;
    }
    public void setShadowOffsetY(float shadowOffsetY) {
        this.shadowOffsetY = shadowOffsetY;
    }
}