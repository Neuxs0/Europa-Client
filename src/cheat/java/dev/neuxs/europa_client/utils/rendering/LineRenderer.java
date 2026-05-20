package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;

@SuppressWarnings("unused")
public class LineRenderer extends Renderer {
    public Vector2 startPoint;
    public Vector2 endPoint;
    public float width;

    public LineRenderer() {
        this.startPoint = new Vector2(0f, 0f); // X, Y
        this.endPoint = new Vector2(0f, 0f); // X, Y
        this.width = 1f;
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

        Color fillColor = getFillColor();
        if (fillColor == null || fillColor.a <= 0 || width <= 0) return;

        shapeRenderer.setColor(fillColor);
        shapeRenderer.rectLine(startPoint, endPoint, this.width);
    }

    public Vector2 getStartPoint() {
        return startPoint;
    }
    public float getStartPointX() {
        return startPoint.x;
    }
    public float getStartPointY() {
        return startPoint.y;
    }
    public Vector2 getEndPoint() {
        return endPoint;
    }
    public float getEndX() {
        return endPoint.x;
    }
    public float getEndY() {
        return endPoint.y;
    }
    @Override public float getWidth() {
        return width;
    }

    public void setStartPoint(Vector2 startPoint) {
        if (startPoint != null) this.startPoint.set(startPoint);
    }
    public void setStartPointX(float x) {
        if (x != this.startPoint.x) this.startPoint.set(x, this.startPoint.y);
    }
    public void setStartPointY(float y) {
        if (y != this.startPoint.y) this.startPoint.set(this.startPoint.x, y);
    }
    public void setEndPoint(Vector2 endPoint) {
        if (endPoint != null) this.endPoint.set(endPoint);
    }
    public void setEndPointX(float x) {
        if (x != this.endPoint.x) this.endPoint.set(x, this.endPoint.y);
    }
    public void setEndPointY(float y) {
        if (y != this.endPoint.y) this.endPoint.set(this.endPoint.x, y);
    }
    @Override public void setWidth(float width) {
        if (width != this.width && width >= 0f) this.width = width;
    }
}
