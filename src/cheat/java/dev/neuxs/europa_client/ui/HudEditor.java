package dev.neuxs.europa_client.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.ui.HudManager;
import dev.neuxs.europa_client.modules.ui.HudModule;
import dev.neuxs.europa_client.settings.SettingsManager;
import dev.neuxs.europa_client.ui.widgets.HudSettingsWidget;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.GridRenderer;
import dev.neuxs.europa_client.utils.rendering.LineRenderer;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;
import dev.neuxs.europa_client.utils.rendering.Renderer;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;
import dev.neuxs.europa_client.utils.rendering.ui.Button;
import dev.neuxs.europa_client.utils.rendering.ui.Dropdown;
import finalforeach.cosmicreach.gamestates.GameState;

import java.util.ArrayList;
import java.util.List;

public class HudEditor extends GameState implements InputProcessor {
    private static final float SNAP_DISTANCE = 6f;
    private static final float CONTEXT_WIDTH = 220f;
    private static final float CONTEXT_PADDING = 8f;
    private static final float CONTEXT_SPACING = 6f;
    private static final float CONTEXT_TITLE_HEIGHT = 22f;
    private static final float CONTEXT_ITEM_HEIGHT = 28f;

    private enum ContextMode {
        NONE, ADD, ELEMENT
    }

    private final GameState backgroundState;
    private final boolean returnToGui;
    private final RenderUtil renderUtil = new RenderUtil();
    private final Vector2 touchPos = new Vector2();
    private final Vector2 dragOffset = new Vector2();
    private final Vector2 mouseWorld = new Vector2();

    private Viewport viewport;
    private float screenW;
    private float screenH;

    private HudModule draggingElement;
    private HudModule contextElement;
    private HudSettingsWidget settingsWidget;

    private final GridRenderer editorGrid = new GridRenderer();
    private final LineRenderer hoverTopLine = new LineRenderer();
    private final LineRenderer hoverBottomLine = new LineRenderer();
    private final LineRenderer hoverLeftLine = new LineRenderer();
    private final LineRenderer hoverRightLine = new LineRenderer();
    private final TextRenderer titleText = new TextRenderer();
    private final TextRenderer exitText = new TextRenderer();
    private final LineRenderer verticalSnapLine = new LineRenderer();
    private final LineRenderer horizontalSnapLine = new LineRenderer();

    private final BoxRenderer contextPanel = new BoxRenderer();
    private final TextRenderer contextTitle = new TextRenderer();
    private final Dropdown addDropdown = new Dropdown();
    private final Button lockButton = new Button();
    private final Button settingsButton = new Button();
    private final Button deleteButton = new Button();
    private final List<Renderer> contextRenderers = new ArrayList<>();

    private ContextMode contextMode = ContextMode.NONE;
    private boolean contextRenderersAdded;
    private float contextRequestX;
    private float contextRequestY;
    private float contextX;
    private float contextY;
    private float contextWidth;
    private float contextHeight;

    public HudEditor(GameState backgroundState, boolean returnToGui) {
        this.backgroundState = backgroundState;
        this.returnToGui = returnToGui;
    }

