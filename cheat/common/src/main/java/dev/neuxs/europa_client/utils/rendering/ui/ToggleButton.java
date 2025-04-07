package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.utils.ColorUtils;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class ToggleButton extends Button {
    private boolean isToggled;
    private Consumer<Boolean> onToggle;
    private Color toggledNormalFillColor;
    private Color toggledHoverFillColor;
    private Color toggledPressedFillColor;
    private Color toggledNormalBorderColor;
    private Color toggledHoverBorderColor;
    private Color toggledPressedBorderColor;
    private static final Color defaultToggledNormalFill = ColorUtils.color(70, 130, 200, 255);
    private static final Color defaultToggledHoverFill = ColorUtils.color(90, 150, 220, 255);
    private static final Color defaultToggledPressedFill = ColorUtils.color(110, 170, 240, 255);
    private static final Color defaultToggledNormalBorder = ColorUtils.color(20, 20, 20, 255);
    private static final Color defaultToggledHoverBorder = ColorUtils.color(20, 20, 20, 255);
    private static final Color defaultToggledPressedBorder = ColorUtils.color(20, 20, 20, 255);

    public ToggleButton() {
        super();
        this.isToggled = false;
        this.onToggle = (toggled) -> {};
        this.toggledNormalFillColor = defaultToggledNormalFill.cpy();
        this.toggledHoverFillColor = defaultToggledHoverFill.cpy();
        this.toggledPressedFillColor = defaultToggledPressedFill.cpy();
        this.toggledNormalBorderColor = defaultToggledNormalBorder.cpy();
        this.toggledHoverBorderColor = defaultToggledHoverBorder.cpy();
        this.toggledPressedBorderColor = defaultToggledPressedBorder.cpy();

        super.setOnClick(button -> {
            this.isToggled = !this.isToggled;
            if (this.onToggle != null) {
                this.onToggle.accept(this.isToggled);
            }
        });
    }
    @Override
    public void renderShape(ShapeRenderer shapeRenderer, Viewport viewport) {
        super.update(viewport);

        Color currentFill;
        Color currentBorder;

        if (isToggled) {
            currentBorder = switch (getCurrentState()) {
                case HOVERED -> {
                    currentFill = toggledHoverFillColor;
                    yield toggledHoverBorderColor;
                }
                case PRESSED -> {
                    currentFill = toggledPressedFillColor;
                    yield toggledPressedBorderColor;
                }
                default -> {
                    currentFill = toggledNormalFillColor;
                    yield toggledNormalBorderColor;
                }
            };
        } else {
            currentBorder = switch (getCurrentState()) {
                case HOVERED -> {
                    currentFill = getHoverFillColor();
                    yield getHoverBorderColor();
                }
                case PRESSED -> {
                    currentFill = getPressedFillColor();
                    yield getPressedBorderColor();
                }
                default -> {
                    currentFill = getNormalFillColor();
                    yield getNormalBorderColor();
                }
            };
        }

        getBoxRenderer().setFillColor(currentFill);
        getBoxRenderer().setBorderColor(currentBorder);
        getBoxRenderer().render(shapeRenderer);
    }

    @Override
    public void renderText(SpriteBatch spriteBatch, GlyphLayout glyphLayout, Viewport viewport) {
        super.renderText(spriteBatch, glyphLayout, viewport);
    }

    public boolean isToggled() {
        return isToggled;
    }
    public void setToggled(boolean toggled) {
        this.isToggled = toggled;
    }
    public Consumer<Boolean> getOnToggle() {
        return onToggle;
    }
    public void setOnToggle(Consumer<Boolean> onToggle) {
        this.onToggle = onToggle != null ? onToggle : (toggled) -> {};
    }
    public Color getToggledNormalFillColor() { return toggledNormalFillColor.cpy(); }
    public void setToggledNormalFillColor(Color color) { this.toggledNormalFillColor = (color != null) ? color.cpy() : defaultToggledNormalFill.cpy(); }
    public Color getToggledHoverFillColor() { return toggledHoverFillColor.cpy(); }
    public void setToggledHoverFillColor(Color color) { this.toggledHoverFillColor = (color != null) ? color.cpy() : defaultToggledHoverFill.cpy(); }
    public Color getToggledPressedFillColor() { return toggledPressedFillColor.cpy(); }
    public void setToggledPressedFillColor(Color color) { this.toggledPressedFillColor = (color != null) ? color.cpy() : defaultToggledPressedFill.cpy(); }
    public Color getToggledNormalBorderColor() { return toggledNormalBorderColor.cpy(); }
    public void setToggledNormalBorderColor(Color color) { this.toggledNormalBorderColor = (color != null) ? color.cpy() : defaultToggledNormalBorder.cpy(); }
    public Color getToggledHoverBorderColor() { return toggledHoverBorderColor.cpy(); }
    public void setToggledHoverBorderColor(Color color) { this.toggledHoverBorderColor = (color != null) ? color.cpy() : defaultToggledHoverBorder.cpy(); }
    public Color getToggledPressedBorderColor() { return toggledPressedBorderColor.cpy(); }
    public void setToggledPressedBorderColor(Color color) { this.toggledPressedBorderColor = (color != null) ? color.cpy() : defaultToggledPressedBorder.cpy(); }
}
