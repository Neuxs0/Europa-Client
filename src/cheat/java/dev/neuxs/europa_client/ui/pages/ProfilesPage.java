package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.settings.ProfileManager;
import dev.neuxs.europa_client.settings.ProfileSection;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;
import dev.neuxs.europa_client.utils.rendering.ui.Button;
import dev.neuxs.europa_client.utils.rendering.ui.Dropdown;
import dev.neuxs.europa_client.utils.rendering.ui.TextInput;

import java.util.EnumSet;
import java.util.List;

public class ProfilesPage extends Page implements InputProcessor {
    private Viewport viewport;
    private final Vector4 pageDim;
    private final BoxRenderer pageContainer;
    private final Vector2 touchPos = new Vector2();

    private final Dropdown profileDropdown = new Dropdown();
    private final TextInput profileNameInput = new TextInput();
    private final Button modulesSectionButton = new Button();
    private final Button moduleSettingsSectionButton = new Button();
    private final Button saveButton = new Button();
    private final Button loadButton = new Button();
    private final Button deleteButton = new Button();
    private final TextRenderer statusText = new TextRenderer();

    private boolean includeModules = true;
    private boolean includeModuleSettings = true;
    private final float padding = 8f;
    private final float elementSpacing = 6f;
    private final float inputHeight = 26f;
    private final float buttonHeight = 30f;

    public ProfilesPage(BoxRenderer pageContainer) {
        super("Profiles", pageContainer);
        this.pageContainer = pageContainer;
        this.pageDim = new Vector4(pageContainer.getPosX(), pageContainer.getPosY(), pageContainer.getWidth(), pageContainer.getHeight());
    }

    @Override
    public void create(Viewport viewport, float width, float height) {
        super.create(viewport, width, height);
        this.viewport = viewport;

        profileDropdown.setPlaceholderText("Select profile");
        profileDropdown.setBorderRadius(7.5f);
        profileDropdown.setPadding(3f);
        profileDropdown.setOnSelectionChanged(selected -> {
            profileNameInput.setTextSilent(selected);
            setStatus("Selected " + selected);
        });

        profileNameInput.setPlaceholder("Profile name");
        profileNameInput.setBorderRadius(7.5f);

        configureSectionButton(modulesSectionButton, () -> {
            includeModules = !includeModules;
            syncSectionButtons();
        });
        configureSectionButton(moduleSettingsSectionButton, () -> {
            includeModuleSettings = !includeModuleSettings;
            syncSectionButtons();
        });

        configureActionButton(saveButton, "Save", this::saveProfile);
        configureActionButton(loadButton, "Load", this::loadProfile);
        configureActionButton(deleteButton, "Delete", this::deleteProfile);

        statusText.setText("Profiles: " + ProfileManager.getActiveProfileName());

        refreshProfileList(ProfileManager.getActiveProfileName());
        syncSectionButtons();
        resize(width, height);
    }

    @Override
    public void resize(float width, float height) {
        super.resize(width, height);
        this.pageDim.set(pageContainer.getPosX(), pageContainer.getPosY(), pageContainer.getWidth(), pageContainer.getHeight());

        float usableWidth = pageDim.z - padding * 2f;
        float currentY = pageDim.y + pageDim.w - padding - inputHeight;
        float dropdownWidth = Math.max(110f, usableWidth * 0.42f);
        float nameWidth = usableWidth - dropdownWidth - elementSpacing;

        profileDropdown.setSize(dropdownWidth, inputHeight);
        profileDropdown.setPosition(pageDim.x + padding, currentY);
        profileDropdown.setZIndex(40);

        profileNameInput.setSize(nameWidth, inputHeight);
        profileNameInput.setPosition(pageDim.x + padding + dropdownWidth + elementSpacing, currentY);
        profileNameInput.setZIndex(10);

        currentY -= buttonHeight + elementSpacing;
        float sectionWidth = (usableWidth - elementSpacing) / 2f;
        layoutButton(modulesSectionButton, pageDim.x + padding, currentY, sectionWidth);
        layoutButton(moduleSettingsSectionButton, pageDim.x + padding + sectionWidth + elementSpacing, currentY, sectionWidth);

        currentY -= buttonHeight + elementSpacing * 1.5f;
        float actionWidth = (usableWidth - elementSpacing * 2f) / 3f;
        layoutButton(saveButton, pageDim.x + padding, currentY, actionWidth);
        layoutButton(loadButton, pageDim.x + padding + actionWidth + elementSpacing, currentY, actionWidth);
        layoutButton(deleteButton, pageDim.x + padding + (actionWidth + elementSpacing) * 2f, currentY, actionWidth);

        statusText.setPos(pageDim.x + padding, pageDim.y + padding);
    }

    @Override
    public void addRenderers(RenderUtil renderUtil) {
        renderUtil.addRenderer(profileDropdown);
        renderUtil.addRenderer(profileNameInput);
        renderUtil.addRenderer(modulesSectionButton);
        renderUtil.addRenderer(moduleSettingsSectionButton);
        renderUtil.addRenderer(saveButton);
        renderUtil.addRenderer(loadButton);
        renderUtil.addRenderer(deleteButton);
        renderUtil.addRenderer(statusText);
    }

