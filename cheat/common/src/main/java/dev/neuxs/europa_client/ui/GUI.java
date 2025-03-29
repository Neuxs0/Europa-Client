package dev.neuxs.europa_client.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.accessor.InGameAccessor;
import dev.neuxs.europa_client.ui.pages.*;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;
import dev.neuxs.europa_client.utils.rendering.ui.Button;
import finalforeach.cosmicreach.entities.PlayerController;
import finalforeach.cosmicreach.gamestates.GameState;

@SuppressWarnings("DuplicatedCode")
public class GUI extends GameState implements InputProcessor {
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final GlyphLayout glyphLayout = new GlyphLayout();

    private final Color mainDimColor = ColorUtils.color(0, 0, 0, 75);
    private final Color mainBackgroundColor = ColorUtils.color(40, 40, 40, 255);
    private final Color mainBorderColor = ColorUtils.color(60, 60, 60, 255);
    private Viewport viewport = GameState.IN_GAME.isCreated() && GameState.IN_GAME.newUiViewport != null ? GameState.IN_GAME.newUiViewport : this.uiViewport;
    private float screenW = viewport.getWorldWidth();
    private float screenH = viewport.getWorldHeight();
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
    private Page currentPage = homePage;

    public Vector2 menuSize = new Vector2(screenW / 1.8f, screenH / 1.65f);
    public float menuPadding = 7.5f;
    public boolean sideMenuCollapsed = false;
    public float sideMenuWidth = 110f;
    public float sideMenuButtonPadding = 10f;
    public float sideMenuButtonBorderRadius = 7.5f;

    public GUI() {}

