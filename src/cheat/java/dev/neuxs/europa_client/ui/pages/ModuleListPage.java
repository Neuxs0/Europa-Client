package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.modules.Module;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "rawtypes"})
public abstract class ModuleListPage extends Page implements InputProcessor {
    private static final Map<String, Set<String>> EXPANDED_MODULE_IDS_BY_PAGE = new LinkedHashMap<>();
    private static final Map<String, SortType> SORT_TYPES_BY_PAGE = new LinkedHashMap<>();

    private final Supplier<List<Module>> modulesSupplier;
    private final Vector4 pageDim;
    private final BoxRenderer pageContainer;
    private final TextInput searchInput = new TextInput();
    private final Dropdown sortDropdown = new Dropdown();
    private final Vector2 touchPos = new Vector2();
    private final List<ModuleEntry> moduleEntries = new ArrayList<>();
    private final List<ModuleEntry> filteredAndSortedEntries = new ArrayList<>();
    private final TextRenderer leftClickText = new TextRenderer();
    private final TextRenderer rightClickText = new TextRenderer();
    private RenderUtil renderUtil;
    private Viewport viewport;
    private boolean renderersAdded = false;

    private final float padding = 5f;
    private final float elementSpacing = 5f;
    private final float topBarHeight = 25f;
    private final float sortButtonWidth = 100f;
    private final float moduleButtonHeight = 30f;
    private final float expandedPadding = 6f;
    private final float expandedBoxOverlap = 8f;
    private final float settingRowHeight = 30f;

    public enum SortType {
        A_Z, Z_A, ON_OFF, OFF_ON
    }

    private SortType currentSortType = SortType.A_Z;
    private String previousSearchText = "";
    private final Map<String, SortType> sortDisplayMap = new LinkedHashMap<>();

    protected ModuleListPage(String pageTitle, BoxRenderer pageContainer, Supplier<List<Module>> modulesSupplier) {
        super(pageTitle, pageContainer);
        this.pageContainer = pageContainer;
        this.modulesSupplier = modulesSupplier;
        this.pageDim = new Vector4(pageContainer.getPosX(), pageContainer.getPosY(), pageContainer.getWidth(), pageContainer.getHeight());
        initializeSortMap();
    }

    @Override
    public void create(Viewport viewport, float width, float height) {
        super.create(viewport, width, height);
        this.viewport = viewport;
        this.currentSortType = getSavedSortType();

        searchInput.setSize(150, topBarHeight);
        searchInput.setBorderRadius(7.5f);
        searchInput.setPlaceholder("Search...");
        searchInput.setOnTextChanged(text -> {
            previousSearchText = text;
            applyFiltersAndSort();
        });
        searchInput.setOnFocusLost(text -> previousSearchText = text);

        sortDropdown.setSize(sortButtonWidth, topBarHeight);
        sortDropdown.setBorderRadius(7.5f);
        sortDropdown.setPlaceholderText(getSortDisplayName(currentSortType));
        sortDropdown.setPadding(3f);
        sortDropdown.setOptions(new ArrayList<>(sortDisplayMap.keySet()));
        sortDropdown.setSelectedOptionSilent(getSortDisplayName(currentSortType));
        sortDropdown.setOnSelectionChanged(selectedDisplayName -> setSortType(sortDisplayMap.getOrDefault(selectedDisplayName, SortType.A_Z)));

        moduleEntries.clear();
        for (Module module : modulesSupplier.get()) {
            moduleEntries.add(new ModuleEntry(module));
        }

        leftClickText.setText("Left click to toggle");
        leftClickText.setFillColor(ColorUtils.color(255, 255, 255, 255));

        rightClickText.setText("Right click to expand");
        rightClickText.setFillColor(ColorUtils.color(255, 255, 255, 255));

        resize(width, height);
        applyFiltersAndSort();
    }

    @Override
    public void resize(float width, float height) {
        super.resize(width, height);
        this.pageDim.set(pageContainer.getPosX(), pageContainer.getPosY(), pageContainer.getWidth(), pageContainer.getHeight());

        float currentX = pageDim.x + padding;
        float currentY = pageDim.y + pageDim.w - padding - topBarHeight;

        float searchWidth = pageDim.z - padding * 2 - sortButtonWidth - elementSpacing;
        searchInput.setSize(searchWidth, topBarHeight);
        searchInput.setPosition(currentX, currentY);
        searchInput.setZIndex(10);

        currentX += searchWidth + elementSpacing;
        sortDropdown.setPosition(currentX, currentY);
        sortDropdown.setZIndex(20);

        repositionEntries();

        leftClickText.setPos(pageDim.x + padding, pageDim.y + padding);
        rightClickText.setPos(pageDim.x + pageDim.z - padding - rightClickText.getTextWidth(viewport), pageDim.y + padding);
    }