    @Override
    public void create() {
        super.create();
        viewport = this.newUiViewport;
        Gdx.input.setCursorCatched(false);
        Gdx.input.setInputProcessor(this);

        configureSnapLines();
        configureEditorOverlay();
        configureContextRenderers();

        renderUtil.addRenderer(editorGrid);
        renderUtil.addRenderer(hoverTopLine);
        renderUtil.addRenderer(hoverBottomLine);
        renderUtil.addRenderer(hoverLeftLine);
        renderUtil.addRenderer(hoverRightLine);
        renderUtil.addRenderer(titleText);
        renderUtil.addRenderer(exitText);
        renderUtil.addRenderer(verticalSnapLine);
        renderUtil.addRenderer(horizontalSnapLine);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (viewport == null) {
            viewport = this.newUiViewport;
        }
        if (viewport == null) {
            return;
        }

        screenW = viewport.getWorldWidth();
        screenH = viewport.getWorldHeight();

        editorGrid.setPos(0f, 0f);
        editorGrid.setSize(screenW, screenH);
        layoutEditorTitle();

        if (contextMode != ContextMode.NONE) {
            layoutContextMenu();
        }
        if (settingsWidget != null) {
            settingsWidget.layout(viewport, contextX + contextWidth + CONTEXT_SPACING, contextY);
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if (backgroundState == GameState.IN_GAME && GameState.IN_GAME.isCreated()) {
            GameState.IN_GAME.update(deltaTime);
        }
        if (settingsWidget != null) {
            settingsWidget.update(deltaTime);
        }
        if (draggingElement != null && !Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            finishDrag();
        }
    }

    @Override
    public void render() {
        super.render();

        if (viewport == null) {
            clearBackground();
            return;
        }

        renderBackground();

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        viewport.apply();
        Matrix4 uiMatrix = viewport.getCamera().combined;

        updateHoverHitbox();
        renderUtil.syncRenderers();
        renderUtil.updateAll(viewport);
        renderUtil.renderAll(uiMatrix, viewport);

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);

        if (this.firstFrame) {
            this.firstFrame = false;
        }
    }

    @Override
    public void switchAwayTo(GameState gameState) {
        dispose();
        super.switchAwayTo(gameState);
    }

    @Override
    public void dispose() {
        closeSettingsWidget();
        closeContextMenu();
        renderUtil.removeRenderer(editorGrid);
        renderUtil.removeRenderer(hoverTopLine);
        renderUtil.removeRenderer(hoverBottomLine);
        renderUtil.removeRenderer(hoverLeftLine);
        renderUtil.removeRenderer(hoverRightLine);
        renderUtil.removeRenderer(titleText);
        renderUtil.removeRenderer(exitText);
        renderUtil.removeRenderer(verticalSnapLine);
        renderUtil.removeRenderer(horizontalSnapLine);
        renderUtil.dispose();
        if (Gdx.input.getInputProcessor() == this) {
            Gdx.input.setInputProcessor(null);
        }
    }

    private void configureSnapLines() {
        verticalSnapLine.setWidth(1.5f);
        verticalSnapLine.setZIndex(200);
        horizontalSnapLine.setWidth(1.5f);
        horizontalSnapLine.setZIndex(200);
        hideSnapLines();
    }

    private void configureEditorOverlay() {
        editorGrid.setCellSize(32f);
        editorGrid.setLineWidth(1f);
        editorGrid.setCenterAnchored(true);
        editorGrid.setFillColor(ColorUtils.color(150, 150, 150, 70));
        editorGrid.setZIndex(20);

        configureHoverLine(hoverTopLine);
        configureHoverLine(hoverBottomLine);
        configureHoverLine(hoverLeftLine);
        configureHoverLine(hoverRightLine);
        hideHoverHitbox();

        titleText.setText("HUD Editor");
        titleText.setTextColor(ColorUtils.WHITE);
        titleText.setZIndex(210);

        exitText.setText("(ESC to exit)");
        exitText.setTextColor(ColorUtils.color(220, 220, 220, 210));
        exitText.setScale(0.75f);
        exitText.setZIndex(210);
    }

    private void configureHoverLine(LineRenderer lineRenderer) {
        lineRenderer.setWidth(1.5f);
        lineRenderer.setFillColor(ColorUtils.WHITE);
        lineRenderer.setZIndex(205);
    }

    private void layoutEditorTitle() {
        float titleWidth = titleText.getTextWidth(viewport);
        float titleHeight = titleText.getTextHeight(viewport);
        float exitWidth = exitText.getTextWidth(viewport);
        float exitHeight = exitText.getTextHeight(viewport);
        float titleY = screenH - 10f - titleHeight;

        titleText.setPos(screenW / 2f - titleWidth / 2f, titleY);
        exitText.setPos(screenW / 2f - exitWidth / 2f, titleY - exitHeight - 4f);
    }

