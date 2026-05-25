package dev.neuxs.europa_client.ui;

// Some issue somewhere with the Side Menu but can't seem to find how to reproduce and the issue is rare.

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.settings.ClientSettings;
import dev.neuxs.europa_client.ui.pages.*;
import dev.neuxs.europa_client.utils.rendering.BackgroundBlurRenderer;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;
import dev.neuxs.europa_client.utils.rendering.Renderer;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;
import dev.neuxs.europa_client.utils.rendering.ui.Button;
import finalforeach.cosmicreach.gamestates.GameState;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"DuplicatedCode"})
public class GUI extends GameState {
    private static final RenderUtil renderUtil = new RenderUtil();
    private static final String PAGE_HOME = "Home";
    private static String lastPageTitle = PAGE_HOME;
    private static boolean lastSideMenuCollapsed = false;

    private final Color mainDimColor = ColorUtils.color(0, 0, 0, 75);
    private final Color mainBackgroundColor = ColorUtils.color(40, 40, 40, 255);
    private final Color mainBorderColor = ColorUtils.color(60, 60, 60, 255);

    private final GameState previousGamestate;
    private final BackgroundBlurRenderer backgroundBlurRenderer = new BackgroundBlurRenderer();
    private Viewport viewport;
    private float screenW;
    private float screenH;

    private final BoxRenderer backgroundDim = new BoxRenderer();
    private final BoxRenderer menuContainer = new BoxRenderer();
    private final BoxRenderer sideMenu = new BoxRenderer();
    private final BoxRenderer contentMenu = new BoxRenderer();
    private final TextRenderer pageTitle = new TextRenderer();
    private final Button collapseSideMenuButton = new Button();
    private final List<PageEntry> pageEntries = new ArrayList<>();

    private final HomePage homePage = new HomePage(contentMenu);
    private Page currentPage;

    private static final class PageEntry {
        private final Page page;
        private final Button button;

        private PageEntry(Page page, Button button) {
            this.page = page;
            this.button = button;
        }
    }

    public Vector2 menuSize = new Vector2(0, 0);
    public float menuPadding = 7.5f;
    public boolean sideMenuCollapsed = false;
    public float sideMenuWidth = 108f;
    public float sideMenuButtonPadding = 8f;
    public float sideMenuButtonBorderRadius = 5f;
    public float sideMenuButtonHeight = 34f;

    public GUI(GameState previousGamestate) {
        this.previousGamestate = previousGamestate;
    }

