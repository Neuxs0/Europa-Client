package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import dev.neuxs.europa_client.utils.rendering.ui.FontRenderer;
import dev.neuxs.europa_client.utils.rendering.ui.LineRenderer;

@SuppressWarnings({"unused"})
public class Renderer {
    public static final ShapeRenderer shapeRenderer = new ShapeRenderer();
    protected static final SpriteBatch batch = new SpriteBatch();
    protected static final GlyphLayout glyphLayout = new GlyphLayout();

    // LineRenderer
    public static void drawLine(Matrix4 projectionMatrix, float x1, float y1, float x2, float y2, float w, Color color) {
        LineRenderer.drawLine(projectionMatrix, x1, y1, x2, y2, w, color);
    }
    public static void drawDebugLines(Matrix4 projectionMatrix, float x, float y, float w, float h) {
        LineRenderer.drawDebugLines(projectionMatrix, x, y, w, h);
    }

    // FontRenderer
    public static Vector2 getTextDimensions(String fontName, String text) {
        return FontRenderer.getTextDimensions(fontName, text);
    }
    public static void drawText(Matrix4 projectionMatrix, String fontName, String text, float x, float y, Color color, int alignment, float wrapWidth, boolean wrap) {
        FontRenderer.drawText(projectionMatrix, fontName, text, x, y, color, alignment, wrapWidth, wrap);
    }
}