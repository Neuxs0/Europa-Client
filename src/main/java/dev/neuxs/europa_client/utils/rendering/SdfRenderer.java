package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import dev.neuxs.europa_client.Client;

public class SdfRenderer implements Disposable {
    private static final String VERTEX_SHADER_PATH = "assets/europa_client/shaders/ui/sdf.vert.glsl";
    private static final String FRAGMENT_SHADER_PATH = "assets/europa_client/shaders/ui/sdf.frag.glsl";
    private static final int SHAPE_ROUNDED_RECT = 0;
    private static final int SHAPE_CIRCLE = 1;
    private static final int SHAPE_CAPSULE = 2;
    private static final float[] VERTICES = new float[16];
    private static final short[] INDICES = {0, 1, 2, 2, 3, 0};
    private static SdfRenderer shared;

    private final Mesh mesh;
    private final ShaderProgram shader;
    private final Matrix4 projectionMatrix = new Matrix4();

    private SdfRenderer() {
        ShaderProgram.pedantic = false;
        this.shader = new ShaderProgram(
                Gdx.files.internal(VERTEX_SHADER_PATH),
                Gdx.files.internal(FRAGMENT_SHADER_PATH)
        );
        if (!shader.isCompiled()) {
            Client.LOGGER.error("Failed to compile SDF renderer shader: {}", shader.getLog());
        }
        this.mesh = new Mesh(
                false,
                4,
                6,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")
        );
        this.mesh.setIndices(INDICES);
    }

    public static SdfRenderer get() {
        if (shared == null) {
            shared = new SdfRenderer();
        }
        return shared;
    }

    public void setProjectionMatrix(Matrix4 projectionMatrix) {
        if (projectionMatrix != null) {
            this.projectionMatrix.set(projectionMatrix);
        }
    }

    public void drawRoundedRect(float x, float y, float width, float height, float radius, Color color) {
        if (!canDraw(width, height, color)) {
            return;
        }
        drawQuad(x, y, width, height, Math.max(0f, Math.min(radius, Math.min(width, height) / 2f)), color, SHAPE_ROUNDED_RECT, null, null, 0f);
    }

    public void drawCircle(float centerX, float centerY, float radius, Color color) {
        if (radius <= 0f || color == null || color.a <= 0f) {
            return;
        }
        float diameter = radius * 2f;
        drawQuad(centerX - radius, centerY - radius, diameter, diameter, radius, color, SHAPE_CIRCLE, null, null, 0f);
    }

    public void drawLine(Vector2 start, Vector2 end, float width, Color color) {
        if (start == null || end == null || width <= 0f || color == null || color.a <= 0f) {
            return;
        }

        float halfWidth = width / 2f;
        float minX = Math.min(start.x, end.x) - halfWidth - 1f;
        float minY = Math.min(start.y, end.y) - halfWidth - 1f;
        float maxX = Math.max(start.x, end.x) + halfWidth + 1f;
        float maxY = Math.max(start.y, end.y) + halfWidth + 1f;
        Vector2 localStart = new Vector2(start.x - minX, start.y - minY);
        Vector2 localEnd = new Vector2(end.x - minX, end.y - minY);
        drawQuad(minX, minY, maxX - minX, maxY - minY, halfWidth, color, SHAPE_CAPSULE, localStart, localEnd, halfWidth);
    }

    private boolean canDraw(float width, float height, Color color) {
        return width > 0f && height > 0f && color != null && color.a > 0f && shader.isCompiled();
    }

    private void drawQuad(
            float x,
            float y,
            float width,
            float height,
            float radius,
            Color color,
            int shape,
            Vector2 lineStart,
            Vector2 lineEnd,
            float halfWidth
    ) {
        setVertices(x, y, width, height);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shader.begin();
        shader.setUniformMatrix("u_projTrans", projectionMatrix);
        shader.setUniformf("u_color", color);
        shader.setUniformf("u_size", width, height);
        shader.setUniformf("u_radius", radius);
        shader.setUniformi("u_shape", shape);
        shader.setUniformf("u_halfWidth", halfWidth);
        shader.setUniformf("u_lineStart", lineStart == null ? 0f : lineStart.x, lineStart == null ? 0f : lineStart.y);
        shader.setUniformf("u_lineEnd", lineEnd == null ? 0f : lineEnd.x, lineEnd == null ? 0f : lineEnd.y);
        mesh.render(shader, GL20.GL_TRIANGLES);
        shader.end();
    }

    private void setVertices(float x, float y, float width, float height) {
        VERTICES[0] = x;
        VERTICES[1] = y;
        VERTICES[2] = 0f;
        VERTICES[3] = 0f;
        VERTICES[4] = x + width;
        VERTICES[5] = y;
        VERTICES[6] = width;
        VERTICES[7] = 0f;
        VERTICES[8] = x + width;
        VERTICES[9] = y + height;
        VERTICES[10] = width;
        VERTICES[11] = height;
        VERTICES[12] = x;
        VERTICES[13] = y + height;
        VERTICES[14] = 0f;
        VERTICES[15] = height;
        mesh.setVertices(VERTICES);
    }

    @Override
    public void dispose() {
        mesh.dispose();
        shader.dispose();
        if (shared == this) {
            shared = null;
        }
    }
}