    @Override
    public void create() {
        super.create();

        Gdx.input.setCursorCatched(false);

        viewport = this.newUiViewport;

        menuSize.set(screenW / 1.8f, screenH / 1.65f);

        if (Client.isInitialized()) {
            Modules.initModules();
        }

        syncClientSettings();
        menuContainer.setFillColor(mainBorderColor);
        menuContainer.setBorderRadius(15f);
        contentMenu.setFillColor(mainBackgroundColor);
        contentMenu.setBorderRadius(10f);
        sideMenu.setFillColor(mainBackgroundColor);
        sideMenu.setBorderRadius(10f);

        collapseSideMenuButton.setSize(18f, 18f);
        collapseSideMenuButton.setFillColor(ColorUtils.color(0, 0, 0, 0));
        collapseSideMenuButton.setHoverFillColor(ColorUtils.color(0, 0, 0, 0));
        collapseSideMenuButton.setPressedFillColor(ColorUtils.color(0, 0, 0, 0));
        collapseSideMenuButton.setBorder(false);
        collapseSideMenuButton.setText(sideMenuCollapsed ? ">" : "<");
        collapseSideMenuButton.setOnClickUp(createToggleSideMenuAction());

        pageEntries.clear();
        addSideMenuPage(new UtilitiesPage(contentMenu));
        addSideMenuPage(new UIPage(contentMenu));
        if (Client.getVariant() != null) {
            for (Page page : Client.getVariant().createExtraPages(contentMenu)) {
                addSideMenuPage(page);
            }
        }
        addSideMenuPage(new ProfilesPage(contentMenu));
        addSideMenuPage(new SettingsPage(contentMenu));

        sideMenuCollapsed = lastSideMenuCollapsed;
        currentPage = getPageByTitle(lastPageTitle);
        pageTitle.setText(currentPage.getTitle());

        renderUtil.addRenderer(backgroundDim);
        renderUtil.addRenderer(menuContainer);
        renderUtil.addRenderer(contentMenu);
        renderUtil.addRenderer(pageTitle);
        renderUtil.addRenderer(collapseSideMenuButton);

        if (!sideMenuCollapsed) {
            renderUtil.addRenderer(sideMenu);
            addSideMenuRenderers();
        }

        currentPage.create(viewport, screenW, screenH);
        currentPage.addRenderers(renderUtil);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        if (viewport != null) {
            screenW = viewport.getWorldWidth();
            screenH = viewport.getWorldHeight();
        } else {
            Client.LOGGER.error("Viewport is NULL during resize!");
            return;
        }

        backgroundDim.setSize(screenW, screenH);

        menuSize.set(screenW / 1.8f, screenH / 1.65f);
        menuContainer.setSize(menuSize);
        menuContainer.setPos(
                screenW / 2f - menuContainer.getWidth() / 2f,
                screenH / 2f - menuContainer.getHeight() / 2f
        );

        sideMenu.setSize(sideMenuWidth, menuContainer.getHeight() - (menuPadding * 2));
        sideMenu.setPos(
                menuContainer.getPosX() + menuPadding,
                menuContainer.getPosY() + menuPadding
        );

        float contentHeight = menuContainer.getHeight() - (16f + (menuPadding * 3));
        if (sideMenuCollapsed) {
            contentMenu.setSize(
                    menuContainer.getWidth() - menuPadding * 2,
                    contentHeight
            );
            contentMenu.setPos(
                    menuContainer.getPosX() + menuPadding,
                    menuContainer.getPosY() + menuPadding
            );
        } else {
            contentMenu.setSize(
                    menuContainer.getWidth() - sideMenu.getWidth() - menuPadding * 3,
                    contentHeight
            );
            contentMenu.setPos(
                    sideMenu.getPosX() + sideMenu.getWidth() + menuPadding,
                    menuContainer.getPosY() + menuPadding
            );
        }

        float titleAreaY = contentMenu.getPosY() + contentMenu.getHeight() + menuPadding;
        float titleWidth = pageTitle.getTextWidth(viewport);
        pageTitle.setPos(
                contentMenu.getPosX() + (contentMenu.getWidth() / 2f) - (titleWidth / 2f),
                titleAreaY
        );

        collapseSideMenuButton.setPos(
                contentMenu.getPosX(),
                titleAreaY
        );

        if (!sideMenuCollapsed) {
            float sideMenuButtonX = sideMenu.getPosX() + sideMenuButtonPadding;
            float currentY = sideMenu.getPosY() + sideMenu.getHeight() - sideMenuButtonPadding;
            int bottomButtonCount = Math.min(2, pageEntries.size());
            int topButtonLimit = pageEntries.size() - bottomButtonCount;

            for (int i = 0; i < topButtonLimit; i++) {
                Button button = pageEntries.get(i).button;
                currentY -= button.getHeight();
                button.setPos(sideMenuButtonX, currentY);
                currentY -= sideMenuButtonPadding;
            }

            currentY = sideMenu.getPosY() + sideMenuButtonPadding;
            for (int i = pageEntries.size() - 1; i >= topButtonLimit; i--) {
                Button button = pageEntries.get(i).button;
                button.setPos(sideMenuButtonX, currentY);
                currentY += button.getHeight() + sideMenuButtonPadding;
            }
        }

        if (currentPage != null) {
            currentPage.resize(screenW, screenH);
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if (GameState.IN_GAME.isCreated()) {
            GameState.IN_GAME.update(deltaTime);
        }

        if (this.stage != null) {
            this.stage.act(deltaTime);
        }

        if (currentPage != null) {
            currentPage.update(deltaTime);
        }

        syncClientSettings();
    }

    @Override
    public void render() {
        super.render();

        if (viewport == null) {
            Client.LOGGER.error("No viewport available in GUI.render()!");
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            return;
        }

        renderBackground();

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        viewport.apply();
        Matrix4 uiMatrix = viewport.getCamera().combined;

        renderUtil.syncRenderers();
        renderUtil.updateAll(viewport);
        renderUtil.renderAll(uiMatrix, viewport);

        if (this.stage != null) this.stage.draw();

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);

        if (this.firstFrame) this.firstFrame = false;
    }

    @Override
    public void switchAwayTo(GameState gameState) {
        Client.LOGGER.info("GUI switchAwayTo called. Disposing GUI resources.");
        this.dispose();
        super.switchAwayTo(gameState);
    }

