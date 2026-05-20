package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.settings.ClientSettings;
import dev.neuxs.europa_client.settings.Setting;
import dev.neuxs.europa_client.settings.SettingsManager;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;
import dev.neuxs.europa_client.utils.rendering.Renderer;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;
import dev.neuxs.europa_client.utils.rendering.ui.Button;
import dev.neuxs.europa_client.utils.rendering.ui.Dropdown;
import dev.neuxs.europa_client.utils.rendering.ui.Slider;
import dev.neuxs.europa_client.utils.rendering.ui.TextInput;
import dev.neuxs.europa_client.utils.rendering.ui.Toggle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings({"unused", "rawtypes"})
public class SettingsPage extends Page implements InputProcessor {
    private Viewport viewport;
    private final Vector4 pageDim;
    private final BoxRenderer pageContainer;
    private final Vector2 touchPos = new Vector2();

    private final Button resetButton = new Button();
    private final List<SettingRow> settingRows = new ArrayList<>();

    private final float padding = 8f;
    private final float elementSpacing = 6f;
    private final float inputHeight = 26f;
    private final float rowHeight = 38f;
    private final float resetButtonWidth = 90f;

    public SettingsPage(BoxRenderer pageContainer) {
        super("Settings", pageContainer);
        this.pageContainer = pageContainer;
        this.pageDim = new Vector4(pageContainer.getPosX(), pageContainer.getPosY(), pageContainer.getWidth(), pageContainer.getHeight());
    }

    @Override
    public void create(Viewport viewport, float width, float height) {
        super.create(viewport, width, height);
        this.viewport = viewport;

        configureResetButton();

        settingRows.clear();
        for (Setting<?> setting : ClientSettings.getSettings().values()) {
            settingRows.add(new SettingRow(setting));
        }

        resize(width, height);
    }

    @Override
    public void resize(float width, float height) {
        super.resize(width, height);
        this.pageDim.set(pageContainer.getPosX(), pageContainer.getPosY(), pageContainer.getWidth(), pageContainer.getHeight());

        float usableWidth = pageDim.z - padding * 2f;
        float currentY = pageDim.y + pageDim.w - padding - inputHeight;

        resetButton.setSize(resetButtonWidth, inputHeight);
        resetButton.setPos(pageDim.x + pageDim.z - padding - resetButtonWidth, currentY);
        resetButton.setZIndex(5);

        currentY -= rowHeight + elementSpacing * 1.5f;
        for (SettingRow row : settingRows) {
            row.layout(pageDim.x + padding, currentY, usableWidth, rowHeight);
            currentY -= rowHeight + elementSpacing;
        }
    }

    @Override
    public void addRenderers(RenderUtil renderUtil) {
        renderUtil.addRenderer(resetButton);
        for (SettingRow row : settingRows) {
            row.addRenderers(renderUtil);
        }
    }

