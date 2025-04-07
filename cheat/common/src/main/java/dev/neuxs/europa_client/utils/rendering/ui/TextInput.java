package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
public class TextInput implements InputProcessor {

    private final BoxRenderer boxRenderer;
    private final TextRenderer textRenderer;
    private final TextRenderer placeholderRenderer;
    private final FontManager fontManager;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    private StringBuilder text = new StringBuilder();
    private String placeholder = "";
    private int cursorPosition = 0;
    private boolean focused = false;
    private float blinkTimer = 0f;
    private boolean showCursor = true;
    private final float blinkInterval = 0.5f;
    private final float cursorWidth = 1f;

    private Consumer<String> onEnterPressed = (text) -> {};
    private Consumer<String> onFocusLost = (text) -> {};
    private Consumer<String> onTextChanged = (text) -> {};

    private final Vector2 mousePos = new Vector2();
    private InputProcessor previousInputProcessor = null;

    private Color focusedBorderColor;
    private Color unfocusedBorderColor;
    private Color cursorColor;
    private Color placeholderColor;
    private static final Color defaultFocusedBorder = ColorUtils.color(20, 20, 20, 255);
    private static final Color defaultUnfocusedBorder = ColorUtils.color(20, 20, 20, 255);
    private static final Color defaultCursorColor = ColorUtils.color(240, 240, 240, 255);
    private static final Color defaultPlaceholderColor = ColorUtils.color(150, 150, 150, 255);

    public TextInput() {
        this.boxRenderer = new BoxRenderer();
        this.textRenderer = new TextRenderer();
        this.placeholderRenderer = new TextRenderer();
        this.fontManager = FontManager.getInstance();
        this.boxRenderer.setBorderEnabled(true);
        this.boxRenderer.setFillColor(ColorUtils.color(50, 50, 50, 255));
        this.unfocusedBorderColor = defaultUnfocusedBorder.cpy();
        this.focusedBorderColor = defaultFocusedBorder.cpy();
        this.cursorColor = defaultCursorColor.cpy();
        this.placeholderColor = defaultPlaceholderColor.cpy();
        this.boxRenderer.setBorderColor(this.unfocusedBorderColor);
        this.textRenderer.setColor(ColorUtils.color(255, 255, 255, 255));
        this.placeholderRenderer.setColor(this.placeholderColor);
        this.placeholderRenderer.setFont(this.textRenderer.getFont());
    }

    public void update(Viewport viewport, float delta) {
        mousePos.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mousePos);

        boolean mouseOver = mousePos.x >= boxRenderer.getPosX() && mousePos.x <= boxRenderer.getPosX() + boxRenderer.getWidth() &&
                mousePos.y >= boxRenderer.getPosY() && mousePos.y <= boxRenderer.getPosY() + boxRenderer.getHeight();

        if (InputManager.isMouseButtonDown(Input.Buttons.LEFT)) {
            boolean previouslyFocused = this.focused;
            this.focused = mouseOver;

            if (previouslyFocused && !this.focused) {
                if (onFocusLost != null) {
                    onFocusLost.accept(text.toString());
                }
                if (Gdx.input.getInputProcessor() == this) {
                    Gdx.input.setInputProcessor(previousInputProcessor);
                }
            } else if (!previouslyFocused && this.focused) {
                previousInputProcessor = Gdx.input.getInputProcessor();
                Gdx.input.setInputProcessor(this);
                cursorPosition = text.length();
                blinkTimer = 0f;
                showCursor = true;
            } else if (this.focused) {
                cursorPosition = text.length();
                blinkTimer = 0f;
                showCursor = true;
            }
        }

        boxRenderer.setBorderColor(focused ? focusedBorderColor : unfocusedBorderColor);

