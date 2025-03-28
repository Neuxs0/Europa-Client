package dev.neuxs.europa_client.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.accessor.InGameAccessor;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.Renderer;
import dev.neuxs.europa_client.utils.rendering.ui.BoxRenderer;
import finalforeach.cosmicreach.entities.PlayerController;
import finalforeach.cosmicreach.gamestates.GameState;

public class GUI extends GameState implements InputProcessor {
    private final Color mainDimColor = ColorUtils.color(0, 0, 0, 75);
    private final Color mainBackgroundColor = ColorUtils.color(40, 40, 40, 255);
    private final Color mainBorderColor = ColorUtils.color(60, 60, 60, 255);

    private final BoxRenderer backgroundDim = new BoxRenderer();
    private final BoxRenderer menuContainer = new BoxRenderer();

    public GUI() {}

    public void renderMenu(Viewport viewport, Matrix4 projectionMatrix) {
        if (viewport == null || projectionMatrix == null) {
            return;
        }

        float worldW = viewport.getWorldWidth();
        float worldH = viewport.getWorldHeight();

        backgroundDim.setSize(worldW, worldH);
        backgroundDim.render(projectionMatrix);


        menuContainer.setSize(worldW / 1.5f, worldH / 1.5f);
        menuContainer.setPosition(
                worldW / 2f - menuContainer.getWidth() / 2f,
                worldH / 2f - menuContainer.getHeight() / 2f
        );
        menuContainer.render(projectionMatrix);

        // Side menu seperator
        float sideMenuSeperatorX = menuContainer.getPosX() + 125; // menuContainerX + distance from left edge of menu
        Renderer.drawLine(
                projectionMatrix,
                sideMenuSeperatorX,
                menuContainer.getPosY(),
                sideMenuSeperatorX,
                menuContainer.getPosY() + menuContainer.getHeight(),
                menuContainer.getBorderWidth(),
                mainBorderColor
        );

        // Page area corner dimensions:
        // Bottom-Left:  (308, 105)
        // Bottom-Right: (884, 105)
        // Top-Right:    (884, 495)
        // Top-Left:     (308, 495)

//        HomePage.renderContent(projectionMatrix);
    }

    @Override
    public void create() {
        super.create();
        InputMultiplexer inputMultiplexer = new InputMultiplexer(this.stage, this);
        Gdx.input.setInputProcessor(inputMultiplexer);
        Gdx.input.setCursorCatched(false);

        backgroundDim.setFillColor(mainDimColor);

        menuContainer.setFillColor(mainBackgroundColor);
        menuContainer.setBorderWidth(5f);
        menuContainer.setBorderEnabled(true);
        menuContainer.setBorderColor(mainBorderColor);
        menuContainer.setBorderRadius(15f);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        if (GameState.IN_GAME.isCreated()) {
            GameState.IN_GAME.resize(width, height);
        }
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

        Viewport relevantViewport;
        if (GameState.IN_GAME.isCreated()) {
            relevantViewport = GameState.IN_GAME.newUiViewport;
            if (relevantViewport == null) {
                relevantViewport = this.newUiViewport;
            }
        } else {
            relevantViewport = this.newUiViewport;
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

        Matrix4 currentUiMatrix;
        relevantViewport.apply(true);
        currentUiMatrix = relevantViewport.getCamera().combined;

        int screenPixelWidth = Gdx.graphics.getWidth();
        int screenPixelHeight = Gdx.graphics.getHeight();
        Gdx.gl.glViewport(0, 0, screenPixelWidth, screenPixelHeight);

        renderMenu(relevantViewport, currentUiMatrix);

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