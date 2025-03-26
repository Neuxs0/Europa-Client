package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Null;
import dev.neuxs.europa_client.utils.rendering.Renderer;

public class BoxRenderer extends Renderer {
    public static void drawBox(Matrix4 projectionMatrix, float x, float y, float w, float h, @Null Color color) {
        if (color == null || color.a <= 0 || w <= 0 || h <= 0) return;

        shapeRenderer.setProjectionMatrix(projectionMatrix);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.end();
    }

    public static void drawBorderedBox(Matrix4 projectionMatrix, float x, float y, float w, float h, float borderW, @Null Color fillColor, @Null Color borderColor) {
        if (w <= 0 || h <= 0) return;

        shapeRenderer.setProjectionMatrix(projectionMatrix);

        boolean fill = fillColor != null && fillColor.a > 0;
        boolean border = borderColor != null && borderColor.a > 0 && borderW > 0;

        if (!fill && !border) return;

        if (fill) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(fillColor);
            shapeRenderer.rect(x, y, w, h);
            shapeRenderer.end();
        }

        if (border) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(borderColor);

            shapeRenderer.rect(x, y + h - borderW, w, borderW);
            shapeRenderer.rect(x, y, w, borderW);
            shapeRenderer.rect(x, y, borderW, h);
            shapeRenderer.rect(x + w - borderW, y, borderW, h);

            shapeRenderer.end();
        }
    }
}