    @Override
    public void addRenderers(RenderUtil renderUtil) {
        this.renderUtil = renderUtil;
        this.renderersAdded = true;

        renderUtil.addRenderer(searchInput);
        for (ModuleEntry entry : filteredAndSortedEntries) {
            entry.addRenderers(renderUtil);
        }
        renderUtil.addRenderer(sortDropdown);
        renderUtil.addRenderer(leftClickText);
        renderUtil.addRenderer(rightClickText);
    }

    @Override
    public void removeRenderers(RenderUtil renderUtil) {
        this.renderersAdded = false;

        renderUtil.removeRenderer(searchInput);
        renderUtil.removeRenderer(sortDropdown);
        for (ModuleEntry entry : filteredAndSortedEntries) {
            entry.removeRenderers(renderUtil);
        }
        renderUtil.removeRenderer(leftClickText);
        renderUtil.removeRenderer(rightClickText);
    }

    @Override
    public void update(float deltaTime) {
        searchInput.update(viewport, deltaTime);
        sortDropdown.update(viewport);
        for (ModuleEntry entry : filteredAndSortedEntries) {
            entry.update(viewport, deltaTime);
        }

        String currentSearchText = searchInput.getText();
        if (!currentSearchText.equals(previousSearchText)) {
            previousSearchText = currentSearchText;
            applyFiltersAndSort();
        }
    }

    @Override
    public void dispose(RenderUtil renderUtil) {
        super.dispose(renderUtil);
        moduleEntries.clear();
        filteredAndSortedEntries.clear();
    }

    private void repositionEntries() {
        float gridWidth = pageDim.z - padding * 2f;
        float moduleWidth = (gridWidth - elementSpacing) / 2f;
        float leftX = pageDim.x + padding;
        float rightX = leftX + moduleWidth + elementSpacing;
        float leftY = pageDim.y + pageDim.w - padding - topBarHeight - elementSpacing - moduleButtonHeight;
        float rightY = leftY;

        for (ModuleEntry entry : filteredAndSortedEntries) {
            boolean useLeftColumn = leftY >= rightY;
            float x = useLeftColumn ? leftX : rightX;
            float y = useLeftColumn ? leftY : rightY;
            entry.layout(x, y, moduleWidth);
            float nextY = y - entry.getHeight() - elementSpacing;
            if (useLeftColumn) {
                leftY = nextY;
            } else {
                rightY = nextY;
            }
        }
    }

    private void initializeSortMap() {
        sortDisplayMap.put("A-Z", SortType.A_Z);
        sortDisplayMap.put("Z-A", SortType.Z_A);
        sortDisplayMap.put("On-Off", SortType.ON_OFF);
        sortDisplayMap.put("Off-On", SortType.OFF_ON);
    }

    private String getSortDisplayName(SortType type) {
        for (Map.Entry<String, SortType> entry : sortDisplayMap.entrySet()) {
            if (entry.getValue() == type) {
                return entry.getKey();
            }
        }
        return "A-Z";
    }

    private void applyFiltersAndSort() {
        String searchTerm = searchInput.getText().toLowerCase(Locale.ROOT);
        List<ModuleEntry> previousEntries = new ArrayList<>(filteredAndSortedEntries);

        List<ModuleEntry> filtered = moduleEntries.stream()
                .filter(entry -> entry.module.getId().toLowerCase(Locale.ROOT).contains(searchTerm))
                .collect(Collectors.toList());

        Comparator<ModuleEntry> comparator = switch (currentSortType) {
            case Z_A -> Comparator.<ModuleEntry, String>comparing(entry -> entry.module.getId()).reversed();
            case ON_OFF -> Comparator.<ModuleEntry, Boolean>comparing(entry -> entry.module.isEnabled()).reversed()
                    .thenComparing(entry -> entry.module.getId());
            case OFF_ON -> Comparator.<ModuleEntry, Boolean>comparing(entry -> entry.module.isEnabled())
                    .thenComparing(entry -> entry.module.getId());
            default -> Comparator.comparing(entry -> entry.module.getId());
        };

        filtered.sort(comparator);

        filteredAndSortedEntries.clear();
        filteredAndSortedEntries.addAll(filtered);

        repositionEntries();
        syncFilteredRenderers(previousEntries);
    }