    private void configureContextRenderers() {
        contextPanel.setFillColor(ColorUtils.color(35, 35, 35, 245));
        contextPanel.setBorder(true);
        contextPanel.setBorderColor(ColorUtils.color(20, 20, 20, 255));
        contextPanel.setBorderWidth(1.5f);
        contextPanel.setBorderRadius(7.5f);
        contextPanel.setZIndex(230);

        contextTitle.setTextColor(ColorUtils.WHITE);
        contextTitle.setZIndex(240);

        addDropdown.setBorderRadius(7.5f);
        addDropdown.setPadding(3f);
        addDropdown.setZIndex(250);
        addDropdown.setOnSelectionChanged(this::addHudElement);

        configureContextButton(lockButton, "");
        lockButton.setOnClickUp((renderer, button) -> {
            if (button == Input.Buttons.LEFT && contextElement != null) {
                contextElement.setHudLocked(!contextElement.isHudLocked());
                SettingsManager.saveSettings();
                closeContextMenu();
            }
        });

        configureContextButton(settingsButton, "Settings");
        settingsButton.setOnClickUp((renderer, button) -> {
            if (button == Input.Buttons.LEFT && contextElement != null) {
                HudModule module = contextElement;
                closeContextMenu();
                openSettingsWidget(module);
            }
        });

        configureContextButton(deleteButton, "Delete");
        deleteButton.setOnClickUp((renderer, button) -> {
            if (button == Input.Buttons.LEFT && contextElement != null) {
                deleteHudElement(contextElement);
                closeContextMenu();
            }
        });
    }

    private void configureContextButton(Button button, String text) {
        button.setText(text);
        button.setBorderWidth(1.5f);
        button.setBorderRadius(7.5f);
        button.setZIndex(245);
    }

    private void openAddContextMenu(float worldX, float worldY) {
        closeSettingsWidget();
        closeContextMenu();

        contextMode = ContextMode.ADD;
        contextElement = null;
        contextRequestX = worldX;
        contextRequestY = worldY;

        contextTitle.setText("Add HUD Element");
        syncAddDropdownOptions();

        contextRenderers.clear();
        contextRenderers.add(contextPanel);
        contextRenderers.add(contextTitle);
        contextRenderers.add(addDropdown);

        layoutContextMenu();
        addContextRenderers();
    }

    private void openElementContextMenu(HudModule module, float worldX, float worldY) {
        closeSettingsWidget();
        closeContextMenu();

        contextMode = ContextMode.ELEMENT;
        contextElement = module;
        contextRequestX = worldX;
        contextRequestY = worldY;

        contextTitle.setText(module.getHudDisplayName());
        lockButton.setText(module.isHudLocked() ? "Unlock" : "Lock");

        contextRenderers.clear();
        contextRenderers.add(contextPanel);
        contextRenderers.add(contextTitle);
        contextRenderers.add(lockButton);
        if (module.hasHudSettings()) {
            contextRenderers.add(settingsButton);
        }
        contextRenderers.add(deleteButton);

        layoutContextMenu();
        addContextRenderers();
    }

    private void syncAddDropdownOptions() {
        List<String> options = new ArrayList<>();
        for (HudModule module : HudManager.getHiddenHudModules()) {
            options.add(module.getHudDisplayName());
        }

        addDropdown.clearOptions();
        addDropdown.setOptions(options);
        addDropdown.setPlaceholderText(options.isEmpty() ? "No elements" : "Select element");
    }

