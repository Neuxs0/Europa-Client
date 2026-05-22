package dev.neuxs.europa_client.modules.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.utils.Chat;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;

import java.nio.IntBuffer;
import java.util.Locale;

@SuppressWarnings("unused")
public class VelocityHud extends HudModule {
    private static final float BACKGROUND_PADDING_X = 5f;
    private static final float BACKGROUND_PADDING_Y = 3f;
    private static final float BACKGROUND_RADIUS = 4f;
    private static final float DISPLAY_UPDATE_INTERVAL = 0.1f;
    private static final float DEFAULT_TOP_OFFSET = 84f;

    private RenderUtil renderUtil;
    private final BoxRenderer background = new BoxRenderer();
    private final TextRenderer velocityText = new TextRenderer();
    private final IntBuffer glStateBuffer = BufferUtils.newIntBuffer(1);

    private float displayUpdateTimer = DISPLAY_UPDATE_INTERVAL;
    private String cachedCounterText = "";
    private boolean renderersAdded = false;

    public VelocityHud(int keybind, boolean defaultEnabled) {
        super("Velocity HUD", keybind, defaultEnabled);

        background.setFillColor(ColorUtils.color(0, 0, 0, 150));
        background.setBorderRadius(BACKGROUND_RADIUS);
        background.setZIndex(0);

        velocityText.setTextColor(ColorUtils.WHITE);
        velocityText.setZIndex(1);
    }

    @Override
    public void enable(boolean messaging) {
        resetTracking();
        setEnabled(true);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Velocity HUD enabled");
    }

    @Override
    public void disable(boolean messaging) {
        resetTracking();
        setEnabled(false);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Velocity HUD disabled");
    }

    @Override
    public void renderHud(Viewport viewport) {
        if (!isEnabled() || Gdx.graphics == null || viewport == null || viewport.getCamera() == null) {
            return;
        }

        updateCounterText(Gdx.graphics.getDeltaTime());
        velocityText.setText(cachedCounterText);

        Vector2 hudSize = getHudSize(viewport);
        Vector2 hudPosition = getHudPosition(viewport, hudSize);
        background.setPos(hudPosition.x, hudPosition.y);
        background.setSize(hudSize.x, hudSize.y);
        velocityText.setPos(hudPosition.x + BACKGROUND_PADDING_X, hudPosition.y + BACKGROUND_PADDING_Y);

        GlState glState = captureGlState();
        try {
            viewport.apply();
            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
            Gdx.gl.glDisable(GL20.GL_CULL_FACE);
            Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            RenderUtil hudRenderUtil = getRenderUtil();
            if (!renderersAdded) {
                hudRenderUtil.addRenderer(background);
                hudRenderUtil.addRenderer(velocityText);
                renderersAdded = true;
            }

            hudRenderUtil.syncRenderers();
            hudRenderUtil.renderAll(viewport.getCamera().combined, viewport);
        } finally {
            restoreGlState(glState);
        }
    }

    @Override
    public Vector2 getHudSize(Viewport viewport) {
        velocityText.setText(getMeasuredCounterText());
        return new Vector2(
                velocityText.getTextWidth(viewport) + BACKGROUND_PADDING_X * 2f,
                velocityText.getTextHeight(viewport) + BACKGROUND_PADDING_Y * 2f
        );
    }

    @Override
    protected Vector2 getDefaultHudPosition(Viewport viewport, Vector2 size) {
        float viewportHeight = viewport == null ? 0f : viewport.getWorldHeight();
        float height = size == null ? 0f : size.y;
        return new Vector2(8f, Math.max(0f, viewportHeight - 8f - DEFAULT_TOP_OFFSET - height));
    }

    private String getMeasuredCounterText() {
        if (!cachedCounterText.isEmpty()) {
            return cachedCounterText;
        }
        return buildCounterText();
    }

    private void updateCounterText(float deltaTime) {
        displayUpdateTimer += deltaTime;
        if (!cachedCounterText.isEmpty() && displayUpdateTimer < DISPLAY_UPDATE_INTERVAL) {
            return;
        }

        cachedCounterText = buildCounterText();
        displayUpdateTimer = 0f;
    }

    private String buildCounterText() {
        VelocityTracker.Snapshot snapshot = VelocityTracker.getSnapshot();
        return snapshot.available()
                ? String.format(Locale.ROOT, "%.2f bps", snapshot.blocksPerSecond())
                : "-- bps";
    }

    private void resetTracking() {
        VelocityTracker.reset();
        cachedCounterText = "";
        displayUpdateTimer = DISPLAY_UPDATE_INTERVAL;
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

    private RenderUtil getRenderUtil() {
        if (renderUtil == null) {
            renderUtil = new RenderUtil();
        }
        return renderUtil;
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
