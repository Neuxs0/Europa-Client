package dev.neuxs.europa_client.modules.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.utils.Chat;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;

import java.nio.IntBuffer;

@SuppressWarnings("unused")
public class FpsCounter extends Module {
    private static final float X = 8f;
    private static final float Y = 8f;
    private static final float BACKGROUND_PADDING_X = 5f;
    private static final float BACKGROUND_PADDING_Y = 3f;
    private static final float BACKGROUND_RADIUS = 4f;
    private final RenderUtil renderUtil = new RenderUtil();
    private final BoxRenderer background = new BoxRenderer();
    private final TextRenderer fpsText = new TextRenderer();
    private final IntBuffer glStateBuffer = BufferUtils.newIntBuffer(1);
    private boolean renderersAdded = false;

    public FpsCounter(int keybind, boolean defaultEnabled) {
        super("FPS Counter", keybind, defaultEnabled);
        background.setFillColor(ColorUtils.color(0, 0, 0, 150));
        background.setBorderRadius(BACKGROUND_RADIUS);
        background.setZIndex(0);

        fpsText.setTextColor(ColorUtils.WHITE);
        fpsText.setZIndex(1);
    }

    @Override
    public void enable(boolean messaging) {
        setEnabled(true);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "FPS Counter enabled");
    }

    @Override
    public void disable(boolean messaging) {
        setEnabled(false);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "FPS Counter disabled");
    }

    public void render(Viewport viewport) {
        if (!isEnabled() || Gdx.graphics == null) {
            return;
        }

        if (viewport == null || viewport.getCamera() == null) {
            return;
        }

        String text = "FPS: " + Gdx.graphics.getFramesPerSecond();
        fpsText.setText(text);

        float textWidth = fpsText.getTextWidth(viewport);
        float textHeight = fpsText.getTextHeight(viewport);
        float backgroundWidth = textWidth + BACKGROUND_PADDING_X * 2f;
        float backgroundHeight = textHeight + BACKGROUND_PADDING_Y * 2f;
        float backgroundX = X;
        float backgroundY = viewport.getWorldHeight() - Y - backgroundHeight;
        float textX = backgroundX + BACKGROUND_PADDING_X;
        float textY = backgroundY + BACKGROUND_PADDING_Y;

        background.setPos(backgroundX, backgroundY);
        background.setSize(backgroundWidth, backgroundHeight);
        fpsText.setPos(textX, textY);

        GlState glState = captureGlState();

        try {
            viewport.apply();
            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
            Gdx.gl.glDisable(GL20.GL_CULL_FACE);
            Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            if (!renderersAdded) {
                renderUtil.addRenderer(background);
                renderUtil.addRenderer(fpsText);
                renderersAdded = true;
            }

            renderUtil.syncRenderers();
            renderUtil.renderAll(viewport.getCamera().combined, viewport);
        } finally {
            restoreGlState(glState);
        }
    }

    private GlState captureGlState() {
        return new GlState(
                Gdx.gl.glIsEnabled(GL20.GL_BLEND),
                Gdx.gl.glIsEnabled(GL20.GL_DEPTH_TEST),
                Gdx.gl.glIsEnabled(GL20.GL_CULL_FACE),
                Gdx.gl.glIsEnabled(GL20.GL_SCISSOR_TEST),
                getGlInteger(GL20.GL_BLEND_SRC_RGB),
                getGlInteger(GL20.GL_BLEND_DST_RGB),
                getGlInteger(GL20.GL_DEPTH_FUNC),
                getGlInteger(GL20.GL_CULL_FACE_MODE)
        );
    }

    private int getGlInteger(int parameter) {
        glStateBuffer.clear();
        Gdx.gl.glGetIntegerv(parameter, glStateBuffer);
        return glStateBuffer.get(0);
    }

    private void restoreGlState(GlState state) {
        setGlEnabled(GL20.GL_BLEND, state.blendEnabled);
        setGlEnabled(GL20.GL_DEPTH_TEST, state.depthTestEnabled);
        setGlEnabled(GL20.GL_CULL_FACE, state.cullFaceEnabled);
        setGlEnabled(GL20.GL_SCISSOR_TEST, state.scissorTestEnabled);
        Gdx.gl.glBlendFunc(state.blendSrcRgb, state.blendDstRgb);
        Gdx.gl.glDepthFunc(state.depthFunc);
        Gdx.gl.glCullFace(state.cullFaceMode);
    }

    private void setGlEnabled(int capability, boolean enabled) {
        if (enabled) {
            Gdx.gl.glEnable(capability);
        } else {
            Gdx.gl.glDisable(capability);
        }
    }

    private record GlState(
            boolean blendEnabled,
            boolean depthTestEnabled,
            boolean cullFaceEnabled,
            boolean scissorTestEnabled,
            int blendSrcRgb,
            int blendDstRgb,
            int depthFunc,
            int cullFaceMode
    ) {
    }
}
