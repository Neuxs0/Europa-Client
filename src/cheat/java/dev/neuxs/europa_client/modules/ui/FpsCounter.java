package dev.neuxs.europa_client.modules.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.settings.Setting;
import dev.neuxs.europa_client.utils.Chat;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.nio.IntBuffer;

@SuppressWarnings("unused")
public class FpsCounter extends HudModule {
    private static final String ADVANCED_SETTING_KEY = "advanced";
    private static final float BACKGROUND_PADDING_X = 5f;
    private static final float BACKGROUND_PADDING_Y = 3f;
    private static final float BACKGROUND_RADIUS = 4f;
    private static final int MAX_FRAME_SAMPLES = 300;
    private static final float DISPLAY_UPDATE_INTERVAL = 1f;
    private static final long BYTES_PER_MEBIBYTE = 1024L * 1024L;
    private RenderUtil renderUtil;
    private final BoxRenderer background = new BoxRenderer();
    private final TextRenderer fpsText = new TextRenderer();
    private final List<Float> frameTimes = new ArrayList<>();
    private final IntBuffer glStateBuffer = BufferUtils.newIntBuffer(1);
    private float totalFrameTime = 0f;
    private float displayUpdateTimer = DISPLAY_UPDATE_INTERVAL;
    private boolean lastAdvancedViewEnabled = false;
    private String cachedCounterText = "";
    private boolean renderersAdded = false;

    public FpsCounter(int keybind, boolean defaultEnabled) {
        super("FPS Counter", keybind, defaultEnabled);
        customSettings.put(ADVANCED_SETTING_KEY, new Setting<>("advanced", false)
                .withDisplayName("Stats")
                .withDescription("Show FPS, average FPS, 1% low, and RAM usage."));

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
        renderHud(viewport);
    }

    @Override
    public void renderHud(Viewport viewport) {
        if (!isEnabled() || Gdx.graphics == null) {
            return;
        }

        if (viewport == null || viewport.getCamera() == null) {
            return;
        }

        float deltaTime = Gdx.graphics.getDeltaTime();
        updateFrameStats(deltaTime);
        updateCounterText(deltaTime);

        fpsText.setText(cachedCounterText);

        Vector2 hudSize = getHudSize(viewport);
        Vector2 hudPosition = getHudPosition(viewport, hudSize);
        float backgroundWidth = hudSize.x;
        float backgroundHeight = hudSize.y;
        float backgroundX = hudPosition.x;
        float backgroundY = hudPosition.y;
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

            RenderUtil hudRenderUtil = getRenderUtil();
            if (!renderersAdded) {
                hudRenderUtil.addRenderer(background);
                hudRenderUtil.addRenderer(fpsText);
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
        fpsText.setText(getMeasuredCounterText());
        float textWidth = fpsText.getTextWidth(viewport);
        float textHeight = fpsText.getTextHeight(viewport);
        return new Vector2(
                textWidth + BACKGROUND_PADDING_X * 2f,
                textHeight + BACKGROUND_PADDING_Y * 2f
        );
    }

    private String getMeasuredCounterText() {
        if (!cachedCounterText.isEmpty()) {
            return cachedCounterText;
        }
        return buildCounterText(isAdvancedViewEnabled());
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

    @SuppressWarnings("unchecked")
    private boolean isAdvancedViewEnabled() {
        Setting<Boolean> advancedSetting = (Setting<Boolean>) customSettings.get(ADVANCED_SETTING_KEY);
        return advancedSetting != null && advancedSetting.getValue();
    }

    private void updateCounterText(float deltaTime) {
        boolean advancedViewEnabled = isAdvancedViewEnabled();
        displayUpdateTimer += deltaTime;

        if (!cachedCounterText.isEmpty()
                && displayUpdateTimer < DISPLAY_UPDATE_INTERVAL
                && advancedViewEnabled == lastAdvancedViewEnabled) {
            return;
        }

        cachedCounterText = buildCounterText(advancedViewEnabled);
        lastAdvancedViewEnabled = advancedViewEnabled;
        displayUpdateTimer = 0f;
    }

    private String buildCounterText(boolean advancedViewEnabled) {
        int fps = Gdx.graphics.getFramesPerSecond();
        if (!advancedViewEnabled) {
            return "FPS: " + fps;
        }

        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / BYTES_PER_MEBIBYTE;
        long maxMemory = runtime.maxMemory() / BYTES_PER_MEBIBYTE;

        // Cosmic Reach's flipped font draw path displays newline-separated text bottom-first.
        return String.format(
                Locale.ROOT,
                "RAM: %d/%d MB\n1%%Low: %d\nAvg: %d\nFPS: %d",
                usedMemory,
                maxMemory,
                Math.round(getOnePercentLowFps()),
                Math.round(getAverageFps()),
                fps
        );
    }

    private void updateFrameStats(float deltaTime) {
        if (!Float.isFinite(deltaTime) || deltaTime <= 0f) {
            return;
        }

        float cappedDeltaTime = Math.min(deltaTime, 1f);
        frameTimes.add(cappedDeltaTime);
        totalFrameTime += cappedDeltaTime;

        while (frameTimes.size() > MAX_FRAME_SAMPLES) {
            totalFrameTime -= frameTimes.remove(0);
        }
    }

    private float getAverageFps() {
        if (frameTimes.isEmpty() || totalFrameTime <= 0f) {
            return Gdx.graphics.getFramesPerSecond();
        }
        return frameTimes.size() / totalFrameTime;
    }

    private float getOnePercentLowFps() {
        if (frameTimes.isEmpty()) {
            return Gdx.graphics.getFramesPerSecond();
        }

        List<Float> sortedFrameTimes = new ArrayList<>(frameTimes);
        sortedFrameTimes.sort(Collections.reverseOrder());

        int sampleCount = Math.max(1, (int) Math.ceil(sortedFrameTimes.size() * 0.01f));
        float slowestFrameTimeTotal = 0f;
        for (int i = 0; i < sampleCount; i++) {
            slowestFrameTimeTotal += sortedFrameTimes.get(i);
        }

        float averageSlowestFrameTime = slowestFrameTimeTotal / sampleCount;
        if (averageSlowestFrameTime <= 0f) {
            return Gdx.graphics.getFramesPerSecond();
        }
        return 1f / averageSlowestFrameTime;
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
