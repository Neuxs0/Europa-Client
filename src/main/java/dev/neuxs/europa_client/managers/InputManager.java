package dev.neuxs.europa_client.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.settings.SettingsManager;
import dev.neuxs.europa_client.ui.GUI;
import dev.neuxs.europa_client.utils.KeybindUtil;
import finalforeach.cosmicreach.gamestates.ChatMenu;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class InputManager extends InputAdapter {
    private static InputManager instance;

    public static InputManager getInstance() {
        if (instance == null) {
            instance = new InputManager();
        }
        return instance;
    }

    private boolean captureTextInput = false;
    private final StringBuilder textInputBuffer = new StringBuilder();
    private Consumer<String> textInputEnterAction = null;
    private Runnable textInputCancelAction = null;
    private Object textInputRequester = null;
    private Vector2 mousePos = new Vector2(0, 0);
    private static final Set<Integer> consumedMouseButtons = new HashSet<>();

    private InputManager() {}

    public void initialize() {
        if (GameState.currentGameState instanceof ChatMenu) {
            return;
        }

        if (GameState.currentGameState instanceof InGame) {
            processInGameKeybinds();
            return;
        }

        if (GameState.currentGameState instanceof GUI && Gdx.input.getInputProcessor() != this) {
            Gdx.input.setInputProcessor(this);
        }
    }

    public void startTextInput(Object requester, Consumer<String> onEnter, Runnable onCancel, String initialText) {
        if (captureTextInput) {
            Client.LOGGER.warn("InputManager received startTextInput request while already capturing for {}. Ignoring new request.", textInputRequester);
            return;
        }
        Client.LOGGER.info("InputManager starting text capture for: {}", requester);
        this.captureTextInput = true;
        this.textInputRequester = requester;
        this.textInputEnterAction = onEnter;
        this.textInputCancelAction = onCancel;
        this.textInputBuffer.setLength(0);
        if (initialText != null) {
            this.textInputBuffer.append(initialText);
        }
    }

    public void stopTextInput() {
        if (captureTextInput) {
            Client.LOGGER.info("InputManager stopping text capture for: {}", textInputRequester);
        }
        this.captureTextInput = false;
        this.textInputRequester = null;
        this.textInputEnterAction = null;
        this.textInputCancelAction = null;
        this.textInputBuffer.setLength(0);
    }

    public void cancelTextInput() {
        if (captureTextInput) {
            Client.LOGGER.info("InputManager cancelling text capture for: {}", textInputRequester);
            if (textInputCancelAction != null) {
                try {
                    textInputCancelAction.run();
                } catch (Exception e) {
                    Client.LOGGER.error("Error running text input cancel action", e);
                }
            }
        }
        stopTextInput();
    }

    public String getTextInputBuffer() {
        return textInputBuffer.toString();
    }

    public boolean isCapturingFor(Object requester) {
        return captureTextInput && this.textInputRequester == requester;
    }

    @Override
    public boolean keyDown(int keycode) {
        GameState currentState = GameState.currentGameState;
        Stage currentStage = getCurrentStage();

        if (captureTextInput) {
            switch (keycode) {
                case Input.Keys.ENTER:
                    if (textInputEnterAction != null) {
                        try {
                            textInputEnterAction.accept(textInputBuffer.toString());
                        } catch (Exception e) {
                            Client.LOGGER.error("Error running text input enter action", e);
                        }
                    }
                    stopTextInput();
                    return true;

                case Input.Keys.ESCAPE:
                    cancelTextInput();
                    return true;

                case Input.Keys.BACKSPACE:
                    if (!textInputBuffer.isEmpty()) {
                        textInputBuffer.deleteCharAt(textInputBuffer.length() - 1);
                    }
                    return true;
            }
        }

        if (currentState instanceof InGame) {
            return false;

        } else if (currentState instanceof GUI) {
            Object currentPage = ((GUI) currentState).getCurrentPage();
            if (currentPage instanceof InputProcessor inputProcessor && inputProcessor.keyDown(keycode)) {
                return true;
            }

            if (keycode == Input.Keys.ESCAPE) {
                if (currentStage == null || currentStage.getKeyboardFocus() == null) {
                    GameState previousState = GameState.IN_GAME;
                    if (previousState.isCreated()) {
                        GameState.switchToGameState(previousState);
                        Gdx.input.setCursorCatched(true);
                        return true;
                    }
                }
            }

            if (currentStage != null) {
                return currentStage.keyDown(keycode);
            }
            return false;

        } else if (currentState instanceof ChatMenu) {
            if (currentStage != null) {
                return currentStage.keyDown(keycode);
            }
            return false;
        }

        return false;
    }
    @Override
    public boolean keyUp(int keycode) {
        GameState currentState = GameState.currentGameState;
        Stage currentStage = getCurrentStage();

        if (currentState instanceof GUI || currentState instanceof ChatMenu) {
            if (currentState instanceof GUI) {
                Object currentPage = ((GUI) currentState).getCurrentPage();
                if (currentPage instanceof InputProcessor inputProcessor && inputProcessor.keyUp(keycode)) {
                    return true;
                }
            }

            if (currentStage != null) {
                return currentStage.keyUp(keycode);
            }
        }

        return false;
    }
    @Override
    public boolean keyTyped(char character) {
        GameState currentState = GameState.currentGameState;
        Stage currentStage = getCurrentStage();

        if (captureTextInput) {
            if (character >= 32 || character == '\t') {
                textInputBuffer.append(character);
            }

            return true;
        }

        if (currentState instanceof GUI || currentState instanceof ChatMenu) {
            if (currentState instanceof GUI) {
                Object currentPage = ((GUI) currentState).getCurrentPage();
                if (currentPage instanceof InputProcessor inputProcessor && inputProcessor.keyTyped(character)) {
                    return true;
                }
            }

            if (currentStage != null) {
                return currentStage.keyTyped(character);
            }
        }

        return false;
    }
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        GameState currentState = GameState.currentGameState;
        Stage currentStage = getCurrentStage();

        if (currentState instanceof GUI || currentState instanceof ChatMenu) {
            if (currentState instanceof GUI) {
                Object currentPage = ((GUI) currentState).getCurrentPage();
                if (currentPage instanceof InputProcessor inputProcessor && inputProcessor.touchDown(screenX, screenY, pointer, button)) {
                    consumedMouseButtons.add(button);
                    return true;
                }
            }

            if (currentStage != null) {
                boolean handled = currentStage.touchDown(screenX, screenY, pointer, button);
                if (handled) {
                    consumedMouseButtons.add(button);
                }
                return handled;
            }
        }

        return false;
    }
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Stage currentStage = getCurrentStage();
        consumedMouseButtons.remove(button);
        if (currentStage != null) {
            return currentStage.touchUp(screenX, screenY, pointer, button);
        }
        return false;
    }
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Stage currentStage = getCurrentStage();
        if (currentStage != null) {
            return currentStage.touchDragged(screenX, screenY, pointer);
        }
        return false;
    }
    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        consumedMouseButtons.remove(button);
        Stage currentStage = getCurrentStage();
        if (currentStage != null) {
            return currentStage.touchCancelled(screenX, screenY, pointer, button);
        }
        return false;
    }
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        Stage currentStage = getCurrentStage();
        if (currentStage != null) {
            mousePos.set(screenX, screenY);
            currentStage.getViewport().unproject(mousePos);
            return currentStage.mouseMoved(screenX, screenY);
        }
        return false;
    }
    @Override
    public boolean scrolled(float amountX, float amountY) {
        GameState currentState = GameState.currentGameState;
        if (currentState instanceof GUI) {
            Object currentPage = ((GUI) currentState).getCurrentPage();
            if (currentPage instanceof InputProcessor inputProcessor && inputProcessor.scrolled(amountX, amountY)) {
                return true;
            }
        }

        Stage currentStage = getCurrentStage();
        if (currentStage != null) {
            return currentStage.scrolled(amountX, amountY);
        }
        return false;
    }

    private Stage getCurrentStage() {
        GameState currentState = GameState.currentGameState;
        if (currentState instanceof GUI) {
            try {
                return ((GUI) currentState).getStage();
            } catch (ClassCastException | NullPointerException e) {
                Client.LOGGER.error("Could not get Stage from current GameState: {}", currentState, e);
                return null;
            }
        }
        return null;
    }

    private void processInGameKeybinds() {
        if (isFirstFrameKeyDown(Input.Keys.BACKSLASH)) {
            Gdx.input.setCursorCatched(false);
            GameState.switchToGameState(new GUI(GameState.currentGameState));
            return;
        }

        for (Module module : Modules.moduleList) {
            String keybind = module.getKeybindCombo();
            if (module == Modules.zoom) {
                updateHeldZoomKeybind(keybind);
                continue;
            }
            if (KeybindUtil.isActive(keybind)) {
                Client.LOGGER.debug("Module keybind pressed: {} for {}", KeybindUtil.format(keybind), module.getId());
                module.toggle(true);
            }
        }
    }

    private void updateHeldZoomKeybind(String keybind) {
        if (Modules.zoom == null) {
            return;
        }

        boolean shouldZoom = KeybindUtil.isPressed(keybind);
        if (shouldZoom == Modules.zoom.isEnabled()) {
            return;
        }

        Client.LOGGER.debug("Zoom keybind {}: {}", shouldZoom ? "held" : "released", KeybindUtil.format(keybind));
        boolean previousAutoSave = SettingsManager.isAutoSaveEnabled();
        SettingsManager.setAutoSaveEnabled(false);
        try {
            if (shouldZoom) {
                Modules.zoom.enable(Modules.zoom.shouldNotify(true));
            } else {
                Modules.zoom.disable(Modules.zoom.shouldNotify(true));
            }
        } finally {
            SettingsManager.setAutoSaveEnabled(previousAutoSave);
        }
        if (!shouldZoom && previousAutoSave && Modules.zoom.shouldPersistZoomAmount()) {
            SettingsManager.saveSettings();
        }
    }

    public static boolean isKeyDown(int keycode) { return Gdx.input.isKeyPressed(keycode); }
    public static boolean isKeyUp(int keycode) { return !Gdx.input.isKeyPressed(keycode); }
    public static boolean isFirstFrameKeyDown(int keycode) { return Gdx.input.isKeyJustPressed(keycode); }
    public static boolean isMouseButtonDown(int keycode) { return Gdx.input.isButtonPressed(keycode); }
    public static boolean isMouseButtonUp(int keycode) { return !Gdx.input.isButtonPressed(keycode); }
    public static boolean isFirstFrameMouseButtonDown(int keycode) {
        if (!Gdx.input.isButtonPressed(keycode)) {
            consumedMouseButtons.remove(keycode);
        }
        return Gdx.input.isButtonJustPressed(keycode) && !consumedMouseButtons.contains(keycode);
    }

    public Vector2 getMousePos() {
        return this.mousePos;
    }
}
