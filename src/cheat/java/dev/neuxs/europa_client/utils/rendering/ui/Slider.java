package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
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
    private final InputManager inputManager;
    private final BoxRenderer trackRenderer;
    private final BoxRenderer fillRenderer;
    private final CircleRenderer handleRenderer;

    private float minValue;
    private float maxValue;
    private float currentValue;
    private float valueRange;
    private boolean snapEnabled;
    private List<Float> snapPoints;
    private boolean isDragging;
    private Consumer<Float> onValueChanged;
    private float padding;
    private float trackHeightMultiplier;

    private Color trackColor;
    private Color fillColor; // Overrides the base class fillColor for the fill part
    private Color handleColor;
    private Color handleHoverColor;
    private Color handleDragColor;

    private static final Color defaultTrackColor = ColorUtils.color(80, 80, 80, 255);
    private static final Color defaultFillColor = ColorUtils.color(160, 160, 160, 255);
    private static final Color defaultHandleColor = ColorUtils.color(240, 240, 240, 255);
    private static final Color defaultHandleHoverColor = ColorUtils.color(255, 255, 255, 255);
    private static final Color defaultHandleDragColor = ColorUtils.color(220, 220, 220, 255);

    public Slider() {
        super();
        this.inputManager = InputManager.getInstance(); // Keep for direct mouse position access during drag
        this.trackRenderer = new BoxRenderer();
        this.fillRenderer = new BoxRenderer();
        this.handleRenderer = new CircleRenderer();

        this.setRenderType(RenderUtil.RenderType.SHAPE);
        this.setShapeType(ShapeRenderer.ShapeType.Filled);

        this.minValue = 0f;
        this.maxValue = 100f;
        this.valueRange = this.maxValue - this.minValue;
        this.snapEnabled = false;
        this.snapPoints = new ArrayList<>();
        this.isDragging = false;
        this.onValueChanged = (value) -> {};
        this.padding = 3f;
        this.trackHeightMultiplier = 0.4f;

        this.trackColor = defaultTrackColor.cpy();
        this.fillColor = defaultFillColor.cpy(); // Assign to specific fillColor field
        this.handleColor = defaultHandleColor.cpy();
        this.handleHoverColor = defaultHandleHoverColor.cpy();
        this.handleDragColor = defaultHandleDragColor.cpy();

        // Configure sub-renderers
        this.trackRenderer.setFillColor(this.trackColor);
        this.trackRenderer.setBorder(false);
        this.fillRenderer.setFillColor(this.fillColor);
        this.fillRenderer.setBorder(false);
        this.handleRenderer.setFillColor(this.handleColor);
        this.handleRenderer.setBorder(false);

        // Set initial value and update geometry
        this.setValueInternal(MathUtils.clamp(50f, this.minValue, this.maxValue), false);
        this.updateGeometry();
    }

    @Override
    public void update(Viewport viewport) {
        super.update(viewport); // Handles hover state

        Vector2 mousePos = inputManager.getMousePos(); // Use InputManager for real-time position during drag
        boolean mouseOver = isMouseOver(viewport);
        boolean mousePressed = InputManager.isMouseButtonDown(Input.Buttons.LEFT);
        boolean isCurrentlyPressed = getState() == State.PRESSED;

        // Start dragging
        if (!this.isDragging && mouseOver && mousePressed && isCurrentlyPressed) {
            this.isDragging = true;
            this.updateValueFromMouse(mousePos.x, viewport);
        }

        // Stop dragging
        if (this.isDragging && !mousePressed) {
            this.isDragging = false;
        }

        // Update value while dragging
        if (this.isDragging) {
            this.updateValueFromMouse(mousePos.x, viewport);
        }

        this.updateVisuals(mouseOver);
    }

    private void updateValueFromMouse(float mouseX, Viewport viewport) {
        if (!isMouseOver(viewport) && !isDragging) return; // Only update if dragging or initially clicked on

        float sliderHeight = getHeight();
        if (sliderHeight <= 0) return;
        float handleRadius = sliderHeight / 2f;

        float sliderX = getPosX();
        float sliderWidth = getWidth();

        float handleCenterX_Min = sliderX + handleRadius + this.padding;
        float handleCenterX_Max = sliderX + sliderWidth - handleRadius - this.padding;
        float usableTrackWidth = Math.max(0, handleCenterX_Max - handleCenterX_Min);

        float newValue;
        if (usableTrackWidth > 0) {
            float clampedMouseX = MathUtils.clamp(mouseX, handleCenterX_Min, handleCenterX_Max);
            float ratio = (clampedMouseX - handleCenterX_Min) / usableTrackWidth;
            newValue = this.minValue + ratio * this.valueRange;
        } else {
            // If track is too small, snap to min or max based on mouse position relative to center
            float overallCenterX = sliderX + sliderWidth / 2f;
            newValue = (mouseX < overallCenterX) ? this.minValue : this.maxValue;
        }

        // Apply snapping if enabled
        if (this.snapEnabled && this.snapPoints != null && !this.snapPoints.isEmpty()) {
            newValue = this.findClosestSnapPoint(newValue);
        } else {
            newValue = MathUtils.clamp(newValue, this.minValue, this.maxValue);
        }

        this.setValueInternal(newValue, true);
    }

    private void setValueInternal(float newValue, boolean triggerCallback) {
        newValue = MathUtils.clamp(newValue, this.minValue, this.maxValue);
        // Use a small epsilon for float comparison
        if (Math.abs(newValue - this.currentValue) > 1e-6f) {
            this.currentValue = newValue;
            this.updateHandlePosition();
            this.updateFillWidth();
            if (triggerCallback && this.onValueChanged != null) {
                this.onValueChanged.accept(this.currentValue);
            }
        } else if (!triggerCallback) {
            // Ensure visuals are updated even if value doesn't change (e.g., on initial set or range change)
            this.updateHandlePosition();
            this.updateFillWidth();
        }
    }

    private void updateVisuals(boolean mouseOver) {
        Color targetHandleColor = this.handleColor;
        if (this.isDragging || getState() == State.PRESSED) {
            targetHandleColor = this.handleDragColor;
        } else if (mouseOver || getState() == State.HOVERED) {
            targetHandleColor = this.handleHoverColor;
        }
        this.handleRenderer.setFillColor(targetHandleColor);
        this.trackRenderer.setFillColor(this.trackColor);
        this.fillRenderer.setFillColor(this.fillColor);
    }

    @Override
    public void renderShape(Viewport viewport, ShapeRenderer shapeRenderer) {
        // Update visual state based on interaction
        updateVisuals(isMouseOver(viewport));

        // Render the components
        this.trackRenderer.renderShape(viewport, shapeRenderer);
        this.fillRenderer.renderShape(viewport, shapeRenderer);
        this.handleRenderer.renderShape(viewport, shapeRenderer);
    }

    @Override
    public void renderSprite(Viewport viewport, SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        // Slider does not render sprites directly
    }

    private void updateGeometry() {
        float sliderX = getPosX();
        float sliderY = getPosY();
        float sliderWidth = getWidth();
        float sliderHeight = getHeight();

        if (sliderWidth <= 0 || sliderHeight <= 0) return;

        float handleRadius = sliderHeight / 2f;
        this.handleRenderer.setRadius(handleRadius);
        // Center the handle vertically within the slider's height
        float handleCenterY = sliderY + handleRadius;
        this.handleRenderer.setPosY(handleCenterY); // Position is center for circle

        // Calculate track geometry
        float trackHeight = Math.max(1f, sliderHeight * this.trackHeightMultiplier);
        float trackRadius = trackHeight / 2f;
        // Position track vertically centered with handle
        float trackY = handleCenterY - trackRadius;

        this.trackRenderer.setPos(sliderX, trackY);
        this.trackRenderer.setSize(sliderWidth, trackHeight);
        this.trackRenderer.setBorderRadius(trackRadius);

        // Calculate fill geometry
        this.fillRenderer.setPos(sliderX, trackY);
        this.fillRenderer.setHeight(trackHeight);
        this.fillRenderer.setBorderRadius(trackRadius);

        // Update positions based on the current value
        this.updateHandlePosition();
        this.updateFillWidth();
    }

    private void updateHandlePosition() {
        float sliderX = getPosX();
        float sliderWidth = getWidth();
        float handleRadius = this.handleRenderer.getRadius();

        // Check for invalid state
        if (handleRadius <= 0 || this.valueRange <= 1e-9f || sliderWidth <= (handleRadius * 2 + padding * 2)) {
            // Default to min position if invalid dimensions or range
            this.handleRenderer.setPosX(sliderX + handleRadius + this.padding);
            return;
        }

        // Calculate the valid range for the handle's center X coordinate
        float handleCenterX_Min = sliderX + handleRadius + this.padding;
        float handleCenterX_Max = sliderX + sliderWidth - handleRadius - this.padding;
        float usableTrackWidth = Math.max(0, handleCenterX_Max - handleCenterX_Min);

        // Calculate the position ratio based on the current value
        float ratio = (this.currentValue - this.minValue) / this.valueRange;
        // Calculate the target center X for the handle
        float handleTargetCenterX = handleCenterX_Min + ratio * usableTrackWidth;

        // Set the handle's position (center X)
        this.handleRenderer.setPosX(handleTargetCenterX);
    }

    private void updateFillWidth() {
        float fillStartX = this.trackRenderer.getPosX();
        // The fill should end at the center of the handle
        float fillEndX = this.handleRenderer.getPosX();

        // Width is the difference between the handle center and the track start
        float fillWidth = Math.max(0, fillEndX - fillStartX);

        // Update the fill renderer's position and width
        this.fillRenderer.setPosX(fillStartX);
        this.fillRenderer.setWidth(fillWidth);
    }

    private float findClosestSnapPoint(float value) {
        if (this.snapPoints == null || this.snapPoints.isEmpty()) {
            return value; // No snapping if list is null or empty
        }

        // Clamp snap points to the slider's min/max range first
        List<Float> clampedSnaps = new ArrayList<>();
        for (float p : this.snapPoints) {
            clampedSnaps.add(MathUtils.clamp(p, this.minValue, this.maxValue));
        }

        // Find the snap point closest to the current value
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

    // --- Overridden Setters to trigger geometry updates ---

    @Override
    public void setPos(float x, float y, int z) {
        super.setPos(x, y, z);
        this.updateGeometry();
    }

    @Override
    public void setPos(float x, float y) {
        super.setPos(x, y);
        this.updateGeometry();
    }

    @Override
    public void setPosX(float x) {
        super.setPosX(x);
        this.updateGeometry();
    }

    @Override
    public void setPosY(float y) {
        super.setPosY(y);
        this.updateGeometry();
    }

    @Override
    public void setSize(float w, float h) {
        float effectiveHeight = Math.max(1f, h); // Ensure minimum height
        // Ensure width is at least enough for the handle diameter + padding
        float handleDiameter = effectiveHeight;
        float minWidth = handleDiameter + (this.padding * 2f);
        float effectiveWidth = Math.max(minWidth, w);

        super.setSize(effectiveWidth, effectiveHeight);
        this.updateGeometry();
    }

    @Override
    public void setWidth(float width) {
        this.setSize(width, getHeight()); // Use existing height
    }

    @Override
    public void setHeight(float height) {
        this.setSize(getWidth(), height); // Use existing width
    }

    // --- Slider Specific Setters ---

    public void setMinValue(float minValue) {
        // Ensure min is strictly less than max
        if (minValue >= this.maxValue) {
            this.maxValue = minValue + 1e-6f; // Add a small epsilon if they are equal or inverted
        }
        this.minValue = minValue;
        this.valueRange = this.maxValue - this.minValue;
        // Re-clamp and update visuals/position without triggering callback
        this.setValueInternal(this.currentValue, false);
    }

    public void setMaxValue(float maxValue) {
        // Ensure max is strictly greater than min
        if (maxValue <= this.minValue) {
            this.minValue = maxValue - 1e-6f; // Subtract a small epsilon if they are equal or inverted
        }
        this.maxValue = maxValue;
        this.valueRange = this.maxValue - this.minValue;
        // Re-clamp and update visuals/position without triggering callback
        this.setValueInternal(this.currentValue, false);
    }

    public void setValue(float value) {
        float targetValue = value;
        // Apply snapping if enabled when setting value externally
        if (this.snapEnabled && this.snapPoints != null && !this.snapPoints.isEmpty()) {
            targetValue = this.findClosestSnapPoint(targetValue);
        }
        targetValue = MathUtils.clamp(targetValue, this.minValue, this.maxValue);
        this.setValueInternal(targetValue, true); // Trigger callback
    }

    public void setValueSilent(float value) {
        float targetValue = value;
        // Apply snapping if enabled when setting value externally
        if (this.snapEnabled && this.snapPoints != null && !this.snapPoints.isEmpty()) {
            targetValue = this.findClosestSnapPoint(targetValue);
        }
        targetValue = MathUtils.clamp(targetValue, this.minValue, this.maxValue);
        this.setValueInternal(targetValue, false); // Do not trigger callback
    }

    public void setSnapEnabled(boolean snapEnabled) {
        this.snapEnabled = snapEnabled;
        // If enabling snapping, immediately snap the current value
        if (this.snapEnabled) {
            // Use setValue to trigger potential callback if value changes due to snapping
            this.setValue(this.currentValue);
        }
    }

    public void setSnapPoints(List<Float> snapPoints) {
        if (snapPoints == null) {
            this.snapPoints = new ArrayList<>();
            // Consider if disabling snap is desired when list is set to null/empty
            // this.snapEnabled = false;
        } else {
            this.snapPoints = new ArrayList<>(snapPoints);
            Collections.sort(this.snapPoints); // Keep snap points sorted
            // If snap is enabled, re-snap the current value to the new points
            if (this.snapEnabled) {
                this.setValue(this.currentValue); // Use setValue to trigger potential callback
            }
        }
    }

    public void setPadding(float padding) {
        this.padding = Math.max(0, padding);
        this.updateGeometry(); // Padding affects geometry
    }

    public void setTrackHeightMultiplier(float multiplier) {
        this.trackHeightMultiplier = MathUtils.clamp(multiplier, 0.0f, 1.0f);
        this.updateGeometry(); // Track height affects geometry
    }

    public void setOnValueChanged(Consumer<Float> onValueChanged) {
        this.onValueChanged = onValueChanged != null ? onValueChanged : (value) -> {};
    }

    public void setTrackColor(Color trackColor) {
        this.trackColor = (trackColor != null) ? trackColor.cpy() : defaultTrackColor.cpy();
        this.trackRenderer.setFillColor(this.trackColor);
    }

    // Override setFillColor to affect the fill part, not the base renderer color
    @Override
    public void setFillColor(Color fillColor) {
        this.fillColor = (fillColor != null) ? fillColor.cpy() : defaultFillColor.cpy();
        this.fillRenderer.setFillColor(this.fillColor);
        // Note: super.setFillColor() is not called, as the base fill color isn't directly used.
    }

    public void setHandleColor(Color handleColor) {
        this.handleColor = (handleColor != null) ? handleColor.cpy() : defaultHandleColor.cpy();
        this.updateVisuals(isMouseOver(null)); // Update visuals immediately
    }

    public void setHandleHoverColor(Color handleHoverColor) {
        this.handleHoverColor = (handleHoverColor != null) ? handleHoverColor.cpy() : defaultHandleHoverColor.cpy();
        this.updateVisuals(isMouseOver(null)); // Update visuals immediately
    }

    public void setHandleDragColor(Color handleDragColor) {
        this.handleDragColor = (handleDragColor != null) ? handleDragColor.cpy() : defaultHandleDragColor.cpy();
        this.updateVisuals(isMouseOver(null)); // Update visuals immediately
    }

    // --- Getters ---

    public float getMinValue() { return this.minValue; }
    public float getMaxValue() { return this.maxValue; }
    public float getValue() { return this.currentValue; }
    public boolean isSnapEnabled() { return this.snapEnabled; }
    public List<Float> getSnapPoints() { return new ArrayList<>(this.snapPoints); } // Return copy
    public Consumer<Float> getOnValueChanged() { return this.onValueChanged; }
    public float getPadding() { return this.padding; }
    public float getTrackHeightMultiplier() { return this.trackHeightMultiplier; }
    public Color getTrackColor() { return this.trackColor.cpy(); }

    // Override getFillColor to return the specific fill color
    @Override
    public Color getFillColor() {
        return this.fillColor.cpy();
    }

    public Color getHandleColor() { return this.handleColor.cpy(); }
    public Color getHandleHoverColor() { return this.handleHoverColor.cpy(); }
    public Color getHandleDragColor() { return this.handleDragColor.cpy(); }

    // Provide access to sub-renderers if needed externally
    public BoxRenderer getTrackRenderer() { return this.trackRenderer; }
    public BoxRenderer getFillRenderer() { return this.fillRenderer; }
    public CircleRenderer getHandleRenderer() { return this.handleRenderer; }
}