    private void layoutContextMenu() {
        int itemCount = contextMode == ContextMode.ADD
                ? 1
                : 1 + (contextElement != null && contextElement.hasHudSettings() ? 1 : 0) + 1;
        contextWidth = CONTEXT_WIDTH;
        contextHeight = CONTEXT_PADDING * 2f
                + CONTEXT_TITLE_HEIGHT
                + CONTEXT_SPACING
                + itemCount * CONTEXT_ITEM_HEIGHT
                + Math.max(0, itemCount - 1) * CONTEXT_SPACING;

        contextX = MathUtils.clamp(contextRequestX, 0f, Math.max(0f, screenW - contextWidth));
        contextY = MathUtils.clamp(contextRequestY - contextHeight, 0f, Math.max(0f, screenH - contextHeight));

        contextPanel.setPos(contextX, contextY);
        contextPanel.setSize(contextWidth, contextHeight);

        float currentY = contextY + contextHeight - CONTEXT_PADDING - CONTEXT_TITLE_HEIGHT;
        contextTitle.setPos(
                contextX + CONTEXT_PADDING,
                currentY + (CONTEXT_TITLE_HEIGHT - contextTitle.getTextHeight(viewport)) / 2f
        );

        currentY -= CONTEXT_SPACING + CONTEXT_ITEM_HEIGHT;
        if (contextMode == ContextMode.ADD) {
            addDropdown.setSize(contextWidth - CONTEXT_PADDING * 2f, CONTEXT_ITEM_HEIGHT);
            addDropdown.setPosition(contextX + CONTEXT_PADDING, currentY);
            return;
        }

        layoutContextButton(lockButton, currentY);
        currentY -= CONTEXT_ITEM_HEIGHT + CONTEXT_SPACING;
        if (contextElement != null && contextElement.hasHudSettings()) {
            layoutContextButton(settingsButton, currentY);
            currentY -= CONTEXT_ITEM_HEIGHT + CONTEXT_SPACING;
        }
        layoutContextButton(deleteButton, currentY);
    }

    private void layoutContextButton(Button button, float y) {
        button.setSize(contextWidth - CONTEXT_PADDING * 2f, CONTEXT_ITEM_HEIGHT);
        button.setPos(contextX + CONTEXT_PADDING, y);
    }

    private void addContextRenderers() {
        for (Renderer renderer : contextRenderers) {
            renderUtil.addRenderer(renderer);
        }
        contextRenderersAdded = true;
    }

    private void closeContextMenu() {
        if (contextRenderersAdded) {
            for (Renderer renderer : contextRenderers) {
                renderUtil.removeRenderer(renderer);
            }
        }
        contextRenderersAdded = false;
        contextRenderers.clear();
        contextMode = ContextMode.NONE;
        contextElement = null;
        addDropdown.setOpen(false);
    }

    private void openSettingsWidget(HudModule module) {
        closeSettingsWidget();
        settingsWidget = new HudSettingsWidget(
                module.getHudDisplayName(),
                module.getHudSettings(),
                this::closeSettingsWidget
        );
        settingsWidget.layout(viewport, contextX + contextWidth + CONTEXT_SPACING, contextY);
        settingsWidget.addRenderers(renderUtil);
    }

    private void closeSettingsWidget() {
        if (settingsWidget != null) {
            settingsWidget.removeRenderers(renderUtil);
            settingsWidget = null;
        }
    }

    private void addHudElement(String displayName) {
        HudModule module = HudManager.getHudModuleByDisplayName(displayName);
        if (module == null || module.isEnabled()) {
            return;
        }

        saveHudChange(() -> {
            module.enable(false);
            if (!module.isEnabled()) {
                module.setEnabled(true);
            }
            module.setHudPosition(contextRequestX, contextRequestY, viewport);
        });
        closeContextMenu();
    }

    private void deleteHudElement(HudModule module) {
        saveHudChange(() -> {
            module.disable(false);
            if (module.isEnabled()) {
                module.setEnabled(false);
            }
        });
        if (draggingElement == module) {
            draggingElement = null;
            hideSnapLines();
        }
    }

