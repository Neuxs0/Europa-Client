package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class LineRenderer {
    public static void drawLine(ShapeRenderer shapeRenderer, float x1, float y1, float x2, float y2, float w, Color color) {
        if (color == null || color.a <= 0 || w <= 0) return;
        shapeRenderer.setColor(color);
        shapeRenderer.rectLine(x1, y1, x2, y2, w);
    }
}
