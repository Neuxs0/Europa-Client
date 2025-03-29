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

@SuppressWarnings("unused")
public class Dropdown {
    private final Button mainButton;
    private final TextRenderer indicatorRenderer;
    private final BoxRenderer optionsBackground;
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
    // TODO: Store these defaults to apply to future addOption calls
    private Color defaultOptionNormalFill = ColorUtils.color(75, 75, 75, 255);
    private Color defaultOptionHoverFill = ColorUtils.color(95, 95, 95, 255);
    private Color defaultOptionPressedFill = ColorUtils.color(115, 115, 115, 255);

    public Dropdown() {
        this.fontManager = FontManager.getInstance();
        this.mainButton = new Button();
        this.indicatorRenderer = new TextRenderer();
        this.optionsBackground = new BoxRenderer();
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
            updateOptionsLayout();
        }
    }

    public void addOption(String option) {
        Objects.requireNonNull(option, "Option cannot be null");
        if (!options.contains(option)) {
            options.add(option);
            Button optionButton = createOptionButton(option, options.size() - 1);
            optionButtons.add(optionButton);
            if (isOpen) updateOptionsLayout();
        }
    }

    public void removeOption(String option) {
        int index = options.indexOf(option);
        if (index != -1) {
            options.remove(index);
            optionButtons.remove(index);

            if (selectedIndex == index) {
                selectOption(null, -1, true);
            } else if (selectedIndex > index) {
                selectedIndex--;
            }

            for (int i = index; i < optionButtons.size(); i++) {
                final int currentOptionIndex = i;
                Button btn = optionButtons.get(i);
                String optText = options.get(i);
                btn.setOnClick(b -> selectOption(optText, currentOptionIndex, true));
            }

            if (isOpen) updateOptionsLayout();
        }
    }

    public void clearOptions() {
        options.clear();
        optionButtons.clear();
        selectOption(null, -1, true);
        if (isOpen) updateOptionsLayout();
    }

    private Button createOptionButton(String optionText, int index) {
        Button btn = new Button();
        btn.getTextRenderer().setText(optionText);
        if (mainButton.getTextRenderer().getFont() != null) {
            btn.getTextRenderer().setFont(this.mainButton.getTextRenderer().getFont());
        } else {
            btn.getTextRenderer().setFont(fontManager.getCosmicReachFont());
        }
        btn.getTextRenderer().setColor(this.mainButton.getTextRenderer().getColor());
        btn.setSize(this.getWidth(), optionHeight);
        btn.setNormalFillColor(ColorUtils.color(75, 75, 75, 255));
        btn.setHoverFillColor(ColorUtils.color(95, 95, 95, 255));
        btn.setPressedFillColor(ColorUtils.color(115, 115, 115, 255));
        btn.getBoxRenderer().setBorderEnabled(false);

        final String currentOptionText = optionText;
        final int currentOptionIndex = index;
        btn.setOnClick(button -> selectOption(currentOptionText, currentOptionIndex, true));

        return btn;
    }

    private void selectOption(String option, int index, boolean triggerCallback) {
        boolean changed = this.selectedIndex != index || (option == null && this.selectedIndex != -1);

        this.selectedOption = option;
        this.selectedIndex = index;
        this.mainButton.getTextRenderer().setText(option != null ? option : placeholderText);
        this.isOpen = false;
        this.indicatorRenderer.setText("∨");

        if (changed && triggerCallback && onSelectionChanged != null) {
            onSelectionChanged.accept(this.selectedOption);
        }
    }

    private void updateOptionsLayout() {
        if (!isOpen) return;

        float bgX = mainButton.getX();
        float bgY = mainButton.getY() - (optionButtons.size() * optionHeight);

        float bgWidth = mainButton.getWidth();
        float requiredHeight = optionButtons.size() * optionHeight;
        float bgHeight = Math.min(optionsMaxHeight, requiredHeight);
        // TODO: Implement scrolling if requiredHeight > optionsMaxHeight

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

                boolean overMain = mainButton.getX() <= unprojectedMousePos.x && unprojectedMousePos.x <= mainButton.getX() + mainButton.getWidth() &&
                        mainButton.getY() <= unprojectedMousePos.y && unprojectedMousePos.y <= mainButton.getY() + mainButton.getHeight();

                boolean overOptions = optionsBackground.getWidth() > 0 && optionsBackground.getHeight() > 0 &&
                        optionsBackground.getPosX() <= unprojectedMousePos.x && unprojectedMousePos.x <= optionsBackground.getPosX() + optionsBackground.getWidth() &&
                        optionsBackground.getPosY() <= unprojectedMousePos.y && unprojectedMousePos.y <= optionsBackground.getPosY() + optionsBackground.getHeight();

                if (!overMain && !overOptions) {
                    this.isOpen = false;
                    this.indicatorRenderer.setText("∨");
                }
            }
        }
    }

    public void renderShape(ShapeRenderer shapeRenderer, Viewport viewport) {
        mainButton.renderShape(shapeRenderer, viewport);

        if (isOpen) {
            optionsBackground.render(shapeRenderer);
            for (Button optionBtn : optionButtons) {
                optionBtn.renderShape(shapeRenderer, viewport);
            }
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

        mainTextRenderer.setPosition(
                buttonX + padding,
                buttonY + (buttonHeight - mainTextRenderer.getHeight(viewport)) / 2f
        );
        mainTextRenderer.render(spriteBatch, glyphLayout, viewport);

        indicatorRenderer.setPosition(
                buttonX + buttonWidth - indicatorRenderer.getWidth(viewport) - padding,
                buttonY + (buttonHeight - indicatorRenderer.getHeight(viewport)) / 2f
        );
        indicatorRenderer.render(spriteBatch, glyphLayout, viewport);

        if (isOpen) {
            for (Button optionBtn : optionButtons) {
                optionBtn.renderText(spriteBatch, glyphLayout, viewport);
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
        return new ArrayList<>(options);
    }
    public boolean isOpen() {
        return isOpen;
    }
    public float getX() { return mainButton.getX(); }
    public float getY() { return mainButton.getY(); }
    public float getWidth() { return mainButton.getWidth(); }
    public float getHeight() { return mainButton.getHeight(); }

    public void setPosition(float x, float y) {
        mainButton.setPosition(x, y);
        if (isOpen) updateOptionsLayout();
    }
    public void setSize(float width, float height) {
        mainButton.setSize(width, height);
        for(Button btn : optionButtons) {
            btn.setWidth(width);
        }
        if (isOpen) updateOptionsLayout();
    }
    public void setWidth(float width) {
        mainButton.setWidth(width);
        for(Button btn : optionButtons) {
            btn.setWidth(width);
        }
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
        mainButton.getTextRenderer().setFont(fontName);
        indicatorRenderer.setFont(fontName);
        for (Button btn : optionButtons) {
            btn.getTextRenderer().setFont(fontName);
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
        if (isOpen) updateOptionsLayout();
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
        int index = options.indexOf(option);
        if (index != -1) {
            selectOption(option, index, false); // Don't trigger callback for programmatic set
        } else if (option == null) {
            selectOption(null, -1, false); // Clear selection
        }
        // If option is not in the list, do nothing.
    }
    public void setSelectedIndex(int index) {
        if (index >= 0 && index < options.size()) {
            selectOption(options.get(index), index, false);
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

        // Apply to existing buttons
        for(Button btn : optionButtons) {
            btn.setNormalFillColor(this.defaultOptionNormalFill);
            btn.setHoverFillColor(this.defaultOptionHoverFill);
            btn.setPressedFillColor(this.defaultOptionPressedFill);
        }
    }
    public void setOptionsBackgroundColor(Color fillColor, Color borderColor) {
        optionsBackground.setFillColor(fillColor != null ? fillColor.cpy() : DEFAULT_OPTION_BG_FILL.cpy());
        optionsBackground.setBorderColor(borderColor != null ? borderColor.cpy() : DEFAULT_OPTION_BG_BORDER.cpy());
        optionsBackground.setBorderEnabled(borderColor != null && borderColor.a > 0);
    }
}