        if (focused) {
            blinkTimer += delta;
            if (blinkTimer >= blinkInterval) {
                blinkTimer -= blinkInterval;
                showCursor = !showCursor;
            }
        } else {
            showCursor = false;
            if (Gdx.input.getInputProcessor() == this) {
                Gdx.input.setInputProcessor(previousInputProcessor);
            }
        }
    }

    public void renderShape(ShapeRenderer shapeRenderer, Viewport viewport) {
        boxRenderer.render(shapeRenderer);

        if (focused && showCursor) {
            BitmapFont font = textRenderer.getFont();
            if (font != null) {
                shapeRenderer.setColor(cursorColor);

                float textX = boxRenderer.getPosX() + 5f;
                float cursorXOffset = 0;
                if (cursorPosition > 0 && !text.isEmpty()) {
                    glyphLayout.setText(font, text.substring(0, cursorPosition));
                    cursorXOffset = glyphLayout.width;
                }

                float cursorRenderX = textX + cursorXOffset;
                float cursorRenderY = boxRenderer.getPosY() + 4f;
                float cursorHeight = boxRenderer.getHeight() - 8f;

                shapeRenderer.rectLine(cursorRenderX, cursorRenderY, cursorRenderX, cursorRenderY + cursorHeight, cursorWidth);
            }
        }
    }

    public void renderText(SpriteBatch spriteBatch, GlyphLayout glyphLayout, Viewport viewport) {
        BitmapFont font = textRenderer.getFont();
        if (font == null) {
            return;
        }

        if (text.isEmpty() && !placeholder.isEmpty() && !focused) {
            placeholderRenderer.setPosition(
                    boxRenderer.getPosX() + 5f,
                    boxRenderer.getPosY() + (boxRenderer.getHeight() - placeholderRenderer.getHeight(viewport)) / 2f
            );
            placeholderRenderer.render(spriteBatch, glyphLayout, viewport);
        } else {
            textRenderer.setText(text.toString());
            textRenderer.setPosition(
                    boxRenderer.getPosX() + 5f,
                    boxRenderer.getPosY() + (boxRenderer.getHeight() - textRenderer.getHeight(viewport)) / 2f
            );
            textRenderer.render(spriteBatch, glyphLayout, viewport);
        }
    }


    @Override
    public boolean keyDown(int keycode) {
        if (!focused) return false;

        blinkTimer = 0f;
        showCursor = true;

        boolean textChanged = false;

        switch (keycode) {
            case Input.Keys.BACKSPACE:
                if (cursorPosition > 0) {
                    text.deleteCharAt(cursorPosition - 1);
                    cursorPosition--;
                    textChanged = true;
                }
                break;
            case Input.Keys.FORWARD_DEL:
                if (cursorPosition < text.length()) {
                    text.deleteCharAt(cursorPosition);
                    textChanged = true;
                }
                break;
            case Input.Keys.LEFT:
                if (cursorPosition > 0) {
                    cursorPosition--;
                }
                break;
            case Input.Keys.RIGHT:
                if (cursorPosition < text.length()) {
                    cursorPosition++;
                }
                break;
            case Input.Keys.HOME:
                cursorPosition = 0;
                break;
            case Input.Keys.END:
                cursorPosition = text.length();
                break;
            case Input.Keys.ENTER:
                if (onEnterPressed != null) {
                    onEnterPressed.accept(text.toString());
                }
                return true;
            default:
                return false;
        }

        if (textChanged && onTextChanged != null) {
            onTextChanged.accept(text.toString());
        }

        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        return focused;
    }

    @Override
    public boolean keyTyped(char character) {
        if (!focused) return false;

        if (Character.isISOControl(character)) {
            if (character == '\r' || character == '\n' || character == '\b') {
                return false;
            }
        }

        if (character >= 32 && character != 127) {
            text.insert(cursorPosition, character);
            cursorPosition++;
            if (onTextChanged != null) {
                onTextChanged.accept(text.toString());
            }
            blinkTimer = 0f;
            showCursor = true;
            return true;
        }

        return false;
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

    public String getText() {
        return text.toString();
    }

    public void setText(String text) {
        this.text = new StringBuilder(text != null ? text : "");
        this.cursorPosition = Math.min(this.cursorPosition, this.text.length());
        if (onTextChanged != null) {
            onTextChanged.accept(this.text.toString());
        }
        blinkTimer = 0f;
        showCursor = true;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder != null ? placeholder : "";
        this.placeholderRenderer.setText(this.placeholder);
    }

    public void setPosition(float x, float y) {
        boxRenderer.setPosition(x, y);
    }

    public void setSize(float width, float height) {
        boxRenderer.setSize(width, height);
    }

    public float getX() { return boxRenderer.getPosX(); }
    public float getY() { return boxRenderer.getPosY(); }
    public float getWidth() { return boxRenderer.getWidth(); }
    public float getHeight() { return boxRenderer.getHeight(); }

    public boolean isFocused() {
        return focused;
    }

    public void setFocus(boolean focus) {
        if (this.focused == focus) return;

        if (focus) {
            this.focused = true;
            previousInputProcessor = Gdx.input.getInputProcessor();
            Gdx.input.setInputProcessor(this);
            cursorPosition = text.length();
            blinkTimer = 0f;
            showCursor = true;
            boxRenderer.setBorderColor(focusedBorderColor);
        } else {
            this.focused = false;
            if (Gdx.input.getInputProcessor() == this) {
                Gdx.input.setInputProcessor(previousInputProcessor);
            }
            if (onFocusLost != null) {
                onFocusLost.accept(text.toString());
            }
            showCursor = false;
            boxRenderer.setBorderColor(unfocusedBorderColor);
        }
    }

    public void setOnEnterPressed(Consumer<String> onEnterPressed) {
        this.onEnterPressed = onEnterPressed != null ? onEnterPressed : (t) -> {};
    }

    public void setOnFocusLost(Consumer<String> onFocusLost) {
        this.onFocusLost = onFocusLost != null ? onFocusLost : (t) -> {};
    }

    public void setOnTextChanged(Consumer<String> onTextChanged) {
        this.onTextChanged = onTextChanged != null ? onTextChanged : (t) -> {};
    }

    public void setFont(BitmapFont font) {
        this.textRenderer.setFont(font);
        this.placeholderRenderer.setFont(font);
    }

    public void setFont(String fontName) {
        this.textRenderer.setFont(fontName);
        this.placeholderRenderer.setFont(fontName);
    }

    public void setTextColor(Color color) {
        this.textRenderer.setColor(color);
    }

    public void setPlaceholderColor(Color color) {
        this.placeholderColor = (color != null) ? color.cpy() : defaultPlaceholderColor.cpy();
        this.placeholderRenderer.setColor(this.placeholderColor);
    }

    public void setFillColor(Color color) {
        this.boxRenderer.setFillColor(color);
    }

    public void setFocusedBorderColor(Color color) {
        this.focusedBorderColor = (color != null) ? color.cpy() : defaultFocusedBorder.cpy();
        if (focused) boxRenderer.setBorderColor(this.focusedBorderColor);
    }

    public void setUnfocusedBorderColor(Color color) {
        this.unfocusedBorderColor = (color != null) ? color.cpy() : defaultUnfocusedBorder.cpy();
        if (!focused) boxRenderer.setBorderColor(this.unfocusedBorderColor);
    }

    public void setCursorColor(Color color) {
        this.cursorColor = (color != null) ? color.cpy() : defaultCursorColor.cpy();
    }

    public void setBorderRadius(float radius) {
        this.boxRenderer.setBorderRadius(radius);
    }

    public BoxRenderer getBoxRenderer() {
        return boxRenderer;
    }

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }
}