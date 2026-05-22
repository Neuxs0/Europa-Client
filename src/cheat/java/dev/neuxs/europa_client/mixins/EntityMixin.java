package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.graphics.Camera;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.modules.ui.VelocityTracker;
import dev.neuxs.europa_client.utils.FullbrightLighting;
import finalforeach.cosmicreach.entities.GameEntity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"DuplicatedCode", "unused", "ConstantConditions"})
@Mixin(GameEntity.class)
public abstract class EntityMixin {
    @Inject(method = "renderModelAfterMatrixSet", at = @At("HEAD"))
    private void europa_client$applyFullbrightEntityLighting(Camera worldCamera, boolean shouldRender, CallbackInfo ci) {
        if (Modules.fullbright != null && Modules.fullbright.isEnabled()) {
            ((GameEntity) (Object) this).modelLightColor.set(
                    FullbrightLighting.MAX_DAYLIGHT_BRIGHTNESS,
                    FullbrightLighting.MAX_DAYLIGHT_BRIGHTNESS,
                    FullbrightLighting.MAX_DAYLIGHT_BRIGHTNESS,
                    1.0F
            );
        }
    }

    @Inject(method = "updatePositions", at = @At("RETURN"))
    private void europa_client$recordLocalPlayerVelocity(Zone zone, float deltaTime, CallbackInfo ci) {
        GameEntity self = (GameEntity) (Object) this;
        Player localPlayer = InGame.getLocalPlayer();
        if (localPlayer != null && localPlayer.getEntity() == self) {
            VelocityTracker.recordLocalPlayerMovement(self, deltaTime);
        }
    }

    @ModifyArg(
            method = "updatePositions(Lfinalforeach/cosmicreach/world/Zone;F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/badlogic/gdx/math/Vector3;set(FFF)Lcom/badlogic/gdx/math/Vector3;",
                    ordinal = 0
            ),
            index = 0
    )
    private float modifyPosDiffX(float x) {
        GameEntity self = (GameEntity) (Object) this;
        float modifier = 1.0F;
        if (self.isNoClip()) {
            modifier *= Modules.noClip.getSpeed();
        }
        if (Modules.speed.isEnabled()) {
            modifier *= Modules.speed.getSpeed();
        }
        return x * modifier;
    }

    @ModifyArg(
            method = "updatePositions(Lfinalforeach/cosmicreach/world/Zone;F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/badlogic/gdx/math/Vector3;set(FFF)Lcom/badlogic/gdx/math/Vector3;",
                    ordinal = 0
            ),
            index = 1
    )
    private float modifyPosDiffY(float y) {
        GameEntity self = (GameEntity) (Object) this;
        float modifier = 1.0F;
        if (self.isNoClip()) {
            modifier *= Modules.noClip.getSpeed();
        }
        return y * modifier;
    }

    @ModifyArg(
            method = "updatePositions(Lfinalforeach/cosmicreach/world/Zone;F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/badlogic/gdx/math/Vector3;set(FFF)Lcom/badlogic/gdx/math/Vector3;",
                    ordinal = 0
            ),
            index = 2
    )
    private float modifyPosDiffZ(float z) {
        GameEntity self = (GameEntity) (Object) this;
        float modifier = 1.0F;
        if (self.isNoClip()) {
            modifier *= Modules.noClip.getSpeed();
        }
        if (Modules.speed.isEnabled()) {
            modifier *= Modules.speed.getSpeed();
        }
        return z * modifier;
    }
}
