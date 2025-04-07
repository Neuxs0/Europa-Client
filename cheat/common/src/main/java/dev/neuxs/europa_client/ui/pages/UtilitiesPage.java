package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;
import dev.neuxs.europa_client.utils.rendering.ui.Button;
import dev.neuxs.europa_client.utils.rendering.ui.Dropdown; // Import Dropdown
import dev.neuxs.europa_client.utils.rendering.ui.TextInput;
import dev.neuxs.europa_client.utils.rendering.ui.ToggleButton;

import java.util.*;
import java.util.stream.Collectors;

public class UtilitiesPage extends Page {
    private Viewport viewport;
    private Vector4 pageDim; // x y z w (x, y, width, height)
    private final BoxRenderer pageContainer;
    private final TextInput searchInput = new TextInput();
    private final Dropdown sortDropdown = new Dropdown();
    private final List<ToggleButton> moduleButtons = new ArrayList<>();
    private final List<ToggleButton> filteredAndSortedButtons = new ArrayList<>();
    private final TextRenderer leftClickText = new TextRenderer();
    private final TextRenderer rightClickText = new TextRenderer();
    private final float padding = 5f;
    private final float elementSpacing = 5f;
    private final float topBarHeight = 25f;
    private final float sortButtonWidth = 100f;
    private final float moduleButtonHeight = 30f;
    private final int modulesPerRow = 2;
    public enum SortType {
        A_Z, Z_A, ON_OFF, OFF_ON
    }
    private SortType currentSortType = SortType.A_Z;
    private String previousSearchText = "";
    private final Map<String, SortType> sortDisplayMap = new HashMap<>();