    @Override
    public void removeRenderers(RenderUtil renderUtil) {
        renderUtil.removeRenderer(resetButton);
        for (SettingRow row : settingRows) {
            row.removeRenderers(renderUtil);
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        for (SettingRow row : settingRows) {
            row.update(viewport, deltaTime);
        }
    }

    @Override
    public void dispose(RenderUtil renderUtil) {
        super.dispose(renderUtil);
        settingRows.clear();
    }

    private void configureResetButton() {
        resetButton.setText("Reset");
        resetButton.setBorderWidth(1.5f);
        resetButton.setBorderRadius(7.5f);
        resetButton.setOnClickUp((renderer, button) -> {
            saveSettingsChange(ClientSettings::resetAll);
            for (SettingRow row : settingRows) {
                row.syncFromSetting(true);
            }
        });
    }

    private boolean applySettingValue(Setting<?> setting, Object rawValue) {
        try {
            saveSettingsChange(() -> setting.setValueFromObject(rawValue));
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private void saveSettingsChange(Runnable change) {
        boolean previousAutoSave = SettingsManager.isAutoSaveEnabled();
        SettingsManager.setAutoSaveEnabled(false);
        try {
            change.run();
        } finally {
            SettingsManager.setAutoSaveEnabled(previousAutoSave);
        }
        SettingsManager.saveClientSettings();
    }

    @Override
    public boolean keyDown(int keycode) {
        for (SettingRow row : settingRows) {
            if (row.keyDown(keycode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        for (SettingRow row : settingRows) {
            if (row.keyUp(keycode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        for (SettingRow row : settingRows) {
            if (row.keyTyped(character)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (viewport == null) {
            return false;
        }

        touchPos.set(screenX, screenY);
        viewport.unproject(touchPos);

        boolean handled = false;
        SettingRow activeRow = null;
        for (SettingRow row : settingRows) {
            if (row.handleTouchDown(touchPos.x, touchPos.y, button)) {
                handled = true;
                activeRow = row;
            }
        }

        if (handled) {
            for (SettingRow row : settingRows) {
                if (row != activeRow) {
                    row.clearTextFocus();
                }
            }
        }
        return handled;
    }

    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }
    @Override public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }
    @Override public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    private static String formatSettingValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Boolean bool) {
            return bool ? "On" : "Off";
        }
        if (value instanceof Float || value instanceof Double) {
            return formatNumber(((Number) value).doubleValue());
        }
        return String.valueOf(value);
    }

    private static String formatNumber(double value) {
        if (!Double.isFinite(value)) {
            return String.valueOf(value);
        }

        String text = String.format(Locale.ROOT, "%.3f", value);
        while (text.contains(".") && text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);
        }
        if (text.endsWith(".")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    private class SettingRow {
        private final Setting<?> setting;
        private final TextRenderer labelText = new TextRenderer();
        private final List<Renderer> renderers = new ArrayList<>();
        private final List<Object> optionValues = new ArrayList<>();

        private Toggle toggle;
        private TextRenderer booleanValueText;
        private TextInput textInput;
        private Slider slider;
        private Dropdown dropdown;
        private float controlX;
        private float controlY;
        private float controlWidth;
        private float controlHeight;

        private SettingRow(Setting<?> setting) {
            this.setting = setting;

            labelText.setText(setting.getDisplayName());
            labelText.setTextColor(ColorUtils.color(255, 255, 255, 255));
            labelText.setZIndex(10);
            renderers.add(labelText);

            if (setting.hasOptions()) {
                createDropdownControl();
            } else if (setting.getDefaultValue() instanceof Boolean) {
                createBooleanControl();
            } else if (setting.getDefaultValue() instanceof Number) {
                createNumberControl();
            } else {
                createTextControl();
            }

            syncFromSetting(true);
        }

        private void createBooleanControl() {
            toggle = new Toggle();
            toggle.setZIndex(10);
            toggle.setInputHandlingEnabled(false);
            toggle.setOnValueChanged(value -> {
                if (applySettingValue(setting, value)) {
                    syncFromSetting(true);
                } else {
                    syncFromSetting(true);
                }
            });
            renderers.add(toggle);

            booleanValueText = new TextRenderer();
            booleanValueText.setTextColor(ColorUtils.color(210, 210, 210, 255));
            booleanValueText.setZIndex(11);
            renderers.add(booleanValueText);
        }

        private void createTextControl() {
            textInput = new TextInput();
            configureTextInput(textInput);
            textInput.setOnEnterPressed(this::applyTextValue);
            textInput.setOnFocusLost(this::applyTextValue);
            renderers.add(textInput);
        }

        private void createNumberControl() {
            slider = new Slider();
            slider.setRange(getSliderMin(), getSliderMax());
            slider.setZIndex(5);
            slider.setOnValueChanged(value -> {
                if (applySettingValue(setting, normalizeStep(value))) {
                    syncFromSetting(true);
                }
            });
            renderers.add(slider);

            createTextControl();
        }

        private void createDropdownControl() {
            dropdown = new Dropdown();
            dropdown.setBorderRadius(7.5f);
            dropdown.setPadding(3f);
            dropdown.setZIndex(35);

            List<String> optionLabels = new ArrayList<>();
            for (Object option : setting.getOptions()) {
                optionValues.add(option);
                optionLabels.add(formatSettingValue(option));
            }
            dropdown.setOptions(optionLabels);
            dropdown.setOnSelectionChanged(selected -> {
                Object selectedValue = findOptionValue(selected);
                if (selectedValue != null && applySettingValue(setting, selectedValue)) {
                    syncFromSetting(true);
                }
            });
            renderers.add(dropdown);
        }

        private void configureTextInput(TextInput input) {
            input.setBorderWidth(1.5f);
            input.setBorderRadius(7.5f);
            input.setPadding(5f);
            input.setZIndex(10);
        }

        private void layout(float x, float y, float width, float height) {
            float labelWidth = Math.min(130f, Math.max(90f, width * 0.32f));
            controlX = x + labelWidth + elementSpacing;
            controlY = y + (height - inputHeight) / 2f;
            controlWidth = Math.max(0f, width - labelWidth - elementSpacing);
            controlHeight = inputHeight;

            labelText.setPos(x, centerTextY(labelText, y, height));

            if (toggle != null) {
                toggle.setSize(42f, 22f);
                toggle.setPos(controlX, y + (height - toggle.getHeight()) / 2f);
                if (booleanValueText != null) {
                    booleanValueText.setPos(controlX + toggle.getWidth() + elementSpacing, centerTextY(booleanValueText, y, height));
                }
            } else if (slider != null && textInput != null) {
                float inputWidth = Math.min(82f, Math.max(62f, controlWidth * 0.28f));
                float sliderWidth = Math.max(60f, controlWidth - inputWidth - elementSpacing);
                slider.setSize(sliderWidth, 18f);
                slider.setPos(controlX, y + (height - slider.getHeight()) / 2f);

                textInput.setSize(inputWidth, inputHeight);
                textInput.setPosition(controlX + sliderWidth + elementSpacing, y + (height - inputHeight) / 2f);
            } else if (dropdown != null) {
                dropdown.setSize(controlWidth, inputHeight);
                dropdown.setPosition(controlX, y + (height - inputHeight) / 2f);
            } else if (textInput != null) {
                textInput.setSize(controlWidth, inputHeight);
                textInput.setPosition(controlX, y + (height - inputHeight) / 2f);
            }
        }

        private float centerTextY(TextRenderer renderer, float y, float height) {
            return y + (height - renderer.getTextHeight(viewport)) / 2f;
        }

        private void update(Viewport viewport, float deltaTime) {
            if (toggle != null) {
                toggle.update(viewport);
            }
            if (slider != null) {
                slider.update(viewport);
            }
            if (textInput != null) {
                textInput.update(viewport, deltaTime);
            }
            if (dropdown != null) {
                dropdown.update(viewport);
            }
            syncFromSetting(false);
        }

        private void syncFromSetting(boolean forceTextSync) {
            Object value = setting.getValue();
            if (toggle != null && value instanceof Boolean bool) {
                if (toggle.isToggled() != bool) {
                    toggle.setEnabledSilent(bool);
                }
                if (booleanValueText != null) {
                    booleanValueText.setText(formatSettingValue(bool));
                }
            }
            if (slider != null && value instanceof Number) {
                slider.setValueSilent(((Number) value).floatValue());
            }
            if (textInput != null && (forceTextSync || !textInput.isFocused())) {
                textInput.setTextSilent(formatSettingValue(value));
            }
            if (dropdown != null) {
                String selectedLabel = formatSettingValue(value);
                if (dropdown.getOptions().contains(selectedLabel)) {
                    dropdown.setSelectedOptionSilent(selectedLabel);
                }
            }
        }

        private boolean handleTouchDown(float worldX, float worldY, int button) {
            if (toggle != null && button == Input.Buttons.LEFT && containsControlPoint(worldX, worldY)) {
                toggle.toggleState();
                clearTextFocus();
                return true;
            }
            if (dropdown != null && dropdown.handleTouchDown(worldX, worldY, button)) {
                clearTextFocus();
                return true;
            }
            return textInput != null && textInput.handleTouchDown(worldX, worldY, button);
        }

        private boolean keyDown(int keycode) {
            return textInput != null && textInput.keyDown(keycode);
        }

        private boolean keyUp(int keycode) {
            return textInput != null && textInput.keyUp(keycode);
        }

        private boolean keyTyped(char character) {
            return textInput != null && textInput.keyTyped(character);
        }

        private void clearTextFocus() {
            if (textInput != null) {
                textInput.setFocus(false);
            }
        }

        private boolean containsControlPoint(float worldX, float worldY) {
            return worldX >= controlX && worldX <= controlX + controlWidth
                    && worldY >= controlY && worldY <= controlY + controlHeight;
        }

        private void addRenderers(RenderUtil renderUtil) {
            for (Renderer renderer : renderers) {
                renderUtil.addRenderer(renderer);
            }
        }

        private void removeRenderers(RenderUtil renderUtil) {
            for (Renderer renderer : renderers) {
                renderUtil.removeRenderer(renderer);
            }
        }

        private void applyTextValue(String text) {
            if (applySettingValue(setting, text)) {
                syncFromSetting(true);
            }
        }

        private Object findOptionValue(String label) {
            for (Object optionValue : optionValues) {
                if (formatSettingValue(optionValue).equals(label)) {
                    return optionValue;
                }
            }
            return null;
        }

        private float getSliderMin() {
            Float configuredMin = setting.getMinValue();
            if (configuredMin != null) {
                return configuredMin;
            }

            float current = getNumericSettingValue();
            return Math.min(0f, current);
        }

        private float getSliderMax() {
            Float configuredMax = setting.getMaxValue();
            if (configuredMax != null) {
                return configuredMax;
            }

            float min = getSliderMin();
            float current = getNumericSettingValue();
            return Math.max(min + 1f, Math.max(1f, current * 4f));
        }

        private float getNumericSettingValue() {
            Object value = setting.getValue();
            return value instanceof Number number ? number.floatValue() : 0f;
        }

        private float normalizeStep(float value) {
            Float step = setting.getStep();
            if (step == null || step <= 0f) {
                return value;
            }

            float base = setting.getMinValue() == null ? 0f : setting.getMinValue();
            return base + Math.round((value - base) / step) * step;
        }
    }
}
