package dev.neuxs.europa_client.ui.widgets;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
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

@SuppressWarnings({"rawtypes", "unused"})
public class HudSettingsWidget {
    private static final float DEFAULT_SLIDER_STEP = 0.1f;

    private final BoxRenderer panel = new BoxRenderer();
    private final TextRenderer titleText = new TextRenderer();
    private final Button closeButton = new Button();
    private final List<SettingRow> settingRows = new ArrayList<>();
    private final List<Renderer> renderers = new ArrayList<>();
    private final Runnable onClose;

    private Viewport viewport;
    private float x;
    private float y;
    private float width;
    private float height;

    private final float padding = 8f;
    private final float elementSpacing = 6f;
    private final float headerHeight = 26f;
    private final float inputHeight = 26f;
    private final float rowHeight = 38f;

    public HudSettingsWidget(String title, List<Setting<?>> settings, Runnable onClose) {
        this.onClose = onClose == null ? () -> {} : onClose;

        panel.setFillColor(ColorUtils.color(35, 35, 35, 245));
        panel.setBorder(true);
        panel.setBorderColor(ColorUtils.color(20, 20, 20, 255));
        panel.setBorderWidth(1.5f);
        panel.setBorderRadius(7.5f);
        panel.setZIndex(220);
        renderers.add(panel);

        titleText.setText(title == null || title.isBlank() ? "HUD Settings" : title);
        titleText.setTextColor(ColorUtils.WHITE);
        titleText.setZIndex(230);
        renderers.add(titleText);

        closeButton.setText("X");
        closeButton.setBorderWidth(1.5f);
        closeButton.setBorderRadius(7.5f);
        closeButton.setZIndex(235);
        closeButton.setOnClickUp((renderer, button) -> {
            if (button == Input.Buttons.LEFT) {
                this.onClose.run();
            }
        });
        renderers.add(closeButton);

        if (settings != null) {
            for (Setting<?> setting : settings) {
                SettingRow row = new SettingRow(setting);
                settingRows.add(row);
                renderers.addAll(row.getRenderers());
            }
        }
    }

    public void layout(Viewport viewport, float requestedX, float requestedY) {
        this.viewport = viewport;
        float screenW = viewport == null ? 0f : viewport.getWorldWidth();
        float screenH = viewport == null ? 0f : viewport.getWorldHeight();

        width = Math.min(340f, Math.max(250f, screenW * 0.34f));
        height = padding * 2f + headerHeight
                + settingRows.size() * rowHeight
                + Math.max(0, settingRows.size()) * elementSpacing;

        x = MathUtils.clamp(requestedX, 0f, Math.max(0f, screenW - width));
        y = MathUtils.clamp(requestedY, 0f, Math.max(0f, screenH - height));

        panel.setPos(x, y);
        panel.setSize(width, height);

        float currentY = y + height - padding - headerHeight;
        titleText.setPos(x + padding, currentY + (headerHeight - titleText.getTextHeight(viewport)) / 2f);

        closeButton.setSize(headerHeight, headerHeight);
        closeButton.setPos(x + width - padding - headerHeight, currentY);

        currentY -= elementSpacing + rowHeight;
        for (SettingRow row : settingRows) {
            row.layout(x + padding, currentY, width - padding * 2f, rowHeight);
            currentY -= rowHeight + elementSpacing;
        }
    }

    public void addRenderers(RenderUtil renderUtil) {
        for (Renderer renderer : renderers) {
            renderUtil.addRenderer(renderer);
        }
    }

    public void removeRenderers(RenderUtil renderUtil) {
        for (Renderer renderer : renderers) {
            renderUtil.removeRenderer(renderer);
        }
    }

    public void update(float deltaTime) {
        for (SettingRow row : settingRows) {
            row.update(viewport, deltaTime);
        }
    }

    public boolean contains(float worldX, float worldY) {
        return worldX >= x && worldX <= x + width
                && worldY >= y && worldY <= y + height;
    }

    public boolean handleTouchDown(float worldX, float worldY, int button) {
        for (SettingRow row : settingRows) {
            if (row.handleTouchDown(worldX, worldY, button)) {
                clearTextFocusExcept(row);
                return true;
            }
        }
        if (button == Input.Buttons.LEFT && !contains(worldX, worldY)) {
            clearTextFocusExcept(null);
        }
        return contains(worldX, worldY);
    }

    public boolean keyDown(int keycode) {
        for (SettingRow row : settingRows) {
            if (row.keyDown(keycode)) {
                return true;
            }
        }
        return false;
    }

