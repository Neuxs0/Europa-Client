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
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.managers.InputManager;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Button {
    private final BoxRenderer boxRenderer;
    private final TextRenderer textRenderer;

    private Consumer<Button> onClick;
    private Consumer<Button> onHoverEnter;
    private Consumer<Button> onHoverExit;
    private Color normalFillColor;
    private Color hoverFillColor;
    private Color pressedFillColor;
    private Color normalBorderColor;
    private Color hoverBorderColor;
    private Color pressedBorderColor;
    private ButtonState currentState;
    private boolean wasPressed;
    private final Vector2 mousePos = new Vector2();
    private final Color DEFAULT_NORMAL_FILL = ColorUtils.color(50, 50, 50, 255);
    private final Color DEFAULT_HOVER_FILL = ColorUtils.color(70, 70, 70, 255);
    private final Color DEFAULT_PRESSED_FILL = ColorUtils.color(90, 90, 90, 255);
    private final Color DEFAULT_NORMAL_BORDER = ColorUtils.color(20, 20, 20, 255);
    private final Color DEFAULT_HOVER_BORDER = ColorUtils.color(20, 20, 20, 255);
    private final Color DEFAULT_PRESSED_BORDER = ColorUtils.color(20, 20, 20, 255);

    public enum ButtonState {
        NORMAL, HOVERED, PRESSED
    }

    public Button() {
        this.boxRenderer = new BoxRenderer();
        this.textRenderer = new TextRenderer();
        this.onClick = (button) -> {};
        this.onHoverEnter = (button) -> {};
        this.onHoverExit = (button) -> {};
        this.normalFillColor = DEFAULT_NORMAL_FILL.cpy();
        this.hoverFillColor = DEFAULT_HOVER_FILL.cpy();
        this.pressedFillColor = DEFAULT_PRESSED_FILL.cpy();
        this.normalBorderColor = DEFAULT_NORMAL_BORDER.cpy();
        this.hoverBorderColor = DEFAULT_HOVER_BORDER.cpy();
        this.pressedBorderColor = DEFAULT_PRESSED_BORDER.cpy();
        this.currentState = ButtonState.NORMAL;
        this.wasPressed = false;
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
            if (InputManager.isMouseButtonDown(Input.Buttons.LEFT)) {
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
        } else if (currentState == ButtonState.NORMAL && (previousState == ButtonState.HOVERED || previousState == ButtonState.PRESSED) && onHoverExit != null) {
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

    public void renderText(SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        float textX = boxRenderer.getPosX()  + boxRenderer.getWidth() / 2f;
        float textY = boxRenderer.getPosY() + boxRenderer.getHeight() / 2f;
        textRenderer.setPosition(textX, textY);
        textRenderer.render(spriteBatch, glyphLayout);
    }

    public BoxRenderer getBoxRenderer() {
        return this.boxRenderer;
    }
    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }
    public ButtonState getCurrentState() { return currentState; }
    public Consumer<Button> getOnClick() { return onClick; }
    public void setOnClick(Consumer<Button> onClick) { this.onClick = onClick != null ? onClick : (button) -> {}; }
    public Consumer<Button> getOnHoverEnter() { return onHoverEnter; }
    public void setOnHoverEnter(Consumer<Button> onHoverEnter) { this.onHoverEnter = onHoverEnter != null ? onHoverEnter : (button) -> {}; }
    public Consumer<Button> getOnHoverExit() { return onHoverExit; }
    public void setOnHoverExit(Consumer<Button> onHoverExit) { this.onHoverExit = onHoverExit != null ? onHoverExit : (button) -> {}; }
    public Color getNormalFillColor() { return normalFillColor.cpy(); }
    public void setNormalFillColor(Color color) { this.normalFillColor = (color != null) ? color.cpy() : DEFAULT_NORMAL_FILL.cpy(); }
    public Color getNormalBorderColor() { return normalBorderColor.cpy(); }
    public void setNormalBorderColor(Color color) { this.normalBorderColor = (color != null) ? color.cpy() : DEFAULT_NORMAL_BORDER.cpy(); }
    public Color getHoverFillColor() { return hoverFillColor.cpy(); }
    public void setHoverFillColor(Color color) { this.hoverFillColor = (color != null) ? color.cpy() : DEFAULT_HOVER_FILL.cpy(); }
    public Color getHoverBorderColor() { return hoverBorderColor.cpy(); }
    public void setHoverBorderColor(Color color) { this.hoverBorderColor = (color != null) ? color.cpy() : DEFAULT_HOVER_BORDER.cpy(); }
    public Color getPressedFillColor() { return pressedFillColor.cpy(); }
    public void setPressedFillColor(Color color) { this.pressedFillColor = (color != null) ? color.cpy() : DEFAULT_PRESSED_FILL.cpy(); }
    public Color getPressedBorderColor() { return pressedBorderColor.cpy(); }
    public void setPressedBorderColor(Color color) { this.pressedBorderColor = (color != null) ? color.cpy() : DEFAULT_PRESSED_BORDER.cpy(); }
    public float getX() { return boxRenderer.getPosX(); }
    public float getY() { return boxRenderer.getPosY(); }
    public void setPosition(float x, float y) { boxRenderer.setPosition(x, y); }
    public float getWidth() { return boxRenderer.getWidth(); }
    public float getHeight() { return boxRenderer.getHeight(); }
    public void setSize(float width, float height) { boxRenderer.setSize(width, height); }
    public void setWidth(float width) { boxRenderer.setWidth(width); }
    public void setHeight(float height) { boxRenderer.setHeight(height); }
}