    @Override
    public void removeRenderers(RenderUtil renderUtil) {
        renderUtil.removeRenderer(profileDropdown);
        renderUtil.removeRenderer(profileNameInput);
        renderUtil.removeRenderer(modulesSectionButton);
        renderUtil.removeRenderer(moduleSettingsSectionButton);
        renderUtil.removeRenderer(saveButton);
        renderUtil.removeRenderer(loadButton);
        renderUtil.removeRenderer(deleteButton);
        renderUtil.removeRenderer(statusText);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        syncSectionButtons();
    }

    @Override
    public void dispose(RenderUtil renderUtil) {
        super.dispose(renderUtil);
    }

    private void saveProfile() {
        runProfileAction(() -> {
            String savedName = ProfileManager.saveProfile(requireProfileName(), getSelectedSections());
            refreshProfileList(savedName);
            setStatus("Saved " + savedName);
        });
    }

    private void loadProfile() {
        runProfileAction(() -> {
            String name = requireProfileName();
            ProfileManager.applyProfile(name, getSelectedSections());
            refreshProfileList(name);
            setStatus("Loaded " + name);
        });
    }

    private void deleteProfile() {
        runProfileAction(() -> {
            String name = requireProfileName();
            ProfileManager.deleteProfile(name);
            refreshProfileList(ProfileManager.getActiveProfileName());
            setStatus("Deleted " + name);
        });
    }

    private void runProfileAction(ProfileAction action) {
        try {
            action.run();
        } catch (Exception e) {
            setStatus(e.getMessage());
        }
    }

    private String requireProfileName() {
        String inputName = profileNameInput.getText().trim();
        if (!inputName.isEmpty()) {
            return inputName;
        }

        String selectedName = profileDropdown.getSelectedOption();
        if (selectedName != null && !selectedName.isBlank()) {
            return selectedName;
        }

        String activeName = ProfileManager.getActiveProfileName();
        if (!activeName.equals("None")) {
            return activeName;
        }

        throw new IllegalArgumentException("Enter a profile name");
    }

    private EnumSet<ProfileSection> getSelectedSections() {
        EnumSet<ProfileSection> sections = EnumSet.noneOf(ProfileSection.class);
        if (includeModules) {
            sections.add(ProfileSection.MODULES);
        }
        if (includeModuleSettings) {
            sections.add(ProfileSection.MODULE_SETTINGS);
        }
        if (sections.isEmpty()) {
            throw new IllegalArgumentException("Select at least one section");
        }
        return sections;
    }

    private void refreshProfileList(String preferredProfile) {
        List<String> profiles = ProfileManager.listProfiles();
        profileDropdown.setOptions(profiles);

        String preferred = preferredProfile == null ? "" : preferredProfile;
        if (profiles.contains(preferred)) {
            profileDropdown.setSelectedOptionSilent(preferred);
            profileNameInput.setTextSilent(preferred);
        } else if (!profiles.isEmpty()) {
            profileDropdown.setSelectedOptionSilent(profiles.get(0));
            profileNameInput.setTextSilent(profiles.get(0));
        } else {
            profileDropdown.setPlaceholderText("No profiles");
            profileNameInput.setTextSilent("");
        }
    }

    private void syncSectionButtons() {
        syncSectionButton(modulesSectionButton, "Modules", includeModules);
        syncSectionButton(moduleSettingsSectionButton, "Settings", includeModuleSettings);
    }

    private void syncSectionButton(Button button, String label, boolean selected) {
        button.setText(label + ": " + (selected ? "On" : "Off"));
        button.setToggled(selected);
    }

    private void configureSectionButton(Button button, Runnable action) {
        configureBaseButton(button);
        button.setToggleEnabled(true);
        button.setOnClickUp((renderer, clickedButton) -> action.run());
    }

    private void configureActionButton(Button button, String text, Runnable action) {
        configureBaseButton(button);
        button.setText(text);
        button.setOnClickUp((renderer, clickedButton) -> action.run());
    }

    private void configureBaseButton(Button button) {
        button.setBorderWidth(1.5f);
        button.setBorderRadius(7.5f);
        button.setSize(100f, buttonHeight);
    }

    private void layoutButton(Button button, float x, float y, float width) {
        button.setSize(width, buttonHeight);
        button.setPos(x, y);
        button.setZIndex(5);
    }

    private void setStatus(String status) {
        statusText.setText(status == null || status.isBlank() ? "Ready" : status);
    }

    @Override
    public boolean keyDown(int keycode) {
        return profileNameInput.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        return profileNameInput.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        return profileNameInput.keyTyped(character);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (viewport == null) {
            return false;
        }

        touchPos.set(screenX, screenY);
        viewport.unproject(touchPos);

        if (profileDropdown.handleTouchDown(touchPos.x, touchPos.y, button)) {
            profileNameInput.setFocus(false);
            return true;
        }

        return profileNameInput.handleTouchDown(touchPos.x, touchPos.y, button);
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

    @FunctionalInterface
    private interface ProfileAction {
        void run() throws Exception;
    }
}
