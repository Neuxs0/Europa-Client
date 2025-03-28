package dev.neuxs.europa_client.utils.rendering.ui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.Renderer;
import dev.neuxs.europa_client.utils.rendering.ui.FontRenderer;

import java.util.Objects;

@SuppressWarnings({"DuplicatedCode", "unused"})
public class Button extends Renderer {

    // --- Core Properties (from BoxRenderer concept) ---
    public float posX;
    public float posY;
    public float width;
    public float height;
    public float borderWidth;
    public float borderRadius;
    public float shadowOffsetX;
    public float shadowOffsetY;

    public boolean border = false;
    public boolean dropShadow = false;

    // --- State-Based Colors ---
    public Color fillColor = ColorUtils.color(200, 200, 200, 255);
    public Color borderColor = ColorUtils.color(150, 150, 150, 255);
    public Color dropShadowColor = ColorUtils.color(0, 0, 0, 127);
    public Color textColor = ColorUtils.color(0, 0, 0, 255);

    public Color hoverFillColor = ColorUtils.color(220, 220, 220, 255);
    public Color hoverBorderColor = ColorUtils.color(170, 170, 170, 255);
    public Color hoverTextColor = ColorUtils.color(0, 0, 0, 255);

    public Color pressedFillColor = ColorUtils.color(180, 180, 180, 255);
    public Color pressedBorderColor = ColorUtils.color(130, 130, 130, 255);
    public Color pressedTextColor = ColorUtils.color(50, 50, 50, 255);

    // --- Button Specific Properties ---
    private String text = "";
    private Runnable action = null; // Action to perform on click
    private String fontName = "cosmicreach"; // Font name to pass to FontRenderer

    // --- State ---
    private boolean isHovered = false;
    private boolean isPressed = false;

    // --- Constants ---
    private static final int DEFAULT_ARC_SEGMENTS = 20;

    // --- Constructors ---

    public Button() {
        super();
        this.borderWidth = 1f;
        this.borderRadius = 4f;
        this.shadowOffsetX = 1f;
        this.shadowOffsetY = -1f;
    }

    public Button(float x, float y, float w, float h) {
        this();
        this.posX = x;
        this.posY = y;
        this.width = w;
        this.height = h;
    }

    public Button(float x, float y, float w, float h, String text) {
        this(x, y, w, h);
        this.text = text != null ? text : "";
    }

    public Button(float x, float y, float w, float h, String text, Runnable action) {
        this(x, y, w, h, text);
        this.action = action;
    }

    public Button(float x, float y, float w, float h, String text, String fontName, Runnable action) {
        this(x, y, w, h, text, action);
        this.fontName = (fontName != null && !fontName.isEmpty()) ? fontName : "cosmicreach";
    }

    // --- Core Logic ---

    public void update(float mouseX, float mouseY, boolean isMouseDown) {
        isHovered = isMouseOver(mouseX, mouseY);

        if (isHovered && isMouseDown) {
            isPressed = true;
        } else {
            if (isPressed && !isMouseDown && isHovered && action != null) {
                action.run();
            }
            isPressed = false;
        }
    }

    public boolean isMouseOver(float checkX, float checkY) {
        // LibGDX Y-axis is often upwards from bottom-left. Adjust if your setup differs.
        // Assuming posY is the bottom edge.
        return checkX >= posX && checkX <= posX + width &&
                checkY >= posY && checkY <= posY + height;
    }

