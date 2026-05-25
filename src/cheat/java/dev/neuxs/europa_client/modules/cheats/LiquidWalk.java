package dev.neuxs.europa_client.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.settings.Setting;
import dev.neuxs.europa_client.utils.Chat;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.GameEntity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;

@SuppressWarnings("unchecked")
public class LiquidWalk extends Module {
    private static final String WATER_BLOCK_ID = "base:water";
    private static final String LAVA_BLOCK_ID = "base:lava";

    public LiquidWalk(int keybind, boolean defaultEnabled) {
        super("LiquidWalk", keybind, defaultEnabled);
        customSettings.put("water", new Setting<>("water", true)
                .withDisplayName("Water")
                .withDescription("Walk on water blocks"));
        customSettings.put("lava", new Setting<>("lava", true)
                .withDisplayName("Lava")
                .withDescription("Walk on lava blocks"));
    }

    @Override
    public void enable(boolean messaging) {
        setEnabled(true);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "LiquidWalk enabled");
    }

    @Override
    public void disable(boolean messaging) {
        setEnabled(false);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "LiquidWalk disabled");
    }

    public boolean isActiveFor(GameEntity entity) {
        Player localPlayer = InGame.getLocalPlayer();
        return isEnabled() && localPlayer != null && localPlayer.getEntity() == entity;
    }

    public boolean shouldWalkOn(BlockState blockState) {
        if (!isEnabled() || blockState == null || !blockState.isFluid) {
            return false;
        }

        return switch (blockState.getBlockId()) {
            case WATER_BLOCK_ID -> walksOnWater();
            case LAVA_BLOCK_ID -> walksOnLava();
            default -> false;
        };
    }

    public boolean walksOnWater() {
        Setting<Boolean> waterSetting = (Setting<Boolean>) customSettings.get("water");
        return waterSetting.getValue();
    }

    public boolean walksOnLava() {
        Setting<Boolean> lavaSetting = (Setting<Boolean>) customSettings.get("lava");
        return lavaSetting.getValue();
    }

    public void setWalksOnWater(boolean value) {
        Setting<Boolean> waterSetting = (Setting<Boolean>) customSettings.get("water");
        waterSetting.setValue(value);
        Client.clientChat.addMessage(null, "LiquidWalk water set to " + waterSetting.getValue());
    }

    public void setWalksOnLava(boolean value) {
        Setting<Boolean> lavaSetting = (Setting<Boolean>) customSettings.get("lava");
        lavaSetting.setValue(value);
        Client.clientChat.addMessage(null, "LiquidWalk lava set to " + lavaSetting.getValue());
    }
}