    public void setSortType(SortType type) {
        if (this.currentSortType != type) {
            this.currentSortType = type;
            SORT_TYPES_BY_PAGE.put(getTitle(), type);
            sortDropdown.setSelectedOptionSilent(getSortDisplayName(type));
            applyFiltersAndSort();
        }
    }

    private SortType getSavedSortType() {
        return SORT_TYPES_BY_PAGE.getOrDefault(getTitle(), SortType.A_Z);
    }

    private void syncFilteredRenderers(List<ModuleEntry> previousEntries) {
        if (!renderersAdded || renderUtil == null) {
            return;
        }

        Set<ModuleEntry> previousSet = new HashSet<>(previousEntries);
        Set<ModuleEntry> currentSet = new HashSet<>(filteredAndSortedEntries);

        for (ModuleEntry entry : previousEntries) {
            if (!currentSet.contains(entry)) {
                entry.removeRenderers(renderUtil);
            }
        }
        for (ModuleEntry entry : filteredAndSortedEntries) {
            if (!previousSet.contains(entry)) {
                entry.addRenderers(renderUtil);
            } else {
                entry.syncExpandedRenderers(renderUtil);
            }
        }
    }

    private void refreshEntryLayout(ModuleEntry entry) {
        if (renderersAdded && renderUtil != null) {
            entry.syncExpandedRenderers(renderUtil);
        }
        repositionEntries();
    }

    private void refreshAfterModuleToggle(ModuleEntry entry) {
        entry.syncFromModule();
        if (currentSortType == SortType.ON_OFF || currentSortType == SortType.OFF_ON) {
            applyFiltersAndSort();
        }
    }