    /**
     * Renders the button using ShapeRenderer for the box and FontRenderer for text.
     *
     * @param projectionMatrix The camera projection matrix.
     */
    public void render(Matrix4 projectionMatrix) {
        if (width <= 0 || height <= 0) return;

        // Determine current colors based on state
        Color currentFill = isPressed ? pressedFillColor : (isHovered ? hoverFillColor : fillColor);
        Color currentBorder = isPressed ? pressedBorderColor : (isHovered ? hoverBorderColor : borderColor);
        Color currentText = isPressed ? pressedTextColor : (isHovered ? hoverTextColor : textColor);
        Color currentShadow = dropShadowColor;

        // Rendering the Box
        boolean hasFill = currentFill != null && currentFill.a > 0;
        boolean hasBorder = border && borderWidth > 0 && currentBorder != null && currentBorder.a > 0;
        boolean hasShadow = dropShadow && currentShadow != null && currentShadow.a > 0;

        // --- 1. Render Shapes (Shadow, Border, Fill) ---
        if (hasFill || hasBorder || hasShadow) {
            float effectiveRadius = Math.max(0, Math.min(this.borderRadius, Math.min(width, height) / 2f));

            shapeRenderer.setProjectionMatrix(projectionMatrix);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            // Draw Shadow
            if (hasShadow) {
                shapeRenderer.setColor(currentShadow);
                drawRoundedRect(posX + shadowOffsetX, posY + shadowOffsetY, width, height, effectiveRadius);
            }

            // Draw Border
            if (hasBorder) {
                shapeRenderer.setColor(currentBorder);
                drawRoundedRect(posX, posY, width, height, effectiveRadius);
            }

            // Draw Fill (inset if border exists)
            if (hasFill) {
                shapeRenderer.setColor(currentFill);
                if (hasBorder) {
                    float inset = borderWidth;
                    float innerWidth = Math.max(0, width - inset * 2);
                    float innerHeight = Math.max(0, height - inset * 2);
                    float innerRadius = Math.max(0, effectiveRadius - inset);

                    if (innerWidth > 0 && innerHeight > 0) {
                        drawRoundedRect(posX + inset, posY + inset, innerWidth, innerHeight, innerRadius);
                    }
                } else {
                    drawRoundedRect(posX, posY, width, height, effectiveRadius);
                }
            }
            shapeRenderer.end(); // Finish shape rendering
        }

        // --- 2. Render Text using FontRenderer ---
        if (text != null && !text.isEmpty() && currentText != null && currentText.a > 0) {
            // FontRenderer handles batch begin/end and projection matrix setting internally

            // Calculate center position for the text
            // FontRenderer.drawText alignment handles the exact placement
            float textDrawX = posX + width / 2f;
            // Assuming FontRenderer aligns vertically to the center based on the y-coordinate
            float textDrawY = posY + height / 2f;

            FontRenderer.drawText(
                    projectionMatrix,
                    this.fontName,
                    this.text,
                    textDrawX,
                    textDrawY, // Pass the center Y
                    currentText,
                    Align.center, // Use LibGDX Align constants
                    0,            // wrapWidth = 0 means no wrap
                    false         // wrap = false
            );
        }
    }

    /**
     * Helper method to draw the rounded rectangle using ShapeRenderer.
     * (Unchanged from previous version)
     */
    private void drawRoundedRect(float x, float y, float width, float height, float radius) {
        radius = Math.max(0, Math.min(radius, Math.min(width, height) / 2f));

        if (radius <= 0.01f) {
            Renderer.shapeRenderer.rect(x, y, width, height);
            return;
        }

        int segments = MathUtils.clamp((int)(6 * (float)Math.cbrt(radius)), 4, DEFAULT_ARC_SEGMENTS * 2);

        Renderer.shapeRenderer.arc(x + radius, y + radius, radius, 180f, 90f, segments);
        Renderer.shapeRenderer.arc(x + radius, y + height - radius, radius, 90f, 90f, segments);
        Renderer.shapeRenderer.arc(x + width - radius, y + height - radius, radius, 0f, 90f, segments);
        Renderer.shapeRenderer.arc(x + width - radius, y + radius, radius, 270f, 90f, segments);

        if (width > 2 * radius) {
            Renderer.shapeRenderer.rect(x + radius, y, width - 2 * radius, radius);
            Renderer.shapeRenderer.rect(x + radius, y + height - radius, width - 2 * radius, radius);
        }
        if (height > 2 * radius) {
            Renderer.shapeRenderer.rect(x, y + radius, radius, height - 2 * radius);
            Renderer.shapeRenderer.rect(x + width - radius, y + radius, radius, height - 2 * radius);
        }

        if (width > 2 * radius && height > 2 * radius) {
            Renderer.shapeRenderer.rect(x + radius, y + radius, width - 2 * radius, height - 2 * radius);
        }
    }