    private void saveHudChange(Runnable change) {
        boolean previousAutoSave = SettingsManager.isAutoSaveEnabled();
        SettingsManager.setAutoSaveEnabled(false);
        try {
            change.run();
        } finally {
            SettingsManager.setAutoSaveEnabled(previousAutoSave);
        }
        SettingsManager.saveSettings();
    }

    private boolean isInsideContextMenu(float worldX, float worldY) {
        return contextMode != ContextMode.NONE
                && worldX >= contextX
                && worldX <= contextX + contextWidth
                && worldY >= contextY
                && worldY <= contextY + contextHeight;
    }

    private void updateHoverHitbox() {
        if (viewport == null) {
            hideHoverHitbox();
            return;
        }

        mouseWorld.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mouseWorld);

        if (isInsideContextMenu(mouseWorld.x, mouseWorld.y)
                || (settingsWidget != null && settingsWidget.contains(mouseWorld.x, mouseWorld.y))) {
            hideHoverHitbox();
            return;
        }

        HudModule hovered = draggingElement == null
                ? HudManager.findTopElementAt(mouseWorld.x, mouseWorld.y, viewport)
                : draggingElement;
        if (hovered == null) {
            hideHoverHitbox();
            return;
        }

        Rectangle bounds = hovered.getHudBounds(viewport);
        setHoverLine(hoverTopLine, bounds.x, bounds.y + bounds.height, bounds.x + bounds.width, bounds.y + bounds.height);
        setHoverLine(hoverBottomLine, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y);
        setHoverLine(hoverLeftLine, bounds.x, bounds.y, bounds.x, bounds.y + bounds.height);
        setHoverLine(hoverRightLine, bounds.x + bounds.width, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
    }

    private void setHoverLine(LineRenderer lineRenderer, float x1, float y1, float x2, float y2) {
        lineRenderer.setFillColor(ColorUtils.WHITE);
        lineRenderer.setStartPoint(new Vector2(x1, y1));
        lineRenderer.setEndPoint(new Vector2(x2, y2));
    }

    private void hideHoverHitbox() {
        hideHoverLine(hoverTopLine);
        hideHoverLine(hoverBottomLine);
        hideHoverLine(hoverLeftLine);
        hideHoverLine(hoverRightLine);
    }

    private void hideHoverLine(LineRenderer lineRenderer) {
        lineRenderer.setFillColor(ColorUtils.TRANSPARENT);
        lineRenderer.setStartPoint(new Vector2(0f, 0f));
        lineRenderer.setEndPoint(new Vector2(0f, 0f));
    }

    private void beginDrag(HudModule module, float worldX, float worldY) {
        Rectangle bounds = module.getHudBounds(viewport);
        draggingElement = module;
        dragOffset.set(worldX - bounds.x, worldY - bounds.y);
        hideSnapLines();
    }

    private void updateDrag(float worldX, float worldY) {
        if (draggingElement == null) {
            return;
        }

        Vector2 size = draggingElement.getHudSize(viewport);
        float requestedX = worldX - dragOffset.x;
        float requestedY = worldY - dragOffset.y;
        Vector2 snappedPosition = applySnapping(draggingElement, requestedX, requestedY, size);
        draggingElement.setHudPosition(snappedPosition.x, snappedPosition.y, viewport);
    }

    private void finishDrag() {
        if (draggingElement == null) {
            return;
        }
        draggingElement = null;
        hideSnapLines();
        SettingsManager.saveSettings();
    }

    private Vector2 applySnapping(HudModule movingModule, float x, float y, Vector2 size) {
        Rectangle movingBounds = new Rectangle(x, y, size.x, size.y);
        SnapMatch xMatch = findBestXSnap(movingModule, movingBounds);
        SnapMatch yMatch = findBestYSnap(movingModule, movingBounds);

        if (xMatch != null) {
            showVerticalSnapLine(xMatch.linePosition);
            x = xMatch.snappedPosition;
        } else {
            hideVerticalSnapLine();
        }

        if (yMatch != null) {
            showHorizontalSnapLine(yMatch.linePosition);
            y = yMatch.snappedPosition;
        } else {
            hideHorizontalSnapLine();
        }

        return new Vector2(x, y);
    }

