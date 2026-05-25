package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.CheatModules;
import finalforeach.cosmicreach.entities.GameEntity;
import finalforeach.cosmicreach.entities.components.IFallDamageComponent;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"unused", "ConstantConditions"})
@Mixin(GameEntity.class)
public abstract class CheatEntityMovementMixin {
    private float europa_client$positionDeltaTime;

    @Inject(method = "updatePositions", at = @At("HEAD"))
    private void europa_client$capturePositionDeltaTime(Zone zone, float deltaTime, CallbackInfo ci) {
        this.europa_client$positionDeltaTime = deltaTime;
        GameEntity self = (GameEntity) (Object) this;
        if (CheatModules.fly != null && CheatModules.fly.isActiveFor(self)) {
            CheatModules.fly.prepare(InGame.getLocalPlayer());
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
        if (CheatModules.fly != null && CheatModules.fly.isActiveFor(self)) {
            return CheatModules.fly.getPositionDeltaX(self, europa_client$positionDeltaTime);
        }

        float modifier = 1.0F;
        if (self.isNoClip() && CheatModules.noClip != null) {
            modifier *= CheatModules.noClip.getSpeed();
        }
        if (CheatModules.speed != null && CheatModules.speed.isEnabled()) {
            modifier *= CheatModules.speed.getSpeed();
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
        if (CheatModules.fly != null && CheatModules.fly.isActiveFor(self)) {
            return CheatModules.fly.getPositionDeltaY(self, europa_client$positionDeltaTime);
        }

        float modifier = 1.0F;
        if (self.isNoClip() && CheatModules.noClip != null) {
            modifier *= CheatModules.noClip.getSpeed();
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
        if (CheatModules.fly != null && CheatModules.fly.isActiveFor(self)) {
            return CheatModules.fly.getPositionDeltaZ(self, europa_client$positionDeltaTime);
        }

        float modifier = 1.0F;
        if (self.isNoClip() && CheatModules.noClip != null) {
            modifier *= CheatModules.noClip.getSpeed();
        }
        if (CheatModules.speed != null && CheatModules.speed.isEnabled()) {
            modifier *= CheatModules.speed.getSpeed();
        }
        return z * modifier;
    }

    @Redirect(
            method = "updatePositions(Lfinalforeach/cosmicreach/world/Zone;F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lfinalforeach/cosmicreach/entities/components/IFallDamageComponent;onLand(Lfinalforeach/cosmicreach/entities/GameEntity;F)V"
            )
    )
    private void europa_client$cancelFallDamage(IFallDamageComponent fallDamage, GameEntity entity, float landingVelocity) {
        Player localPlayer = InGame.getLocalPlayer();
        if (CheatModules.noFall != null
                && CheatModules.noFall.isEnabled()
                && localPlayer != null
                && localPlayer.getEntity() == entity) {
            return;
        }

        fallDamage.onLand(entity, landingVelocity);
    }
}
