package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;
import dev.neuxs.europa_client.utils.rendering.Renderer;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;

@SuppressWarnings("unused")
public class Button extends Renderer {
    private final BoxRenderer boxRenderer;
    private final TextRenderer textRenderer;

    private Color hoverFillColor;
    private Color pressedFillColor;
    private Color hoverToggledFillColor;
    private Color pressedToggledFillColor;
    private Color hoverBorderColor;
    private Color pressedBorderColor;
    private Color hoverToggledBorderColor;
    private Color pressedToggledBorderColor;


    public Button() {
        this.boxRenderer = new BoxRenderer();
        this.textRenderer = new TextRenderer();
        this.setRenderType(RenderUtil.RenderType.SHAPE_SPRITE);
        this.setFillColor(ColorUtils.color(50, 50, 50, 255));
        this.setBorderColor(ColorUtils.color(20, 20, 20, 255));
        this.hoverFillColor = ColorUtils.color(70, 70, 70, 255);
        this.pressedFillColor = ColorUtils.color(90, 90, 90, 255);
        this.hoverToggledFillColor = ColorUtils.color(90, 90, 90, 255);
        this.pressedToggledFillColor = ColorUtils.color(110, 110, 110, 255);
        this.hoverBorderColor = ColorUtils.color(20, 20, 20, 255);
        this.pressedBorderColor = ColorUtils.color(20, 20, 20, 255);
        this.hoverToggledBorderColor = ColorUtils.color(20, 20, 20, 255);
        this.pressedToggledBorderColor = ColorUtils.color(20, 20, 20, 255);
        this.setBorder(true);
        this.setText("");
    }

    @Override
    public void renderShape(Viewport viewport, ShapeRenderer shapeRenderer) {
        Color fillColor;
        Color borderColor = switch (getState()) {
            case HOVERED -> {
                fillColor = hoverFillColor;
                yield hoverBorderColor;
            }
            case TOGGLED, PRESSED -> {
                fillColor = pressedFillColor;
                yield pressedBorderColor;
            }
            case HOVER_TOGGLED -> {
                fillColor = hoverToggledFillColor;
                yield hoverToggledBorderColor;
            }
            case HOVER_PRESSED -> {
                fillColor = pressedToggledFillColor;
                yield pressedToggledBorderColor;
            }
            default -> {
                fillColor = getFillColor();
                yield getBorderColor();
            }
        };

        boxRenderer.setPos(getPos());
        boxRenderer.setSize(getSize());
        boxRenderer.setFillColor(fillColor);
        boxRenderer.setBorder(isBorder());
        boxRenderer.setBorderColor(borderColor);
        boxRenderer.setBorderWidth(getBorderWidth());
        boxRenderer.setBorderRadius(getBorderRadius());

        boxRenderer.renderShape(viewport, shapeRenderer);
    }

    @Override
    public void renderSprite(Viewport viewport, SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        if (getText() == null || getText().isEmpty() || textRenderer.getFont() == null) {
            return;
        }

        textRenderer.setPos(
                getPosX() + (getWidth() - textRenderer.getTextWidth(viewport)) / 2f,
                getPosY() + (getHeight() - textRenderer.getTextHeight(viewport)) / 2f
        );
        textRenderer.renderSprite(viewport, spriteBatch, glyphLayout);
    }

    public BoxRenderer getBoxRenderer() {
        return this.boxRenderer;
    }
    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }
    public String getText() {
        return this.textRenderer.getText();
    }
    public Color getHoverFillColor() {
        return hoverFillColor.cpy();
    }
    public Color getHoverBorderColor() {
        return hoverBorderColor.cpy();
    }
    public Color getPressedFillColor() {
        return pressedFillColor.cpy();
    }
    public Color getPressedBorderColor() {
        return pressedBorderColor.cpy();
    }
    public Color getToggledHoverFillColor() {
        return hoverToggledFillColor.cpy();
    }
    public Color getToggledHoverBorderColor() {
        return hoverToggledBorderColor.cpy();
    }
    public Color getToggledPressedFillColor() {
        return pressedToggledFillColor.cpy();
    }
    public Color getToggledPressedBorderColor() {
        return pressedToggledBorderColor.cpy();
    }

    public void setText(String text) {
        this.textRenderer.setText(text);
    }
    public void setHoverFillColor(Color color) {
        if (color != null) this.hoverFillColor = color.cpy();
    }
    public void setHoverBorderColor(Color color) {
        if (color != null) this.hoverBorderColor = color.cpy();
    }
    public void setPressedFillColor(Color color) {
        if (color != null) this.pressedFillColor = color.cpy();
    }
    public void setPressedBorderColor(Color color) {
        if (color != null) this.pressedBorderColor = color.cpy();
    }
    public void setToggledHoverFillColor(Color color) {
        if (color != null) this.hoverToggledFillColor = color.cpy();
    }
    public void setToggledHoverBorderColor(Color color) {
        if (color != null) this.hoverToggledBorderColor = color.cpy();
    }
    public void setToggledPressedFillColor(Color color) {
        if (color != null) this.pressedToggledFillColor = color.cpy();
    }
    public void setToggledPressedBorderColor(Color color) {
        if (color != null) this.pressedToggledBorderColor = color.cpy();
    }
}
