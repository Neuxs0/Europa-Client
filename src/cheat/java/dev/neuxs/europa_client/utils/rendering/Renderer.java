package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.managers.FontManager;
import dev.neuxs.europa_client.managers.InputManager;
import dev.neuxs.europa_client.utils.ColorUtils;

import java.util.function.Consumer;

@SuppressWarnings({"unused"})

public abstract class Renderer {
    @FunctionalInterface
    public interface OnClick {
        void handle(Renderer renderer, int button);
    }

    private final FontManager fontManager = new FontManager();

    private RenderUtil.RenderType renderType = RenderUtil.RenderType.NONE;
    private ShapeRenderer.ShapeType shapeType = ShapeRenderer.ShapeType.Filled;

    private OnClick onClickDown;
    private OnClick onClickUp;
    private Consumer<Renderer> onHoverEnter;
    private Consumer<Renderer> onHoverExit;

    private final Vector3 pos = new Vector3(0f, 0f, 0f);
    private final Vector2 size = new Vector2(0f, 0f);

    private Color fillColor = ColorUtils.color(0, 0, 0, 255);

    private boolean toggleEnabled = false;
    private State state = State.NORMAL;
    private State toggleState = State.NORMAL;
    private int clickButton = -1;
    private static Renderer clickOwner = null;
    private static int clickOwnerButton = -1;
    private static Renderer mouseTarget = null;
    public enum State {
        NORMAL, HOVERED, PRESSED,
        TOGGLED, HOVER_TOGGLED, HOVER_PRESSED
    }

    // Shape Specific
    private boolean border = false;
    private boolean topBorder = true;
    private boolean bottomBorder = true;
    private boolean leftBorder = true;
    private boolean rightBorder = true;
    private float borderWidth = 0f;
    private float borderRadius = 0f;
    private Color borderColor = ColorUtils.color(150, 150, 150, 255);

    private boolean shadow = false;
    private float shadowOffsetX = 0f;
    private float shadowOffsetY = 0f;
    private Color shadowColor = ColorUtils.color(0, 0, 0, 127);

    // Sprite Specific
    private Color textColor = ColorUtils.color(255, 255, 255, 255);
    private String text = "";
    private BitmapFont font = fontManager.getFont(fontManager.cosmicReachFontKey);


    public Renderer() {}

    public void renderShape(Viewport viewport, ShapeRenderer shapeRenderer) {}

    public void renderSprite(Viewport viewport, SpriteBatch spriteBatch, GlyphLayout glyphLayout) {}

    public void update(Viewport viewport) {
        State previousState = state;

        boolean mouseOver = isMouseOver(viewport);
        boolean wasPressed = state == State.PRESSED;

        if (clickButton == -1 && mouseOver) {
            int pressedButton = -1;
            if (InputManager.isFirstFrameMouseButtonDown(Input.Buttons.LEFT)) {
                pressedButton = Input.Buttons.LEFT;
            } else if (InputManager.isFirstFrameMouseButtonDown(Input.Buttons.RIGHT)) {
                pressedButton = Input.Buttons.RIGHT;
            }

            if (pressedButton != -1 && canClaimClick(pressedButton)) {
                clickOwner = this;
                clickOwnerButton = pressedButton;
                clickButton = pressedButton;
                state = State.PRESSED;
            }
        }

        if (clickButton != -1) {
            boolean isClickButtonStillDown = InputManager.isMouseButtonDown(clickButton);
            boolean ownsClick = clickOwner == this && clickOwnerButton == clickButton;

            if (isClickButtonStillDown) {
                state = mouseOver && ownsClick ? State.PRESSED : State.NORMAL;
                if (mouseOver && ownsClick && onClickDown != null) {
                    onClickDown.handle(this, clickButton);
                }
            } else {
                if (mouseOver && wasPressed && ownsClick) {
                    if (toggleState == State.TOGGLED || toggleState == State.HOVER_TOGGLED) {
                        toggleState = State.HOVERED;
                    } else {
                        toggleState = State.HOVER_TOGGLED;
                    }

                    if (onClickUp != null) {
                        onClickUp.handle(this, clickButton);
                    }
                }

                if (ownsClick) {
                    clickOwner = null;
                    clickOwnerButton = -1;
                }

                state = mouseOver ? State.HOVERED : State.NORMAL;
                clickButton = -1;
            }
        } else {
            state = mouseOver ? State.HOVERED : State.NORMAL;

            if (toggleState == State.TOGGLED || toggleState == State.HOVER_TOGGLED) {
                toggleState = mouseOver ? State.HOVER_TOGGLED : State.TOGGLED;
            } else {
                toggleState = mouseOver ? State.HOVERED : State.NORMAL;
            }
        }

        boolean isHoveringNow = state == State.HOVERED || toggleState == State.HOVER_TOGGLED;
        boolean wasHoveringBefore = previousState == State.HOVERED || previousState == State.HOVER_TOGGLED;

        if (isHoveringNow && !wasHoveringBefore && onHoverEnter != null) {
            onHoverEnter.accept(this);
        } else if (!isHoveringNow && wasHoveringBefore && onHoverExit != null) {
            onHoverExit.accept(this);
        }
    }

