package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import dev.neuxs.europa_client.utils.ColorUtils;

@SuppressWarnings("unused")
public class LineRenderer {
    public float startX;
    public float startY;
    public float endX;
    public float endY;
    public float width;
    public Color color;

    private final Color defaultColor = ColorUtils.color(255, 255, 255, 255);

    public LineRenderer() {
        this.startX = 0f;
        this.startY = 0f;
        this.endX = 0f;
        this.endY = 0f;
        this.width = 1f;
        this.color = defaultColor;
    }

    public LineRenderer(float x1, float y1, float x2, float y2, float w, Color color) {
        this.startX = x1;
        this.startY = y1;
        this.endX = x2;
        this.endY = y2;
        this.width = Math.max(0, w);
        this.color = (color != null) ? color.cpy() : defaultColor;
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (color == null || color.a <= 0 || width <= 0) return;
        shapeRenderer.setColor(this.color);
        shapeRenderer.rectLine(this.startX, this.startY, this.endX, this.endY, this.width);
    }

    public float getStartX() {
        return startX;
    }
    public float getStartY() {
        return startY;
    }
    public float getEndX() {
        return endX;
    }
    public float getEndY() {
        return endY;
    }
    public float getWidth() {
        return width;
    }
    public Color getColor() {
        return color;
    }

    public void setStartPoint(float x, float y) {
        this.startX = x;
        this.startY = y;
    }
    public void setStartX(float startX) {
        this.startX = startX;
    }
    public void setStartY(float startY) {
        this.startY = startY;
    }
    public void setEndPoint(float x, float y) {
        this.endX = x;
        this.endY = y;
    }
    public void setEndX(float endX) {
        this.endX = endX;
    }
    public void setEndY(float endY) {
        this.endY = endY;
    }
    public void setWidth(float width) {
        this.width = Math.max(0, width);
    }
    public void setColor(Color color) {
        this.color = (color != null) ? color.cpy() : defaultColor;
    }
}
