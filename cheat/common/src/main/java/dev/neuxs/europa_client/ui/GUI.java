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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.accessor.InGameAccessor;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;
import dev.neuxs.europa_client.utils.rendering.LineRenderer;
import dev.neuxs.europa_client.utils.rendering.ui.Button;
import finalforeach.cosmicreach.entities.PlayerController;
import finalforeach.cosmicreach.gamestates.GameState;

public class GUI extends GameState implements InputProcessor {
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private final Color mainDimColor = ColorUtils.color(0, 0, 0, 75);
    private final Color mainBackgroundColor = ColorUtils.color(40, 40, 40, 255);
    private final Color mainBorderColor = ColorUtils.color(60, 60, 60, 255);
    private final BoxRenderer backgroundDim = new BoxRenderer();
    private final BoxRenderer menuContainer = new BoxRenderer();
    private final LineRenderer sideMenuSeparator = new LineRenderer();
    private final Button utilitiesButton = new Button();
    private final Button cheatsButton = new Button();
    private final Button profilesButton = new Button();
    private final Button settingsButton = new Button();
    private Viewport viewport = GameState.IN_GAME.isCreated() && GameState.IN_GAME.newUiViewport != null ? GameState.IN_GAME.newUiViewport : this.uiViewport;
    private float screenW = viewport.getWorldWidth();
    private float screenH = viewport.getWorldHeight();

    public float sideMenuSeperatorAmount = 125f;

    public GUI() {}

    @Override
    public void create() {
        super.create();
        InputMultiplexer inputMultiplexer = new InputMultiplexer(this.stage, this);
        Gdx.input.setInputProcessor(inputMultiplexer);
        Gdx.input.setCursorCatched(false);

        /*
         Page area corner dimensions:
         Bottom-Left:  (308, 105)
         Bottom-Right: (884, 105)
         Top-Right:    (884, 495)
         Top-Left:     (308, 495)
        */

        backgroundDim.setSize(screenW, screenH);
        backgroundDim.setFillColor(mainDimColor);

        menuContainer.setSize(screenW / 1.5f, screenH / 1.5f);
        menuContainer.setPosition(
                screenW / 2f - menuContainer.getWidth() / 2f,
                screenH / 2f - menuContainer.getHeight() / 2f
        );
        menuContainer.setFillColor(mainBackgroundColor);
        menuContainer.setBorderWidth(5f);
        menuContainer.setBorderEnabled(true);
        menuContainer.setBorderColor(mainBorderColor);
        menuContainer.setBorderRadius(15f);

        sideMenuSeparator.setStartPoint(menuContainer.getPosX() + sideMenuSeperatorAmount, menuContainer.getPosY());
        sideMenuSeparator.setEndPoint(menuContainer.getPosX() + sideMenuSeperatorAmount, menuContainer.getPosY() + menuContainer.getHeight());
        sideMenuSeparator.setColor(mainBorderColor);
        sideMenuSeparator.setWidth(menuContainer.getBorderWidth());

        // Side Menu
        utilitiesButton.setSize(100, 50);
        utilitiesButton.setPosition(
                menuContainer.getPosX() + 12.5f,
                (menuContainer.getPosY() + menuContainer.getHeight() - 12.5f) - utilitiesButton.getHeight()
        );
        utilitiesButton.getTextRenderer().setText("Utilities");
        utilitiesButton.getTextRenderer().setColor(ColorUtils.color(255, 255, 255, 255));
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

        backgroundDim.setSize(screenW, screenH);

        menuContainer.setSize(screenW / 1.5f, screenH / 1.5f);
        menuContainer.setPosition(
                screenW / 2f - menuContainer.getWidth() / 2f,
                screenH / 2f - menuContainer.getHeight() / 2f
        );


        sideMenuSeparator.setStartPoint(menuContainer.getPosX() + sideMenuSeperatorAmount, menuContainer.getPosY());
        sideMenuSeparator.setEndPoint(menuContainer.getPosX() + sideMenuSeperatorAmount, menuContainer.getPosY() + menuContainer.getHeight());

        utilitiesButton.setPosition(
                menuContainer.getPosX() + 12.5f,
                (menuContainer.getPosY() + menuContainer.getHeight() - 12.5f) - utilitiesButton.getHeight()
        );
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
        sideMenuSeparator.render(shapeRenderer);

        utilitiesButton.renderShape(shapeRenderer, viewport);
        cheatsButton.renderShape(shapeRenderer, viewport);
        profilesButton.renderShape(shapeRenderer, viewport);
        settingsButton.renderShape(shapeRenderer, viewport);

        shapeRenderer.end();

        // Sprite Batch batch
        spriteBatch.setProjectionMatrix(uiMatrix);
        spriteBatch.begin();

        utilitiesButton.renderText(spriteBatch, glyphLayout, viewport);
        cheatsButton.renderText(spriteBatch, glyphLayout, viewport);
        profilesButton.renderText(spriteBatch, glyphLayout, viewport);
        settingsButton.renderText(spriteBatch, glyphLayout, viewport);

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
