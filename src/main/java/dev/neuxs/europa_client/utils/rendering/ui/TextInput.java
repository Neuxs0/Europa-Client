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
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;
import dev.neuxs.europa_client.utils.rendering.Renderer;
import dev.neuxs.europa_client.utils.rendering.SdfRenderer;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class TextInput extends Renderer implements InputProcessor {
    private static final float DEFAULT_PADDING = 5f;
    private static final float DEFAULT_BLINK_INTERVAL = 0.5f;
    private static final float DEFAULT_CURSOR_WIDTH = 1f;
    private static final float DEFAULT_CURSOR_X_OFFSET = 1f;
    private static final float KEY_REPEAT_DELAY = 0.5f;
    private static final float KEY_REPEAT_INTERVAL = 0.05f;
    private static final float KEY_RELEASE_GRACE = 0.075f;
    private static final float MAX_KEY_REPEAT_DELTA = 0.1f;

    private static final Color DEFAULT_FILL_COLOR = ColorUtils.color(50, 50, 50, 255);
    private static final Color DEFAULT_TEXT_COLOR = ColorUtils.color(255, 255, 255, 255);
    private static final Color DEFAULT_FOCUSED_BORDER = ColorUtils.color(20, 20, 20, 255);
    private static final Color DEFAULT_UNFOCUSED_BORDER = ColorUtils.color(20, 20, 20, 255);
    private static final Color DEFAULT_CURSOR_COLOR = ColorUtils.color(240, 240, 240, 255);
    private static final Color DEFAULT_PLACEHOLDER_COLOR = ColorUtils.color(150, 150, 150, 255);
    private static final Color DEFAULT_SELECTION_COLOR = ColorUtils.color(70, 120, 215, 160);

    private final BoxRenderer boxRenderer;
    private final TextRenderer textRenderer;
    private final TextRenderer placeholderRenderer;
    private final GlyphLayout glyphLayout;
    private final Vector2 mousePos;
    private final StringBuilder text;

    private String placeholder;
    private int cursorPosition;
    private int selectionAnchor;
    private boolean selectingWithKeyboard;
    private boolean focused;
    private long blinkStartNanos;
    private boolean showCursor;
    private float blinkInterval;
    private float cursorWidth;
    private float padding;
    private int repeatingKeycode;
    private float keyRepeatTimer;
    private long lastKeyRepeatNanos;
    private boolean keyReleasePending;
    private float keyReleaseTimer;

    private Consumer<String> onEnterPressed;
    private Consumer<String> onFocusLost;
    private Consumer<String> onTextChanged;

    private Color focusedBorderColor;
    private Color unfocusedBorderColor;
    private Color cursorColor;
    private Color placeholderColor;
    private Color selectionColor;

    public TextInput() {
        this.boxRenderer = new BoxRenderer();
        this.textRenderer = new TextRenderer();
        this.placeholderRenderer = new TextRenderer();
        this.glyphLayout = new GlyphLayout();
        this.mousePos = new Vector2();
        this.text = new StringBuilder();

        this.placeholder = "";
        this.cursorPosition = 0;
        this.selectionAnchor = 0;
        this.selectingWithKeyboard = false;
        this.focused = false;
        this.blinkStartNanos = 0L;
        this.showCursor = false;
        this.blinkInterval = DEFAULT_BLINK_INTERVAL;
        this.cursorWidth = DEFAULT_CURSOR_WIDTH;
        this.padding = DEFAULT_PADDING;
        this.repeatingKeycode = Input.Keys.UNKNOWN;
        this.keyRepeatTimer = 0f;
        this.lastKeyRepeatNanos = 0L;
        this.keyReleasePending = false;
        this.keyReleaseTimer = 0f;

        this.onEnterPressed = (value) -> {};
        this.onFocusLost = (value) -> {};
        this.onTextChanged = (value) -> {};

        this.focusedBorderColor = DEFAULT_FOCUSED_BORDER.cpy();
        this.unfocusedBorderColor = DEFAULT_UNFOCUSED_BORDER.cpy();
        this.cursorColor = DEFAULT_CURSOR_COLOR.cpy();
        this.placeholderColor = DEFAULT_PLACEHOLDER_COLOR.cpy();
        this.selectionColor = DEFAULT_SELECTION_COLOR.cpy();

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

        updateCursorBlink(delta);
        updateKeyRepeat();
        syncBoxRenderer();
    }

    @Override
    public void renderShape(Viewport viewport, ShapeRenderer shapeRenderer) {
        updateKeyRepeat();
        syncBoxRenderer();
        boxRenderer.renderShape(viewport, shapeRenderer);

        if (focused && hasSelection()) {
            renderSelection(shapeRenderer);
        }

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

    public boolean handleTouchDown(float worldX, float worldY, int button) {
        if (button != Input.Buttons.LEFT) {
            return false;
        }

        boolean mouseOver = isMouseOver(worldX, worldY);
        setFocus(mouseOver);
        if (mouseOver) {
            moveCursorTo(getCursorPositionFromX(worldX), false);
        }
        return mouseOver;
    }

    private void updateCursorBlink(float delta) {
        if (!focused) {
            showCursor = false;
            blinkStartNanos = 0L;
            return;
        }

        long now = System.nanoTime();
        if (blinkStartNanos == 0L) {
            blinkStartNanos = now;
            showCursor = true;
            return;
        }

        long intervalNanos = Math.max(1L, (long) (blinkInterval * 1_000_000_000L));
        long elapsedIntervals = (now - blinkStartNanos) / intervalNanos;
        showCursor = elapsedIntervals % 2L == 0L;
    }

    private void resetCursorBlink() {
        blinkStartNanos = System.nanoTime();
        showCursor = focused;
    }

    private void updateKeyRepeat() {
        if (!focused || repeatingKeycode == Input.Keys.UNKNOWN) {
            stopKeyRepeat();
            return;
        }

        long now = System.nanoTime();
        if (lastKeyRepeatNanos == 0L) {
            lastKeyRepeatNanos = now;
            return;
        }

        float delta = Math.min((now - lastKeyRepeatNanos) / 1_000_000_000f, MAX_KEY_REPEAT_DELTA);
        lastKeyRepeatNanos = now;
        updateKeyRepeat(delta);
    }

    private void updateKeyRepeat(float delta) {
        if (!focused || repeatingKeycode == Input.Keys.UNKNOWN) {
            stopKeyRepeat();
            return;
        }

        keyRepeatTimer -= Math.max(0f, delta);
        if (keyReleasePending) {
            keyReleaseTimer -= Math.max(0f, delta);
            if (keyReleaseTimer <= 0f) {
                stopKeyRepeat();
                return;
            }
        }

        while (keyRepeatTimer <= 0f) {
            handleEditingKeyDown(repeatingKeycode);
            keyRepeatTimer += KEY_REPEAT_INTERVAL;
        }
    }

    private void startKeyRepeat(int keycode) {
        if (!isRepeatableKey(keycode)) {
            stopKeyRepeat();
            return;
        }

        if (repeatingKeycode != keycode) {
            repeatingKeycode = keycode;
            keyRepeatTimer = KEY_REPEAT_DELAY;
            lastKeyRepeatNanos = System.nanoTime();
        }
        keyReleasePending = false;
        keyReleaseTimer = 0f;
    }

    private void stopKeyRepeat() {
        repeatingKeycode = Input.Keys.UNKNOWN;
        keyRepeatTimer = 0f;
        lastKeyRepeatNanos = 0L;
        keyReleasePending = false;
        keyReleaseTimer = 0f;
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

        float cursorX = getTextStartX() + getCursorTextWidth() + DEFAULT_CURSOR_X_OFFSET;
        float cursorY = getPosY() + padding;
        float cursorHeight = Math.max(0f, getHeight() - padding * 2f);

        if (cursorHeight <= 0f) {
            return;
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(cursorColor);
        shapeRenderer.rectLine(cursorX, cursorY, cursorX, cursorY + cursorHeight, cursorWidth);
        shapeRenderer.end();
    }

    private void renderSelection(ShapeRenderer shapeRenderer) {
        if (shapeRenderer == null || textRenderer.getFont() == null || text.isEmpty()) {
            return;
        }

        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();
        float startX = getTextStartX() + getTextWidth(0, selectionStart);
        float endX = getTextStartX() + getTextWidth(0, selectionEnd);
        float selectionY = getPosY() + padding;
        float selectionHeight = Math.max(0f, getHeight() - padding * 2f);

        if (selectionHeight <= 0f || endX <= startX) {
            return;
        }

        SdfRenderer.get().drawRoundedRect(startX, selectionY, endX - startX, selectionHeight, 0f, selectionColor);
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

    private float getTextWidth(int startInclusive, int endExclusive) {
        BitmapFont font = textRenderer.getFont();
        if (font == null || startInclusive >= endExclusive || text.isEmpty()) {
            return 0f;
        }

        int start = MathUtils.clamp(startInclusive, 0, text.length());
        int end = MathUtils.clamp(endExclusive, start, text.length());
        glyphLayout.setText(font, text.substring(start, end));
        return glyphLayout.width;
    }

    private int getCursorPositionFromX(float worldX) {
        BitmapFont font = textRenderer.getFont();
        if (font == null || text.isEmpty()) {
            return 0;
        }

        float localX = Math.max(0f, worldX - getTextStartX());
        int closestPosition = 0;
        float closestDistance = Float.MAX_VALUE;

        for (int i = 0; i <= text.length(); i++) {
            float cursorX = getTextWidth(0, i);
            float distance = Math.abs(localX - cursorX);
            if (distance < closestDistance) {
                closestPosition = i;
                closestDistance = distance;
            }
        }

        return closestPosition;
    }

    private void insertText(String value) {
        if (value == null || value.isEmpty()) {
            return;
        }

        value = sanitizeInputText(value);
        if (value.isEmpty()) {
            return;
        }

        deleteSelection();
        text.insert(cursorPosition, value);
        cursorPosition += value.length();
        clearSelection();
        notifyTextChanged();
    }

    private void deletePreviousCharacter() {
        if (deleteSelection()) {
            return;
        }

        if (cursorPosition <= 0) {
            return;
        }

        text.deleteCharAt(cursorPosition - 1);
        cursorPosition--;
        clearSelection();
        notifyTextChanged();
    }

    private void deleteNextCharacter() {
        if (deleteSelection()) {
            return;
        }

        if (cursorPosition >= text.length()) {
            return;
        }

        text.deleteCharAt(cursorPosition);
        clearSelection();
        notifyTextChanged();
    }

    private void deletePreviousWord() {
        if (deleteSelection()) {
            return;
        }

        int previousWordPosition = findPreviousWordBoundary(cursorPosition);
        if (previousWordPosition == cursorPosition) {
            return;
        }

        text.delete(previousWordPosition, cursorPosition);
        cursorPosition = previousWordPosition;
        clearSelection();
        notifyTextChanged();
    }

    private void deleteNextWord() {
        if (deleteSelection()) {
            return;
        }

        int nextWordPosition = findNextWordBoundary(cursorPosition);
        if (nextWordPosition == cursorPosition) {
            return;
        }

        text.delete(cursorPosition, nextWordPosition);
        clearSelection();
        notifyTextChanged();
    }

    private boolean deleteSelection() {
        if (!hasSelection()) {
            return false;
        }

        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();
        text.delete(selectionStart, selectionEnd);
        cursorPosition = selectionStart;
        clearSelection();
        notifyTextChanged();
        return true;
    }

    private String sanitizeInputText(String value) {
        StringBuilder sanitized = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character == '\r' || character == '\n') {
                sanitized.append(' ');
            } else if (!Character.isISOControl(character) && character != 127) {
                sanitized.append(character);
            }
        }
        return sanitized.toString();
    }

    private void copySelectionToClipboard() {
        if (hasSelection()) {
            Gdx.app.getClipboard().setContents(text.substring(getSelectionStart(), getSelectionEnd()));
        }
    }

    private void cutSelectionToClipboard() {
        if (!hasSelection()) {
            return;
        }

        copySelectionToClipboard();
        deleteSelection();
    }

    private void selectAll() {
        selectionAnchor = 0;
        cursorPosition = text.length();
    }

    private boolean hasSelection() {
        return selectionAnchor != cursorPosition;
    }

    private int getSelectionStart() {
        return Math.min(selectionAnchor, cursorPosition);
    }

    private int getSelectionEnd() {
        return Math.max(selectionAnchor, cursorPosition);
    }

    private void clearSelection() {
        selectionAnchor = cursorPosition;
        selectingWithKeyboard = false;
    }

    private void moveCursorTo(int position, boolean selecting) {
        int oldPosition = cursorPosition;
        cursorPosition = MathUtils.clamp(position, 0, text.length());
        if (selecting) {
            if (!selectingWithKeyboard) {
                selectionAnchor = oldPosition;
                selectingWithKeyboard = true;
            }
        } else {
            clearSelection();
        }
        resetCursorBlink();
    }

    private int findPreviousWordBoundary(int position) {
        int index = MathUtils.clamp(position, 0, text.length());
        while (index > 0 && Character.isWhitespace(text.charAt(index - 1))) {
            index--;
        }
        while (index > 0 && !Character.isWhitespace(text.charAt(index - 1))) {
            index--;
        }
        return index;
    }

    private int findNextWordBoundary(int position) {
        int index = MathUtils.clamp(position, 0, text.length());
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        while (index < text.length() && !Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        return index;
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

        if (keycode == repeatingKeycode && isRepeatableKey(keycode)) {
            if (keyReleasePending) {
                boolean handled = handleEditingKeyDown(keycode);
                if (handled) {
                    keyRepeatTimer = KEY_REPEAT_DELAY;
                    lastKeyRepeatNanos = System.nanoTime();
                }
                keyReleasePending = false;
                keyReleaseTimer = 0f;
                return handled;
            }

            return true;
        }

        boolean handled = handleEditingKeyDown(keycode);
        if (handled) {
            startKeyRepeat(keycode);
        }

        return handled;
    }

    private boolean handleEditingKeyDown(int keycode) {
        if (isControlPressed()) {
            return handleControlKeyDown(keycode);
        }

        boolean shiftPressed = isShiftPressed();
        switch (keycode) {
            case Input.Keys.BACKSPACE -> deletePreviousCharacter();
            case Input.Keys.FORWARD_DEL -> deleteNextCharacter();
            case Input.Keys.LEFT -> moveCursorTo(!shiftPressed && hasSelection() ? getSelectionStart() : cursorPosition - 1, shiftPressed);
            case Input.Keys.RIGHT -> moveCursorTo(!shiftPressed && hasSelection() ? getSelectionEnd() : cursorPosition + 1, shiftPressed);
            case Input.Keys.HOME -> moveCursorTo(0, shiftPressed);
            case Input.Keys.END -> moveCursorTo(text.length(), shiftPressed);
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
        boolean shiftPressed = isShiftPressed();
        switch (keycode) {
            case Input.Keys.A -> selectAll();
            case Input.Keys.C -> copySelectionToClipboard();
            case Input.Keys.X -> cutSelectionToClipboard();
            case Input.Keys.V -> insertText(Gdx.app.getClipboard().getContents());
            case Input.Keys.BACKSPACE -> deletePreviousWord();
            case Input.Keys.FORWARD_DEL -> deleteNextWord();
            case Input.Keys.LEFT -> moveCursorTo(!shiftPressed && hasSelection() ? getSelectionStart() : findPreviousWordBoundary(cursorPosition), shiftPressed);
            case Input.Keys.RIGHT -> moveCursorTo(!shiftPressed && hasSelection() ? getSelectionEnd() : findNextWordBoundary(cursorPosition), shiftPressed);
            case Input.Keys.HOME -> moveCursorTo(0, shiftPressed);
            case Input.Keys.END -> moveCursorTo(text.length(), shiftPressed);
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

    private boolean isShiftPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }

    private boolean isRepeatableKey(int keycode) {
        return switch (keycode) {
            case Input.Keys.BACKSPACE,
                 Input.Keys.FORWARD_DEL,
                 Input.Keys.LEFT,
                 Input.Keys.RIGHT,
                 Input.Keys.HOME,
                 Input.Keys.END -> true;
            default -> false;
        };
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == repeatingKeycode && isRepeatableKey(keycode)) {
            keyReleasePending = true;
            keyReleaseTimer = KEY_RELEASE_GRACE;
        }
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
        clearSelection();
        notifyTextChanged();
    }

    public void setTextSilent(String text) {
        this.text.setLength(0);
        if (text != null) {
            this.text.append(text);
        }
        cursorPosition = MathUtils.clamp(cursorPosition, 0, this.text.length());
        clearSelection();
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
            clearSelection();
            resetCursorBlink();
        } else {
            clearSelection();
            stopKeyRepeat();
            showCursor = false;
            blinkStartNanos = 0L;
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
        clearSelection();
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

    public void setSelectionColor(Color color) {
        selectionColor = color != null ? color.cpy() : DEFAULT_SELECTION_COLOR.cpy();
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

    public Color getSelectionColor() {
        return selectionColor.cpy();
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
