package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
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
import dev.neuxs.europa_client.utils.rendering.TextRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Dropdown extends Renderer {
    private static final Color DEFAULT_FILL_COLOR = ColorUtils.color(50, 50, 50, 255);
    private static final Color DEFAULT_HOVER_FILL_COLOR = ColorUtils.color(70, 70, 70, 255);
    private static final Color DEFAULT_PRESSED_FILL_COLOR = ColorUtils.color(90, 90, 90, 255);
    private static final Color DEFAULT_OPEN_FILL_COLOR = ColorUtils.color(60, 60, 60, 255);
    private static final Color DEFAULT_OPTION_HOVER_FILL_COLOR = ColorUtils.color(80, 80, 80, 255);
    private static final Color DEFAULT_BORDER_COLOR = ColorUtils.color(20, 20, 20, 255);
    private static final Color DEFAULT_TEXT_COLOR = ColorUtils.color(255, 255, 255, 255);
    private static final Color DEFAULT_PLACEHOLDER_COLOR = ColorUtils.color(170, 170, 170, 255);
    private static final float PANEL_OVERLAP = 8f;

    private final BoxRenderer boxRenderer;
    private final BoxRenderer panelRenderer;
    private final BoxRenderer optionRenderer;
    private final TextRenderer textRenderer;
    private final Vector2 mousePos;
    private final List<String> options;

    private String placeholderText;
    private String selectedOption;
    private boolean open;
    private int hoveredOptionIndex;
    private float padding;
    private Consumer<String> onSelectionChanged;

    private Color hoverFillColor;
    private Color pressedFillColor;
    private Color hoverToggledFillColor;
    private Color pressedToggledFillColor;
    private Color hoverBorderColor;
    private Color pressedBorderColor;
    private Color hoverToggledBorderColor;
    private Color pressedToggledBorderColor;
    private Color optionHoverFillColor;
    private Color placeholderTextColor;

    public Dropdown() {
        this.boxRenderer = new BoxRenderer();
        this.panelRenderer = new BoxRenderer();
        this.optionRenderer = new BoxRenderer();
        this.textRenderer = new TextRenderer();
        this.mousePos = new Vector2();
        this.options = new ArrayList<>();

        this.placeholderText = "";
        this.selectedOption = "";
        this.open = false;
        this.hoveredOptionIndex = -1;
        this.padding = 5f;
        this.onSelectionChanged = (selected) -> {};

        this.hoverFillColor = DEFAULT_HOVER_FILL_COLOR.cpy();
        this.pressedFillColor = DEFAULT_PRESSED_FILL_COLOR.cpy();
        this.hoverToggledFillColor = DEFAULT_OPEN_FILL_COLOR.cpy();
        this.pressedToggledFillColor = DEFAULT_OPEN_FILL_COLOR.cpy();
        this.hoverBorderColor = DEFAULT_BORDER_COLOR.cpy();
        this.pressedBorderColor = DEFAULT_BORDER_COLOR.cpy();
        this.hoverToggledBorderColor = DEFAULT_BORDER_COLOR.cpy();
        this.pressedToggledBorderColor = DEFAULT_BORDER_COLOR.cpy();
        this.optionHoverFillColor = DEFAULT_OPTION_HOVER_FILL_COLOR.cpy();
        this.placeholderTextColor = DEFAULT_PLACEHOLDER_COLOR.cpy();

        setRenderType(RenderUtil.RenderType.SHAPE_SPRITE);
        setShapeType(ShapeRenderer.ShapeType.Filled);
        setFillColor(DEFAULT_FILL_COLOR);
        setBorderColor(DEFAULT_BORDER_COLOR);
        setTextColor(DEFAULT_TEXT_COLOR);
        setBorder(true);
        setBorderWidth(1f);
        setText("");

        this.boxRenderer.setBorder(true);
        this.panelRenderer.setBorder(true);
        this.panelRenderer.setTopLeftRounded(false);
        this.panelRenderer.setTopRightRounded(false);
        this.optionRenderer.setBorder(false);
    }

    @Override
    public void update(Viewport viewport) {
        if (viewport == null) {
            return;
        }

        updateMousePosition(viewport);
        boolean mouseTarget = isMouseTarget();
        hoveredOptionIndex = open && mouseTarget ? getOptionIndexAt(mousePos.x, mousePos.y) : -1;

        boolean mouseOverHeader = mouseTarget && isMouseOverHeader(mousePos.x, mousePos.y);
        if (mouseOverHeader) {
            setState(open ? State.HOVER_TOGGLED : State.HOVERED);
        } else {
            setState(open ? State.TOGGLED : State.NORMAL);
        }

    }

    @Override
    public void renderShape(Viewport viewport, ShapeRenderer shapeRenderer) {
        if (!open) {
            syncHeaderRenderer();
            boxRenderer.renderShape(viewport, shapeRenderer);
            return;
        }

        List<String> visibleOptions = getVisibleOptions();
        if (!visibleOptions.isEmpty()) {
            syncPanelRenderer(visibleOptions.size());
            panelRenderer.renderShape(viewport, shapeRenderer);
        }

        if (hoveredOptionIndex >= 0 && hoveredOptionIndex < visibleOptions.size()) {
            syncOptionRenderer(hoveredOptionIndex, visibleOptions.size());
            optionRenderer.renderShape(viewport, shapeRenderer);
        }

        syncHeaderRenderer();
        boxRenderer.renderShape(viewport, shapeRenderer);
    }

    @Override
    public void renderSprite(Viewport viewport, SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        renderText(viewport, spriteBatch, glyphLayout, getDisplayText(), getPosX(), getPosY(), getWidth(), getHeight(), isPlaceholderVisible());

        if (!open) {
            return;
        }

        List<String> visibleOptions = getVisibleOptions();
        for (int i = 0; i < visibleOptions.size(); i++) {
            renderText(viewport, spriteBatch, glyphLayout, visibleOptions.get(i), getOptionX(), getOptionY(i), getOptionWidth(), getOptionHeight(), false);
        }
    }

    private void updateMousePosition(Viewport viewport) {
        mousePos.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mousePos);
    }

    private boolean isMouseOverHeader(float x, float y) {
        return x >= getPosX() && x <= getPosX() + getWidth()
                && y >= getPosY() && y <= getPosY() + getHeight();
    }

    private int getOptionIndexAt(float x, float y) {
        if (x < getOptionX() || x > getOptionX() + getOptionWidth()) {
            return -1;
        }

        int visibleOptionCount = getVisibleOptions().size();
        for (int i = 0; i < visibleOptionCount; i++) {
            float optionY = getOptionY(i);
            if (y >= optionY && y <= optionY + getOptionHeight()) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public boolean blocksMouseAt(float x, float y) {
        return isMouseOverHeader(x, y) || (open && getOptionIndexAt(x, y) >= 0);
    }

    public boolean handleTouchDown(float worldX, float worldY, int button) {
        if (button != Input.Buttons.LEFT) {
            return false;
        }

        hoveredOptionIndex = open ? getOptionIndexAt(worldX, worldY) : -1;
        if (isMouseOverHeader(worldX, worldY)) {
            open = !open;
            setState(open ? State.HOVER_TOGGLED : State.HOVERED);
            return true;
        }

        if (open && hoveredOptionIndex >= 0) {
            selectOption(hoveredOptionIndex, true);
            open = false;
            setState(State.NORMAL);
            return true;
        }

        if (open) {
            open = false;
            setState(State.NORMAL);
            return true;
        }

        return false;
    }

    private void syncHeaderRenderer() {
        Color fillColor = switch (getState()) {
            case PRESSED -> pressedFillColor;
            case HOVERED -> hoverFillColor;
            case TOGGLED, HOVER_TOGGLED -> hoverToggledFillColor;
            case HOVER_PRESSED -> pressedToggledFillColor;
            default -> getFillColor();
        };

        Color borderColor = switch (getState()) {
            case PRESSED -> pressedBorderColor;
            case HOVERED -> hoverBorderColor;
            case TOGGLED, HOVER_TOGGLED -> hoverToggledBorderColor;
            case HOVER_PRESSED -> pressedToggledBorderColor;
            default -> getBorderColor();
        };

        boxRenderer.setPos(getPos());
        boxRenderer.setSize(getSize());
        boxRenderer.setFillColor(fillColor);
        boxRenderer.setBorder(isBorder());
        boxRenderer.setBorderWidth(getBorderWidth());
        boxRenderer.setBorderRadius(getBorderRadius());
        boxRenderer.setBorderColor(borderColor);
    }

    private void syncPanelRenderer(int optionCount) {
        float overlap = getPanelOverlap();
        float optionHeight = getOptionHeight();

        panelRenderer.setPos(getPosX(), getPosY() - optionHeight * optionCount);
        panelRenderer.setSize(getWidth(), optionHeight * optionCount + overlap);
        panelRenderer.setFillColor(getFillColor());
        panelRenderer.setBorder(isBorder());
        panelRenderer.setBorderWidth(getBorderWidth());
        panelRenderer.setBorderRadius(getBorderRadius());
        panelRenderer.setBorderColor(getBorderColor());
    }

    private void syncOptionRenderer(int index, int optionCount) {
        float borderInset = isBorder() ? getBorderWidth() : 0f;
        float optionY = getOptionY(index);
        float optionHeight = getOptionHeight();

        if (index == 0) {
            optionHeight += getPanelOverlap();
        }

        if (index == optionCount - 1) {
            optionY += borderInset;
            optionHeight = Math.max(0f, optionHeight - borderInset);
        }

        boolean bottomOption = index == optionCount - 1;
        optionRenderer.setPos(getOptionX() + borderInset, optionY);
        optionRenderer.setSize(Math.max(0f, getOptionWidth() - borderInset * 2f), optionHeight);
        optionRenderer.setFillColor(optionHoverFillColor);
        optionRenderer.setBorder(false);
        optionRenderer.setBorderWidth(0f);
        optionRenderer.setBorderRadius(bottomOption ? Math.max(0f, getBorderRadius() - borderInset) : 0f);
        optionRenderer.setBorderColor(getBorderColor());
        optionRenderer.setTopLeftRounded(false);
        optionRenderer.setTopRightRounded(false);
        optionRenderer.setBottomLeftRounded(bottomOption);
        optionRenderer.setBottomRightRounded(bottomOption);
    }

    private void renderText(Viewport viewport, SpriteBatch spriteBatch, GlyphLayout glyphLayout, String text, float x, float y, float width, float height, boolean placeholder) {
        if (text == null || text.isEmpty() || textRenderer.getFont() == null) {
            return;
        }

        textRenderer.setText(text);
        textRenderer.setTextColor(placeholder ? placeholderTextColor : getTextColor());
        textRenderer.fitToBox(
                viewport,
                Math.max(0f, width - padding * 2f),
                Math.max(0f, height - padding * 2f)
        );
        textRenderer.setPos(
                x + padding,
                y + (height - textRenderer.getTextHeight(viewport)) / 2f
        );
        textRenderer.renderSprite(viewport, spriteBatch, glyphLayout);
    }

    private String getDisplayText() {
        return selectedOption == null || selectedOption.isEmpty() ? placeholderText : selectedOption;
    }

    private boolean isPlaceholderVisible() {
        return selectedOption == null || selectedOption.isEmpty();
    }

    private float getOptionX() {
        return getPosX();
    }

    private float getOptionWidth() {
        return getWidth();
    }

    private float getOptionHeight() {
        return Math.max(1f, getHeight());
    }

    private float getOptionY(int index) {
        return getPosY() - getOptionHeight() * (index + 1);
    }

    private float getPanelOverlap() {
        return Math.min(PANEL_OVERLAP, Math.max(0f, getHeight()));
    }

    private void selectOption(int index, boolean triggerCallback) {
        List<String> visibleOptions = getVisibleOptions();
        if (index < 0 || index >= visibleOptions.size()) {
            return;
        }

        setSelectedOption(visibleOptions.get(index), triggerCallback);
    }

    private List<String> getVisibleOptions() {
        List<String> visibleOptions = new ArrayList<>(options);
        visibleOptions.remove(selectedOption);
        return visibleOptions;
    }

    private void setSelectedOption(String option, boolean triggerCallback) {
        if (option == null || !options.contains(option)) {
            return;
        }

        boolean changed = !option.equals(selectedOption);
        selectedOption = option;
        setText(option);

        if (changed && triggerCallback && onSelectionChanged != null) {
            onSelectionChanged.accept(selectedOption);
        }
    }

    @Override
    public void setPos(Vector3 pos) {
        super.setPos(pos);
    }

    @Override
    public void setPos(Vector2 pos) {
        super.setPos(pos);
    }

    public void setPosition(float x, float y) {
        setPos(x, y);
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(Math.max(0f, width), Math.max(1f, height));
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(Math.max(1f, height));
    }

    @Override
    public void setText(String text) {
        super.setText(text == null ? "" : text);
        textRenderer.setText(text == null ? "" : text);
    }

    @Override
    public String getText() {
        return textRenderer.getText();
    }

    @Override
    public void setTextColor(Color color) {
        super.setTextColor(color);
        textRenderer.setTextColor(color);
    }

    public void addOption(String option) {
        if (option == null || option.isEmpty() || options.contains(option)) {
            return;
        }

        options.add(option);
    }

    public void removeOption(String option) {
        if (option == null || !options.remove(option)) {
            return;
        }

        if (option.equals(selectedOption)) {
            selectedOption = "";
            setText("");
        }

        hoveredOptionIndex = MathUtils.clamp(hoveredOptionIndex, -1, options.size() - 1);
    }

    public void clearOptions() {
        options.clear();
        selectedOption = "";
        hoveredOptionIndex = -1;
        open = false;
        setText("");
    }

    public void setOptions(List<String> options) {
        clearOptions();
        if (options == null) {
            return;
        }

        for (String option : options) {
            addOption(option);
        }
    }

    public void setSelectedOption(String option) {
        setSelectedOption(option, true);
    }

    public void setSelectedOptionSilent(String option) {
        setSelectedOption(option, false);
    }

    public void setPlaceholderText(String placeholderText) {
        this.placeholderText = placeholderText == null ? "" : placeholderText;
    }

    public void setPadding(float padding) {
        this.padding = Math.max(0f, padding);
    }

    public void setOpen(boolean open) {
        this.open = open;
        hoveredOptionIndex = -1;
    }

    public void toggleOpen() {
        setOpen(!open);
    }

    public void setOnSelectionChanged(Consumer<String> onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged != null ? onSelectionChanged : (selected) -> {};
    }

    public BoxRenderer getBoxRenderer() {
        return boxRenderer;
    }

    public BoxRenderer getPanelRenderer() {
        return panelRenderer;
    }

    public BoxRenderer getOptionRenderer() {
        return optionRenderer;
    }

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    public List<String> getOptions() {
        return new ArrayList<>(options);
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    public String getPlaceholderText() {
        return placeholderText;
    }

    public boolean isOpen() {
        return open;
    }

    public int getHoveredOptionIndex() {
        return hoveredOptionIndex;
    }

    public float getPadding() {
        return padding;
    }

    public Consumer<String> getOnSelectionChanged() {
        return onSelectionChanged;
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

    public Color getOptionHoverFillColor() {
        return optionHoverFillColor.cpy();
    }

    public Color getPlaceholderTextColor() {
        return placeholderTextColor.cpy();
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

    public void setOptionHoverFillColor(Color color) {
        if (color != null) this.optionHoverFillColor = color.cpy();
    }

    public void setPlaceholderTextColor(Color color) {
        if (color != null) this.placeholderTextColor = color.cpy();
    }
}
