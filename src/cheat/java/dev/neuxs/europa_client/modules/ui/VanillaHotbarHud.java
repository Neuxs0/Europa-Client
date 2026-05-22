package dev.neuxs.europa_client.modules.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.Viewport;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.ui.InGameUI;
import finalforeach.cosmicreach.ui.screens.ItemStorageScreen;

import java.util.Map;

public class VanillaHotbarHud extends HudModule {
    private static final float FALLBACK_WIDTH = 296f;
    private static final float FALLBACK_HEIGHT = 40f;

    public VanillaHotbarHud(int keybind, boolean defaultEnabled) {
        super("Vanilla Hotbar", keybind, defaultEnabled);
    }

    @Override
    public void enable(boolean messaging) {
        setEnabled(true);
    }

    @Override
    public void disable(boolean messaging) {
        setEnabled(true);
    }

    @Override
    public void renderHud(Viewport viewport) {
        applyToCurrentHotbar(viewport);
    }

    @Override
    public Vector2 getHudSize(Viewport viewport) {
        Actor hotbarActor = getHotbarActor();
        if (hotbarActor == null) {
            return new Vector2(FALLBACK_WIDTH, FALLBACK_HEIGHT);
        }
        return new Vector2(
                Math.max(FALLBACK_WIDTH, hotbarActor.getWidth()),
                Math.max(FALLBACK_HEIGHT, hotbarActor.getHeight())
        );
    }

    @Override
    public String getHudDisplayName() {
        return "Vanilla Hotbar";
    }

    @Override
    public boolean canBeHiddenInHudEditor() {
        return false;
    }

    @Override
    protected Vector2 getDefaultHudPosition(Viewport viewport, Vector2 size) {
        float viewportWidth = viewport == null ? 0f : viewport.getWorldWidth();
        float width = size == null ? FALLBACK_WIDTH : size.x;
        return new Vector2(Math.max(0f, (viewportWidth - width) / 2f), 0f);
    }

    @Override
    public void importSettings(Map<String, Object> data) {
        super.importSettings(data);
        setEnabled(true);
    }

    public void applyToCurrentHotbar(Viewport viewport) {
        InGameUI inGameUI = InGame.IN_GAME == null ? null : InGame.IN_GAME.inGameUI;
        if (inGameUI == null || inGameUI.hotbarScreen == null) {
            return;
        }

        Actor hotbarActor = inGameUI.hotbarScreen.getActor();
        if (hotbarActor == null) {
            return;
        }

        Vector2 position = getHudPosition(viewport, getHudSize(viewport));
        hotbarActor.setPosition(position.x, position.y);
    }

    private Actor getHotbarActor() {
        InGameUI inGameUI = InGame.IN_GAME == null ? null : InGame.IN_GAME.inGameUI;
        ItemStorageScreen hotbarScreen = inGameUI == null ? null : inGameUI.hotbarScreen;
        return hotbarScreen == null ? null : hotbarScreen.getActor();
    }
}
