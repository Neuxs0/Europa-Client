package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import dev.neuxs.europa_client.utils.rendering.Renderer;

public class LineRenderer extends Renderer {
    public static void drawDebugLines(Matrix4 projectionMatrix, float x, float y, float w, float h) {
        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(shapeRenderer.getCurrentType());
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.end();
    }

    public static void drawLine(Matrix4 projectionMatrix, float x1, float y1, float x2, float y2, float w, Color color) {
        if (projectionMatrix == null || color == null || color.a <= 0 || w <= 0) return;

        shapeRenderer.setProjectionMatrix(projectionMatrix);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rectLine(x1, y1, x2, y2, w);
        shapeRenderer.end();
    }
}