    @Override
    public void create() {
        super.create();
        InputMultiplexer inputMultiplexer = new InputMultiplexer(this.stage, this);
        Gdx.input.setInputProcessor(inputMultiplexer);
        Gdx.input.setCursorCatched(false);

        backgroundDim.setSize(screenW, screenH);
        backgroundDim.setFillColor(mainDimColor);

        // Menu
        menuContainer.setSize(menuSize.x, menuSize.y);
        menuContainer.setFillColor(mainBorderColor);
        menuContainer.setBorderRadius(15f);

        sideMenu.setSize(sideMenuWidth, menuContainer.getHeight() - (menuPadding * 2));
        sideMenu.setFillColor(mainBackgroundColor);
        sideMenu.setBorderRadius(10f);

        contentMenu.setFillColor(mainBackgroundColor);
        contentMenu.setBorderRadius(10f);

        pageTitle.setText(currentPage.getTitle());

        collapseSideMenuButton.setSize(18f, 18f);
        collapseSideMenuButton.setNormalFillColor(ColorUtils.color(0, 0, 0, 0));
        collapseSideMenuButton.setHoverFillColor(ColorUtils.color(0, 0, 0, 0));
        collapseSideMenuButton.setPressedFillColor(ColorUtils.color(0, 0, 0, 0));
        collapseSideMenuButton.getBoxRenderer().setBorderEnabled(false);
        collapseSideMenuButton.getTextRenderer().setText("<");
        collapseSideMenuButton.setOnClick((button) -> toggleSideMenu());

        // Side Menu Buttons
        utilitiesButton.setSize(sideMenuWidth - sideMenuButtonPadding * 2, 50);
        utilitiesButton.getTextRenderer().setText("Utilities");
        utilitiesButton.getTextRenderer().setColor(ColorUtils.color(255, 255, 255, 255));
        utilitiesButton.getBoxRenderer().setBorderWidth(1.5f);
        utilitiesButton.getBoxRenderer().setBorderRadius(sideMenuButtonBorderRadius);
        utilitiesButton.setOnClick((button) -> switchPage(utilitiesPage));

        cheatsButton.setSize(sideMenuWidth - sideMenuButtonPadding * 2, 50);
        cheatsButton.getTextRenderer().setText("Cheats");
        cheatsButton.getTextRenderer().setColor(ColorUtils.color(255, 255, 255, 255));
        cheatsButton.getBoxRenderer().setBorderWidth(1.5f);
        cheatsButton.getBoxRenderer().setBorderRadius(sideMenuButtonBorderRadius);
        cheatsButton.setOnClick((button) -> switchPage(cheatsPage));

        profilesButton.setSize(sideMenuWidth - sideMenuButtonPadding * 2, 50);
        profilesButton.getTextRenderer().setText("Profiles");
        profilesButton.getTextRenderer().setColor(ColorUtils.color(255, 255, 255, 255));
        profilesButton.getBoxRenderer().setBorderWidth(1.5f);
        profilesButton.getBoxRenderer().setBorderRadius(sideMenuButtonBorderRadius);
        profilesButton.setOnClick((button) -> switchPage(profilesPage));

        settingsButton.setSize(sideMenuWidth - sideMenuButtonPadding * 2, 50);
        settingsButton.getTextRenderer().setText("Settings");
        settingsButton.getTextRenderer().setColor(ColorUtils.color(255, 255, 255, 255));
        settingsButton.getBoxRenderer().setBorderWidth(1.5f);
        settingsButton.getBoxRenderer().setBorderRadius(sideMenuButtonBorderRadius);
        settingsButton.setOnClick((button) -> switchPage(settingsPage));

        currentPage.create(viewport, screenW, screenH);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        if (GameState.IN_GAME.isCreated()) {
            GameState.IN_GAME.resize(width, height);
            if (GameState.IN_GAME.newUiViewport != null) {
                viewport = GameState.IN_GAME.newUiViewport;
            }
            screenW = viewport.getWorldWidth();
            screenH = viewport.getWorldHeight();
        }

        menuContainer.setPosition(
                screenW / 2f - menuContainer.getWidth() / 2f,
                screenH / 2f - menuContainer.getHeight() / 2f
        );

        sideMenu.setPosition(
                menuContainer.getPosX() + menuPadding,
                menuContainer.getPosY() + menuPadding
        );

        if (sideMenuCollapsed) {
            contentMenu.setSize(
                    menuContainer.getWidth() - menuPadding * 2,
                    menuContainer.getHeight() - (16f + (menuPadding * 3))
            );
            contentMenu.setPosition(
                    menuContainer.getPosX() + menuPadding,
                    menuContainer.getPosY() + menuPadding
            );
        } else {
            contentMenu.setSize(
                    menuContainer.getWidth() - sideMenu.getWidth() - menuPadding * 3,
                    menuContainer.getHeight() - (16f + (menuPadding * 3))
            );
            contentMenu.setPosition(
                    sideMenu.getPosX() + sideMenu.getWidth() + menuPadding,
                    menuContainer.getPosY() + menuPadding
            );
        }

        pageTitle.setPosition(
                contentMenu.getPosX() + ((contentMenu.getWidth() / 2) - (pageTitle.getWidth(viewport) / 2)),
                contentMenu.getPosY() + contentMenu.getHeight() + menuPadding
        );

        collapseSideMenuButton.setPosition(
                contentMenu.getPosX(),
                contentMenu.getPosY() + contentMenu.getHeight() + menuPadding
        );

        float sideMenuButtonX = sideMenu.getPosX() + sideMenuButtonPadding;
        float sideMenuButtonY = sideMenu.getPosY() + sideMenu.getHeight() - sideMenuButtonPadding;

        sideMenuButtonY -= utilitiesButton.getHeight();
        utilitiesButton.setPosition(sideMenuButtonX, sideMenuButtonY);

        sideMenuButtonY -= sideMenuButtonPadding + cheatsButton.getHeight();
        cheatsButton.setPosition(sideMenuButtonX, sideMenuButtonY);

        sideMenuButtonY = sideMenu.getPosY() + sideMenuButtonPadding;
        settingsButton.setPosition(sideMenuButtonX, sideMenuButtonY);

        sideMenuButtonY += settingsButton.getHeight() + sideMenuButtonPadding;
        profilesButton.setPosition(sideMenuButtonX, sideMenuButtonY);

        currentPage.resize(screenW, screenH);
    }

    @Override
    public void update(float deltaTime) {
        if (this.stage != null) {
            this.stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        }

        if (GameState.IN_GAME.isCreated()) {
            try {
                InGameAccessor inGameAccessor = (InGameAccessor) GameState.IN_GAME;
                PlayerController pc = inGameAccessor.europa_client$getPlayerController_accessor();
                if (pc != null && GameState.IN_GAME.getWorldCamera() instanceof PerspectiveCamera) {
                    pc.updateCamera((PerspectiveCamera) GameState.IN_GAME.getWorldCamera());
                }
            } catch (Exception e) {
                Client.LOGGER.error("Error updating player controller camera: {}", e.getMessage());
            }
        }

        currentPage.update(deltaTime);
    }

