package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.managers.font.FontManager;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;
import dev.neuxs.europa_client.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "DuplicatedCode"})
public class Dropdown {
    private final Button mainButton;
    private final TextRenderer indicatorRenderer;
    private final BoxRenderer optionsBackground;
    private final List<String> allOptions;
    private final List<String> options;
    private final List<Button> optionButtons;
    private final FontManager fontManager;
    private final Vector2 unprojectedMousePos = new Vector2();
    private boolean isOpen = false;
    private String selectedOption = null;
    private int selectedIndex = -1;
    private String placeholderText = "Select...";
    private float optionHeight = 25f;
    private float optionsMaxHeight = 150f;
    private float padding = 5f;
    private Consumer<String> onSelectionChanged;
    private final Color DEFAULT_OPTION_BG_FILL = ColorUtils.color(60, 60, 60, 240);
    private final Color DEFAULT_OPTION_BG_BORDER = ColorUtils.color(30, 30, 30, 255);
    private final Color DEFAULT_INDICATOR_COLOR = ColorUtils.color(200, 200, 200, 255);
    private Color defaultOptionNormalFill = ColorUtils.color(75, 75, 75, 255);
    private Color defaultOptionHoverFill = ColorUtils.color(95, 95, 95, 255);
    private Color defaultOptionPressedFill = ColorUtils.color(115, 115, 115, 255);

    public Dropdown() {
        this.fontManager = FontManager.getInstance();
        this.mainButton = new Button();
        this.indicatorRenderer = new TextRenderer();
        this.optionsBackground = new BoxRenderer();
        this.allOptions = new ArrayList<>();
        this.options = new ArrayList<>();
        this.optionButtons = new ArrayList<>();
        this.onSelectionChanged = (selection) -> {};
        this.mainButton.getTextRenderer().setText(placeholderText);
        this.mainButton.getTextRenderer().setAlignment(Align.left);
        this.mainButton.setOnClick(button -> toggleDropdown());
        this.indicatorRenderer.setText("∨");
        this.indicatorRenderer.setColor(DEFAULT_INDICATOR_COLOR.cpy());
        this.indicatorRenderer.setAlignment(Align.right);
        this.optionsBackground.setFillColor(DEFAULT_OPTION_BG_FILL.cpy());
        this.optionsBackground.setBorderColor(DEFAULT_OPTION_BG_BORDER.cpy());
        this.optionsBackground.setBorderEnabled(true);
        this.optionsBackground.setBorderWidth(1f);
        this.optionsBackground.setBorderRadius(3f);
        this.setSize(150, 30);
    }

    private void toggleDropdown() {
        this.isOpen = !this.isOpen;
        this.indicatorRenderer.setText(isOpen ? "∧" : "∨");
        if (isOpen) {
            rebuildVisibleOptions();
            updateOptionsLayout();
        }
    }

    public void addOption(String option) {
        Objects.requireNonNull(option, "Option cannot be null");
        if (!allOptions.contains(option)) {
            allOptions.add(option);
            if (!option.equals(this.selectedOption)) {
                rebuildVisibleOptions();
            } else if (isOpen) {
                updateOptionsLayout();
            }
        }
    }

    public void removeOption(String option) {
        int index = allOptions.indexOf(option);
        if (index != -1) {
            allOptions.remove(index);

            boolean wasSelected = option.equals(this.selectedOption);
            if (wasSelected) {
                this.selectedOption = null;
                this.selectedIndex = -1;
                this.mainButton.getTextRenderer().setText(placeholderText);
            }

            rebuildVisibleOptions();

            if (!wasSelected && this.selectedIndex > index) {
                this.selectedIndex--;
            }
        }
    }

    public void clearOptions() {
        allOptions.clear();
        selectOption(null, -1, true);
    }

    private Button createOptionButton(String optionText, int originalIndex) {
        Button btn = new Button();
        btn.getTextRenderer().setText(optionText);
        if (mainButton.getTextRenderer().getFont() != null) {
            btn.getTextRenderer().setFont(this.mainButton.getTextRenderer().getFont());
        } else {
            btn.getTextRenderer().setFont(fontManager.getCosmicReachFont());
        }
        btn.getTextRenderer().setColor(this.mainButton.getTextRenderer().getColor());
        btn.setNormalFillColor(this.defaultOptionNormalFill);
        btn.setHoverFillColor(this.defaultOptionHoverFill);
        btn.setPressedFillColor(this.defaultOptionPressedFill);
        btn.getBoxRenderer().setBorderEnabled(false);

        btn.setSize(this.getWidth(), optionHeight);

        final String currentOptionText = optionText;
        btn.setOnClick(button -> selectOption(currentOptionText, originalIndex, true));

        return btn;
    }