    private boolean applySettingValue(Setting<?> setting, Object rawValue) {
        try {
            saveModuleChange(() -> setting.setValueFromObject(rawValue));
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private void saveModuleChange(Runnable change) {
        boolean previousAutoSave = SettingsManager.isAutoSaveEnabled();
        SettingsManager.setAutoSaveEnabled(false);
        try {
            change.run();
        } finally {
            SettingsManager.setAutoSaveEnabled(previousAutoSave);
        }
        SettingsManager.saveSettings();
    }

    @Override public boolean keyDown(int keycode) {
        for (ModuleEntry entry : filteredAndSortedEntries) {
            if (entry.keyDown(keycode)) {
                return true;
            }
        }
        return searchInput.keyDown(keycode);
    }

    @Override public boolean keyUp(int keycode) {
        for (ModuleEntry entry : filteredAndSortedEntries) {
            if (entry.keyUp(keycode)) {
                return true;
            }
        }
        return searchInput.keyUp(keycode);
    }

    @Override public boolean keyTyped(char character) {
        for (ModuleEntry entry : filteredAndSortedEntries) {
            if (entry.keyTyped(character)) {
                return true;
            }
        }
        return searchInput.keyTyped(character);
    }

    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (viewport == null) {
            return false;
        }

        touchPos.set(screenX, screenY);
        viewport.unproject(touchPos);

        for (ModuleEntry entry : filteredAndSortedEntries) {
            if (entry.handleTouchDown(touchPos.x, touchPos.y, button)) {
                searchInput.setFocus(false);
                clearTextFocusExcept(entry);
                return true;
            }
        }

        if (sortDropdown.handleTouchDown(touchPos.x, touchPos.y, button)) {
            searchInput.setFocus(false);
            clearTextFocusExcept(null);
            return true;
        }

        boolean handled = searchInput.handleTouchDown(touchPos.x, touchPos.y, button);
        if (handled) {
            clearTextFocusExcept(null);
        }
        return handled;
    }

    @Override public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }
    @Override public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }
    @Override public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }
    @Override public boolean mouseMoved(int i, int i1) {
        return false;
    }
    @Override public boolean scrolled(float v, float v1) {
        return false;
    }

    private void clearTextFocusExcept(ModuleEntry activeEntry) {
        for (ModuleEntry entry : filteredAndSortedEntries) {
            if (entry != activeEntry) {
                entry.clearTextFocus();
            }
        }
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

    private class ModuleEntry {
        private final Module module;
        private final Button button = new Button();
        private final BoxRenderer settingsBox = new BoxRenderer();
        private final List<Renderer> expandedRenderers = new ArrayList<>();
        private final List<ModuleSettingRow> settingRows = new ArrayList<>();
        private boolean expanded = false;
        private boolean expandedRenderersAdded = false;
        private float height = moduleButtonHeight;

        private ModuleEntry(Module module) {
            this.module = module;
            this.expanded = getExpandedModuleIds().contains(module.getId());

            button.setToggleEnabled(true);
            button.setText(module.getId());
            button.setBorderWidth(1.5f);
            button.setBorderRadius(7.5f);
            button.setSize(100, moduleButtonHeight);
            button.setOnClickUp((renderer, clickedButton) -> {
                if (clickedButton == Input.Buttons.RIGHT) {
                    setExpanded(!expanded);
                    button.setToggled(module.isEnabled());
                    refreshEntryLayout(this);
                    return;
                }
                if (clickedButton == Input.Buttons.LEFT) {
                    saveModuleChange(() -> module.toggle(true));
                    refreshAfterModuleToggle(this);
                }
            });

            settingsBox.setFillColor(ColorUtils.color(35, 35, 35, 255));
            settingsBox.setBorder(true);
            settingsBox.setBorderColor(ColorUtils.color(20, 20, 20, 255));
            settingsBox.setBorderWidth(1.5f);
            settingsBox.setBorderRadius(7.5f);
            settingsBox.setTopLeftRounded(false);
            settingsBox.setTopRightRounded(false);
            settingsBox.setZIndex(4);
            expandedRenderers.add(settingsBox);

            for (Setting<?> setting : module.getCustomSettings().values()) {
                ModuleSettingRow row = new ModuleSettingRow(setting);
                settingRows.add(row);
                expandedRenderers.addAll(row.getRenderers());
            }

            syncFromModule();
        }

        private Set<String> getExpandedModuleIds() {
            return EXPANDED_MODULE_IDS_BY_PAGE.computeIfAbsent(getTitle(), ignored -> new HashSet<>());
        }

        private void setExpanded(boolean expanded) {
            this.expanded = expanded;
            Set<String> expandedModuleIds = getExpandedModuleIds();
            if (expanded) {
                expandedModuleIds.add(module.getId());
            } else {
                expandedModuleIds.remove(module.getId());
            }
        }

        private void layout(float x, float y, float width) {
            button.setSize(width, moduleButtonHeight);
            button.setPos(x, y);
            button.setZIndex(5);

            height = moduleButtonHeight;
            if (!expanded) {
                return;
            }

            float settingsHeight = getSettingsBoxHeight();
            float settingsY = y - settingsHeight;
            float rowX = x + expandedPadding;
            float rowWidth = Math.max(0f, width - expandedPadding * 2f);

            settingsBox.setPos(x, settingsY);
            settingsBox.setSize(width, settingsHeight + expandedBoxOverlap);

            float currentY = settingsY + settingsHeight - expandedPadding - settingRowHeight;
            for (ModuleSettingRow row : settingRows) {
                row.layout(rowX, currentY, rowWidth, settingRowHeight);
                currentY -= settingRowHeight + elementSpacing;
            }

            height = moduleButtonHeight + settingsHeight;
        }

        private void update(Viewport viewport, float deltaTime) {
            syncFromModule();
            if (!expanded) {
                return;
            }
            for (ModuleSettingRow row : settingRows) {
                row.update(viewport, deltaTime);
            }
        }

        private void syncFromModule() {
            button.setToggled(module.isEnabled());
            for (ModuleSettingRow row : settingRows) {
                row.syncFromSetting(false);
            }
        }

        private float getHeight() {
            return height;
        }

        private void addRenderers(RenderUtil renderUtil) {
            renderUtil.addRenderer(button);
            syncExpandedRenderers(renderUtil);
        }

        private void removeRenderers(RenderUtil renderUtil) {
            renderUtil.removeRenderer(button);
            if (expandedRenderersAdded) {
                for (Renderer renderer : expandedRenderers) {
                    renderUtil.removeRenderer(renderer);
                }
                expandedRenderersAdded = false;
            }
        }

        private void syncExpandedRenderers(RenderUtil renderUtil) {
            if (expanded && !expandedRenderersAdded) {
                for (Renderer renderer : expandedRenderers) {
                    renderUtil.addRenderer(renderer);
                }
                expandedRenderersAdded = true;
            } else if (!expanded && expandedRenderersAdded) {
                for (Renderer renderer : expandedRenderers) {
                    renderUtil.removeRenderer(renderer);
                }
                expandedRenderersAdded = false;
            }
        }

        private boolean handleTouchDown(float worldX, float worldY, int button) {
            if (!expanded) {
                return false;
            }
            for (ModuleSettingRow row : settingRows) {
                if (row.handleTouchDown(worldX, worldY, button)) {
                    return true;
                }
            }
            return false;
        }

        private boolean keyDown(int keycode) {
            if (!expanded) {
                return false;
            }
            for (ModuleSettingRow row : settingRows) {
                if (row.keyDown(keycode)) {
                    return true;
                }
            }
            return false;
        }

        private boolean keyUp(int keycode) {
            if (!expanded) {
                return false;
            }
            for (ModuleSettingRow row : settingRows) {
                if (row.keyUp(keycode)) {
                    return true;
                }
            }
            return false;
        }

        private boolean keyTyped(char character) {
            if (!expanded) {
                return false;
            }
            for (ModuleSettingRow row : settingRows) {
                if (row.keyTyped(character)) {
                    return true;
                }
            }
            return false;
        }

        private void clearTextFocus() {
            for (ModuleSettingRow row : settingRows) {
                row.clearTextFocus();
            }
        }

        private float getSettingsBoxHeight() {
            if (settingRows.isEmpty()) {
                return expandedPadding * 2f;
            }
            return expandedPadding * 2f
                    + settingRows.size() * settingRowHeight
                    + Math.max(0, settingRows.size() - 1) * elementSpacing;
        }
    }

    private class ModuleSettingRow {
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

        private ModuleSettingRow(Setting<?> setting) {
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
                applySettingValue(setting, value);
                syncFromSetting(true);
            });
            renderers.add(toggle);

            booleanValueText = new TextRenderer();
            booleanValueText.setTextColor(ColorUtils.color(210, 210, 210, 255));
            booleanValueText.setZIndex(11);
            renderers.add(booleanValueText);
        }

        private void createTextControl() {
            textInput = new TextInput();
            textInput.setBorderWidth(1.5f);
            textInput.setBorderRadius(7.5f);
            textInput.setPadding(5f);
            textInput.setZIndex(10);
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

        private void layout(float x, float y, float width, float height) {
            float labelWidth = Math.min(90f, Math.max(55f, width * 0.36f));
            controlX = x + labelWidth + elementSpacing;
            controlY = y + (height - topBarHeight) / 2f;
            controlWidth = Math.max(0f, width - labelWidth - elementSpacing);
            controlHeight = topBarHeight;

            labelText.setPos(x, y + (height - labelText.getTextHeight(viewport)) / 2f);

            if (toggle != null) {
                toggle.setSize(42f, 22f);
                toggle.setPos(controlX, y + (height - toggle.getHeight()) / 2f);
                if (booleanValueText != null) {
                    booleanValueText.setPos(controlX + toggle.getWidth() + elementSpacing, y + (height - booleanValueText.getTextHeight(viewport)) / 2f);
                }
            } else if (slider != null && textInput != null) {
                float inputWidth = Math.min(74f, Math.max(58f, controlWidth * 0.34f));
                float sliderWidth = Math.max(50f, controlWidth - inputWidth - elementSpacing);
                slider.setSize(sliderWidth, 18f);
                slider.setPos(controlX, y + (height - slider.getHeight()) / 2f);

                textInput.setSize(inputWidth, topBarHeight);
                textInput.setPosition(controlX + sliderWidth + elementSpacing, y + (height - topBarHeight) / 2f);
            } else if (dropdown != null) {
                dropdown.setSize(controlWidth, topBarHeight);
                dropdown.setPosition(controlX, y + (height - topBarHeight) / 2f);
            } else if (textInput != null) {
                textInput.setSize(controlWidth, topBarHeight);
                textInput.setPosition(controlX, y + (height - topBarHeight) / 2f);
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
                return value;
            }

            float base = setting.getMinValue() == null ? 0f : setting.getMinValue();
            return base + Math.round((value - base) / step) * step;
        }
    }
}
