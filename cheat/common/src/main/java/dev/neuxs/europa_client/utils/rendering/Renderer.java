package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Null;
import dev.neuxs.europa_client.utils.rendering.ui.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.ui.LineRenderer;

@SuppressWarnings({"unused"})
public class Renderer {
    protected static final ShapeRenderer shapeRenderer = new ShapeRenderer();

    // LineRenderer
    public static void drawLine(Matrix4 projectionMatrix, float x1, float y1, float x2, float y2, float w, Color color) {
        LineRenderer.drawLine(projectionMatrix, x1, y1, x2, y2, w, color);
    }
    public static void drawDebugLines(Matrix4 projectionMatrix, float x, float y, float w, float h) {
        LineRenderer.drawDebugLines(projectionMatrix, x, y, w, h);
    }

    // BoxRenderer
    public static void drawBox(Matrix4 projectionMatrix, float x, float y, float w, float h, @Null Color color) {
        BoxRenderer.drawBox(projectionMatrix, x, y, w, h, color);
    }
    public static void drawBorderedBox(Matrix4 projectionMatrix, float x, float y, float w, float h, float borderW, @Null Color fillColor, @Null Color borderColor) {
        BoxRenderer.drawBorderedBox(projectionMatrix, x, y, w, h, borderW, fillColor, borderColor);
    }
}