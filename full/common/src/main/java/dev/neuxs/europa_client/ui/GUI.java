package dev.neuxs.europa_client.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import finalforeach.cosmicreach.entities.PlayerController;
import finalforeach.cosmicreach.gamestates.GameState;
import dev.neuxs.europa_client.accessor.InGameAccessor;
import finalforeach.cosmicreach.ui.actions.AlignXAction;
import finalforeach.cosmicreach.ui.actions.AlignYAction;
import finalforeach.cosmicreach.ui.widgets.CRButton;

public class GUI extends GameState implements InputProcessor {
    private InputProcessor previousInputProcessor;
    private boolean previousCursorCatchedState;
    private InputMultiplexer inputMultiplexer;

    public GUI() {}

    @Override
    public void create() {
        super.create();
        this.previousInputProcessor = Gdx.input.getInputProcessor();
        this.previousCursorCatchedState = Gdx.input.isCursorCatched();
        this.inputMultiplexer = new InputMultiplexer(this.stage, this);
        Gdx.input.setInputProcessor(this.inputMultiplexer);
        Gdx.input.setCursorCatched(false);

        // On-Screen UI Rendering

        CRButton exampleButton = new CRButton("Test Button") {
            @Override
            public void onClick() {
                super.onClick();
                System.out.println("Test Button Clicked!");
            }
        };
        exampleButton.addAction(new AlignXAction(1, 0.5f));
        exampleButton.addAction(new AlignYAction(8, 0.5f, 25.0f));
        exampleButton.setSize(200, 50);
        this.stage.addActor(exampleButton);
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
        this.stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));

        if (GameState.IN_GAME.isCreated()) {
            InGameAccessor inGameAccessor = (InGameAccessor) GameState.IN_GAME;
            PlayerController pc = inGameAccessor.europa_client$getPlayerController_accessor();
            if (pc != null && GameState.IN_GAME.getWorldCamera() instanceof PerspectiveCamera) {
                pc.updateCamera((PerspectiveCamera) GameState.IN_GAME.getWorldCamera());
            }
        }
    }

    @Override
    public void render() {
        super.render();

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
        this.newUiViewport.apply(true);

        this.stage.draw();

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);

        if (this.firstFrame) {
            this.firstFrame = false;
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (!this.firstFrame && keycode == Input.Keys.ESCAPE && this.stage.getKeyboardFocus() == null) {
            switchToPreviousState();
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

    @Override
    public void onSwitchTo() {
        super.onSwitchTo();
        if (this.inputMultiplexer == null) {
            this.inputMultiplexer = new InputMultiplexer(this.stage, this);
        }
        Gdx.input.setInputProcessor(this.inputMultiplexer);
        Gdx.input.setCursorCatched(false);
        System.out.println("EuropaMenu onSwitchTo().");
    }

    @Override
    public void switchAwayTo(GameState nextGameState) {
        super.switchAwayTo(nextGameState);
        if (this.previousInputProcessor != null) {
            Gdx.input.setInputProcessor(this.previousInputProcessor);
        } else {
            Gdx.input.setInputProcessor(null);
        }
        System.out.println("EuropaMenu switchAwayTo(). Restored input to: " + (Gdx.input.getInputProcessor() != null ? Gdx.input.getInputProcessor().getClass().getSimpleName() : "null"));

        if (nextGameState == GameState.IN_GAME) {
            Gdx.input.setCursorCatched(this.previousCursorCatchedState);
            System.out.println("EuropaMenu: Re-catching cursor: " + this.previousCursorCatchedState);
        } else {
            Gdx.input.setCursorCatched(false);
            System.out.println("EuropaMenu: Releasing cursor.");
        }
    }

    private void switchToPreviousState() {
        GameState.switchToGameState(GameState.IN_GAME);
    }

    @Override public boolean dropsCursorItems() { return true; }
    @Override public void dispose() {
        super.dispose();
        if (Gdx.input.getInputProcessor() == this.inputMultiplexer) {
            Gdx.input.setInputProcessor(null);
        }
        if (this.stage != null) {
            this.stage.dispose();
        }
    }
}