    private SnapMatch findBestXSnap(HudModule movingModule, Rectangle movingBounds) {
        SnapMatch best = null;
        best = chooseBestSnap(best, movingBounds.x + movingBounds.width / 2f, screenW / 2f, movingBounds.width / 2f);

        for (HudModule module : HudManager.getVisibleHudModules()) {
            if (module == movingModule) {
                continue;
            }
            Rectangle bounds = module.getHudBounds(viewport);
            float[] targets = {
                    bounds.x,
                    bounds.x + bounds.width / 2f,
                    bounds.x + bounds.width
            };
            float[] offsets = {
                    0f,
                    movingBounds.width / 2f,
                    movingBounds.width
            };
            float[] anchors = {
                    movingBounds.x,
                    movingBounds.x + movingBounds.width / 2f,
                    movingBounds.x + movingBounds.width
            };

            for (int targetIndex = 0; targetIndex < targets.length; targetIndex++) {
                for (int anchorIndex = 0; anchorIndex < anchors.length; anchorIndex++) {
                    best = chooseBestSnap(best, anchors[anchorIndex], targets[targetIndex], offsets[anchorIndex]);
                }
            }
        }

        return best;
    }

    private SnapMatch findBestYSnap(HudModule movingModule, Rectangle movingBounds) {
        SnapMatch best = null;
        best = chooseBestSnap(best, movingBounds.y + movingBounds.height / 2f, screenH / 2f, movingBounds.height / 2f);

        for (HudModule module : HudManager.getVisibleHudModules()) {
            if (module == movingModule) {
                continue;
            }
            Rectangle bounds = module.getHudBounds(viewport);
            float[] targets = {
                    bounds.y,
                    bounds.y + bounds.height / 2f,
                    bounds.y + bounds.height
            };
            float[] offsets = {
                    0f,
                    movingBounds.height / 2f,
                    movingBounds.height
            };
            float[] anchors = {
                    movingBounds.y,
                    movingBounds.y + movingBounds.height / 2f,
                    movingBounds.y + movingBounds.height
            };

            for (int targetIndex = 0; targetIndex < targets.length; targetIndex++) {
                for (int anchorIndex = 0; anchorIndex < anchors.length; anchorIndex++) {
                    best = chooseBestSnap(best, anchors[anchorIndex], targets[targetIndex], offsets[anchorIndex]);
                }
            }
        }

        return best;
    }

    private SnapMatch chooseBestSnap(SnapMatch currentBest, float movingAnchor, float target, float offset) {
        float distance = Math.abs(movingAnchor - target);
        if (distance > SNAP_DISTANCE) {
            return currentBest;
        }

        if (currentBest == null || distance < currentBest.distance) {
            return new SnapMatch(distance, target - offset, target);
        }
        return currentBest;
    }

    private void showVerticalSnapLine(float x) {
        verticalSnapLine.setFillColor(ColorUtils.color(255, 0, 0, 220));
        verticalSnapLine.setStartPoint(new Vector2(x, 0f));
        verticalSnapLine.setEndPoint(new Vector2(x, screenH));
    }

    private void showHorizontalSnapLine(float y) {
        horizontalSnapLine.setFillColor(ColorUtils.color(255, 0, 0, 220));
        horizontalSnapLine.setStartPoint(new Vector2(0f, y));
        horizontalSnapLine.setEndPoint(new Vector2(screenW, y));
    }

    private void hideSnapLines() {
        hideVerticalSnapLine();
        hideHorizontalSnapLine();
    }

