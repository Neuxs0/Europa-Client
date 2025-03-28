package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import dev.neuxs.europa_client.utils.ColorUtils;

@SuppressWarnings({"unused", "CommentedOutCode"})
public class BoxRenderer {
    public float posX;
    public float posY;
    public float width;
    public float height;
    public float borderWidth;
    public float borderRadius;
    public float shadowOffsetX;
    public float shadowOffsetY;

    public boolean border = false;
    public boolean dropShadow = false;

    public Color fillColor = ColorUtils.color(255, 255, 255, 255);
    public Color borderColor = ColorUtils.color(150, 150, 150, 255);
    public Color dropShadowColor = ColorUtils.color(0, 0, 0, 127);

    // Number of segments used to approximate the corner arcs. Higher = smoother.
    private static final int DEFAULT_ARC_SEGMENTS = 20;

    // public Gradient fillGradient; // More complex fill
    // public Texture backgroundTexture; // Use SpriteBatch for this

    public BoxRenderer() {
        super();
        this.borderWidth = 1f;
        this.borderRadius = 0f;
        this.shadowOffsetX = 2f;
        this.shadowOffsetY = -2f;
    }

    public BoxRenderer(float x, float y, float w, float h) {
        this();
        this.posX = x;
        this.posY = y;
        this.width = w;
        this.height = h;
    }

    public BoxRenderer(float x, float y, float w, float h, Color fillColor) {
        this(x, y, w, h);
        this.fillColor = fillColor;
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (width <= 0 || height <= 0) return;

        boolean hasFill = fillColor != null && fillColor.a > 0;
        boolean hasBorder = border && borderWidth > 0 && borderColor != null && borderColor.a > 0;
        boolean hasShadow = dropShadow && dropShadowColor != null && dropShadowColor.a > 0;

        if (!hasFill && !hasBorder && !hasShadow) {
            return;
        }

        float effectiveRadius = Math.max(0, Math.min(this.borderRadius, Math.min(width, height) / 2f));

        if (hasShadow) {
            shapeRenderer.setColor(dropShadowColor);
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
            shapeRenderer.setColor(borderColor);
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
            shapeRenderer.setColor(fillColor);
            if (hasBorder) {
                float inset = borderWidth;
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

        int segments = MathUtils.clamp((int)(6 * (float)Math.cbrt(radius)), 4, DEFAULT_ARC_SEGMENTS * 2);

        shapeRenderer.arc(x + radius, y + radius, radius, 180f, 90f, segments);
        shapeRenderer.arc(x + radius, y + height - radius, radius, 90f, 90f, segments);
        shapeRenderer.arc(x + width - radius, y + height - radius, radius, 0f, 90f, segments);
        shapeRenderer.arc(x + width - radius, y + radius, radius, 270f, 90f, segments);

        if (width > 2 * radius) {
            shapeRenderer.rect(x + radius, y, width - 2 * radius, radius); // Bottom
            shapeRenderer.rect(x + radius, y + height - radius, width - 2 * radius, radius); // Top
        }
        if (height > 2 * radius) {
            shapeRenderer.rect(x, y + radius, radius, height - 2 * radius); // Left
            shapeRenderer.rect(x + width - radius, y + radius, radius, height - 2 * radius); // Right
        }

        if (width > 2 * radius && height > 2 * radius) {
            shapeRenderer.rect(x + radius, y + radius, width - 2 * radius, height - 2 * radius);
        }
    }


    public float getPosX() { return posX; }
    public float getPosY() { return posY; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getBorderWidth() { return borderWidth; }
    public float getBorderRadius() { return borderRadius; }
    public float getShadowOffsetX() { return shadowOffsetX; }
    public float getShadowOffsetY() { return shadowOffsetY; }
    public boolean isBorder() { return border; }
    public boolean isDropShadow() { return dropShadow; }
    public Color getFillColor() { return fillColor; }
    public Color getBorderColor() { return borderColor; }
    public Color getDropShadowColor() { return dropShadowColor; }


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
        this.width = w;
        this.height = h;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
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
        this.dropShadowColor = dropShadowColor;
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
