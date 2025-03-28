package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import dev.neuxs.europa_client.utils.ColorUtils;

@SuppressWarnings("unused")
public class BoxRenderer {
    private float posX;
    private float posY;
    private float width;
    private float height;
    private boolean border;
    private float borderWidth;
    private float borderRadius;
    private boolean dropShadow;
    private float shadowOffsetX;
    private float shadowOffsetY;
    private Color fillColor;
    private Color borderColor;
    private Color dropShadowColor;
    private static final int defaultArcSegments = 20; // Higher = smoother & less performance.
    private final Color defaultFillColor = ColorUtils.color(255, 255, 255, 255);
    private final Color defaultBorderColor = ColorUtils.color(150, 150, 150, 255);
    private final Color defaultDropShadowColor = ColorUtils.color(0, 0, 0, 127);

    public BoxRenderer() {
        super();
        this.posX = 0f;
        this.posY = 0f;
        this.width = 0f;
        this.height = 0f;
        this.borderWidth = 1f;
        this.borderRadius = 0f;
        this.shadowOffsetX = 2f;
        this.shadowOffsetY = -2f;
        this.border = false;
        this.dropShadow = false;
        this.fillColor = defaultFillColor.cpy();
        this.borderColor = defaultBorderColor.cpy();
        this.dropShadowColor = defaultDropShadowColor.cpy();
    }

    public BoxRenderer(float x, float y, float w, float h) {
        this();
        this.posX = x;
        this.posY = y;
        this.width = Math.max(0, w);
        this.height = Math.max(0, h);
    }

    public BoxRenderer(float x, float y, float w, float h, Color fillColor) {
        this(x, y, w, h);
        setFillColor(fillColor);
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (width <= 0 || height <= 0) return;

        boolean hasFill = this.fillColor != null && this.fillColor.a > 0;
        boolean hasBorder = this.border && this.borderWidth > 0 && this.borderColor != null && this.borderColor.a > 0;
        boolean hasShadow = this.dropShadow && this.dropShadowColor != null && this.dropShadowColor.a > 0;

        if (!hasFill && !hasBorder && !hasShadow) return;

        float effectiveRadius = Math.max(0, Math.min(this.borderRadius, Math.min(width, height) / 2f));

        if (hasShadow) {
            shapeRenderer.setColor(this.dropShadowColor);
            drawRoundedRect(
                    shapeRenderer,
                    posX + shadowOffsetX,
                    posY + shadowOffsetY,
                    width,
                    height,
                    effectiveRadius
            );
        }

        if (hasBorder) {
            shapeRenderer.setColor(this.borderColor);
            drawRoundedRect(
                    shapeRenderer,
                    posX,
                    posY,
                    width,
                    height,
                    effectiveRadius
            );
        }

        if (hasFill) {
            shapeRenderer.setColor(this.fillColor);
            if (hasBorder) {
                float inset = this.borderWidth;
                float innerWidth = Math.max(0, width - inset * 2);
                float innerHeight = Math.max(0, height - inset * 2);
                float innerRadius = Math.max(0, effectiveRadius - inset);

                if (innerWidth > 0 && innerHeight > 0) {
                    drawRoundedRect(
                            shapeRenderer,
                            posX + inset,
                            posY + inset,
                            innerWidth,
                            innerHeight,
                            innerRadius
                    );
                }
            } else {
                drawRoundedRect(
                        shapeRenderer,
                        posX,
                        posY,
                        width,
                        height,
                        effectiveRadius
                );
            }
        }
    }

    private void drawRoundedRect(ShapeRenderer shapeRenderer, float x, float y, float width, float height, float radius) {
        radius = Math.max(0, Math.min(radius, Math.min(width, height) / 2f));

        if (radius <= 0.01f) {
            shapeRenderer.rect(x, y, width, height);
            return;
        }

        int segments = MathUtils.clamp((int)(6 * (float)Math.cbrt(radius)), 4, defaultArcSegments * 2);

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

    public float getPosX() {
        return posX;
    }
    public float getPosY() {
        return posY;
    }
    public float getWidth() {
        return width;
    }
    public float getHeight() {
        return height;
    }
    public float getBorderWidth() {
        return borderWidth;
    }
    public float getBorderRadius() {
        return borderRadius;
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

    public void setPosition(float x, float y) {
        this.posX = x;
        this.posY = y;
    }
    public void setPosX(float posX) {
        this.posX = posX;
    }
    public void setPosY(float posY) {
        this.posY = posY;
    }
    public void setSize(float w, float h) {
        this.width = Math.max(0, w);
        this.height = Math.max(0, h);
    }
    public void setWidth(float width) {
        this.width = Math.max(0, width);
    }
    public void setHeight(float height) {
        this.height = Math.max(0, height);
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
    public void setBorderRadius(float borderRadius) {
        this.borderRadius = Math.max(0, borderRadius);
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