    private void hideVerticalSnapLine() {
        verticalSnapLine.setFillColor(ColorUtils.TRANSPARENT);
    }

    private void hideHorizontalSnapLine() {
        horizontalSnapLine.setFillColor(ColorUtils.TRANSPARENT);
    }

    private void renderBackground() {
        try {
            HudManager.setInGameHudSuppressed(true);
            if (backgroundState == null || !backgroundState.isCreated()) {
                clearBackground();
                return;
            }

            try {
                backgroundState.render();
            } catch (Exception e) {
                Client.LOGGER.error("Error rendering HUD editor background: {}", e.getMessage(), e);
                clearBackground();
            }
        } finally {
            HudManager.setInGameHudSuppressed(false);
            HudManager.render(viewport);
        }
    }

    private void clearBackground() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    private void exitEditor() {
        finishDrag();
        SettingsManager.saveSettings();
        Gdx.app.postRunnable(() -> {
            if (returnToGui) {
                GameState switchBackground = GameState.IN_GAME.isCreated() ? GameState.IN_GAME : backgroundState;
                GameState.switchToGameState(new GUI(switchBackground));
                return;
            }

            if (backgroundState != null && backgroundState.isCreated()) {
                GameState.switchToGameState(backgroundState);
                Gdx.input.setCursorCatched(true);
            } else if (GameState.IN_GAME.isCreated()) {
                GameState.switchToGameState(GameState.IN_GAME);
                Gdx.input.setCursorCatched(true);
            }
        });
    }

    @Override
    public boolean keyDown(int keycode) {
        if (settingsWidget != null && settingsWidget.keyDown(keycode)) {
            return true;
        }
        if (keycode == Input.Keys.ESCAPE) {
            if (contextMode != ContextMode.NONE) {
                closeContextMenu();
                return true;
            }
            if (settingsWidget != null) {
                closeSettingsWidget();
                return true;
            }
            exitEditor();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return settingsWidget != null && settingsWidget.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        return settingsWidget != null && settingsWidget.keyTyped(character);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (viewport == null) {
            return false;
        }

        touchPos.set(screenX, screenY);
        viewport.unproject(touchPos);

        if (settingsWidget != null) {
            if (settingsWidget.handleTouchDown(touchPos.x, touchPos.y, button)) {
                return true;
            }
            if (button == Input.Buttons.LEFT) {
                closeSettingsWidget();
                return true;
            }
        }

        if (contextMode == ContextMode.ADD && addDropdown.handleTouchDown(touchPos.x, touchPos.y, button)) {
            return true;
        }

        if (contextMode != ContextMode.NONE && isInsideContextMenu(touchPos.x, touchPos.y)) {
            return true;
        }

        if (contextMode != ContextMode.NONE && button == Input.Buttons.LEFT) {
            closeContextMenu();
            return true;
        }

        if (button == Input.Buttons.RIGHT) {
            HudModule target = HudManager.findTopElementAt(touchPos.x, touchPos.y, viewport);
            if (target != null) {
                openElementContextMenu(target, touchPos.x, touchPos.y);
            } else {
                openAddContextMenu(touchPos.x, touchPos.y);
            }
            return true;
        }

        if (button == Input.Buttons.LEFT) {
            HudModule target = HudManager.findTopElementAt(touchPos.x, touchPos.y, viewport);
            if (target != null && !target.isHudLocked()) {
                beginDrag(target, touchPos.x, touchPos.y);
                return true;
            }
            return target != null;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT && draggingElement != null) {
            finishDrag();
            return true;
        }
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT && draggingElement != null) {
            finishDrag();
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (draggingElement == null || viewport == null) {
            return false;
        }

        touchPos.set(screenX, screenY);
        viewport.unproject(touchPos);
        updateDrag(touchPos.x, touchPos.y);
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    private record SnapMatch(float distance, float snappedPosition, float linePosition) {
    }
}
