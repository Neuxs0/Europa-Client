package dev.neuxs.europa_client.modules;

import com.badlogic.gdx.math.Vector3;
import dev.neuxs.europa_client.SmokeTestSupport;
import dev.neuxs.europa_client.modules.cheats.Fly;
import dev.neuxs.europa_client.modules.cheats.JetpackHeight;
import dev.neuxs.europa_client.modules.cheats.LiquidWalk;
import dev.neuxs.europa_client.modules.cheats.NoClip;
import dev.neuxs.europa_client.modules.cheats.Reach;
import dev.neuxs.europa_client.modules.cheats.Speed;
import dev.neuxs.europa_client.modules.cheats.Xray;
import dev.neuxs.europa_client.modules.utils.Freecam;
import dev.neuxs.europa_client.modules.utils.Fullbright;
import dev.neuxs.europa_client.modules.utils.NoFog;
import dev.neuxs.europa_client.modules.utils.Zoom;
import dev.neuxs.europa_client.utils.ShaderDecisions;
import dev.neuxs.europa_client.variant.CheatVariant;
import finalforeach.cosmicreach.entities.GameEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleBehaviorSmokeTest {
    @BeforeEach
    void resetClientState() {
        SmokeTestSupport.resetForVariant(new CheatVariant());
    }

    @Test
    void moduleTogglesUpdateEnabledState() {
        Fullbright fullbright = Modules.fullbright;
        assertFalse(fullbright.isEnabled());
        fullbright.toggle(false);
        assertTrue(fullbright.isEnabled());
        fullbright.toggle(false);
        assertFalse(fullbright.isEnabled());
    }

    @Test
    void zoomMixinFacingDecisionsAreStateful() {
        Zoom zoom = Modules.zoom;
        assertEquals(90.0f, zoom.getZoomedFov(90.0f));
        assertTrue(zoom.shouldShowHand());
        assertTrue(zoom.shouldShowHotbar());
        assertFalse(zoom.shouldSmoothCamera());

        zoom.enable(false);
        assertTrue(zoom.getZoomedFov(90.0f, 90.0f) < 90.0f);
        assertEquals(1.0f, zoom.getZoomedFov(-1000.0f, 90.0f));
        assertFalse(zoom.shouldShowHand());
        assertTrue(zoom.shouldShowHotbar());
        assertTrue(zoom.shouldSmoothCamera());

        setting(zoom, "showHand", Boolean.class).setValue(true);
        assertTrue(zoom.shouldShowHand());
        setting(zoom, "showHotbar", Boolean.class).setValue(false);
        assertFalse(zoom.shouldShowHotbar());

        Map<String, Object> imported = new HashMap<>();
        imported.put("savedZoomAmount", 6.0f);
        zoom.importSettings(imported);
        assertEquals(6.0f, ((Number) zoom.exportSettings().get("savedZoomAmount")).floatValue());
    }

    @Test
    void jetpackHeightAllowsVanillaUnlessEnabled() {
        JetpackHeight jetpackHeight = CheatModules.jetpackHeight;
        assertEquals(42.0f, jetpackHeight.getHeightAllowance(42.0f));
        jetpackHeight.enable(false);
        assertEquals(1_000_000.0f, jetpackHeight.getHeightAllowance(42.0f));
    }

    @Test
    void speedAndReachExposeExpectedDefaultsAndSetters() {
        Speed speed = CheatModules.speed;
        assertEquals(1.5f, speed.getSpeed());
        assertEquals(1.5f, speed.getJetpackSpeed());
        speed.setSpeed(3.0f);
        speed.setJetpackSpeed(2.0f);
        assertEquals(3.0f, speed.getSpeed());
        assertEquals(2.0f, speed.getJetpackSpeed());

        Reach reach = CheatModules.reach;
        assertEquals(6.0f, reach.getReachDistance());
        reach.setReachDistance(12.0f);
        assertEquals(12.0f, reach.getReachDistance());
    }

    @Test
    void liquidWalkDecisionsRespectEnabledFluidAndPerFluidToggles() {
        LiquidWalk liquidWalk = CheatModules.liquidWalk;
        assertFalse(liquidWalk.shouldWalkOn("base:water", true));

        liquidWalk.enable(false);
        assertTrue(liquidWalk.shouldWalkOn("base:water", true));
        assertTrue(liquidWalk.shouldWalkOn("base:lava", true));
        assertFalse(liquidWalk.shouldWalkOn("base:oil", true));
        assertFalse(liquidWalk.shouldWalkOn("base:water", false));

        liquidWalk.setWalksOnWater(false);
        assertFalse(liquidWalk.shouldWalkOn("base:water", true));
        assertTrue(liquidWalk.shouldWalkOn("base:lava", true));
        liquidWalk.setWalksOnLava(false);
        assertFalse(liquidWalk.shouldWalkOn("base:lava", true));
    }

    @Test
    void flyMovementHelpersUseInjectedInputAndDeltaTime() {
        Fly fly = CheatModules.fly;
        GameEntity entity = new GameEntity("test:player");
        entity.viewDirection.set(0.0f, 0.0f, -1.0f);

        Vector3 originalVelocity = new Vector3(entity.velocity);
        fly.apply(entity);
        assertEquals(originalVelocity, entity.velocity);

        fly.enable(false);
        fly.setSpeed(10.0f);
        fly.setMovementInput(new FakeMovementInput(1, 0, 0, 0, true, false, false));
        fly.prepare(entity);
        assertEquals(0.0f, entity.gravityModifier);
        assertEquals(Vector3.Zero, entity.onceVelocity);

        assertEquals(0.0f, fly.getPositionDeltaX(entity, 0.5f), 0.0001f);
        assertEquals(5.0f, fly.getPositionDeltaY(entity, 0.5f), 0.0001f);
        assertEquals(-5.0f, fly.getPositionDeltaZ(entity, 0.5f), 0.0001f);
    }

    @Test
    void freecamDamageRuleOnlyDisablesWhenConfigured() {
        Freecam freecam = Modules.freecam;
        freecam.enable(false);
        freecam.setDisableOnDamage(false);
        freecam.onLocalPlayerDamaged(5.0f);
        assertTrue(freecam.isEnabled());

        freecam.setDisableOnDamage(true);
        freecam.onLocalPlayerDamaged(0.0f);
        assertTrue(freecam.isEnabled());
        freecam.onLocalPlayerDamaged(1.0f);
        assertFalse(freecam.isEnabled());
    }

    @Test
    void noClipEnableWithNoLocalPlayerDoesNotCrashAndDirectPlayerPathWorks() {
        NoClip noClip = CheatModules.noClip;
        assertDoesNotThrow(() -> noClip.enable(false));

        GameEntity entity = new GameEntity("test:player");
        entity.velocity.set(1.0f, 2.0f, 3.0f);

        noClip.setNoClip(entity, true);
        assertTrue(entity.isNoClip());
        assertEquals(Vector3.Zero, entity.velocity);
        noClip.setNoClip(entity, false);
        assertFalse(entity.isNoClip());
    }

    @Test
    void renderStateModulesExposeMixinDecisions() {
        Fullbright fullbright = Modules.fullbright;
        NoFog noFog = Modules.noFog;
        Xray xray = CheatModules.xray;

        assertFalse(fullbright.isEnabled());
        assertFalse(noFog.isEnabled());
        assertFalse(xray.isEnabled());
        fullbright.enable(false);
        noFog.enable(false);
        xray.enable(false);
        assertTrue(fullbright.isEnabled());
        assertTrue(noFog.isEnabled());
        assertTrue(xray.isEnabled());

        assertEquals(1.0f, ShaderDecisions.getFullbrightUniformValue(true));
        assertEquals(0.0f, ShaderDecisions.getFullbrightUniformValue(false));
        assertEquals(0.0f, ShaderDecisions.getFogDensityOverride(true, 0.7f));
        assertEquals(0.7f, ShaderDecisions.getFogDensityOverride(false, 0.7f));
    }

    @SuppressWarnings("unchecked")
    private static <T> dev.neuxs.europa_client.settings.Setting<T> setting(Module module, String name, Class<T> type) {
        return (dev.neuxs.europa_client.settings.Setting<T>) module.getCustomSettings().get(name);
    }

    private record FakeMovementInput(
            float forward,
            float backward,
            float left,
            float right,
            boolean jump,
            boolean crouch,
            boolean sprint
    ) implements MovementInput {
    }
}
