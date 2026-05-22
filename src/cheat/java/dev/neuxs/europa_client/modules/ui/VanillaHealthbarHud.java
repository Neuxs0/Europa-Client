package dev.neuxs.europa_client.modules.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.Viewport;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.ui.Healthbar;
import finalforeach.cosmicreach.ui.InGameUI;

import java.util.Map;

public class VanillaHealthbarHud extends HudModule {
    private static final float FALLBACK_WIDTH = 296f;
    private static final float FALLBACK_HEIGHT = 8f;
    private static final float DEFAULT_HOTBAR_HEIGHT = 40f;
    private static final float DEFAULT_HOTBAR_GAP = 1f;

    public VanillaHealthbarHud(int keybind, boolean defaultEnabled) {
        super("Vanilla Health Bar", keybind, defaultEnabled);
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
        applyToCurrentHealthbar(viewport);
    }

    @Override
    public Vector2 getHudSize(Viewport viewport) {
        Actor healthbarActor = getHealthbarActor();
        if (healthbarActor == null) {
            return new Vector2(FALLBACK_WIDTH, FALLBACK_HEIGHT);
        }
        return new Vector2(
                Math.max(FALLBACK_WIDTH, healthbarActor.getWidth()),
                Math.max(FALLBACK_HEIGHT, healthbarActor.getHeight())
        );
    }

    @Override
    public String getHudDisplayName() {
        return "Vanilla Health Bar";
    }

    @Override
    public boolean canBeHiddenInHudEditor() {
        return false;
    }

    @Override
    protected Vector2 getDefaultHudPosition(Viewport viewport, Vector2 size) {
        float viewportWidth = viewport == null ? 0f : viewport.getWorldWidth();
        float width = size == null ? FALLBACK_WIDTH : size.x;
        return new Vector2(
                Math.max(0f, (viewportWidth - width) / 2f),
                DEFAULT_HOTBAR_HEIGHT + DEFAULT_HOTBAR_GAP
        );
    }

    @Override
    public void importSettings(Map<String, Object> data) {
        super.importSettings(data);
        setEnabled(true);
    }

    public void applyToCurrentHealthbar(Viewport viewport) {
        Actor healthbarActor = getHealthbarActor();
        if (healthbarActor == null) {
            return;
        }

        Vector2 position = getHudPosition(viewport, getHudSize(viewport));
        healthbarActor.setPosition(position.x, position.y);
        healthbarActor.setHeight(FALLBACK_HEIGHT);
    }

    private Actor getHealthbarActor() {
        InGameUI inGameUI = InGame.IN_GAME == null ? null : InGame.IN_GAME.inGameUI;
        Healthbar healthbar = inGameUI == null ? null : inGameUI.healthBar;
        return healthbar == null ? null : healthbar.getActor();
    }
}
