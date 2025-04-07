package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.managers.InputManager;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;
import dev.neuxs.europa_client.managers.font.FontManager;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Button {
    private final BoxRenderer boxRenderer;
    private final TextRenderer textRenderer;
    private final FontManager fontManager;

    private Consumer<Button> onClick;
    private Consumer<Button> onHoverEnter;
    private Consumer<Button> onHoverExit;
    private ButtonState currentState;
    private boolean wasPressed;
    private int triggerButton;
    private final Vector2 mousePos = new Vector2();
    private Color normalFillColor;
    private Color hoverFillColor;
    private Color pressedFillColor;
    private Color normalBorderColor;
    private Color hoverBorderColor;
    private Color pressedBorderColor;
    private static final Color defaultNormalFill = ColorUtils.color(50, 50, 50, 255);
    private static final Color defaultHoverFill = ColorUtils.color(70, 70, 70, 255);
    private static final Color defaultPressedFill = ColorUtils.color(90, 90, 90, 255);
    private static final Color defaultNormalBorder = ColorUtils.color(20, 20, 20, 255);
    private static final Color defaultHoverBorder = ColorUtils.color(20, 20, 20, 255);
    private static final Color defaultPressedBorder = ColorUtils.color(20, 20, 20, 255);
    private static final int defaultTriggerButton = Input.Buttons.LEFT;
    public enum ButtonState {
        NORMAL, HOVERED, PRESSED
    }

    public Button() {
        this.boxRenderer = new BoxRenderer();
        this.textRenderer = new TextRenderer();
        this.fontManager = FontManager.getInstance();
        this.onClick = (button) -> {};
        this.onHoverEnter = (button) -> {};
        this.onHoverExit = (button) -> {};
        this.normalFillColor = defaultNormalFill.cpy();
        this.hoverFillColor = defaultHoverFill.cpy();
        this.pressedFillColor = defaultPressedFill.cpy();
        this.normalBorderColor = defaultNormalBorder.cpy();
        this.hoverBorderColor = defaultHoverBorder.cpy();
        this.pressedBorderColor = defaultPressedBorder.cpy();
        this.currentState = ButtonState.NORMAL;
        this.wasPressed = false;
        this.triggerButton = defaultTriggerButton;
        this.boxRenderer.setFillColor(this.normalFillColor);
        this.boxRenderer.setBorderColor(this.normalBorderColor);
        this.boxRenderer.setBorderEnabled(true);
        this.textRenderer.setText("");
        this.textRenderer.setAlignment(Align.center);
    }

    public void update(Viewport viewport) {
        ButtonState previousState = currentState;
        mousePos.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mousePos);

        boolean mouseOver = mousePos.x >= boxRenderer.getPosX() && mousePos.x <= boxRenderer.getPosX() + boxRenderer.getWidth() &&
                mousePos.y >= boxRenderer.getPosY() && mousePos.y <= boxRenderer.getPosY() + boxRenderer.getHeight();

        if (mouseOver) {
            if (InputManager.isMouseButtonDown(this.triggerButton)) {
                currentState = ButtonState.PRESSED;
                if (previousState != ButtonState.PRESSED) {
                    wasPressed = true;
                }
            } else {
                if (previousState == ButtonState.PRESSED && wasPressed && onClick != null) {
                    onClick.accept(this);
                }
                currentState = ButtonState.HOVERED;
                wasPressed = false;
            }
        } else {
            currentState = ButtonState.NORMAL;
            wasPressed = false;
        }

        if (currentState == ButtonState.HOVERED && previousState == ButtonState.NORMAL && onHoverEnter != null) {
            onHoverEnter.accept(this);
        }
        else if (currentState == ButtonState.NORMAL && (previousState == ButtonState.HOVERED || previousState == ButtonState.PRESSED) && onHoverExit != null) {
            onHoverExit.accept(this);
        }
    }

    public void renderShape(ShapeRenderer shapeRenderer, Viewport viewport) {
        update(viewport);

        switch (currentState) {
            case HOVERED:
                boxRenderer.setFillColor(hoverFillColor);
                boxRenderer.setBorderColor(hoverBorderColor);
                break;
            case PRESSED:
                boxRenderer.setFillColor(pressedFillColor);
                boxRenderer.setBorderColor(pressedBorderColor);
                break;
            case NORMAL:
            default:
                boxRenderer.setFillColor(normalFillColor);
                boxRenderer.setBorderColor(normalBorderColor);
                break;
        }

        boxRenderer.render(shapeRenderer);
    }

    public void renderText(SpriteBatch spriteBatch, GlyphLayout glyphLayout, Viewport viewport) {
        if (textRenderer.getText() == null || textRenderer.getText().isEmpty() || textRenderer.getFont() == null) {
            return;
        }

        float textX = boxRenderer.getPosX() + (boxRenderer.getWidth() - textRenderer.getWidth(viewport)) / 2f;
        float textY = boxRenderer.getPosY() + (boxRenderer.getHeight() - textRenderer.getHeight(viewport)) / 2f;

        textRenderer.setPosition(textX, textY);
        textRenderer.render(spriteBatch, glyphLayout, viewport);
    }


    public void setOnClick(Consumer<Button> onClick) {
        this.onClick = onClick != null ? onClick : (button) -> {};
    }
    public void setOnHoverEnter(Consumer<Button> onHoverEnter) {
        this.onHoverEnter = onHoverEnter != null ? onHoverEnter : (button) -> {};
    }
    public void setOnHoverExit(Consumer<Button> onHoverExit) {
        this.onHoverExit = onHoverExit != null ? onHoverExit : (button) -> {};
    }
    public void setTriggerButton(int triggerButton) {
        this.triggerButton = triggerButton;
    }
    public void setNormalFillColor(Color color) {
        this.normalFillColor = (color != null) ? color.cpy() : defaultNormalFill.cpy();
    }
    public void setNormalBorderColor(Color color) {
        this.normalBorderColor = (color != null) ? color.cpy() : defaultNormalBorder.cpy();
    }
    public void setHoverFillColor(Color color) {
        this.hoverFillColor = (color != null) ? color.cpy() : defaultHoverFill.cpy();
    }
    public void setHoverBorderColor(Color color) {
        this.hoverBorderColor = (color != null) ? color.cpy() : defaultHoverBorder.cpy();
    }
    public void setPressedFillColor(Color color) {
        this.pressedFillColor = (color != null) ? color.cpy() : defaultPressedFill.cpy();
    }
    public void setPressedBorderColor(Color color) {
        this.pressedBorderColor = (color != null) ? color.cpy() : defaultPressedBorder.cpy();
    }
    public void setPosition(float x, float y) {
        boxRenderer.setPosition(x, y);
    }
    public void setSize(float width, float height) {
        boxRenderer.setSize(width, height);
    }
    public void setWidth(float width) {
        boxRenderer.setWidth(width);
    }
    public void setHeight(float height) {
        boxRenderer.setHeight(height);
    }

    public BoxRenderer getBoxRenderer() {
        return this.boxRenderer;
    }
    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }
    public ButtonState getCurrentState() {
        return currentState;
    }
    public Consumer<Button> getOnClick() {
        return onClick;
    }
    public Consumer<Button> getOnHoverEnter() {
        return onHoverEnter;
    }
    public Consumer<Button> getOnHoverExit() {
        return onHoverExit;
    }
    public int getTriggerButton() {
        return triggerButton;
    }
    public Color getNormalFillColor() { return normalFillColor.cpy(); }
    public Color getNormalBorderColor() { return normalBorderColor.cpy(); }
    public Color getHoverFillColor() { return hoverFillColor.cpy(); }
    public Color getHoverBorderColor() { return hoverBorderColor.cpy(); }
    public Color getPressedFillColor() { return pressedFillColor.cpy(); }
    public Color getPressedBorderColor() { return pressedBorderColor.cpy(); }

    public float getX() { return boxRenderer.getPosX(); }
    public float getY() { return boxRenderer.getPosY(); }
    public float getWidth() { return boxRenderer.getWidth(); }
    public float getHeight() { return boxRenderer.getHeight(); }
}
