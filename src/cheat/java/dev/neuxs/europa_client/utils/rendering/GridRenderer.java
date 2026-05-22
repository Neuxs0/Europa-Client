package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

@SuppressWarnings("unused")
public class GridRenderer extends Renderer {
    private float cellSize = 32f;
    private float lineWidth = 1f;
    private boolean centerAnchored = false;

    public GridRenderer() {
        setRenderType(RenderUtil.RenderType.SHAPE);
        setShapeType(ShapeRenderer.ShapeType.Filled);
    }

    @Override
    public void renderShape(Viewport viewport, ShapeRenderer shapeRenderer) {
        if (viewport == null || getFillColor().a <= 0f || cellSize <= 0f || lineWidth <= 0f) {
            return;
        }

        float width = getWidth();
        float height = getHeight();
        float startX = getPosX();
        float startY = getPosY();
        if (centerAnchored) {
            float centerX = getPosX() + width / 2f;
            float centerY = getPosY() + height / 2f;
            while (centerX - cellSize >= getPosX()) {
                centerX -= cellSize;
            }
            while (centerY - cellSize >= getPosY()) {
                centerY -= cellSize;
            }
            startX = centerX;
            startY = centerY;
        }

        for (float x = startX; x <= getPosX() + width; x += cellSize) {
            SdfRenderer.get().drawLine(new Vector2(x, getPosY()), new Vector2(x, getPosY() + height), lineWidth, getFillColor());
        }
        for (float y = startY; y <= getPosY() + height; y += cellSize) {
            SdfRenderer.get().drawLine(new Vector2(getPosX(), y), new Vector2(getPosX() + width, y), lineWidth, getFillColor());
        }
    }

    @Override
    public boolean blocksMouseAt(float x, float y) {
        return false;
    }

    public void setCellSize(float cellSize) {
        this.cellSize = Math.max(1f, cellSize);
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = Math.max(0f, lineWidth);
    }

    public void setCenterAnchored(boolean centerAnchored) {
        this.centerAnchored = centerAnchored;
    }
}
