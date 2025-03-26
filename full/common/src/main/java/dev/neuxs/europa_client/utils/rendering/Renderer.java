package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Null;

@SuppressWarnings({"SuspiciousNameCombination", "unused"})
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

    public static void drawLine(Matrix4 projectionMatrix, float x1, float y1, float x2, float y2, int width, Color color) {
        if (projectionMatrix == null || color == null || color.a <= 0 || width <= 0) return;

        shapeRenderer.setProjectionMatrix(projectionMatrix);

        Gdx.gl.glLineWidth(width);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(color);
        shapeRenderer.line(x1, y1, x2, y2);
        shapeRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    public static void drawBox(Matrix4 projectionMatrix, float x, float y, float width, float height, @Null Color color) {
        if (color == null || color.a <= 0 || width <= 0 || height <= 0) return;

        shapeRenderer.setProjectionMatrix(projectionMatrix);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
    }

    public static void drawBorderedBox(Matrix4 projectionMatrix, float x, float y, float width, float height, float borderWidth, @Null Color fillColor, @Null Color borderColor) {
        if (width <= 0 || height <= 0) return;

        shapeRenderer.setProjectionMatrix(projectionMatrix);

        boolean fill = fillColor != null && fillColor.a > 0;
        boolean border = borderColor != null && borderColor.a > 0 && borderWidth > 0;

        if (!fill && !border) return;

        if (fill) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(fillColor);
            shapeRenderer.rect(x, y, width, height);
            shapeRenderer.end();
        }

        if (border) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(borderColor);

            shapeRenderer.rect(x, y + height - borderWidth, width, borderWidth);
            shapeRenderer.rect(x, y, width, borderWidth);
            shapeRenderer.rect(x, y, borderWidth, height);
            shapeRenderer.rect(x + width - borderWidth, y, borderWidth, height);

            shapeRenderer.end();
        }
    }
}