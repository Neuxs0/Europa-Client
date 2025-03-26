package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Null;

@SuppressWarnings({"unused"})
public class Renderer {
    private static final ShapeRenderer shapeRenderer = new ShapeRenderer();

    public static Color color(int r, int g, int b, int a) {
        float red = MathUtils.clamp(r, 0, 255) / 255.0f;
        float green = MathUtils.clamp(g, 0, 255) / 255.0f;
        float blue = MathUtils.clamp(b, 0, 255) / 255.0f;
        float alpha = MathUtils.clamp(a, 0, 255) / 255.0f;
        return new Color(red, green, blue, alpha);
    }

    public static Color color(int r, int g, int b) {
        return color(r, g, b, 255);
    }

    public static void drawDebugLines(Matrix4 projectionMatrix, float x, float y, float w, float h) {
        Renderer.shapeRenderer.setProjectionMatrix(projectionMatrix);
        Renderer.shapeRenderer.begin(Renderer.shapeRenderer.getCurrentType());
        Renderer.shapeRenderer.setColor(Color.YELLOW);
        Renderer.shapeRenderer.rect(x, y, w, h);
        Renderer.shapeRenderer.end();
    }

    public static void drawLine(Matrix4 projectionMatrix, float x1, float y1, float x2, float y2, float w, Color color) {
        if (projectionMatrix == null || color == null || color.a <= 0 || w <= 0) return;

        shapeRenderer.setProjectionMatrix(projectionMatrix);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rectLine(x1, y1, x2, y2, w);
        shapeRenderer.end();
    }

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