    public UtilitiesPage(BoxRenderer pageContainer) {
        super("Utilities", pageContainer);
        this.pageContainer = pageContainer;
        this.pageDim = new Vector4(pageContainer.getPosX(), pageContainer.getPosY(), pageContainer.getWidth(), pageContainer.getHeight());
        initializeSortMap();
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

    @Override
    public void create(Viewport viewport, float width, float height) {
        super.create(viewport, width, height);
        this.viewport = viewport;

        searchInput.setSize(150, topBarHeight);
        searchInput.setPlaceholder("Search...");
        searchInput.getTextRenderer().setX(searchInput.getX() + 5f);
        sortDropdown.setSize(sortButtonWidth, topBarHeight);
        sortDropdown.setPlaceholderText(getSortDisplayName(currentSortType));
        sortDropdown.setPadding(3f);
        for (String displayName : sortDisplayMap.keySet()) {
            sortDropdown.addOption(displayName);
        }

        sortDropdown.setOnSelectionChanged(selectedDisplayName -> {
            SortType newSortType = sortDisplayMap.getOrDefault(selectedDisplayName, SortType.A_Z);
            setSortType(newSortType);
            sortDropdown.setPlaceholderText(getSortDisplayName(newSortType));
        });


        for (Module module : Modules.utilModuleList) {
            ToggleButton moduleButton = new ToggleButton();
            moduleButton.getTextRenderer().setText(module.getId());
            moduleButton.getBoxRenderer().setBorderWidth(1.5f);
            moduleButton.getBoxRenderer().setBorderRadius(7.5f);
            moduleButton.setSize(100, moduleButtonHeight);
            moduleButton.setOnToggle(button -> {
                module.toggle(true);
                if (currentSortType == SortType.ON_OFF || currentSortType == SortType.OFF_ON) {
                    applyFiltersAndSort();
                }
            });
            moduleButton.setToggled(module.isEnabled());
            moduleButtons.add(moduleButton);
        }

        leftClickText.setText("Left click to toggle");
        leftClickText.setColor(ColorUtils.color(255, 255, 255, 255));

        rightClickText.setText("Right click to expand");
        rightClickText.setColor(ColorUtils.color(255, 255, 255, 255));

        resize(width, height);
        applyFiltersAndSort();
    }

    private void repositionButtons() {
        float moduleGridWidth = pageDim.z - padding * 2;
        float moduleButtonWidth = (moduleGridWidth - elementSpacing * (modulesPerRow - 1)) / modulesPerRow;

        for (int i = 0; i < filteredAndSortedButtons.size(); i++) {
            Button btn = filteredAndSortedButtons.get(i);
            int col = i % modulesPerRow;
            int row = i / modulesPerRow;

            float buttonX = pageDim.x + padding + col * (moduleButtonWidth + elementSpacing);
            float buttonY = pageDim.y + pageDim.w - padding - topBarHeight - elementSpacing - (row + 1) * moduleButtonHeight - row * elementSpacing;

            btn.setSize(moduleButtonWidth, moduleButtonHeight);
            btn.setPosition(buttonX, buttonY);
        }
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
        searchInput.getTextRenderer().setPosition(
                searchInput.getX() + 5f,
                searchInput.getY() + (searchInput.getHeight() / 2f) + (searchInput.getTextRenderer().getHeight(viewport) / 2f)
        );

        currentX += searchWidth + elementSpacing;
        sortDropdown.setPosition(currentX, currentY);

        repositionButtons();

        leftClickText.setPosition(
                pageDim.x + padding,
                pageDim.y + padding
        );
        rightClickText.setPosition(
                pageDim.x + pageDim.z - padding - rightClickText.getWidth(viewport),
                pageDim.y + padding
        );
    }

    private void applyFiltersAndSort() {
        String searchTerm = searchInput.getText().toLowerCase();

        List<ToggleButton> filtered = moduleButtons.stream()
                .filter(button -> {
                    Module module = Modules.getModuleById(button.getTextRenderer().getText());
                    if (module == null) return false;

                    boolean nameMatch = module.getId().toLowerCase().contains(searchTerm);
                    if (!nameMatch) return false;

                    button.setToggled(module.isEnabled());

                    return true;
                })
                .collect(Collectors.toList());

        Comparator<ToggleButton> comparator = switch (currentSortType) {
            case Z_A ->
                    Comparator.<ToggleButton, String>comparing(btn -> Objects.requireNonNull(Modules.getModuleById(btn.getTextRenderer().getText())).getId()).reversed();
            case ON_OFF -> Comparator.<ToggleButton, Boolean>comparing(ToggleButton::isToggled).reversed()
                    .thenComparing(btn -> Objects.requireNonNull(Modules.getModuleById(btn.getTextRenderer().getText())).getId());
            case OFF_ON -> Comparator.<ToggleButton, Boolean>comparing(ToggleButton::isToggled)
                    .thenComparing(btn -> Objects.requireNonNull(Modules.getModuleById(btn.getTextRenderer().getText())).getId());
            default ->
                    Comparator.comparing(btn -> Objects.requireNonNull(Modules.getModuleById(btn.getTextRenderer().getText())).getId());
        };

        filtered.sort(comparator);

        filteredAndSortedButtons.clear();
        filteredAndSortedButtons.addAll(filtered);

        repositionButtons();
    }

    public void setSortType(SortType type) {
        if (this.currentSortType != type) {
            this.currentSortType = type;
            applyFiltersAndSort();
        }
    }


    @Override
    public void renderShape(ShapeRenderer shapeRenderer) {
        super.renderShape(shapeRenderer);

        searchInput.renderShape(shapeRenderer, viewport);
        for (Button btn : filteredAndSortedButtons) {
            btn.renderShape(shapeRenderer, viewport);
        }
        sortDropdown.renderShape(shapeRenderer, viewport);
    }

    @Override
    public void renderText(SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        super.renderText(spriteBatch, glyphLayout);

        searchInput.renderText(spriteBatch, glyphLayout, viewport);
        for (Button btn : filteredAndSortedButtons) {
            btn.renderText(spriteBatch, glyphLayout, viewport);
        }
        sortDropdown.renderText(spriteBatch, glyphLayout, viewport);
        leftClickText.render(spriteBatch, glyphLayout, viewport);
        rightClickText.render(spriteBatch, glyphLayout, viewport);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        searchInput.update(viewport, deltaTime);
        sortDropdown.update(viewport);

        String currentSearchText = searchInput.getText();
        if (!currentSearchText.equals(previousSearchText)) {
            previousSearchText = currentSearchText;
            applyFiltersAndSort();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        moduleButtons.clear();
        filteredAndSortedButtons.clear();
    }

    @Override
    public boolean keyDown(int keycode) {
        return searchInput.keyDown(keycode);
    }
    @Override
    public boolean keyUp(int keycode) {
        return searchInput.keyUp(keycode);
    }
    @Override
    public boolean keyTyped(char character) {
        return searchInput.keyTyped(character);
    }
}