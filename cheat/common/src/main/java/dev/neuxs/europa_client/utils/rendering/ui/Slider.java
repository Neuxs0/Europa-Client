package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.managers.InputManager;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.CircleRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Slider {
    private final BoxRenderer trackRenderer;
    private final BoxRenderer fillRenderer;
    private final CircleRenderer handleRenderer;
    private final Button interactionButton;
    private float minValue;
    private float maxValue;
    private float currentValue;
    private float valueRange;
    private boolean snapEnabled;
    private List<Float> snapPoints;
    private boolean isDragging;
    private final Vector2 mousePos;
    private Consumer<Float> onValueChanged;
    private float padding;
    private float trackHeightMultiplier;
    private Color trackColor;
    private Color fillColor;
    private Color handleColor;
    private Color handleHoverColor;
    private Color handleDragColor;
    private static final Color DEFAULT_TRACK_COLOR = ColorUtils.color(80, 80, 80, 255);
    private static final Color DEFAULT_FILL_COLOR = ColorUtils.color(160, 160, 160, 255);
    private static final Color DEFAULT_HANDLE_COLOR = ColorUtils.color(240, 240, 240, 255);
    private static final Color DEFAULT_HANDLE_HOVER_COLOR = ColorUtils.color(255, 255, 255, 255);
    private static final Color DEFAULT_HANDLE_DRAG_COLOR = ColorUtils.color(220, 220, 220, 255);
    private float posX;
    private float posY;
    private float width;
    private float height;

    public Slider() {
        super();
        this.trackRenderer = new BoxRenderer();
        this.fillRenderer = new BoxRenderer();
        this.handleRenderer = new CircleRenderer();
        this.interactionButton = new Button();
        this.mousePos = new Vector2();
        this.minValue = 0f;
        this.maxValue = 100f;
        this.valueRange = this.maxValue - this.minValue;
        this.snapEnabled = false;
        this.snapPoints = new ArrayList<>();
        this.isDragging = false;
        this.onValueChanged = (value) -> {};
        this.padding = 3f;
        this.trackHeightMultiplier = 0.4f;
        this.posX = 0f;
        this.posY = 0f;
        this.width = 0f;
        this.height = 0f;
        this.trackColor = DEFAULT_TRACK_COLOR.cpy();
        this.fillColor = DEFAULT_FILL_COLOR.cpy();
        this.handleColor = DEFAULT_HANDLE_COLOR.cpy();
        this.handleHoverColor = DEFAULT_HANDLE_HOVER_COLOR.cpy();
        this.handleDragColor = DEFAULT_HANDLE_DRAG_COLOR.cpy();
        this.trackRenderer.setFillColor(this.trackColor);
        this.trackRenderer.setBorderEnabled(false);
        this.fillRenderer.setFillColor(this.fillColor);
        this.fillRenderer.setBorderEnabled(false);
        this.handleRenderer.setFillColor(this.handleColor);
        this.handleRenderer.setBorderEnabled(false);
        Color transparent = new Color(0, 0, 0, 0);
        this.interactionButton.setNormalFillColor(transparent);
        this.interactionButton.setHoverFillColor(transparent);
        this.interactionButton.setPressedFillColor(transparent);
        this.interactionButton.getBoxRenderer().setBorderEnabled(false);
        this.interactionButton.setOnClick(btn -> {});
        this.setValueInternal(MathUtils.clamp(50f, this.minValue, this.maxValue), false);
    }

    public void update(Viewport viewport) {
        this.interactionButton.update(viewport);

        this.mousePos.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(this.mousePos);

        boolean mouseOver = this.interactionButton.getCurrentState() != Button.ButtonState.NORMAL;
        boolean mousePressed = InputManager.isMouseButtonDown(Input.Buttons.LEFT);

        boolean canStartDrag = mouseOver && mousePressed && this.interactionButton.getCurrentState() == Button.ButtonState.PRESSED;

        if (!this.isDragging && canStartDrag) {
            this.isDragging = true;
            this.updateValueFromMouse(this.mousePos.x);
        }

        if (this.isDragging && !mousePressed) {
            this.isDragging = false;
        }

        if (this.isDragging) {
            this.updateValueFromMouse(this.mousePos.x);
        }

        this.updateVisuals(mouseOver);
    }

    private void updateValueFromMouse(float mouseX) {
        float handleRadius = this.height / 2f;
        if (handleRadius <= 0) return;

        float handleCenterX_Min = this.posX + handleRadius + this.padding;
        float handleCenterX_Max = this.posX + this.width - handleRadius - this.padding;
        float usableTrackWidth = Math.max(0, handleCenterX_Max - handleCenterX_Min);

        float newValue;
        if (usableTrackWidth > 0) {
            float clampedMouseX = MathUtils.clamp(mouseX, handleCenterX_Min, handleCenterX_Max);
            float ratio = (clampedMouseX - handleCenterX_Min) / usableTrackWidth;
            newValue = this.minValue + ratio * this.valueRange;
        } else {
            float overallCenterX = this.posX + this.width / 2f;
            newValue = (mouseX < overallCenterX) ? this.minValue : this.maxValue;
        }

        if (this.snapEnabled && this.snapPoints != null && !this.snapPoints.isEmpty()) {
            newValue = this.findClosestSnapPoint(newValue);
        } else {
            newValue = MathUtils.clamp(newValue, this.minValue, this.maxValue);
        }

        this.setValueInternal(newValue, true);
    }

    private void setValueInternal(float newValue, boolean triggerCallback) {
        newValue = MathUtils.clamp(newValue, this.minValue, this.maxValue);
        if (Math.abs(newValue - this.currentValue) > 1e-6f) {
            this.currentValue = newValue;
            this.updateHandlePosition();
            this.updateFillWidth();
            if (triggerCallback && this.onValueChanged != null) {
                this.onValueChanged.accept(this.currentValue);
            }
        } else if (!triggerCallback) {
            this.updateHandlePosition();
            this.updateFillWidth();
        }
    }

    private void updateVisuals(boolean mouseOver) {
        Color targetHandleColor = this.handleColor;
        if (this.isDragging) {
            targetHandleColor = this.handleDragColor;
        } else if (mouseOver) {
            targetHandleColor = this.handleHoverColor;
        }
        this.handleRenderer.setFillColor(targetHandleColor);
        this.trackRenderer.setFillColor(this.trackColor);
        this.fillRenderer.setFillColor(this.fillColor);
    }

    public void render(ShapeRenderer shapeRenderer) {
        this.trackRenderer.render(shapeRenderer);
        this.fillRenderer.render(shapeRenderer);
        this.handleRenderer.render(shapeRenderer);
    }

    private void updateGeometry() {
        if (this.width <= 0 || this.height <= 0) return;

        float handleRadius = this.height / 2f;
        this.handleRenderer.setRadius(handleRadius);
        float handleCenterY = this.posY + handleRadius;
        this.handleRenderer.setPosY(handleCenterY);

        float trackHeight = Math.max(1f, this.height * this.trackHeightMultiplier);
        float trackRadius = trackHeight / 2f;
        float trackY = handleCenterY - trackRadius;

        this.trackRenderer.setPosition(this.posX, trackY);
        this.trackRenderer.setSize(this.width, trackHeight);
        this.trackRenderer.setBorderRadius(trackRadius);

        this.fillRenderer.setPosition(this.posX, trackY);
        this.fillRenderer.setHeight(trackHeight);
        this.fillRenderer.setBorderRadius(trackRadius);

        this.interactionButton.setPosition(this.posX, this.posY);
        this.interactionButton.setSize(this.width, this.height);

        this.updateHandlePosition();
        this.updateFillWidth();
    }

    private void updateHandlePosition() {
        float handleRadius = this.handleRenderer.getRadius();
        if (handleRadius <= 0 || this.valueRange <= 1e-9f) {
            this.handleRenderer.setPosX(this.posX + handleRadius + this.padding);
            return;
        }

        float handleCenterX_Min = this.posX + handleRadius + this.padding;
        float handleCenterX_Max = this.posX + this.width - handleRadius - this.padding;
        float usableTrackWidth = Math.max(0, handleCenterX_Max - handleCenterX_Min);

        float ratio = (this.currentValue - this.minValue) / this.valueRange;
        float handleTargetX = handleCenterX_Min + ratio * usableTrackWidth;

        this.handleRenderer.setPosX(handleTargetX);
    }

    private void updateFillWidth() {
        float fillStartX = this.trackRenderer.getPosX();
        float fillEndX = this.handleRenderer.getPosX();

        float fillWidth = Math.max(0, fillEndX - fillStartX);

        this.fillRenderer.setPosX(fillStartX);
        this.fillRenderer.setWidth(fillWidth);
    }

    private float findClosestSnapPoint(float value) {
        if (this.snapPoints == null || this.snapPoints.isEmpty()) {
            return value;
        }
        List<Float> clampedSnaps = new ArrayList<>();
        for(float p : this.snapPoints) {
            clampedSnaps.add(MathUtils.clamp(p, this.minValue, this.maxValue));
        }

        float closestSnap = clampedSnaps.get(0);
        float minDiff = Math.abs(value - closestSnap);

        for (int i = 1; i < clampedSnaps.size(); i++) {
            float currentSnap = clampedSnaps.get(i);
            float currentDiff = Math.abs(value - currentSnap);
            if (currentDiff < minDiff) {
                minDiff = currentDiff;
                closestSnap = currentSnap;
            }
        }
        return closestSnap;
    }

    public void setPosition(float x, float y) {
        this.posX = x;
        this.posY = y;
        this.updateGeometry();
    }
    public void setPosX(float posX) {
        this.posX = posX;
        this.updateGeometry();
    }
    public void setPosY(float posY) {
        this.posY = posY;
        this.updateGeometry();
    }
    public void setSize(float w, float h) {
        this.height = Math.max(1f, h);
        float handleDiameter = this.height;
        float minWidth = handleDiameter + (this.padding * 2f);
        this.width = Math.max(minWidth, w);
        this.updateGeometry();
    }
    public void setWidth(float width) {
        this.setSize(width, this.height);
    }
    public void setHeight(float height) {
        this.setSize(this.width, height);
    }
    public void setMinValue(float minValue) {
        if (minValue >= this.maxValue) {
            this.maxValue = minValue + 1e-6f;
        }
        this.minValue = minValue;
        this.valueRange = this.maxValue - this.minValue;
        this.setValueInternal(this.currentValue, false);
    }
    public void setMaxValue(float maxValue) {
        if (maxValue <= this.minValue) {
            this.minValue = maxValue - 1e-6f;
        }
        this.maxValue = maxValue;
        this.valueRange = this.maxValue - this.minValue;
        this.setValueInternal(this.currentValue, false);
    }
    public void setValue(float value) {
        float targetValue = MathUtils.clamp(value, this.minValue, this.maxValue);
        this.setValueInternal(targetValue, true);
    }
    public void setValueSilent(float value) {
        float targetValue = MathUtils.clamp(value, this.minValue, this.maxValue);
        this.setValueInternal(targetValue, false);
    }
    public void setSnapEnabled(boolean snapEnabled) {
        this.snapEnabled = snapEnabled;
        if (this.snapEnabled) {
            this.setValue(this.findClosestSnapPoint(this.currentValue));
        }
    }
    public void setSnapPoints(List<Float> snapPoints) {
        if (snapPoints == null) {
            this.snapPoints = new ArrayList<>();
            this.snapEnabled = false;
        } else {
            this.snapPoints = new ArrayList<>(snapPoints);
            Collections.sort(this.snapPoints);
            if (this.snapEnabled) {
                this.setValue(this.findClosestSnapPoint(this.currentValue));
            }
        }
    }
    public void setPadding(float padding) {
        this.padding = Math.max(0, padding);
        this.updateGeometry();
    }
    public void setTrackHeightMultiplier(float multiplier) {
        this.trackHeightMultiplier = MathUtils.clamp(multiplier, 0.0f, 1.0f);
        this.updateGeometry();
    }
    public void setOnValueChanged(Consumer<Float> onValueChanged) {
        this.onValueChanged = onValueChanged != null ? onValueChanged : (value) -> {};
    }
    public void setTrackColor(Color trackColor) {
        this.trackColor = (trackColor != null) ? trackColor.cpy() : DEFAULT_TRACK_COLOR.cpy();
        this.trackRenderer.setFillColor(this.trackColor);
    }
    public void setFillColor(Color fillColor) {
        this.fillColor = (fillColor != null) ? fillColor.cpy() : DEFAULT_FILL_COLOR.cpy();
        this.fillRenderer.setFillColor(this.fillColor);
        this.updateVisuals(this.interactionButton.getCurrentState() != Button.ButtonState.NORMAL);
    }
    public void setHandleColor(Color handleColor) {
        this.handleColor = (handleColor != null) ? handleColor.cpy() : DEFAULT_HANDLE_COLOR.cpy();
        this.updateVisuals(this.interactionButton.getCurrentState() != Button.ButtonState.NORMAL);
    }
    public void setHandleHoverColor(Color handleHoverColor) {
        this.handleHoverColor = (handleHoverColor != null) ? handleHoverColor.cpy() : DEFAULT_HANDLE_HOVER_COLOR.cpy();
        this.updateVisuals(this.interactionButton.getCurrentState() != Button.ButtonState.NORMAL);
    }
    public void setHandleDragColor(Color handleDragColor) {
        this.handleDragColor = (handleDragColor != null) ? handleDragColor.cpy() : DEFAULT_HANDLE_DRAG_COLOR.cpy();
        this.updateVisuals(this.interactionButton.getCurrentState() != Button.ButtonState.NORMAL);
    }

    public float getPosX() { return this.posX; }
    public float getPosY() { return this.posY; }
    public float getWidth() { return this.width; }
    public float getHeight() { return this.height; }
    public float getMinValue() { return this.minValue; }
    public float getMaxValue() { return this.maxValue; }
    public float getValue() { return this.currentValue; }
    public boolean isSnapEnabled() { return this.snapEnabled; }
    public List<Float> getSnapPoints() { return new ArrayList<>(this.snapPoints); }
    public Consumer<Float> getOnValueChanged() { return this.onValueChanged; }
    public float getPadding() { return this.padding; }
    public float getTrackHeightMultiplier() { return this.trackHeightMultiplier; }
    public Color getTrackColor() { return this.trackColor.cpy(); }
    public Color getFillColor() { return this.fillColor.cpy(); }
    public Color getHandleColor() { return this.handleColor.cpy(); }
    public Color getHandleHoverColor() { return this.handleHoverColor.cpy(); }
    public Color getHandleDragColor() { return this.handleDragColor.cpy(); }
    public BoxRenderer getTrackRenderer() { return this.trackRenderer; }
    public BoxRenderer getFillRenderer() { return this.fillRenderer; }
    public CircleRenderer getHandleRenderer() { return this.handleRenderer; }
    public Button getInteractionButton() { return this.interactionButton; }
}
