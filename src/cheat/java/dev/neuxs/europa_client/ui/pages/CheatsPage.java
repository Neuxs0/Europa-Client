package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;
import dev.neuxs.europa_client.utils.rendering.ui.Button;
import dev.neuxs.europa_client.utils.rendering.ui.Dropdown;
import dev.neuxs.europa_client.utils.rendering.ui.TextInput;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class CheatsPage extends Page implements InputProcessor {
    private Viewport viewport;
    private final Vector4 pageDim; // x y z w (x, y, width, height)
    private final BoxRenderer pageContainer;
    private final TextInput searchInput = new TextInput();
    private final Dropdown sortDropdown = new Dropdown();
    private final Vector2 touchPos = new Vector2();
    private final List<Button> moduleButtons = new ArrayList<>();
    private final List<Button> filteredAndSortedButtons = new ArrayList<>();
    private final TextRenderer leftClickText = new TextRenderer();
    private final TextRenderer rightClickText = new TextRenderer();
    private RenderUtil renderUtil;
    private boolean renderersAdded = false;
    private final float padding = 5f;
    private final float elementSpacing = 5f;
    private final float topBarHeight = 25f;
    private final float sortButtonWidth = 100f;
    private final float moduleButtonHeight = 30f;

    public enum SortType {
        A_Z, Z_A, ON_OFF, OFF_ON
    }
    private SortType currentSortType = SortType.A_Z;
    private String previousSearchText = "";
    private final Map<String, SortType> sortDisplayMap = new LinkedHashMap<>();

    public CheatsPage(BoxRenderer pageContainer) {
        super("Cheats", pageContainer);
        this.pageContainer = pageContainer;
        this.pageDim = new Vector4(pageContainer.getPosX(), pageContainer.getPosY(), pageContainer.getWidth(), pageContainer.getHeight());
        initializeSortMap();
    }

    @Override
    public void create(Viewport viewport, float width, float height) {
        super.create(viewport, width, height);
        this.viewport = viewport;

        searchInput.setSize(150, topBarHeight);
        searchInput.setPlaceholder("Search...");
        searchInput.setOnTextChanged(text -> {
            previousSearchText = text;
            applyFiltersAndSort();
        });
        searchInput.setOnFocusLost(text -> previousSearchText = text);

        sortDropdown.setSize(sortButtonWidth, topBarHeight);
        sortDropdown.setPlaceholderText(getSortDisplayName(currentSortType));
        sortDropdown.setPadding(3f);
        sortDropdown.setOptions(new ArrayList<>(sortDisplayMap.keySet()));
        sortDropdown.setSelectedOptionSilent(getSortDisplayName(currentSortType));

        sortDropdown.setOnSelectionChanged(selectedDisplayName -> {
            SortType newSortType = sortDisplayMap.getOrDefault(selectedDisplayName, SortType.A_Z);
            setSortType(newSortType);
        });

        for (Module module : Modules.cheatModuleList) {
            Button moduleButton = getButton(module);
            moduleButtons.add(moduleButton);
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

        repositionButtons();

        leftClickText.setPos(
                pageDim.x + padding,
                pageDim.y + padding
        );
        rightClickText.setPos(
                pageDim.x + pageDim.z - padding - rightClickText.getTextWidth(viewport),
                pageDim.y + padding
        );
    }

    @Override
    public void addRenderers(RenderUtil renderUtil) {
        this.renderUtil = renderUtil;
        this.renderersAdded = true;

        renderUtil.addRenderer(searchInput);
        for (Button btn : filteredAndSortedButtons) {
            renderUtil.addRenderer(btn);
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
        for (Button btn : filteredAndSortedButtons) {
            renderUtil.removeRenderer(btn);
        }
        renderUtil.removeRenderer(leftClickText);
        renderUtil.removeRenderer(rightClickText);
    }

    @Override
    public void update(float deltaTime) {
        searchInput.update(viewport, deltaTime);
        sortDropdown.update(viewport);

        String currentSearchText = searchInput.getText();
        if (!currentSearchText.equals(previousSearchText)) {
            previousSearchText = currentSearchText;
            applyFiltersAndSort();
        }
    }

    @Override
    public void dispose(RenderUtil renderUtil) {
        super.dispose(renderUtil);
        moduleButtons.clear();
        filteredAndSortedButtons.clear();
    }

    private Button getButton(Module module) {
        Button moduleButton = new Button();
        moduleButton.setToggleEnabled(true);
        moduleButton.setText(module.getId());
        moduleButton.setBorderWidth(1.5f);
        moduleButton.setBorderRadius(7.5f);
        moduleButton.setSize(100, moduleButtonHeight);
        moduleButton.setOnClickUp((renderer, button) -> {
            module.toggle(true);
            if (currentSortType == SortType.ON_OFF || currentSortType == SortType.OFF_ON) {
                applyFiltersAndSort();
            }
        });
        moduleButton.setToggled(module.isEnabled());
        return moduleButton;
    }

    private void repositionButtons() {
        float moduleGridWidth = pageDim.z - padding * 2;
        int modulesPerRow = 2;
        float moduleButtonWidth = (moduleGridWidth - elementSpacing * (modulesPerRow - 1)) / modulesPerRow;

        for (int i = 0; i < filteredAndSortedButtons.size(); i++) {
            Button btn = filteredAndSortedButtons.get(i);
            int col = i % modulesPerRow;
            int row = i / modulesPerRow;

            float buttonX = pageDim.x + padding + col * (moduleButtonWidth + elementSpacing);
            float buttonY = pageDim.y + pageDim.w - padding - topBarHeight - elementSpacing - (row + 1) * moduleButtonHeight - row * elementSpacing;

            btn.setSize(moduleButtonWidth, moduleButtonHeight);
            btn.setPos(buttonX, buttonY);
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
        return "A_Z";
    }

    private void applyFiltersAndSort() {
        String searchTerm = searchInput.getText().toLowerCase();
        List<Button> previousButtons = new ArrayList<>(filteredAndSortedButtons);

        List<Button> filtered = moduleButtons.stream()
                .filter(button -> {
                    Module module = Modules.getModuleById(button.getTextRenderer().getText());
                    if (module == null) return false;

                    boolean nameMatch = module.getId().toLowerCase().contains(searchTerm);
                    if (!nameMatch) return false;

                    button.setToggled(module.isEnabled());

                    return true;
                })
                .collect(Collectors.toList());

        Comparator<Button> comparator = switch (currentSortType) {
            case Z_A ->
                    Comparator.<Button, String>comparing(btn -> Objects.requireNonNull(Modules.getModuleById(btn.getTextRenderer().getText())).getId()).reversed();
            case ON_OFF -> Comparator.comparing(Button::isToggleEnabled).reversed()
                    .thenComparing(btn -> Objects.requireNonNull(Modules.getModuleById(btn.getTextRenderer().getText())).getId());
            case OFF_ON -> Comparator.comparing(Button::isToggleEnabled)
                    .thenComparing(btn -> Objects.requireNonNull(Modules.getModuleById(btn.getTextRenderer().getText())).getId());
            default ->
                    Comparator.comparing(btn -> Objects.requireNonNull(Modules.getModuleById(btn.getTextRenderer().getText())).getId());
        };

        filtered.sort(comparator);

        filteredAndSortedButtons.clear();
        filteredAndSortedButtons.addAll(filtered);

        repositionButtons();
        syncFilteredButtonRenderers(previousButtons);
    }

    public void setSortType(SortType type) {
        if (this.currentSortType != type) {
            this.currentSortType = type;
            sortDropdown.setSelectedOptionSilent(getSortDisplayName(type));
            applyFiltersAndSort();
        }
    }

    private void syncFilteredButtonRenderers(List<Button> previousButtons) {
        if (!renderersAdded || renderUtil == null) {
            return;
        }

        Set<Button> previousButtonSet = new HashSet<>(previousButtons);
        Set<Button> currentButtonSet = new HashSet<>(filteredAndSortedButtons);

        for (Button button : previousButtons) {
            if (!currentButtonSet.contains(button)) {
                renderUtil.removeRenderer(button);
            }
        }
        for (Button button : filteredAndSortedButtons) {
            if (!previousButtonSet.contains(button)) {
                renderUtil.addRenderer(button);
            }
        }
    }

    @Override public boolean keyDown(int keycode) {
        return searchInput.keyDown(keycode);
    }
    @Override public boolean keyUp(int keycode) {
        return searchInput.keyUp(keycode);
    }
    @Override public boolean keyTyped(char character) {
        return searchInput.keyTyped(character);
    }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (viewport == null) {
            return false;
        }

        touchPos.set(screenX, screenY);
        viewport.unproject(touchPos);

        if (sortDropdown.handleTouchDown(touchPos.x, touchPos.y, button)) {
            searchInput.setFocus(false);
            return true;
        }

        return searchInput.handleTouchDown(touchPos.x, touchPos.y, button);
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
}
