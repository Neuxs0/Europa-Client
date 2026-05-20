package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.managers.InputManager;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.CircleRenderer;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;
import dev.neuxs.europa_client.utils.rendering.Renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Slider extends Renderer {
    private static final float MIN_RANGE = 1e-6f;
    private static final float VALUE_EPSILON = 1e-6f;

    private static final Color DEFAULT_TRACK_COLOR = ColorUtils.color(80, 80, 80, 255);
    private static final Color DEFAULT_FILL_COLOR = ColorUtils.color(160, 160, 160, 255);
    private static final Color DEFAULT_HANDLE_COLOR = ColorUtils.color(240, 240, 240, 255);
    private static final Color DEFAULT_HANDLE_HOVER_COLOR = ColorUtils.color(255, 255, 255, 255);
    private static final Color DEFAULT_HANDLE_DRAG_COLOR = ColorUtils.color(220, 220, 220, 255);

    private final BoxRenderer trackRenderer;
    private final BoxRenderer fillRenderer;
    private final CircleRenderer handleRenderer;
    private final Vector2 mousePos;
    private final List<Float> snapPoints;

    private float minValue;
    private float maxValue;
    private float currentValue;
    private boolean snapEnabled;
    private boolean isDragging;
    private Consumer<Float> onValueChanged;
    private float padding;
    private float trackHeightMultiplier;

    private Color trackColor;
    private Color fillColor;
    private Color handleColor;
    private Color handleHoverColor;
    private Color handleDragColor;

    public Slider() {
        this.trackRenderer = new BoxRenderer();
        this.fillRenderer = new BoxRenderer();
        this.handleRenderer = new CircleRenderer();
        this.mousePos = new Vector2();
        this.snapPoints = new ArrayList<>();

        setRenderType(RenderUtil.RenderType.SHAPE);
        setShapeType(ShapeRenderer.ShapeType.Filled);

        this.minValue = 0f;
        this.maxValue = 100f;
        this.currentValue = 50f;
        this.snapEnabled = false;
        this.isDragging = false;
        this.onValueChanged = (value) -> {};
        this.padding = 3f;
        this.trackHeightMultiplier = 0.4f;

        this.trackColor = DEFAULT_TRACK_COLOR.cpy();
        this.fillColor = DEFAULT_FILL_COLOR.cpy();
        this.handleColor = DEFAULT_HANDLE_COLOR.cpy();
        this.handleHoverColor = DEFAULT_HANDLE_HOVER_COLOR.cpy();
        this.handleDragColor = DEFAULT_HANDLE_DRAG_COLOR.cpy();

        configureRendererDefaults();
        updateGeometry();
        updateVisuals(false);
    }

    @Override
    public void update(Viewport viewport) {
        if (viewport == null) {
            return;
        }

        super.update(viewport);

        updateMousePosition(viewport);
        boolean mouseOver = isMouseOver(mousePos.x, mousePos.y);
        boolean mousePressed = InputManager.isMouseButtonDown(Input.Buttons.LEFT);

        if (!isDragging && mouseOver && mousePressed && getState() == State.PRESSED) {
            isDragging = true;
            updateValueFromMouse(mousePos.x);
        }

        if (isDragging && !mousePressed) {
            isDragging = false;
        }

        if (isDragging) {
            updateValueFromMouse(mousePos.x);
        }

        updateVisuals(mouseOver);
    }

    @Override
    public void renderShape(Viewport viewport, ShapeRenderer shapeRenderer) {
        boolean mouseOver = viewport != null && isMouseOver(viewport);
        updateVisuals(mouseOver);

        trackRenderer.renderShape(viewport, shapeRenderer);
        fillRenderer.renderShape(viewport, shapeRenderer);
        handleRenderer.renderShape(viewport, shapeRenderer);
    }

    @Override
    public void renderSprite(Viewport viewport, SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
    }

    private void configureRendererDefaults() {
        trackRenderer.setBorder(false);
        fillRenderer.setBorder(false);
        handleRenderer.setBorder(false);
    }

    private void updateMousePosition(Viewport viewport) {
        mousePos.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mousePos);
    }

    private boolean isMouseOver(float x, float y) {
        return isMouseTarget() && containsPoint(x, y);
    }

    private void updateValueFromMouse(float mouseX) {
        setValueInternal(valueFromMouseX(mouseX), true);
    }

    private float valueFromMouseX(float mouseX) {
        float usableTrackWidth = getUsableTrackWidth();

        if (usableTrackWidth <= 0f) {
            float sliderCenterX = getPosX() + getWidth() / 2f;
            return normalizeValue(mouseX < sliderCenterX ? minValue : maxValue);
        }

        float clampedMouseX = MathUtils.clamp(mouseX, getMinHandleX(), getMaxHandleX());
        float ratio = (clampedMouseX - getMinHandleX()) / usableTrackWidth;
        return normalizeValue(minValue + ratio * getValueRange());
    }

    private float normalizeValue(float value) {
        float clampedValue = MathUtils.clamp(value, minValue, maxValue);
        return snapEnabled && !snapPoints.isEmpty()
                ? findClosestSnapPoint(clampedValue)
                : clampedValue;
    }

    private void setValueInternal(float newValue, boolean triggerCallback) {
        float normalizedValue = normalizeValue(newValue);
        boolean valueChanged = Math.abs(normalizedValue - currentValue) > VALUE_EPSILON;

        if (valueChanged) {
            currentValue = normalizedValue;
        }

        updateValueGeometry();

        if (valueChanged && triggerCallback && onValueChanged != null) {
            onValueChanged.accept(currentValue);
        }
    }

    private void updateVisuals(boolean mouseOver) {
        trackRenderer.setFillColor(trackColor);
        fillRenderer.setFillColor(fillColor);

        if (isDragging || getState() == State.PRESSED) {
            handleRenderer.setFillColor(handleDragColor);
        } else if (mouseOver || getState() == State.HOVERED) {
            handleRenderer.setFillColor(handleHoverColor);
        } else {
            handleRenderer.setFillColor(handleColor);
        }
    }

    private void updateGeometry() {
        if (getWidth() <= 0f || getHeight() <= 0f) {
            return;
        }

        float handleRadius = getHandleRadius();
        float handleCenterY = getPosY() + handleRadius;
        float trackHeight = Math.max(1f, getHeight() * trackHeightMultiplier);
        float trackRadius = trackHeight / 2f;
        float trackY = handleCenterY - trackRadius;

        handleRenderer.setRadius(handleRadius);
        handleRenderer.setPosY(handleCenterY);

        trackRenderer.setPos(getPosX(), trackY);
        trackRenderer.setSize(getWidth(), trackHeight);
        trackRenderer.setBorderRadius(trackRadius);

        fillRenderer.setPos(getPosX(), trackY);
        fillRenderer.setHeight(trackHeight);
        fillRenderer.setBorderRadius(trackRadius);

        updateValueGeometry();
    }

    private void updateValueGeometry() {
        updateHandlePosition();
        updateFillWidth();
    }

    private void updateHandlePosition() {
        float handleX = getMinHandleX();

        if (getUsableTrackWidth() > 0f) {
            handleX += getValueRatio() * getUsableTrackWidth();
        }

        handleRenderer.setPosX(handleX);
    }

    private void updateFillWidth() {
        float fillStartX = trackRenderer.getPosX();
        fillRenderer.setPosX(fillStartX);
        fillRenderer.setWidth(Math.max(0f, handleRenderer.getPosX() - fillStartX));
    }

    private float getHandleRadius() {
        return Math.max(0f, getHeight() / 2f);
    }

    private float getMinHandleX() {
        return getPosX() + getHandleRadius() + padding;
    }

    private float getMaxHandleX() {
        return getPosX() + getWidth() - getHandleRadius() - padding;
    }

    private float getUsableTrackWidth() {
        return Math.max(0f, getMaxHandleX() - getMinHandleX());
    }

    private float getValueRange() {
        return maxValue - minValue;
    }

    private float getValueRatio() {
        float valueRange = getValueRange();
        if (valueRange <= MIN_RANGE) {
            return 0f;
        }
        return MathUtils.clamp((currentValue - minValue) / valueRange, 0f, 1f);
    }

    private float findClosestSnapPoint(float value) {
        if (snapPoints.isEmpty()) {
            return value;
        }

        float closestSnap = MathUtils.clamp(snapPoints.get(0), minValue, maxValue);
        float closestDistance = Math.abs(value - closestSnap);

        for (int i = 1; i < snapPoints.size(); i++) {
            float snapPoint = MathUtils.clamp(snapPoints.get(i), minValue, maxValue);
            float distance = Math.abs(value - snapPoint);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestSnap = snapPoint;
            }
        }

        return closestSnap;
    }

    private void normalizeRangeFromMin() {
        if (maxValue <= minValue) {
            maxValue = minValue + MIN_RANGE;
        }
    }

    private void normalizeRangeFromMax() {
        if (maxValue <= minValue) {
            minValue = maxValue - MIN_RANGE;
        }
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

    @Override
    public void setPosX(float x) {
        super.setPosX(x);
        updateGeometry();
    }

    @Override
    public void setPosY(float y) {
        super.setPosY(y);
        updateGeometry();
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
        float minimumWidth = effectiveHeight + padding * 2f;
        super.setSize(Math.max(minimumWidth, width), effectiveHeight);
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

    public void setRange(float minValue, float maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        normalizeRangeFromMin();
        setValueInternal(currentValue, false);
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
        normalizeRangeFromMin();
        setValueInternal(currentValue, false);
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
        normalizeRangeFromMax();
        setValueInternal(currentValue, false);
    }

    public void setValue(float value) {
        setValueInternal(value, true);
    }

    public void setValueSilent(float value) {
        setValueInternal(value, false);
    }

    public void setSnapEnabled(boolean snapEnabled) {
        this.snapEnabled = snapEnabled;
        setValueInternal(currentValue, true);
    }

    public void setSnapPoints(List<Float> snapPoints) {
        this.snapPoints.clear();
        if (snapPoints != null) {
            this.snapPoints.addAll(snapPoints);
            Collections.sort(this.snapPoints);
        }
        setValueInternal(currentValue, snapEnabled);
    }

    public void setPadding(float padding) {
        this.padding = Math.max(0f, padding);
        setSize(getWidth(), getHeight());
    }

    public void setTrackHeightMultiplier(float multiplier) {
        this.trackHeightMultiplier = MathUtils.clamp(multiplier, 0f, 1f);
        updateGeometry();
    }

    public void setOnValueChanged(Consumer<Float> onValueChanged) {
        this.onValueChanged = onValueChanged != null ? onValueChanged : (value) -> {};
    }

    public void setTrackColor(Color trackColor) {
        this.trackColor = trackColor != null ? trackColor.cpy() : DEFAULT_TRACK_COLOR.cpy();
        updateVisuals(false);
    }

    @Override
    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor != null ? fillColor.cpy() : DEFAULT_FILL_COLOR.cpy();
        updateVisuals(false);
    }

    public void setHandleColor(Color handleColor) {
        this.handleColor = handleColor != null ? handleColor.cpy() : DEFAULT_HANDLE_COLOR.cpy();
        updateVisuals(false);
    }

    public void setHandleHoverColor(Color handleHoverColor) {
        this.handleHoverColor = handleHoverColor != null ? handleHoverColor.cpy() : DEFAULT_HANDLE_HOVER_COLOR.cpy();
        updateVisuals(false);
    }

    public void setHandleDragColor(Color handleDragColor) {
        this.handleDragColor = handleDragColor != null ? handleDragColor.cpy() : DEFAULT_HANDLE_DRAG_COLOR.cpy();
        updateVisuals(false);
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public float getValue() {
        return currentValue;
    }

    public boolean isSnapEnabled() {
        return snapEnabled;
    }

    public List<Float> getSnapPoints() {
        return new ArrayList<>(snapPoints);
    }

    public Consumer<Float> getOnValueChanged() {
        return onValueChanged;
    }

    public float getPadding() {
        return padding;
    }

    public float getTrackHeightMultiplier() {
        return trackHeightMultiplier;
    }

    public Color getTrackColor() {
        return trackColor.cpy();
    }

    @Override
    public Color getFillColor() {
        return fillColor.cpy();
    }

    public Color getHandleColor() {
        return handleColor.cpy();
    }

    public Color getHandleHoverColor() {
        return handleHoverColor.cpy();
    }

    public Color getHandleDragColor() {
        return handleDragColor.cpy();
    }

    public BoxRenderer getTrackRenderer() {
        return trackRenderer;
    }

    public BoxRenderer getFillRenderer() {
        return fillRenderer;
    }

    public CircleRenderer getHandleRenderer() {
        return handleRenderer;
    }
}