    @Override
    public void render() {
        super.render();

        if (GameState.IN_GAME.isCreated() && GameState.IN_GAME.newUiViewport != null) {
            viewport = GameState.IN_GAME.newUiViewport;
        } else {
            Client.LOGGER.error("UI viewport is null");
        }

        if (GameState.IN_GAME.isCreated()) {
            GameState.IN_GAME.render();
        } else {
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        }

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        Matrix4 uiMatrix;
        viewport.apply(true);
        uiMatrix = viewport.getCamera().combined;

        int screenPixelWidth = Gdx.graphics.getWidth();
        int screenPixelHeight = Gdx.graphics.getHeight();
        Gdx.gl.glViewport(0, 0, screenPixelWidth, screenPixelHeight);



        // Shape Renderer batch
        shapeRenderer.setProjectionMatrix(uiMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        backgroundDim.render(shapeRenderer);

        menuContainer.render(shapeRenderer);
        if (!sideMenuCollapsed) sideMenu.render(shapeRenderer);
        contentMenu.render(shapeRenderer);
        collapseSideMenuButton.renderShape(shapeRenderer, viewport);

        if (!sideMenuCollapsed) {
            utilitiesButton.renderShape(shapeRenderer, viewport);
            cheatsButton.renderShape(shapeRenderer, viewport);
            profilesButton.renderShape(shapeRenderer, viewport);
            settingsButton.renderShape(shapeRenderer, viewport);
        }

        currentPage.renderShape(shapeRenderer);

        shapeRenderer.end();

        // Sprite Batch batch
        spriteBatch.setProjectionMatrix(uiMatrix);
        spriteBatch.begin();

        if (!sideMenuCollapsed) {
            utilitiesButton.renderText(spriteBatch, glyphLayout, viewport);
            cheatsButton.renderText(spriteBatch, glyphLayout, viewport);
            profilesButton.renderText(spriteBatch, glyphLayout, viewport);
            settingsButton.renderText(spriteBatch, glyphLayout, viewport);
        }

        pageTitle.render(spriteBatch, glyphLayout);
        collapseSideMenuButton.renderText(spriteBatch, glyphLayout, viewport);

        currentPage.renderText(spriteBatch, glyphLayout);

        spriteBatch.end();



        if (this.stage != null) {
            this.stage.draw();
        }

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);

        if (this.firstFrame) {
            this.firstFrame = false;
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        if (stage != null) stage.dispose();
    }

    public void switchPage(Page page) {
        if (!currentPage.equals(page)) {
            currentPage.dispose();
            currentPage = page;
            currentPage.create(viewport, screenW, screenH);
            pageTitle.setText(currentPage.getTitle());
            pageTitle.setX(contentMenu.getPosX() + ((contentMenu.getWidth() / 2) - (pageTitle.getWidth(viewport) / 2)));
        }
    }

    public void toggleSideMenu() {
        sideMenuCollapsed = !sideMenuCollapsed;
        if (sideMenuCollapsed) {
            collapseSideMenuButton.getTextRenderer().setText(">");

            contentMenu.setSize(
                    menuContainer.getWidth() - menuPadding * 2,
                    menuContainer.getHeight() - (16f + (menuPadding * 3))
            );
            contentMenu.setPosition(
                    menuContainer.getPosX() + menuPadding,
                    menuContainer.getPosY() + menuPadding
            );
        } else {
            collapseSideMenuButton.getTextRenderer().setText("<");

            contentMenu.setSize(
                    menuContainer.getWidth() - sideMenu.getWidth() - menuPadding * 3,
                    menuContainer.getHeight() - (16f + (menuPadding * 3))
            );
            contentMenu.setPosition(
                    sideMenu.getPosX() + sideMenu.getWidth() + menuPadding,
                    menuContainer.getPosY() + menuPadding
            );
        }
        pageTitle.setPosition(
                contentMenu.getPosX() + ((contentMenu.getWidth() / 2) - (pageTitle.getWidth(viewport) / 2)),
                contentMenu.getPosY() + contentMenu.getHeight() + menuPadding
        );

        collapseSideMenuButton.setPosition(
                contentMenu.getPosX(),
                contentMenu.getPosY() + contentMenu.getHeight() + menuPadding
        );
        currentPage.resize(screenW, screenH);
    }

    @Override public boolean keyDown(int keycode) {
        if (!this.firstFrame && keycode == Input.Keys.ESCAPE && (this.stage == null || this.stage.getKeyboardFocus() == null)) {
            GameState.switchToGameState(GameState.IN_GAME);
            return true;
        }
        return false;
    }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
}