    private boolean canClaimClick(int button) {
        if (clickOwner == null || clickOwnerButton == -1 || !InputManager.isMouseButtonDown(clickOwnerButton)) {
            return true;
        }

        return clickOwnerButton == button && getZIndex() >= clickOwner.getZIndex();
    }

    private boolean isToggled() {
        return toggleState == State.TOGGLED || toggleState == State.HOVER_TOGGLED;
    }

    private void setToggled(Viewport viewport, boolean toggled) {
        if (toggled) {
            toggleState = isMouseOver(viewport) ? State.HOVER_TOGGLED : State.TOGGLED;
        } else {
            toggleState = isMouseOver(viewport) ? State.HOVERED : State.NORMAL;
        }
    }


    // ---------- GETTERS ----------
    public RenderUtil.RenderType getRenderType() {
        return renderType;
    }
    public ShapeRenderer.ShapeType getShapeType() {
        return shapeType;
    }

    public Vector3 getPos() {
        return pos;
    }
    public float getPosX() {
        return pos.x;
    }
    public float getPosY() {
        return pos.y;
    }
    public int getZIndex() {
        return (int) pos.z;
    }

    public Vector2 getSize() {
        return size;
    }
    public float getWidth() {
        return size.x;
    }
    public float getHeight() {
        return size.y;
    }

    public boolean isBorder() {
        return border;
    }
    public boolean isTopBorder() {
        return topBorder;
    }
    public boolean isBottomBorder() {
        return bottomBorder;
    }
    public boolean isLeftBorder() {
        return leftBorder;
    }
    public boolean isRightBorder() {
        return rightBorder;
    }
    public float getBorderWidth() {
        return borderWidth;
    }
    public float getBorderRadius() {
        return borderRadius;
    }
    public Color getBorderColor() {
        return borderColor.cpy();
    }

    public boolean isShadow() {
        return shadow;
    }
    public float getShadowOffsetX() {
        return shadowOffsetX;
    }
    public float getShadowOffsetY() {
        return shadowOffsetY;
    }
    public Color getShadowColor() {
        return shadowColor.cpy();
    }

    public String getText() {
        return text;
    }
    public float getTextHeight(Viewport viewport) {
        if (font == null || text == null || text.isEmpty() || viewport == null) return 0f;
        return fontManager.getTextDimensions(viewport, font, text).y;
    }
    public float getTextWidth(Viewport viewport) {
        if (font == null || text == null || text.isEmpty() || viewport == null) return 0f;
        return fontManager.getTextDimensions(viewport, font, text).x;
    }
    public BitmapFont getFont() {
        return font;
    }
    public String getFontName() {
        return fontManager.getFontName(this.font);
    }

    public Color getFillColor() {
        return fillColor.cpy();
    }
    public Color getTextColor() {
        return textColor.cpy();
    }

    public boolean isToggleEnabled() {
        return toggleEnabled;
    }
    public State getState() {
        if (toggleEnabled) return toggleState;
        else return state;
    }
    public boolean isMouseOver(Viewport viewport) {
        Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mousePos);

