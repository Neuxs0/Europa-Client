package dev.neuxs.europa_client.commands;

import dev.neuxs.europa_client.SmokeTestSupport;
import dev.neuxs.europa_client.modules.CheatModules;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.variant.BaseVariant;
import dev.neuxs.europa_client.variant.CheatVariant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandBehaviorSmokeTest {
    @Test
    void utilityCommandsToggleExpectedModules() {
        SmokeTestSupport.resetForVariant(new CheatVariant());

        ClientCommandManager.triggerCommand(null, ".fullbright");
        assertTrue(Modules.fullbright.isEnabled());

        ClientCommandManager.triggerCommand(null, ".nofog");
        assertTrue(Modules.noFog.isEnabled());
    }

    @Test
    void cheatCommandsModifyExpectedSettings() {
        SmokeTestSupport.resetForVariant(new CheatVariant());

        ClientCommandManager.triggerCommand(null, ".freecam set speed 10");
        assertEquals(10.0f, Modules.freecam.getSpeed());

        ClientCommandManager.triggerCommand(null, ".fly set speed 12");
        assertEquals(12.0f, CheatModules.fly.getSpeed());

        ClientCommandManager.triggerCommand(null, ".reach set distance 20");
        assertEquals(20.0f, CheatModules.reach.getReachDistance());
    }

    @Test
    void invalidCommandArgumentsEmitErrorAndLeaveSettingsUnchanged() {
        SmokeTestSupport.FakeChat chat = SmokeTestSupport.resetForVariant(new CheatVariant());
        float previousSpeed = CheatModules.fly.getSpeed();

        ClientCommandManager.triggerCommand(null, ".fly set speed nope");

        assertEquals(previousSpeed, CheatModules.fly.getSpeed());
        assertTrue(chat.lastMessage().contains("Invalid number format"));
    }

    @Test
    void cheatCommandsAreUnavailableInBaseVariantAndAvailableInCheatVariant() {
        SmokeTestSupport.FakeChat baseChat = SmokeTestSupport.resetForVariant(new BaseVariant());
        ClientCommandManager.triggerCommand(null, ".fly");
        assertTrue(baseChat.lastMessage().contains("Unknown command: fly"));

        SmokeTestSupport.resetForVariant(new CheatVariant());
        assertFalse(CheatModules.fly.isEnabled());
        ClientCommandManager.triggerCommand(null, ".fly");
        assertTrue(CheatModules.fly.isEnabled());
    }
}
