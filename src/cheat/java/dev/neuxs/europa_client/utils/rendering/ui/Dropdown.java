package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.Renderer;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;

@SuppressWarnings("unused")
public class Dropdown extends Renderer {
    private final BoxRenderer boxRenderer;
    private final TextRenderer textRenderer;

    private final Button selectedButton;

    private Color hoverFillColor;
    private Color pressedFillColor;
    private Color hoverToggledFillColor;
    private Color pressedToggledFillColor;
    private Color hoverBorderColor;
    private Color pressedBorderColor;
    private Color hoverToggledBorderColor;
    private Color pressedToggledBorderColor;


    public Dropdown() {
        this.boxRenderer = new BoxRenderer();
        this.textRenderer = new TextRenderer();
        this.selectedButton = new Button();
        this.hoverFillColor = ColorUtils.color(70, 70, 70, 255);
        this.pressedFillColor = ColorUtils.color(90, 90, 90, 255);
        this.hoverToggledFillColor = ColorUtils.color(255, 255, 255, 255);
        this.pressedToggledFillColor = ColorUtils.color(255, 255, 255, 255);
        this.hoverBorderColor = ColorUtils.color(20, 20, 20, 255);
        this.pressedBorderColor = ColorUtils.color(20, 20, 20, 255);
        this.hoverToggledBorderColor = ColorUtils.color(255, 255, 255, 255);
        this.pressedToggledBorderColor = ColorUtils.color(255, 255, 255, 255);
        this.setBorder(true);
        this.setText("");
        this.setToggleEnabled(true);
    }

    @Override
    public void renderShape(Viewport viewport, ShapeRenderer shapeRenderer) {

    }

    @Override
    public void renderSprite(Viewport viewport, SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        if (getText() == null || getText().isEmpty() || getFont() == null) return;

        float textX = getPosX() + (getWidth() - getTextWidth(viewport)) / 2f;
        float textY = getPosY() + (getHeight() - getTextHeight(viewport)) / 2f;

        textRenderer.setPos(textX, textY);
        textRenderer.renderSprite(viewport, spriteBatch, glyphLayout);
    }

    public BoxRenderer getBoxRenderer() {
        return this.boxRenderer;
    }
    public TextRenderer getTextRenderer() {
        return this.textRenderer;
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