    private void rebuildVisibleOptions() {
        options.clear();
        optionButtons.clear();
        for (int i = 0; i < allOptions.size(); i++) {
            String opt = allOptions.get(i);
            if (!opt.equals(this.selectedOption)) {
                options.add(opt);
                Button optionButton = createOptionButton(opt, i);
                optionButtons.add(optionButton);
            }
        }
        if (isOpen) {
            updateOptionsLayout();
        }
    }


    private void selectOption(String option, int index, boolean triggerCallback) {
        boolean changed = !Objects.equals(this.selectedOption, option);

        if (changed) {
            this.selectedOption = option;
            this.selectedIndex = (option == null) ? -1 : index;

            rebuildVisibleOptions();

            this.mainButton.getTextRenderer().setText(option != null ? option : placeholderText);
        }

        this.isOpen = false;
        this.indicatorRenderer.setText("∨");

        if (changed && triggerCallback && onSelectionChanged != null) {
            onSelectionChanged.accept(this.selectedOption);
        }
    }


    private void updateOptionsLayout() {
        if (!isOpen) return;

        int visibleOptionCount = optionButtons.size();
        float bgWidth = mainButton.getWidth();

        if (visibleOptionCount == 0) {
            optionsBackground.setSize(bgWidth, 0);
            return;
        }

        float bgX = mainButton.getX();
        float requiredHeight = visibleOptionCount * optionHeight;
        float bgHeight = Math.min(optionsMaxHeight, requiredHeight);
        float bgY = mainButton.getY() - bgHeight;

        optionsBackground.setPosition(bgX, bgY);
        optionsBackground.setSize(bgWidth, bgHeight);

        float currentY = bgY + bgHeight - optionHeight;
        for (Button btn : optionButtons) {
            btn.setPosition(bgX, currentY);
            btn.setSize(bgWidth, optionHeight);
            currentY -= optionHeight;
        }
    }