    // --- Getters (Added fontName) ---
    public float getPosX() { return posX; }
    public float getPosY() { return posY; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getBorderWidth() { return borderWidth; }
    public float getBorderRadius() { return borderRadius; }
    public float getShadowOffsetX() { return shadowOffsetX; }
    public float getShadowOffsetY() { return shadowOffsetY; }
    public boolean isBorder() { return border; }
    public boolean isDropShadow() { return dropShadow; }
    public Color getFillColor() { return fillColor; }
    public Color getBorderColor() { return borderColor; }
    public Color getDropShadowColor() { return dropShadowColor; }
    public Color getTextColor() { return textColor; }
    public Color getHoverFillColor() { return hoverFillColor; }
    public Color getHoverBorderColor() { return hoverBorderColor; }
    public Color getHoverTextColor() { return hoverTextColor; }
    public Color getPressedFillColor() { return pressedFillColor; }
    public Color getPressedBorderColor() { return pressedBorderColor; }
    public Color getPressedTextColor() { return pressedTextColor; }
    public String getText() { return text; }
    public Runnable getAction() { return action; }
    public String getFontName() { return fontName; } // Getter for font name
    public boolean isHovered() { return isHovered; }
    public boolean isPressed() { return isPressed; }


    // --- Setters (Fluent API style - Added fontName) ---
    public Button setPosition(float x, float y) { this.posX = x; this.posY = y; return this; }
    public Button setPosX(float posX) { this.posX = posX; return this; }
    public Button setPosY(float posY) { this.posY = posY; return this; }
    public Button setSize(float w, float h) { this.width = w; this.height = h; return this; }
    public Button setWidth(float width) { this.width = width; return this; }
    public Button setHeight(float height) { this.height = height; return this; }
    public Button setFillColor(Color fillColor) { this.fillColor = Objects.requireNonNullElseGet(fillColor, () -> ColorUtils.color(200, 200, 200, 255)); return this; }
    public Button setFillColor(int r, int g, int b, int a) { this.fillColor = ColorUtils.color(r, g, b, a); return this; }
    public Button setBorderColor(Color borderColor) { this.borderColor = Objects.requireNonNullElseGet(borderColor, () -> ColorUtils.color(150, 150, 150, 255)); return this; }
    public Button setBorderColor(int r, int g, int b, int a) { this.borderColor = ColorUtils.color(r, g, b, a); return this; }
    public Button setBorderWidth(float borderWidth) { this.borderWidth = Math.max(0, borderWidth); return this; }
    public Button setBorderRadius(float borderRadius) { this.borderRadius = Math.max(0, borderRadius); return this; }
    public Button setBorderEnabled(boolean border) { this.border = border; return this; }
    public Button setDropShadowEnabled(boolean dropShadow) { this.dropShadow = dropShadow; return this; }
    public Button setDropShadowColor(Color dropShadowColor) { this.dropShadowColor = Objects.requireNonNullElseGet(dropShadowColor, () -> ColorUtils.color(0, 0, 0, 127)); return this; }
    public Button setDropShadowColor(int r, int g, int b, int a) { this.dropShadowColor = ColorUtils.color(r, g, b, a); return this; }
    public Button setDropShadowOffset(float x, float y) { this.shadowOffsetX = x; this.shadowOffsetY = y; return this; }
    public Button setShadowOffsetX(float shadowOffsetX) { this.shadowOffsetX = shadowOffsetX; return this; }
    public Button setShadowOffsetY(float shadowOffsetY) { this.shadowOffsetY = shadowOffsetY; return this; }
    public Button setText(String text) { this.text = text != null ? text : ""; return this; }
    public Button setTextColor(Color textColor) { this.textColor = Objects.requireNonNullElseGet(textColor, () -> ColorUtils.color(0, 0, 0, 255)); return this; }
    public Button setTextColor(int r, int g, int b, int a) { this.textColor = ColorUtils.color(r, g, b, a); return this; }
    public Button setHoverFillColor(Color hoverFillColor) { this.hoverFillColor = Objects.requireNonNullElseGet(hoverFillColor, () -> ColorUtils.color(220, 220, 220, 255)); return this; }
    public Button setHoverFillColor(int r, int g, int b, int a) { this.hoverFillColor = ColorUtils.color(r, g, b, a); return this; }
    public Button setHoverBorderColor(Color hoverBorderColor) { this.hoverBorderColor = Objects.requireNonNullElseGet(hoverBorderColor, () -> ColorUtils.color(170, 170, 170, 255)); return this; }
    public Button setHoverBorderColor(int r, int g, int b, int a) { this.hoverBorderColor = ColorUtils.color(r, g, b, a); return this; }
    public Button setHoverTextColor(Color hoverTextColor) { this.hoverTextColor = Objects.requireNonNullElseGet(hoverTextColor, () -> ColorUtils.color(0, 0, 0, 255)); return this; }
    public Button setHoverTextColor(int r, int g, int b, int a) { this.hoverTextColor = ColorUtils.color(r, g, b, a); return this; }
    public Button setPressedFillColor(Color pressedFillColor) { this.pressedFillColor = Objects.requireNonNullElseGet(pressedFillColor, () -> ColorUtils.color(180, 180, 180, 255)); return this; }
    public Button setPressedFillColor(int r, int g, int b, int a) { this.pressedFillColor = ColorUtils.color(r, g, b, a); return this; }
    public Button setPressedBorderColor(Color pressedBorderColor) { this.pressedBorderColor = Objects.requireNonNullElseGet(pressedBorderColor, () -> ColorUtils.color(130, 130, 130, 255)); return this; }
    public Button setPressedBorderColor(int r, int g, int b, int a) { this.pressedBorderColor = ColorUtils.color(r, g, b, a); return this; }
    public Button setPressedTextColor(Color pressedTextColor) { this.pressedTextColor = Objects.requireNonNullElseGet(pressedTextColor, () -> ColorUtils.color(50, 50, 50, 255)); return this; }
    public Button setPressedTextColor(int r, int g, int b, int a) { this.pressedTextColor = ColorUtils.color(r, g, b, a); return this; }
    public Button setAction(Runnable action) { this.action = action; return this; }

    /**
     * Sets the name of the font to be used for rendering the button's text.
     * Defaults to "cosmicreach" if null or empty.
     * @param fontName The name recognized by FontRenderer.
     * @return This Button instance for chaining.
     */
    public Button setFontName(String fontName) {
        this.fontName = (fontName != null && !fontName.isEmpty()) ? fontName : "cosmicreach";
        return this;
    }
}