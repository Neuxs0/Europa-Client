package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.CircleRenderer;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Toggle {
    private final Button interactionButton;
    private final BoxRenderer backgroundRenderer;
    private final CircleRenderer handleRenderer;
    private boolean isToggled;
    private Consumer<Boolean> onValueChanged;
    private float padding = 2f;
    private Color backgroundOffColor;
    private Color backgroundOnColor;
    private Color handleOffColor;
    private Color handleOnColor;
    private Color backgroundHoverColorMultiplier = new Color(1.1f, 1.1f, 1.1f, 1f);
    private Color handleHoverColorMultiplier = new Color(1.1f, 1.1f, 1.1f, 1f);
    private float handleCurrentX;
    private Color currentBackgroundColor;
    private Color currentHandleColor;
    private final Color DEFAULT_BG_OFF = ColorUtils.color(80, 80, 80, 255);
    private final Color DEFAULT_BG_ON = ColorUtils.color(70, 130, 200, 255);
    private final Color DEFAULT_HANDLE_OFF = ColorUtils.color(150, 150, 150, 255);
    private final Color DEFAULT_HANDLE_ON = ColorUtils.color(220, 220, 220, 255);

    public Toggle() {
        this.interactionButton = new Button();
        this.backgroundRenderer = new BoxRenderer();
        this.handleRenderer = new CircleRenderer();
        this.isToggled = false;
        this.onValueChanged = (state) -> {};
        this.backgroundOffColor = DEFAULT_BG_OFF.cpy();
        this.backgroundOnColor = DEFAULT_BG_ON.cpy();
        this.handleOffColor = DEFAULT_HANDLE_OFF.cpy();
        this.handleOnColor = DEFAULT_HANDLE_ON.cpy();

        Color transparent = new Color(0, 0, 0, 0);
        this.interactionButton.setNormalFillColor(transparent);
        this.interactionButton.setHoverFillColor(transparent);
        this.interactionButton.setPressedFillColor(transparent);
        this.interactionButton.getBoxRenderer().setBorderEnabled(false);

        this.backgroundRenderer.setBorderEnabled(false);
        this.handleRenderer.setBorderEnabled(false);

        updateTargetPositionAndColors();
        this.handleCurrentX = calculateTargetX();
        this.currentBackgroundColor = this.isToggled ? this.backgroundOnColor : this.backgroundOffColor;
        this.currentHandleColor = this.isToggled ? this.handleOnColor : this.handleOffColor;
        updateComponentPositions();

        this.interactionButton.setOnClick(button -> toggleState());
    }

    public void toggleState() {
        this.isToggled = !this.isToggled;
        updateTargetPositionAndColors();
        if (this.onValueChanged != null) {
            this.onValueChanged.accept(this.isToggled);
        }
    }

    private float calculateTargetX() {
        float handleRadius = handleRenderer.getRadius();
        float trackWidth = backgroundRenderer.getWidth();
        float trackX = backgroundRenderer.getPosX();

        return this.isToggled
                ? trackX + trackWidth - handleRadius - padding
                : trackX + handleRadius + padding;
    }

    private void updateTargetPositionAndColors() {
        this.handleCurrentX = calculateTargetX();
        handleRenderer.setPosX(this.handleCurrentX);

        Color targetBgColor = isToggled ? backgroundOnColor : backgroundOffColor;
        Color targetHandleColor = isToggled ? handleOnColor : handleOffColor;

        if (interactionButton.getCurrentState() == Button.ButtonState.HOVERED ||
                interactionButton.getCurrentState() == Button.ButtonState.PRESSED) {
            targetBgColor = targetBgColor.cpy().mul(backgroundHoverColorMultiplier);
            targetHandleColor = targetHandleColor.cpy().mul(handleHoverColorMultiplier);
        }

        currentBackgroundColor = targetBgColor;
        currentHandleColor = targetHandleColor;
    }

    private void updateComponentPositions() {
        float x = backgroundRenderer.getPosX();
        float y = backgroundRenderer.getPosY();
        float width = backgroundRenderer.getWidth();
        float height = backgroundRenderer.getHeight();

        interactionButton.setPosition(x, y);
        interactionButton.setSize(width, height);

        float handleRadius = Math.max(0, (height / 2f) - padding);
        handleRenderer.setRadius(handleRadius);
        handleRenderer.setPosY(y + height / 2f);

        this.handleCurrentX = calculateTargetX();
        handleRenderer.setPosX(this.handleCurrentX);

        backgroundRenderer.setBorderRadius(height / 2f);
    }


    public void update(Viewport viewport) {
        interactionButton.update(viewport);
        updateTargetPositionAndColors();
    }

    public void render(ShapeRenderer shapeRenderer) {
        backgroundRenderer.setFillColor(currentBackgroundColor);
        backgroundRenderer.render(shapeRenderer);

        handleRenderer.setFillColor(currentHandleColor);
        handleRenderer.render(shapeRenderer);
    }

    public void setPosition(float x, float y) {
        backgroundRenderer.setPosition(x, y);
        updateComponentPositions();
    }
    public void setSize(float width, float height) {
        backgroundRenderer.setSize(width, height);
        if (width < height * 2) {
            width = height * 2;
            backgroundRenderer.setWidth(width);
        }
        updateComponentPositions();
    }
    public void setEnabled(boolean enabled) {
        if (this.isToggled != enabled) {
            this.isToggled = enabled;
            updateTargetPositionAndColors();
            if (this.onValueChanged != null) {
                this.onValueChanged.accept(this.isToggled);
            }
        } else {
            updateTargetPositionAndColors();
        }
    }
    public void setPadding(float padding) {
        this.padding = Math.max(0, padding);
        updateComponentPositions();
    }
    public boolean isToggled() {
        return isToggled;
    }
    public Consumer<Boolean> getOnValueChanged() {
        return onValueChanged;
    }
    public void setOnValueChanged(Consumer<Boolean> onValueChanged) {
        this.onValueChanged = onValueChanged != null ? onValueChanged : (state) -> {};
    }
    public void setBackgroundOffColor(Color color) {
        this.backgroundOffColor = (color != null) ? color.cpy() : DEFAULT_BG_OFF.cpy();
    }
    public void setBackgroundOnColor(Color color) {
        this.backgroundOnColor = (color != null) ? color.cpy() : DEFAULT_BG_ON.cpy();
    }
    public void setHandleOffColor(Color color) {
        this.handleOffColor = (color != null) ? color.cpy() : DEFAULT_HANDLE_OFF.cpy();
    }
    public void setHandleOnColor(Color color) {
        this.handleOnColor = (color != null) ? color.cpy() : DEFAULT_HANDLE_ON.cpy();
    }
    public void setBackgroundHoverColorMultiplier(Color color) {
        this.backgroundHoverColorMultiplier = (color != null) ? color.cpy() : new Color(1.1f, 1.1f, 1.1f, 1f);
    }
    public void setHandleHoverColorMultiplier(Color color) {
        this.handleHoverColorMultiplier = (color != null) ? color.cpy() : new Color(1.1f, 1.1f, 1.1f, 1f);
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
        return backgroundRenderer.getPosX();
    }
    public float getY() {
        return backgroundRenderer.getPosY();
    }
    public float getWidth() {
        return backgroundRenderer.getWidth();
    }
    public float getHeight() {
        return backgroundRenderer.getHeight();
    }
}
