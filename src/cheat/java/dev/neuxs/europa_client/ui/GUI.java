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
import dev.neuxs.europa_client.settings.ClientSettings;
import dev.neuxs.europa_client.ui.pages.*;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;
import dev.neuxs.europa_client.utils.rendering.Renderer;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;
import dev.neuxs.europa_client.utils.rendering.ui.Button;
import finalforeach.cosmicreach.gamestates.GameState;

@SuppressWarnings({"DuplicatedCode"})
public class GUI extends GameState {
    private static final RenderUtil renderUtil = new RenderUtil();

    private final Color mainDimColor = ColorUtils.color(0, 0, 0, 75);
    private final Color mainBackgroundColor = ColorUtils.color(40, 40, 40, 255);
    private final Color mainBorderColor = ColorUtils.color(60, 60, 60, 255);

    private final GameState previousGamestate;
    private Viewport viewport;
    private float screenW;
    private float screenH;

    private final BoxRenderer backgroundDim = new BoxRenderer();
    private final BoxRenderer menuContainer = new BoxRenderer();
    private final BoxRenderer sideMenu = new BoxRenderer();
    private final BoxRenderer contentMenu = new BoxRenderer();
    private final TextRenderer pageTitle = new TextRenderer();
    private final Button collapseSideMenuButton = new Button();
    private final Button utilitiesButton = new Button();
    private final Button cheatsButton = new Button();
    private final Button profilesButton = new Button();
    private final Button settingsButton = new Button();

    private final HomePage homePage = new HomePage(contentMenu);
    private final UtilitiesPage utilitiesPage = new UtilitiesPage(contentMenu);
    private final CheatsPage cheatsPage = new CheatsPage(contentMenu);
    private final ProfilesPage profilesPage = new ProfilesPage(contentMenu);
    private final SettingsPage settingsPage = new SettingsPage(contentMenu);
    private Page currentPage;

    public Vector2 menuSize = new Vector2(0, 0);
    public float menuPadding = 7.5f;
    public boolean sideMenuCollapsed = false;
    public float sideMenuWidth = 110f;
    public float sideMenuButtonPadding = 10f;
    public float sideMenuButtonBorderRadius = 7.5f;

    public GUI(GameState previousGamestate) {
        this.previousGamestate = previousGamestate;
    }

    @Override
    public void create() {
        super.create();

        Gdx.input.setCursorCatched(false);

        viewport = this.newUiViewport;

        menuSize.set(screenW / 1.8f, screenH / 1.65f);

        syncClientSettings();
        menuContainer.setFillColor(mainBorderColor);
        menuContainer.setBorderRadius(15f);
        contentMenu.setFillColor(mainBackgroundColor);
        contentMenu.setBorderRadius(10f);
        sideMenu.setFillColor(mainBackgroundColor);
        sideMenu.setBorderRadius(10f);

        currentPage = homePage;
        pageTitle.setText(currentPage.getTitle());

        collapseSideMenuButton.setSize(18f, 18f);
        collapseSideMenuButton.setFillColor(ColorUtils.color(0, 0, 0, 0));
        collapseSideMenuButton.setHoverFillColor(ColorUtils.color(0, 0, 0, 0));
        collapseSideMenuButton.setPressedFillColor(ColorUtils.color(0, 0, 0, 0));
        collapseSideMenuButton.setBorder(false);
        collapseSideMenuButton.setText(sideMenuCollapsed ? ">" : "<");
        collapseSideMenuButton.setOnClickUp(createToggleSideMenuAction());

        configureSideMenuButton(utilitiesButton, "Utilities", createSwitchPageAction(utilitiesPage));
        configureSideMenuButton(cheatsButton, "Cheats", createSwitchPageAction(cheatsPage));
        configureSideMenuButton(profilesButton, "Profiles", createSwitchPageAction(profilesPage));
        configureSideMenuButton(settingsButton, "Settings", createSwitchPageAction(settingsPage));

        renderUtil.addRenderer(backgroundDim);
        renderUtil.addRenderer(menuContainer);
        renderUtil.addRenderer(contentMenu);
        renderUtil.addRenderer(pageTitle);
        renderUtil.addRenderer(collapseSideMenuButton);

        if (!sideMenuCollapsed) {
            renderUtil.addRenderer(sideMenu);
            renderUtil.addRenderer(utilitiesButton);
            renderUtil.addRenderer(cheatsButton);
            renderUtil.addRenderer(profilesButton);
            renderUtil.addRenderer(settingsButton);
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

            currentY -= utilitiesButton.getHeight();
            utilitiesButton.setPos(sideMenuButtonX, currentY);

            currentY -= (sideMenuButtonPadding + cheatsButton.getHeight());
            cheatsButton.setPos(sideMenuButtonX, currentY);

            currentY = sideMenu.getPosY() + sideMenuButtonPadding;
            settingsButton.setPos(sideMenuButtonX, currentY);

            currentY += settingsButton.getHeight() + sideMenuButtonPadding;
            profilesButton.setPos(sideMenuButtonX, currentY);
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

        try {
            if (previousGamestate.isCreated()) previousGamestate.render();
            else {
                Client.LOGGER.error("Previous GameState is not created! Falling back to empty background.");
                Gdx.gl.glClearColor(0, 0, 0, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
            }
        } catch (Exception e) {
            Client.LOGGER.error("Error rendering previous GameState! Falling back to empty background.\n{}\n\n{}", e.getMessage(), e);
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        }

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
        renderUtil.removeRenderer(utilitiesButton);
        renderUtil.removeRenderer(cheatsButton);
        renderUtil.removeRenderer(profilesButton);
        renderUtil.removeRenderer(settingsButton);

        if (stage != null) {
            stage.dispose();
            stage = null;
        }
    }

    private void configureSideMenuButton(Button button, String text, Renderer.OnClick onClickAction) {
        button.setSize(sideMenuWidth - sideMenuButtonPadding * 2, 50);
        button.setText(text);
        button.setBorderWidth(1.5f);
        button.setBorderRadius(sideMenuButtonBorderRadius);
        button.setOnClickUp(onClickAction);
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
                    renderUtil.removeRenderer(utilitiesButton);
                    renderUtil.removeRenderer(cheatsButton);
                    renderUtil.removeRenderer(profilesButton);
                    renderUtil.removeRenderer(settingsButton);
                } else {
                    renderUtil.addRenderer(sideMenu);
                    renderUtil.addRenderer(utilitiesButton);
                    renderUtil.addRenderer(cheatsButton);
                    renderUtil.addRenderer(profilesButton);
                    renderUtil.addRenderer(settingsButton);
                }

                resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                collapseSideMenuButton.getTextRenderer().setText(sideMenuCollapsed ? ">" : "<");
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
}
