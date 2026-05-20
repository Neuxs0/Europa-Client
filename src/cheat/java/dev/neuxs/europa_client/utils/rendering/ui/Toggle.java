package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.managers.InputManager;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.CircleRenderer;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;
import dev.neuxs.europa_client.utils.rendering.Renderer;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Toggle extends Renderer {
    private static final Color DEFAULT_BACKGROUND_OFF = ColorUtils.color(80, 80, 80, 255);
    private static final Color DEFAULT_BACKGROUND_ON = ColorUtils.color(70, 130, 200, 255);
    private static final Color DEFAULT_HANDLE_OFF = ColorUtils.color(150, 150, 150, 255);
    private static final Color DEFAULT_HANDLE_ON = ColorUtils.color(220, 220, 220, 255);
    private static final Color DEFAULT_HOVER_MULTIPLIER = new Color(1.1f, 1.1f, 1.1f, 1f);

    private final Button interactionButton;
    private final BoxRenderer backgroundRenderer;
    private final CircleRenderer handleRenderer;
    private final Vector2 mousePos;

    private boolean toggled;
    private Consumer<Boolean> onValueChanged;
    private float padding;

    private Color backgroundOffColor;
    private Color backgroundOnColor;
    private Color handleOffColor;
    private Color handleOnColor;
    private Color backgroundHoverColorMultiplier;
    private Color handleHoverColorMultiplier;
    private Color currentBackgroundColor;
    private Color currentHandleColor;

    public Toggle() {
        this.interactionButton = new Button();
        this.backgroundRenderer = new BoxRenderer();
        this.handleRenderer = new CircleRenderer();
        this.mousePos = new Vector2();

        this.toggled = false;
        this.onValueChanged = (state) -> {};
        this.padding = 2f;

        this.backgroundOffColor = DEFAULT_BACKGROUND_OFF.cpy();
        this.backgroundOnColor = DEFAULT_BACKGROUND_ON.cpy();
        this.handleOffColor = DEFAULT_HANDLE_OFF.cpy();
        this.handleOnColor = DEFAULT_HANDLE_ON.cpy();
        this.backgroundHoverColorMultiplier = DEFAULT_HOVER_MULTIPLIER.cpy();
        this.handleHoverColorMultiplier = DEFAULT_HOVER_MULTIPLIER.cpy();
        this.currentBackgroundColor = backgroundOffColor.cpy();
        this.currentHandleColor = handleOffColor.cpy();

        setRenderType(RenderUtil.RenderType.SHAPE);
        setShapeType(ShapeRenderer.ShapeType.Filled);
        setSize(40f, 20f);

        backgroundRenderer.setBorder(false);
        handleRenderer.setBorder(false);
        configureInteractionButton();
        updateGeometry();
        updateVisuals(false);
    }

    @Override
    public void update(Viewport viewport) {
        if (viewport == null) {
            return;
        }

        updateMousePosition(viewport);
        boolean mouseOver = isMouseOver(mousePos.x, mousePos.y);
        updateState(mouseOver);

        interactionButton.update(viewport);

        if (mouseOver && InputManager.isFirstFrameMouseButtonDown(Input.Buttons.LEFT)) {
            toggleState();
        } else {
            updateVisuals(mouseOver);
        }
    }

    @Override
    public void renderShape(Viewport viewport, ShapeRenderer shapeRenderer) {
        backgroundRenderer.setFillColor(currentBackgroundColor);
        handleRenderer.setFillColor(currentHandleColor);

        backgroundRenderer.renderShape(viewport, shapeRenderer);
        handleRenderer.renderShape(viewport, shapeRenderer);
    }

    @Override
    public void renderSprite(Viewport viewport, SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
    }

    public void render(Viewport viewport, ShapeRenderer shapeRenderer) {
        renderShape(viewport, shapeRenderer);
    }

    public void render(ShapeRenderer shapeRenderer) {
    }

    public void toggleState() {
        setEnabled(!toggled);
    }

    private void configureInteractionButton() {
        Color transparent = new Color(0f, 0f, 0f, 0f);
        interactionButton.setFillColor(transparent);
        interactionButton.setHoverFillColor(transparent);
        interactionButton.setPressedFillColor(transparent);
        interactionButton.getBoxRenderer().setBorder(false);
        syncInteractionButton();
    }

    private void updateMousePosition(Viewport viewport) {
        mousePos.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mousePos);
    }

    private boolean isMouseOver(float x, float y) {
        return isMouseTarget() && containsPoint(x, y);
    }

    private void updateState(boolean mouseOver) {
        if (mouseOver && InputManager.isMouseButtonDown(Input.Buttons.LEFT)) {
            setState(State.PRESSED);
        } else if (mouseOver) {
            setState(toggled ? State.HOVER_TOGGLED : State.HOVERED);
        } else {
            setState(toggled ? State.TOGGLED : State.NORMAL);
        }
    }

    private void updateGeometry() {
        float width = getWidth();
        float height = getHeight();
        if (width <= 0f || height <= 0f) {
            return;
        }

        backgroundRenderer.setPos(getPos());
        backgroundRenderer.setSize(width, height);
        backgroundRenderer.setBorderRadius(height / 2f);

        float handleRadius = Math.max(0f, height / 2f - padding);
        handleRenderer.setRadius(handleRadius);
        handleRenderer.setPos(calculateHandleX(), getPosY() + height / 2f);

        syncInteractionButton();
    }

    private float calculateHandleX() {
        float handleRadius = handleRenderer.getRadius();
        return toggled
                ? getPosX() + getWidth() - handleRadius - padding
                : getPosX() + handleRadius + padding;
    }

    private void updateVisuals(boolean hovered) {
        currentBackgroundColor = (toggled ? backgroundOnColor : backgroundOffColor).cpy();
        currentHandleColor = (toggled ? handleOnColor : handleOffColor).cpy();

        if (hovered || getState() == State.PRESSED || getState() == State.HOVER_TOGGLED) {
            currentBackgroundColor.mul(backgroundHoverColorMultiplier);
            currentHandleColor.mul(handleHoverColorMultiplier);
        }

        handleRenderer.setPosX(calculateHandleX());
    }

    private void syncInteractionButton() {
        interactionButton.setPos(getPos());
        interactionButton.setSize(getSize());
    }

    @Override
    public void setPos(Vector3 pos) {
        super.setPos(pos);
        updateGeometry();
    }

    @Override
    public void setPos(Vector2 pos) {
        super.setPos(pos);
        updateGeometry();
    }

    @Override
    public void setPos(float x, float y, int z) {
        super.setPos(x, y, z);
        updateGeometry();
    }

    @Override
    public void setPos(float x, float y) {
        super.setPos(x, y);
        updateGeometry();
    }

    public void setPosition(float x, float y) {
        setPos(x, y);
    }

    @Override
    public void setSize(Vector2 size) {
        if (size != null) {
            setSize(size.x, size.y);
        }
    }

    @Override
    public void setSize(float width, float height) {
        float effectiveHeight = Math.max(1f, height);
        float effectiveWidth = Math.max(effectiveHeight * 2f, width);
        super.setSize(effectiveWidth, effectiveHeight);
        updateGeometry();
    }

    @Override
    public void setWidth(float width) {
        setSize(width, getHeight());
    }

    @Override
    public void setHeight(float height) {
        setSize(getWidth(), height);
    }

    public void setEnabled(boolean enabled) {
        setEnabled(enabled, true);
    }

    public void setEnabledSilent(boolean enabled) {
        setEnabled(enabled, false);
    }

    private void setEnabled(boolean enabled, boolean triggerCallback) {
        boolean changed = toggled != enabled;
        toggled = enabled;
        updateState(false);
        updateGeometry();
        updateVisuals(false);

        if (changed && triggerCallback && onValueChanged != null) {
            onValueChanged.accept(toggled);
        }
    }

    public void setPadding(float padding) {
        this.padding = Math.max(0f, padding);
        updateGeometry();
        updateVisuals(false);
    }

    public boolean isToggled() {
        return toggled;
    }

    public Consumer<Boolean> getOnValueChanged() {
        return onValueChanged;
    }

    public void setOnValueChanged(Consumer<Boolean> onValueChanged) {
        this.onValueChanged = onValueChanged != null ? onValueChanged : (state) -> {};
    }

    public void setBackgroundOffColor(Color color) {
        backgroundOffColor = color != null ? color.cpy() : DEFAULT_BACKGROUND_OFF.cpy();
        updateVisuals(false);
    }

    public void setBackgroundOnColor(Color color) {
        backgroundOnColor = color != null ? color.cpy() : DEFAULT_BACKGROUND_ON.cpy();
        updateVisuals(false);
    }

    public void setHandleOffColor(Color color) {
        handleOffColor = color != null ? color.cpy() : DEFAULT_HANDLE_OFF.cpy();
        updateVisuals(false);
    }

    public void setHandleOnColor(Color color) {
        handleOnColor = color != null ? color.cpy() : DEFAULT_HANDLE_ON.cpy();
        updateVisuals(false);
    }

    public void setBackgroundHoverColorMultiplier(Color color) {
        backgroundHoverColorMultiplier = color != null ? color.cpy() : DEFAULT_HOVER_MULTIPLIER.cpy();
        updateVisuals(false);
    }

    public void setHandleHoverColorMultiplier(Color color) {
        handleHoverColorMultiplier = color != null ? color.cpy() : DEFAULT_HOVER_MULTIPLIER.cpy();
        updateVisuals(false);
    }

    public float getPadding() {
        return padding;
    }

    public Color getBackgroundOffColor() {
        return backgroundOffColor.cpy();
    }

    public Color getBackgroundOnColor() {
        return backgroundOnColor.cpy();
    }

    public Color getHandleOffColor() {
        return handleOffColor.cpy();
    }

    public Color getHandleOnColor() {
        return handleOnColor.cpy();
    }

    public Color getBackgroundHoverColorMultiplier() {
        return backgroundHoverColorMultiplier.cpy();
    }

    public Color getHandleHoverColorMultiplier() {
        return handleHoverColorMultiplier.cpy();
    }

    public BoxRenderer getBackgroundRenderer() {
        return backgroundRenderer;
    }

    public CircleRenderer getHandleRenderer() {
        return handleRenderer;
    }

    public Button getInteractionButton() {
        return interactionButton;
    }

    public float getX() {
        return getPosX();
    }

    public float getY() {
        return getPosY();
    }
}