    public boolean keyUp(int keycode) {
        for (SettingRow row : settingRows) {
            if (row.keyUp(keycode)) {
                return true;
            }
        }
        return false;
    }

    public boolean keyTyped(char character) {
        for (SettingRow row : settingRows) {
            if (row.keyTyped(character)) {
                return true;
            }
        }
        return false;
    }

    private void clearTextFocusExcept(SettingRow activeRow) {
        for (SettingRow row : settingRows) {
            if (row != activeRow) {
                row.clearTextFocus();
            }
        }
    }

    private boolean applySettingValue(Setting<?> setting, Object rawValue) {
        try {
            saveSettingsChange(() -> setting.setValueFromObject(rawValue));
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private boolean applySettingValueSilently(Setting<?> setting, Object rawValue) {
        boolean previousAutoSave = SettingsManager.isAutoSaveEnabled();
        SettingsManager.setAutoSaveEnabled(false);
        try {
            Object convertedValue = setting.convertValue(rawValue);
            Object currentValue = setting.getValue();
            if (currentValue != null && currentValue.equals(convertedValue)) {
                return false;
            }
            setting.setValueFromObject(convertedValue);
            return true;
        } catch (RuntimeException e) {
            return false;
        } finally {
            SettingsManager.setAutoSaveEnabled(previousAutoSave);
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
        SettingsManager.saveSettings();
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
            labelText.setTextColor(ColorUtils.WHITE);
            labelText.setZIndex(230);
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
            toggle.setZIndex(230);
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
            booleanValueText.setZIndex(231);
            renderers.add(booleanValueText);
        }

        private void createTextControl() {
            textInput = new TextInput();
            textInput.setBorderWidth(1.5f);
            textInput.setBorderRadius(7.5f);
            textInput.setPadding(5f);
            textInput.setZIndex(230);
            textInput.setOnEnterPressed(this::applyTextValue);
            textInput.setOnFocusLost(this::applyTextValue);
            renderers.add(textInput);
        }

        private void createNumberControl() {
            slider = new Slider();
            slider.setRange(getSliderMin(), getSliderMax());
            slider.setZIndex(225);
            slider.setOnValueChanged(value -> {
                if (applySettingValueSilently(setting, normalizeStep(value))) {
                    syncFromSetting(true);
                }
            });
            slider.setOnInteractionFinished(SettingsManager::saveSettings);
            renderers.add(slider);

            createTextControl();
        }

        private void createDropdownControl() {
            dropdown = new Dropdown();
            dropdown.setBorderRadius(7.5f);
            dropdown.setPadding(3f);
            dropdown.setZIndex(240);

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

        private void layout(float x, float y, float width, float height) {
            float labelWidth = Math.min(125f, Math.max(82f, width * 0.34f));
            controlX = x + labelWidth + elementSpacing;
            controlY = y + (height - inputHeight) / 2f;
            controlWidth = Math.max(0f, width - labelWidth - elementSpacing);
            controlHeight = inputHeight;

            labelText.setPos(x, y + (height - labelText.getTextHeight(viewport)) / 2f);

            if (toggle != null) {
                toggle.setSize(42f, 22f);
                toggle.setPos(controlX, y + (height - toggle.getHeight()) / 2f);
                if (booleanValueText != null) {
                    booleanValueText.setPos(
                            controlX + toggle.getWidth() + elementSpacing,
                            y + (height - booleanValueText.getTextHeight(viewport)) / 2f
                    );
                }
            } else if (slider != null && textInput != null) {
                float inputWidth = Math.min(80f, Math.max(58f, controlWidth * 0.3f));
                float sliderWidth = Math.max(50f, controlWidth - inputWidth - elementSpacing);
                slider.setSize(sliderWidth, 18f);
                slider.setPos(controlX, y + (height - slider.getHeight()) / 2f);

                textInput.setSize(inputWidth, inputHeight);
                textInput.setPosition(controlX + sliderWidth + elementSpacing, controlY);
            } else if (dropdown != null) {
                dropdown.setSize(controlWidth, inputHeight);
                dropdown.setPosition(controlX, controlY);
            } else if (textInput != null) {
                textInput.setSize(controlWidth, inputHeight);
                textInput.setPosition(controlX, controlY);
            }
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

        private List<Renderer> getRenderers() {
            return renderers;
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
                step = DEFAULT_SLIDER_STEP;
            }

            float base = setting.getMinValue() == null ? 0f : setting.getMinValue();
            return base + Math.round((value - base) / step) * step;
        }
    }
}
