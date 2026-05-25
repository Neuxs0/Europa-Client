package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import dev.neuxs.europa_client.Client;
import finalforeach.cosmicreach.gamestates.GameState;

public class BackgroundBlurRenderer implements Disposable {
    private static final int BLUR_SCALE = 4;
    private static final int MAX_KERNEL_RADIUS = 24;
    private static final String VERTEX_SHADER_PATH = "assets/europa_client/shaders/ui/background-blur.vert.glsl";
    private static final String FRAGMENT_SHADER_PATH = "assets/europa_client/shaders/ui/background-blur.frag.glsl";

    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final Matrix4 pixelMatrix = new Matrix4();
    private ShaderProgram blurShader;
    private FrameBuffer sceneBuffer;
    private FrameBuffer blurBufferA;
    private FrameBuffer blurBufferB;
    private int bufferWidth;
    private int bufferHeight;
    private int blurWidth;
    private int blurHeight;

    public void render(GameState previousGamestate, Runnable fallbackRenderer, float blurStrength) {
        int width = Math.max(1, Gdx.graphics.getWidth());
        int height = Math.max(1, Gdx.graphics.getHeight());
        ensureResources(width, height);

        try {
            ScreenUtils.clear(0f, 0f, 0f, 1f, true);
            previousGamestate.render();
        } catch (Exception e) {
            Client.LOGGER.error("Error rendering blurred GUI background: {}", e.getMessage(), e);
            ScreenUtils.clear(0f, 0f, 0f, 1f, true);
            if (fallbackRenderer != null) {
                fallbackRenderer.run();
            }
        }

        copyBackBufferToSceneTexture(width, height);
        runGaussianBlurPass(Math.max(0.25f, Math.min(MAX_KERNEL_RADIUS / 3f, blurStrength)));
        drawBlurredBackground(width, height);
    }

    private void copyBackBufferToSceneTexture(int width, int height) {
        Texture sceneTexture = sceneBuffer.getColorBufferTexture();
        sceneTexture.bind();
        Gdx.gl.glCopyTexSubImage2D(GL20.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
    }

    private void ensureResources(int width, int height) {
        int nextBlurWidth = Math.max(1, width / BLUR_SCALE);
        int nextBlurHeight = Math.max(1, height / BLUR_SCALE);
        if (sceneBuffer != null
                && width == bufferWidth
                && height == bufferHeight
                && nextBlurWidth == blurWidth
                && nextBlurHeight == blurHeight) {
            return;
        }

        disposeBuffers();

        bufferWidth = width;
        bufferHeight = height;
        blurWidth = nextBlurWidth;
        blurHeight = nextBlurHeight;
        sceneBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, bufferWidth, bufferHeight, true);
        blurBufferA = new FrameBuffer(Pixmap.Format.RGBA8888, blurWidth, blurHeight, false);
        blurBufferB = new FrameBuffer(Pixmap.Format.RGBA8888, blurWidth, blurHeight, false);
        configureTexture(sceneBuffer.getColorBufferTexture());
        configureTexture(blurBufferA.getColorBufferTexture());
        configureTexture(blurBufferB.getColorBufferTexture());

        if (blurShader == null) {
            ShaderProgram.pedantic = false;
            blurShader = new ShaderProgram(
                    Gdx.files.internal(VERTEX_SHADER_PATH),
                    Gdx.files.internal(FRAGMENT_SHADER_PATH)
            );
            if (!blurShader.isCompiled()) {
                Client.LOGGER.error("Failed to compile GUI background blur shader: {}", blurShader.getLog());
                blurShader.dispose();
                blurShader = null;
            }
        }
    }

    private void configureTexture(Texture texture) {
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
    }

    private void runGaussianBlurPass(float blurStrength) {
        drawTexture(sceneBuffer, blurBufferA, null, 0f, 0f);
        drawTexture(blurBufferA, blurBufferB, blurShader, 1f / blurWidth, 0f, blurStrength);
        drawTexture(blurBufferB, blurBufferA, blurShader, 0f, 1f / blurHeight, blurStrength);
    }

    private void drawTexture(FrameBuffer source, FrameBuffer target, ShaderProgram shader, float directionX, float directionY) {
        drawTexture(source, target, shader, directionX, directionY, 0f);
    }

    private void drawTexture(FrameBuffer source, FrameBuffer target, ShaderProgram shader, float directionX, float directionY, float sigma) {
        target.begin();
        try {
            ScreenUtils.clear(0f, 0f, 0f, 1f, true);
            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
            Gdx.gl.glDisable(GL20.GL_CULL_FACE);
            Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

            spriteBatch.setShader(shader);
            spriteBatch.setProjectionMatrix(pixelMatrix.setToOrtho2D(0f, 0f, target.getWidth(), target.getHeight()));
            spriteBatch.disableBlending();
            spriteBatch.begin();
            if (shader != null) {
                shader.setUniformf("u_direction", directionX, directionY);
                shader.setUniformf("u_sigma", sigma);
            }
            spriteBatch.draw(createRegion(source), 0f, 0f, target.getWidth(), target.getHeight());
            spriteBatch.end();
            spriteBatch.setShader(null);
        } finally {
            if (spriteBatch.isDrawing()) {
                spriteBatch.end();
            }
            target.end();
        }
    }

    private void drawBlurredBackground(int width, int height) {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        spriteBatch.setShader(null);
        spriteBatch.setProjectionMatrix(pixelMatrix.setToOrtho2D(0f, 0f, width, height));
        spriteBatch.disableBlending();
        spriteBatch.begin();
        spriteBatch.draw(createRegion(blurBufferA), 0f, 0f, width, height);
        spriteBatch.end();
    }

    private TextureRegion createRegion(FrameBuffer frameBuffer) {
        TextureRegion region = new TextureRegion(frameBuffer.getColorBufferTexture());
        region.flip(false, true);
        return region;
    }

    private void disposeBuffers() {
        if (sceneBuffer != null) {
            sceneBuffer.dispose();
            sceneBuffer = null;
        }
        if (blurBufferA != null) {
            blurBufferA.dispose();
            blurBufferA = null;
        }
        if (blurBufferB != null) {
            blurBufferB.dispose();
            blurBufferB = null;
        }
    }

    @Override
    public void dispose() {
        disposeBuffers();
        if (blurShader != null) {
            blurShader.dispose();
            blurShader = null;
        }
        spriteBatch.dispose();
    }
}