    public void update(Viewport viewport) {
        mainButton.update(viewport);

        if (isOpen) {
            for (Button optionBtn : optionButtons) {
                optionBtn.update(viewport);
            }

            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                unprojectedMousePos.set(Gdx.input.getX(), Gdx.input.getY());
                viewport.unproject(unprojectedMousePos);

                boolean overMain = mainButton.getCurrentState() == Button.ButtonState.HOVERED;

                boolean overOptionsArea = isOverOptionsArea();

                if (!overMain && !overOptionsArea) {
                    this.isOpen = false;
                    this.indicatorRenderer.setText("∨");
                }
            }
        }
    }



    private void renderBoxFill(ShapeRenderer sr, BoxRenderer br) {
        if (br != null && br.getFillColor().a > 0) {
            boolean originalBorderState = br.isBorderEnabled();
            br.setBorderEnabled(false);
            br.render(sr);
            br.setBorderEnabled(originalBorderState);
        }
    }

    private void drawRoundedRectOutline(ShapeRenderer shapeRenderer, float x, float y, float width, float height, float radius, float borderWidth, Color color) {
        if (borderWidth <= 0 || color.a <= 0 || width <= 0 || height <= 0) return;

        radius = Math.max(0, Math.min(radius, Math.min(width / 2f, height / 2f)));
        boolean sharpCorners = radius < 0.1f;

        if (sharpCorners) {
            shapeRenderer.rectLine(x, y + height - borderWidth / 2, x + width, y + height - borderWidth / 2, borderWidth); // Top
            shapeRenderer.rectLine(x + borderWidth / 2, y, x + borderWidth / 2, y + height, borderWidth); // Left
            shapeRenderer.rectLine(x, y + borderWidth / 2, x + width, y + borderWidth / 2, borderWidth); // Bottom
            shapeRenderer.rectLine(x + width - borderWidth / 2, y, x + width - borderWidth / 2, y + height, borderWidth); // Right
        } else {
            shapeRenderer.rectLine(x + radius, y + height - borderWidth / 2, x + width - radius, y + height - borderWidth / 2, borderWidth); // Top
            shapeRenderer.rectLine(x + radius, y + borderWidth / 2, x + width - radius, y + borderWidth / 2, borderWidth); // Bottom
            shapeRenderer.rectLine(x + borderWidth / 2, y + radius, x + borderWidth / 2, y + height - radius, borderWidth); // Left
            shapeRenderer.rectLine(x + width - borderWidth / 2, y + radius, x + width - borderWidth / 2, y + height - radius, borderWidth); // Right

            int segments = Math.max(1, (int) (6 * (float) Math.cbrt(radius)));
            shapeRenderer.arc(x + radius, y + radius, radius, 180f, 90f, segments); // Bottom-left
            shapeRenderer.arc(x + radius, y + height - radius, radius, 90f, 90f, segments); // Top-left
            shapeRenderer.arc(x + width - radius, y + height - radius, radius, 0f, 90f, segments); // Top-right
            shapeRenderer.arc(x + width - radius, y + radius, radius, 270f, 90f, segments); // Bottom-right
        }
    }


    public void renderShape(ShapeRenderer shapeRenderer, Viewport viewport) {
        if (isOpen && !optionButtons.isEmpty()) {
            renderBoxFill(shapeRenderer, mainButton.getBoxRenderer());
            renderBoxFill(shapeRenderer, optionsBackground);
            for (Button optionBtn : optionButtons) {
                renderBoxFill(shapeRenderer, optionBtn.getBoxRenderer());
            }

            if (optionsBackground.isBorderEnabled() && optionsBackground.getBorderWidth() > 0 && optionsBackground.getBorderColor().a > 0) {
                float totalHeight = mainButton.getHeight() + optionsBackground.getHeight();
                float bottomY = mainButton.getY() - optionsBackground.getHeight();
                drawRoundedRectOutline(
                        shapeRenderer,
                        mainButton.getX(),
                        bottomY,
                        mainButton.getWidth(),
                        totalHeight,
                        optionsBackground.getBorderRadius(),
                        optionsBackground.getBorderWidth(),
                        optionsBackground.getBorderColor()
                );
            }
        } else {
            mainButton.renderShape(shapeRenderer, viewport);
        }
    }

    public void renderText(SpriteBatch spriteBatch, GlyphLayout glyphLayout, Viewport viewport) {
        TextRenderer mainTextRenderer = mainButton.getTextRenderer();
        BitmapFont font = mainTextRenderer.getFont();

        if (font == null) {
            return;
        }

        float buttonX = mainButton.getX();
        float buttonY = mainButton.getY();
        float buttonWidth = mainButton.getWidth();
        float buttonHeight = mainButton.getHeight();

        glyphLayout.setText(font, mainTextRenderer.getText(), mainTextRenderer.getColor(), buttonWidth - 2 * padding - indicatorRenderer.getWidth(viewport), Align.left, true);
        mainTextRenderer.setPosition(
                buttonX + padding,
                buttonY + (buttonHeight + glyphLayout.height) / 2f
        );
        mainTextRenderer.render(spriteBatch, glyphLayout, viewport);

        glyphLayout.setText(font, indicatorRenderer.getText());
        indicatorRenderer.setPosition(
                buttonX + buttonWidth - glyphLayout.width - padding,
                buttonY + (buttonHeight + glyphLayout.height) / 2f
        );
        indicatorRenderer.render(spriteBatch, glyphLayout, viewport);

        if (isOpen) {
            for (Button optionBtn : optionButtons) {
                TextRenderer btnTextRenderer = optionBtn.getTextRenderer();
                BitmapFont btnFont = btnTextRenderer.getFont();
                if (btnFont != null) {
                    float btnX = optionBtn.getX();
                    float btnY = optionBtn.getY();
                    float btnWidth = optionBtn.getWidth();
                    float btnHeight = optionBtn.getHeight();

                    glyphLayout.setText(btnFont, btnTextRenderer.getText(), btnTextRenderer.getColor(), btnWidth - 2 * padding, Align.left, true); // Align left, allow wrap/ellipsis
                    btnTextRenderer.setPosition(
                            btnX + padding,
                            btnY + (btnHeight + glyphLayout.height) / 2f
                    );
                    btnTextRenderer.render(spriteBatch, glyphLayout, viewport);
                }
            }
        }
    }


    public Button getMainButton() {
        return mainButton;
    }
    public String getSelectedOption() {
        return selectedOption;
    }
    public int getSelectedIndex() {
        return selectedIndex;
    }
    public List<String> getOptions() {
        return new ArrayList<>(allOptions);
    }
    public boolean isOpen() {
        return isOpen;
    }
    public float getX() { return mainButton.getX(); }
    public float getY() { return mainButton.getY(); }
    public float getWidth() { return mainButton.getWidth(); }
    public float getHeight() { return mainButton.getHeight(); }
    private boolean isOverOptionsArea() {
        float combinedHeight = mainButton.getHeight() + optionsBackground.getHeight();
        float combinedY = mainButton.getY() - optionsBackground.getHeight();
        return unprojectedMousePos.x >= mainButton.getX() &&
                unprojectedMousePos.x <= mainButton.getX() + mainButton.getWidth() &&
                unprojectedMousePos.y >= combinedY &&
                unprojectedMousePos.y <= mainButton.getY() + mainButton.getHeight();
    }

    public void setPosition(float x, float y) {
        mainButton.setPosition(x, y);
        if (isOpen) updateOptionsLayout();
    }
    public void setSize(float width, float height) {
        mainButton.setSize(width, height);
        rebuildVisibleOptions();
        if (isOpen) updateOptionsLayout();
    }
    public void setWidth(float width) {
        mainButton.setWidth(width);
        rebuildVisibleOptions();
        if (isOpen) updateOptionsLayout();
    }
    public void setHeight(float height) {
        mainButton.setHeight(height);
        if (isOpen) updateOptionsLayout();
    }
    public void setFont(BitmapFont font) {
        mainButton.getTextRenderer().setFont(font);
        indicatorRenderer.setFont(font);
        for (Button btn : optionButtons) {
            btn.getTextRenderer().setFont(font);
        }
        if (isOpen) updateOptionsLayout();
    }
    public void setFont(String fontName) {
        BitmapFont font = fontManager.getFont(fontName);
        mainButton.getTextRenderer().setFont(font);
        indicatorRenderer.setFont(font);
        for (Button btn : optionButtons) {
            btn.getTextRenderer().setFont(font);
        }
        if (isOpen) updateOptionsLayout();
    }
    public void setTextColor(Color color) {
        mainButton.getTextRenderer().setColor(color);
        for (Button btn : optionButtons) {
            btn.getTextRenderer().setColor(color);
        }
    }
    public void setIndicatorColor(Color color) {
        indicatorRenderer.setColor(color != null ? color.cpy() : DEFAULT_INDICATOR_COLOR.cpy());
    }
    public void setOptionHeight(float optionHeight) {
        this.optionHeight = Math.max(10f, optionHeight);
        rebuildVisibleOptions();
    }
    public void setOptionsMaxHeight(float optionsMaxHeight) {
        this.optionsMaxHeight = Math.max(this.optionHeight, optionsMaxHeight);
        if (isOpen) updateOptionsLayout();
    }
    public void setPadding(float padding) {
        this.padding = Math.max(0, padding);
    }
    public void setPlaceholderText(String placeholderText) {
        this.placeholderText = placeholderText != null ? placeholderText : "";
        if (this.selectedIndex == -1) {
            mainButton.getTextRenderer().setText(this.placeholderText);
        }
    }
    public void setOnSelectionChanged(Consumer<String> onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged != null ? onSelectionChanged : (selection) -> {};
    }
    public void setSelectedOption(String option) {
        int index = allOptions.indexOf(option);
        if (index != -1) {
            selectOption(option, index, false);
        } else if (option == null) {
            selectOption(null, -1, false);
        }
    }
    public void setSelectedIndex(int index) {
        if (index >= 0 && index < allOptions.size()) {
            selectOption(allOptions.get(index), index, false);
        } else if (index == -1) {
            selectOption(null, -1, false);
        }
    }
    public void setMainButtonColors(Color normalFill, Color hoverFill, Color pressedFill, Color border) {
        mainButton.setNormalFillColor(normalFill);
        mainButton.setHoverFillColor(hoverFill);
        mainButton.setPressedFillColor(pressedFill);
        mainButton.setNormalBorderColor(border);
        mainButton.setHoverBorderColor(border);
        mainButton.setPressedBorderColor(border);
        mainButton.getBoxRenderer().setBorderEnabled(border != null && border.a > 0);
    }
    public void setOptionButtonColors(Color normalFill, Color hoverFill, Color pressedFill) {
        this.defaultOptionNormalFill = (normalFill != null) ? normalFill.cpy() : ColorUtils.color(75, 75, 75, 255);
        this.defaultOptionHoverFill = (hoverFill != null) ? hoverFill.cpy() : ColorUtils.color(95, 95, 95, 255);
        this.defaultOptionPressedFill = (pressedFill != null) ? pressedFill.cpy() : ColorUtils.color(115, 115, 115, 255);

        for(Button btn : optionButtons) {
            btn.setNormalFillColor(this.defaultOptionNormalFill);
            btn.setHoverFillColor(this.defaultOptionHoverFill);
            btn.setPressedFillColor(this.defaultOptionPressedFill);
        }
    }
    public void setOptionsBackgroundColor(Color fillColor, Color borderColor) {
        // This applies to the background box behind options AND the combined border when open
        optionsBackground.setFillColor(fillColor != null ? fillColor.cpy() : DEFAULT_OPTION_BG_FILL.cpy());
        optionsBackground.setBorderColor(borderColor != null ? borderColor.cpy() : DEFAULT_OPTION_BG_BORDER.cpy());
        optionsBackground.setBorderEnabled(borderColor != null && borderColor.a > 0);
    }
}