        return isMouseTarget() && containsPoint(mousePos.x, mousePos.y);
    }
    public boolean containsPoint(float x, float y) {
        return x >= this.pos.x && x <= this.pos.x + this.size.x &&
                y >= this.pos.y && y <= this.pos.y + this.size.y;
    }
    public boolean blocksMouseAt(float x, float y) {
        return containsPoint(x, y);
    }
    public boolean isMouseTarget() {
        return mouseTarget == this;
    }
    public static void setMouseTarget(Renderer renderer) {
        mouseTarget = renderer;
    }
    public OnClick getOnClickDown() {
        return onClickDown;
    }
    public OnClick getOnClickUp() {
        return onClickUp;
    }
    public Consumer<Renderer> getOnHoverEnter() {
        return onHoverEnter;
    }
    public Consumer<Renderer> getOnHoverExit() {
        return onHoverExit;
    }


    // ---------- SETTERS ----------
    public void setRenderType(RenderUtil.RenderType renderType) {
        if (renderType != null && renderType != this.renderType) this.renderType = renderType;
    }
    public void setShapeType(ShapeRenderer.ShapeType shapeType) {
        if (shapeType != null && shapeType != this.shapeType) this.shapeType = shapeType;
    }

    public void setPos(Vector3 pos) {
        if (pos != null) this.pos.set(pos);
    }
    public void setPos(Vector2 pos) {
        if (pos != null) this.pos.set(pos, this.pos.z);
    }
    public void setPos(float x, float y, int z) {
        this.pos.set(x, y, z);
    }
    public void setPos(float x, float y) {
        this.pos.set(x, y, this.pos.z);
    }
    public void setPosX(float x) {
        if (x != this.pos.x) this.pos.set(x, this.pos.y, this.pos.z);
    }
    public void setPosY(float y) {
        if (y != this.pos.y) this.pos.set(this.pos.x, y, this.pos.z);
    }
    public void setZIndex(int z) {
        if ((float) z != this.pos.z) this.pos.set(this.pos.x, this.pos.y, (float) z);
    }

    public void setSize(Vector2 size) {
        if (size != null && size.x >= 0 && size.y >= 0) this.size.set(size);
    }
    public void setSize(float w, float h) {
        if (w >= 0f && h >= 0f) this.size.set(w, h);
    }
    public void setWidth(float width) {
        if (width >= 0f && width != this.size.x) this.size.set(width, this.size.y);
    }
    public void setHeight(float height) {
        if (height >= 0f && height != this.size.y) this.size.set(this.size.x, height);
    }

    public void setBorder(boolean enabled) {
        if (enabled != this.border) this.border = enabled;
    }
    public void setTopBorder(boolean enabled) {
        if (enabled != this.topBorder) this.topBorder = enabled;
    }
    public void setBottomBorder(boolean enabled) {
        if (enabled != this.bottomBorder) this.bottomBorder = enabled;
    }
    public void setLeftBorder(boolean enabled) {
        if (enabled != this.leftBorder) this.leftBorder = enabled;
    }
    public void setRightBorder(boolean enabled) {
        if (enabled != this.rightBorder) this.rightBorder = enabled;
    }
    public void setBorderWidth(float width) {
        if (width >= 0f && width != this.borderWidth) this.borderWidth = width;
    }
    public void setBorderRadius(float radius) {
        if (radius >= 0f && radius != this.borderRadius) this.borderRadius = radius;
    }
    public void setBorderColor(Color color) {
        if (color != null) this.borderColor = color.cpy();
    }

    public void setShadow(boolean enabled) {
        if (enabled != this.shadow) this.shadow = enabled;
    }
    public void setShadowOffsetX(float x) {
        if (x != this.shadowOffsetX) this.shadowOffsetX = x;
    }
    public void setShadowOffsetY(float y) {
        if (y != this.shadowOffsetY) this.shadowOffsetY = y;
    }
    public void setShadowColor(Color color) {
        if (color != null) this.shadowColor = color.cpy();
    }

    public void setText(String text) {
        if (text != null && !text.isEmpty() && !text.equals(this.text)) this.text = text;
    }
    public void setFont(BitmapFont font) {
        if (font != null && fontManager.getFontName(font) != null) {
            this.font = font;
        } else {
            this.font = fontManager.getFont(fontManager.cosmicReachFontKey);
            if (font != null) {
                Client.LOGGER.warn("Attempted to set an unknown BitmapFont instance in TextRenderer. Reverting to default.");
            }
        }
        if (this.font == null) Client.LOGGER.error("Font is null in TextRenderer even after attempting to set/fallback!");
    }
    public void setFont(String fontName) {
        BitmapFont resolvedFont = null;
        if (fontName != null && !fontName.trim().isEmpty()) resolvedFont = fontManager.getFont(fontName.toLowerCase());

        if (resolvedFont != null) {
            this.font = resolvedFont;
        } else {
            this.font = fontManager.getFont(fontManager.cosmicReachFontKey);
            if (fontName != null) {
                Client.LOGGER.warn("Font name '{}' not found in FontManager. Reverting TextRenderer to default.", fontName);
            }
        }

        if (this.font == null) Client.LOGGER.error("Font is null in TextRenderer even after attempting to set/fallback by name!");
    }

    public void setFillColor(Color color) {
        if (color != null) this.fillColor = color.cpy();
    }
    public void setTextColor(Color color) {
        if (color != null) this.textColor = color.cpy();
    }

    public void setToggleEnabled(boolean enabled) {
        if (enabled != this.toggleEnabled) this.toggleEnabled = enabled;
    }
    public void setState(State state) {
        if (state != null && (state != this.state || state != this.toggleState)) {
            if (toggleEnabled) this.toggleState = state;
            else this.state = state;
        }
    }
    public void setOnClickDown(OnClick func) {
        this.onClickDown = func;
    }
    public void setOnClickUp(OnClick func) {
        this.onClickUp = func;
    }
    public void setOnHoverEnter(Consumer<Renderer> onHoverEnter) {
        this.onHoverEnter = onHoverEnter;
    }
    public void setOnHoverExit(Consumer<Renderer> onHoverExit) {
        this.onHoverExit = onHoverExit;
    }
}
