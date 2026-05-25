package dev.neuxs.europa_client.modules.cheats;

import com.badlogic.gdx.Input;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.managers.InputManager;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.settings.Setting;
import dev.neuxs.europa_client.utils.Chat;
import finalforeach.cosmicreach.BlockSelection;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.entities.GameEntity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.settings.Controls;
import finalforeach.cosmicreach.world.Zone;

import java.util.List;

@SuppressWarnings("unchecked")
public class ClickTp extends Module {
    private static final long TELEPORT_COOLDOWN_NANOS = 150_000_000L;
    private static final String LEFT_BUTTON = "Left";
    private static final String MIDDLE_BUTTON = "Middle";
    private static final String RIGHT_BUTTON = "Right";
    private long lastTeleportTimeNanos = 0L;

    public ClickTp(int keybind, boolean defaultEnabled) {
        super("Click-TP", keybind, defaultEnabled);
        customSettings.put("button", new Setting<>("button", MIDDLE_BUTTON)
                .withDisplayName("Button")
                .withOptions(List.of(LEFT_BUTTON, MIDDLE_BUTTON, RIGHT_BUTTON)));
    }

    @Override
    public void enable(boolean messaging) {
        setEnabled(true);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Click-TP enabled");
    }

    @Override
    public void disable(boolean messaging) {
        setEnabled(false);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Click-TP disabled");
    }

    public void update() {
        if (!isEnabled() || !(GameState.currentGameState instanceof InGame inGame)) {
            return;
        }

        if (!InputManager.isFirstFrameMouseButtonDown(getButtonCode())) {
            return;
        }

        teleportToSelectedBlock(inGame);
    }

    public void setButton(String buttonName) {
        Setting<String> buttonSetting = (Setting<String>) customSettings.get("button");
        buttonSetting.setValue(normalizeButtonName(buttonName));
        Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Click-TP button set to " + buttonSetting.getValue());
    }

    public String getButtonName() {
        Setting<String> buttonSetting = (Setting<String>) customSettings.get("button");
        return buttonSetting.getValue();
    }

    private void teleportToSelectedBlock(InGame inGame) {
        long now = System.nanoTime();
        if (now - lastTeleportTimeNanos < TELEPORT_COOLDOWN_NANOS) {
            return;
        }

        BlockSelection blockSelection = inGame.blockSelection;
        if (blockSelection == null) {
            return;
        }

        BlockPosition selectedBlockPos = blockSelection.getBlockPositionLookingAt();
        if (selectedBlockPos == null || !selectedBlockPos.isValid()) {
            return;
        }

        Player player = InGame.getLocalPlayer();
        if (player == null || player.getEntity() == null) {
            return;
        }

        Zone zone = player.getZone();
        if (zone == null) {
            return;
        }

        int targetX = selectedBlockPos.getGlobalX();
        int targetY = selectedBlockPos.getGlobalY();
        int targetZ = selectedBlockPos.getGlobalZ();
        int destinationY = targetY + 1;
        if (zone.getChunkAtBlock(targetX, targetY, targetZ) == null ||
                zone.getChunkAtBlock(targetX, destinationY, targetZ) == null) {
            return;
        }

        GameEntity entity = player.getEntity();
        player.setPosition(
                selectedBlockPos.getCenterX(),
                destinationY,
                selectedBlockPos.getCenterZ()
        );
        entity.velocity.setZero();
        entity.onceVelocity.setZero();
        Controls.ignoreCurrentlyPressedMouseButtons();
        lastTeleportTimeNanos = now;
    }

    private int getButtonCode() {
        return switch (getButtonName()) {
            case LEFT_BUTTON -> Input.Buttons.LEFT;
            case RIGHT_BUTTON -> Input.Buttons.RIGHT;
            default -> Input.Buttons.MIDDLE;
        };
    }

    private String normalizeButtonName(String buttonName) {
        if (buttonName == null) {
            return MIDDLE_BUTTON;
        }

        return switch (buttonName.trim().toLowerCase()) {
            case "left", "l", "0" -> LEFT_BUTTON;
            case "right", "r", "1" -> RIGHT_BUTTON;
            case "middle", "mid", "m", "2" -> MIDDLE_BUTTON;
            default -> throw new IllegalArgumentException("Invalid button. Use left, middle, or right.");
        };
    }
}
