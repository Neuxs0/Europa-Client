package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
import dev.neuxs.europa_client.utils.rendering.RenderUtil;
import dev.neuxs.europa_client.utils.rendering.Renderer;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class TextInput extends Renderer implements InputProcessor {
    private static final float DEFAULT_PADDING = 5f;
    private static final float DEFAULT_BLINK_INTERVAL = 0.5f;
    private static final float DEFAULT_CURSOR_WIDTH = 1f;

    private static final Color DEFAULT_FILL_COLOR = ColorUtils.color(50, 50, 50, 255);
    private static final Color DEFAULT_TEXT_COLOR = ColorUtils.color(255, 255, 255, 255);
    private static final Color DEFAULT_FOCUSED_BORDER = ColorUtils.color(20, 20, 20, 255);
    private static final Color DEFAULT_UNFOCUSED_BORDER = ColorUtils.color(20, 20, 20, 255);
    private static final Color DEFAULT_CURSOR_COLOR = ColorUtils.color(240, 240, 240, 255);
    private static final Color DEFAULT_PLACEHOLDER_COLOR = ColorUtils.color(150, 150, 150, 255);

    private final BoxRenderer boxRenderer;
    private final TextRenderer textRenderer;
    private final TextRenderer placeholderRenderer;
    private final GlyphLayout glyphLayout;
    private final Vector2 mousePos;
    private final StringBuilder text;

    private String placeholder;
    private int cursorPosition;
    private boolean focused;
    private float blinkTimer;
    private boolean showCursor;
    private float blinkInterval;
    private float cursorWidth;
    private float padding;

    private Consumer<String> onEnterPressed;
    private Consumer<String> onFocusLost;
    private Consumer<String> onTextChanged;

    private Color focusedBorderColor;
    private Color unfocusedBorderColor;
    private Color cursorColor;
    private Color placeholderColor;

    public TextInput() {
        this.boxRenderer = new BoxRenderer();
        this.textRenderer = new TextRenderer();
        this.placeholderRenderer = new TextRenderer();
        this.glyphLayout = new GlyphLayout();
        this.mousePos = new Vector2();
        this.text = new StringBuilder();

        this.placeholder = "";
        this.cursorPosition = 0;
        this.focused = false;
        this.blinkTimer = 0f;
        this.showCursor = false;
        this.blinkInterval = DEFAULT_BLINK_INTERVAL;
        this.cursorWidth = DEFAULT_CURSOR_WIDTH;
        this.padding = DEFAULT_PADDING;

        this.onEnterPressed = (value) -> {};
        this.onFocusLost = (value) -> {};
        this.onTextChanged = (value) -> {};

        this.focusedBorderColor = DEFAULT_FOCUSED_BORDER.cpy();
        this.unfocusedBorderColor = DEFAULT_UNFOCUSED_BORDER.cpy();
        this.cursorColor = DEFAULT_CURSOR_COLOR.cpy();
        this.placeholderColor = DEFAULT_PLACEHOLDER_COLOR.cpy();

        setRenderType(RenderUtil.RenderType.SHAPE_SPRITE);
        setShapeType(ShapeRenderer.ShapeType.Filled);
        setFillColor(DEFAULT_FILL_COLOR);
        setTextColor(DEFAULT_TEXT_COLOR);
        setBorder(true);
        setBorderWidth(1f);
        setBorderColor(unfocusedBorderColor);

        boxRenderer.setBorder(true);
        placeholderRenderer.setTextColor(placeholderColor);
    }

    @Override
    public void update(Viewport viewport) {
        update(viewport, Gdx.graphics.getDeltaTime());
    }

    public void update(Viewport viewport, float delta) {
        if (viewport == null) {
            return;
        }

        updateMousePosition(viewport);

        if (InputManager.isFirstFrameMouseButtonDown(Input.Buttons.LEFT)) {
            setFocus(isMouseOver(mousePos.x, mousePos.y));
        }

        updateCursorBlink(delta);
        syncBoxRenderer();
    }

    @Override
    public void renderShape(Viewport viewport, ShapeRenderer shapeRenderer) {
        syncBoxRenderer();
        boxRenderer.renderShape(viewport, shapeRenderer);

        if (focused && showCursor) {
            renderCursor(shapeRenderer);
        }
    }

    @Override
    public void renderSprite(Viewport viewport, SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        if (text.isEmpty() && !placeholder.isEmpty() && !focused) {
            renderPlaceholder(viewport, spriteBatch, glyphLayout);
        } else {
            renderText(viewport, spriteBatch, glyphLayout);
        }
    }

    private void updateMousePosition(Viewport viewport) {
        mousePos.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mousePos);
    }

    private boolean isMouseOver(float x, float y) {
        return x >= getPosX() && x <= getPosX() + getWidth()
                && y >= getPosY() && y <= getPosY() + getHeight();
    }

    private void updateCursorBlink(float delta) {
        if (!focused) {
            showCursor = false;
            blinkTimer = 0f;
            return;
        }

        blinkTimer += Math.max(0f, delta);
        while (blinkTimer >= blinkInterval) {
            blinkTimer -= blinkInterval;
            showCursor = !showCursor;
        }
    }

    private void resetCursorBlink() {
        blinkTimer = 0f;
        showCursor = focused;
    }

    private void syncBoxRenderer() {
        boxRenderer.setPos(getPos());
        boxRenderer.setSize(getSize());
        boxRenderer.setFillColor(getFillColor());
        boxRenderer.setBorder(isBorder());
        boxRenderer.setBorderWidth(getBorderWidth());
        boxRenderer.setBorderRadius(getBorderRadius());
        boxRenderer.setBorderColor(focused ? focusedBorderColor : unfocusedBorderColor);
    }

    private void renderCursor(ShapeRenderer shapeRenderer) {
        if (shapeRenderer == null || textRenderer.getFont() == null) {
            return;
        }

        float cursorX = getTextStartX() + getCursorTextWidth();
        float cursorY = getPosY() + padding;
        float cursorHeight = Math.max(0f, getHeight() - padding * 2f);

        if (cursorHeight <= 0f) {
            return;
        }

        shapeRenderer.setColor(cursorColor);
        shapeRenderer.rectLine(cursorX, cursorY, cursorX, cursorY + cursorHeight, cursorWidth);
    }

    private void renderText(Viewport viewport, SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        if (text.isEmpty()) {
            return;
        }

        textRenderer.setText(text.toString());
        textRenderer.setTextColor(getTextColor());
        textRenderer.setPos(getTextStartX(), getTextY(viewport, textRenderer));
        textRenderer.renderSprite(viewport, spriteBatch, glyphLayout);
    }

    private void renderPlaceholder(Viewport viewport, SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        placeholderRenderer.setText(placeholder);
        placeholderRenderer.setTextColor(placeholderColor);
        placeholderRenderer.setPos(getTextStartX(), getTextY(viewport, placeholderRenderer));
        placeholderRenderer.renderSprite(viewport, spriteBatch, glyphLayout);
    }

    private float getTextStartX() {
        return getPosX() + padding;
    }

    private float getTextY(Viewport viewport, TextRenderer renderer) {
        return getPosY() + (getHeight() - renderer.getTextHeight(viewport)) / 2f;
    }

    private float getCursorTextWidth() {
        BitmapFont font = textRenderer.getFont();
        if (font == null || cursorPosition <= 0 || text.isEmpty()) {
            return 0f;
        }

        glyphLayout.setText(font, text.substring(0, cursorPosition));
        return glyphLayout.width;
    }

    private void insertText(String value) {
        if (value == null || value.isEmpty()) {
            return;
        }

        text.insert(cursorPosition, value);
        cursorPosition += value.length();
        notifyTextChanged();
    }

    private void deletePreviousCharacter() {
        if (cursorPosition <= 0) {
            return;
        }

        text.deleteCharAt(cursorPosition - 1);
        cursorPosition--;
        notifyTextChanged();
    }

    private void deleteNextCharacter() {
        if (cursorPosition >= text.length()) {
            return;
        }

        text.deleteCharAt(cursorPosition);
        notifyTextChanged();
    }

    private void notifyTextChanged() {
        resetCursorBlink();
        if (onTextChanged != null) {
            onTextChanged.accept(text.toString());
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (!focused) {
            return false;
        }

        if (isControlPressed()) {
            return handleControlKeyDown(keycode);
        }

        switch (keycode) {
            case Input.Keys.BACKSPACE -> deletePreviousCharacter();
            case Input.Keys.FORWARD_DEL -> deleteNextCharacter();
            case Input.Keys.LEFT -> cursorPosition = Math.max(0, cursorPosition - 1);
            case Input.Keys.RIGHT -> cursorPosition = Math.min(text.length(), cursorPosition + 1);
            case Input.Keys.HOME -> cursorPosition = 0;
            case Input.Keys.END -> cursorPosition = text.length();
            case Input.Keys.ENTER, Input.Keys.NUMPAD_ENTER -> {
                if (onEnterPressed != null) {
                    onEnterPressed.accept(text.toString());
                }
            }
            case Input.Keys.ESCAPE -> setFocus(false);
            default -> {
                return false;
            }
        }

        resetCursorBlink();
        return true;
    }

    private boolean handleControlKeyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.A -> cursorPosition = text.length();
            case Input.Keys.V -> insertText(Gdx.app.getClipboard().getContents());
            default -> {
                return false;
            }
        }

        resetCursorBlink();
        return true;
    }

    private boolean isControlPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
    }

    @Override
    public boolean keyUp(int keycode) {
        return focused;
    }

    @Override
    public boolean keyTyped(char character) {
        if (!focused || Character.isISOControl(character) || character == 127) {
            return false;
        }

        insertText(String.valueOf(character));
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public void setPos(Vector3 pos) {
        super.setPos(pos);
        syncBoxRenderer();
    }

    @Override
    public void setPos(Vector2 pos) {
        super.setPos(pos);
        syncBoxRenderer();
    }

    public void setPosition(float x, float y) {
        setPos(x, y);
    }

    @Override
    public void setPos(float x, float y, int z) {
        super.setPos(x, y, z);
        syncBoxRenderer();
    }

    @Override
    public void setPos(float x, float y) {
        super.setPos(x, y);
        syncBoxRenderer();
    }

    @Override
    public void setSize(Vector2 size) {
        super.setSize(size);
        syncBoxRenderer();
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(Math.max(0f, width), Math.max(1f, height));
        syncBoxRenderer();
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(Math.max(0f, width));
        syncBoxRenderer();
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(Math.max(1f, height));
        syncBoxRenderer();
    }

    @Override
    public void setFillColor(Color color) {
        super.setFillColor(color != null ? color : DEFAULT_FILL_COLOR);
        syncBoxRenderer();
    }

    @Override
    public void setBorder(boolean enabled) {
        super.setBorder(enabled);
        syncBoxRenderer();
    }

    @Override
    public void setBorderWidth(float width) {
        super.setBorderWidth(width);
        syncBoxRenderer();
    }

    @Override
    public void setBorderRadius(float radius) {
        super.setBorderRadius(radius);
        syncBoxRenderer();
    }

    @Override
    public void setTextColor(Color color) {
        super.setTextColor(color != null ? color : DEFAULT_TEXT_COLOR);
        textRenderer.setTextColor(getTextColor());
    }

    @Override
    public String getText() {
        return text.toString();
    }

    @Override
    public void setText(String text) {
        this.text.setLength(0);
        if (text != null) {
            this.text.append(text);
        }
        cursorPosition = MathUtils.clamp(cursorPosition, 0, this.text.length());
        notifyTextChanged();
    }

    public void setTextSilent(String text) {
        this.text.setLength(0);
        if (text != null) {
            this.text.append(text);
        }
        cursorPosition = MathUtils.clamp(cursorPosition, 0, this.text.length());
        resetCursorBlink();
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder != null ? placeholder : "";
        placeholderRenderer.setText(this.placeholder);
    }

    public float getX() {
        return getPosX();
    }

    public float getY() {
        return getPosY();
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocus(boolean focus) {
        if (focused == focus) {
            return;
        }

        focused = focus;
        if (focused) {
            cursorPosition = text.length();
            resetCursorBlink();
        } else {
            showCursor = false;
            blinkTimer = 0f;
            if (onFocusLost != null) {
                onFocusLost.accept(text.toString());
            }
        }

        syncBoxRenderer();
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(int cursorPosition) {
        this.cursorPosition = MathUtils.clamp(cursorPosition, 0, text.length());
        resetCursorBlink();
    }

    public float getPadding() {
        return padding;
    }

    public void setPadding(float padding) {
        this.padding = Math.max(0f, padding);
    }

    public float getBlinkInterval() {
        return blinkInterval;
    }

    public void setBlinkInterval(float blinkInterval) {
        this.blinkInterval = Math.max(0.05f, blinkInterval);
        resetCursorBlink();
    }

    public float getCursorWidth() {
        return cursorWidth;
    }

    public void setCursorWidth(float cursorWidth) {
        this.cursorWidth = Math.max(0.1f, cursorWidth);
    }

    public void setOnEnterPressed(Consumer<String> onEnterPressed) {
        this.onEnterPressed = onEnterPressed != null ? onEnterPressed : (value) -> {};
    }

    public void setOnFocusLost(Consumer<String> onFocusLost) {
        this.onFocusLost = onFocusLost != null ? onFocusLost : (value) -> {};
    }

    public void setOnTextChanged(Consumer<String> onTextChanged) {
        this.onTextChanged = onTextChanged != null ? onTextChanged : (value) -> {};
    }

    public void setFont(BitmapFont font) {
        textRenderer.setFont(font);
        placeholderRenderer.setFont(font);
    }

    public void setFont(String fontName) {
        textRenderer.setFont(fontName);
        placeholderRenderer.setFont(fontName);
    }

    public void setFocusedBorderColor(Color color) {
        focusedBorderColor = color != null ? color.cpy() : DEFAULT_FOCUSED_BORDER.cpy();
        syncBoxRenderer();
    }

    public void setUnfocusedBorderColor(Color color) {
        unfocusedBorderColor = color != null ? color.cpy() : DEFAULT_UNFOCUSED_BORDER.cpy();
        syncBoxRenderer();
    }

    public void setCursorColor(Color color) {
        cursorColor = color != null ? color.cpy() : DEFAULT_CURSOR_COLOR.cpy();
    }

    public void setPlaceholderColor(Color color) {
        placeholderColor = color != null ? color.cpy() : DEFAULT_PLACEHOLDER_COLOR.cpy();
        placeholderRenderer.setTextColor(placeholderColor);
    }

    public Consumer<String> getOnEnterPressed() {
        return onEnterPressed;
    }

    public Consumer<String> getOnFocusLost() {
        return onFocusLost;
    }

    public Consumer<String> getOnTextChanged() {
        return onTextChanged;
    }

    public Color getFocusedBorderColor() {
        return focusedBorderColor.cpy();
    }

    public Color getUnfocusedBorderColor() {
        return unfocusedBorderColor.cpy();
    }

    public Color getCursorColor() {
        return cursorColor.cpy();
    }

    public Color getPlaceholderColor() {
        return placeholderColor.cpy();
    }

    public BoxRenderer getBoxRenderer() {
        return boxRenderer;
    }

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    public TextRenderer getPlaceholderRenderer() {
        return placeholderRenderer;
    }
}