    @Override
    public void dispose() {
        Client.LOGGER.info("GUI dispose called. Removing renderers from static RenderUtil.");
        if (currentPage != null) {
            currentPage.dispose(renderUtil);
            currentPage.removeRenderers(renderUtil);
            currentPage = null;
        }

        renderUtil.removeRenderer(backgroundDim);
        renderUtil.removeRenderer(menuContainer);
        renderUtil.removeRenderer(contentMenu);
        renderUtil.removeRenderer(pageTitle);
        renderUtil.removeRenderer(collapseSideMenuButton);
        renderUtil.removeRenderer(sideMenu);
        removeSideMenuRenderers();
        backgroundBlurRenderer.dispose();

        if (stage != null) {
            stage.dispose();
            stage = null;
        }
    }

    private void configureSideMenuButton(Button button, String text, Renderer.OnClick onClickAction) {
        button.setSize(sideMenuWidth - sideMenuButtonPadding * 2, sideMenuButtonHeight);
        button.setText(text);
        button.setBorderWidth(1.5f);
        button.setBorderRadius(sideMenuButtonBorderRadius);
        button.setOnClickUp(onClickAction);
    }

    private void addSideMenuPage(Page page) {
        Button button = new Button();
        configureSideMenuButton(button, page.getTitle(), createSwitchPageAction(page));
        pageEntries.add(new PageEntry(page, button));
    }

    private void addSideMenuRenderers() {
        for (PageEntry entry : pageEntries) {
            renderUtil.addRenderer(entry.button);
        }
    }

    private void removeSideMenuRenderers() {
        for (PageEntry entry : pageEntries) {
            renderUtil.removeRenderer(entry.button);
        }
    }

    public Renderer.OnClick createSwitchPageAction(Page page) {
        return (renderer, button) -> {
            if (page == null || currentPage == page) {
                return;
            }

            Client.LOGGER.info("Queueing page switch from {} to {}", currentPage.getClass().getSimpleName(), page.getClass().getSimpleName());

            if (currentPage != null) {
                currentPage.dispose(renderUtil);
                currentPage.removeRenderers(renderUtil);
            }

            currentPage = page;

            currentPage.create(viewport, screenW, screenH);
            currentPage.addRenderers(renderUtil);

            pageTitle.setText(currentPage.getTitle());
            saveGuiState();

            Gdx.app.postRunnable(() -> resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        };
    }

    public Renderer.OnClick createToggleSideMenuAction() {
        return (renderer, button) -> {
            boolean nextSideMenuCollapsed = !sideMenuCollapsed;
            Client.LOGGER.info("Queueing side menu toggle, collapsed: {}", nextSideMenuCollapsed);

            Gdx.app.postRunnable(() -> {
                sideMenuCollapsed = nextSideMenuCollapsed;

                if (sideMenuCollapsed) {
                    renderUtil.removeRenderer(sideMenu);
                    removeSideMenuRenderers();
                } else {
                    renderUtil.addRenderer(sideMenu);
                    addSideMenuRenderers();
                }

                resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                collapseSideMenuButton.getTextRenderer().setText(sideMenuCollapsed ? ">" : "<");
                saveGuiState();
            });
        };
    }

    public Stage getStage() {
        return this.stage;
    }

    public Page getCurrentPage() {
        return this.currentPage;
    }

    private void syncClientSettings() {
        backgroundDim.setFillColor(ClientSettings.isGuiBackgroundDimEnabled()
                ? mainDimColor
                : ColorUtils.TRANSPARENT);
    }

    private void renderBackground() {
        if (!previousGamestate.isCreated()) {
            Client.LOGGER.error("Previous GameState is not created! Falling back to empty background.");
            clearBackground();
            return;
        }

        if (ClientSettings.isGuiBackgroundBlurEnabled()) {
            backgroundBlurRenderer.render(previousGamestate, this::clearBackground, ClientSettings.getGuiBackgroundBlurStrength());
            return;
        }

        try {
            previousGamestate.render();
        } catch (Exception e) {
            Client.LOGGER.error("Error rendering previous GameState! Falling back to empty background.\n{}\n\n{}", e.getMessage(), e);
            clearBackground();
        }
    }

    private void clearBackground() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    private Page getPageByTitle(String pageTitle) {
        if (PAGE_HOME.equals(pageTitle)) {
            return homePage;
        }

        for (PageEntry entry : pageEntries) {
            if (entry.page.getTitle().equals(pageTitle)) {
                return entry.page;
            }
        }

        return homePage;
    }

    private void saveGuiState() {
        lastPageTitle = currentPage == null ? PAGE_HOME : currentPage.getTitle();
        lastSideMenuCollapsed = sideMenuCollapsed;
    }
}
