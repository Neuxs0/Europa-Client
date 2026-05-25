package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import dev.neuxs.europa_client.modules.CheatModules;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.GameEntity;
import finalforeach.cosmicreach.entities.components.IFallDamageComponent;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.settings.Controls;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"unused", "ConstantConditions"})
@Mixin(GameEntity.class)
public abstract class CheatEntityMovementMixin {
    private static final float LIQUID_SURFACE_EPSILON = 0.02F;

    @Shadow public boolean isOnGround;
    @Shadow public Vector3 position;
    @Shadow public Vector3 velocity;
    @Shadow public Vector3 onceVelocity;
    @Shadow public BoundingBox localBoundingBox;
    @Shadow protected float floorFriction;

    private float europa_client$positionDeltaTime;
    private boolean europa_client$liquidWalkSurfaceCollision;

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

    @Inject(method = "updateConstraints", at = @At("HEAD"))
    private void europa_client$standOnLiquidSurface(Zone zone, Vector3 targetPosition, CallbackInfo ci) {
        europa_client$liquidWalkSurfaceCollision = false;
        GameEntity self = (GameEntity) (Object) this;
        if (CheatModules.liquidWalk == null
                || !CheatModules.liquidWalk.isActiveFor(self)
                || Controls.crouchPressed()
                || self.isNoClip()) {
            return;
        }

        float currentBottom = position.y + localBoundingBox.min.y;
        float targetBottom = targetPosition.y + localBoundingBox.min.y;
        if (targetBottom > currentBottom) {
            return;
        }

        float minX = targetPosition.x + localBoundingBox.min.x;
        float maxX = targetPosition.x + localBoundingBox.max.x;
        float minZ = targetPosition.z + localBoundingBox.min.z;
        float maxZ = targetPosition.z + localBoundingBox.max.z;

        int minBx = (int) Math.floor(minX);
        int maxBx = (int) Math.floor(maxX);
        int minBz = (int) Math.floor(minZ);
        int maxBz = (int) Math.floor(maxZ);
        int minBy = (int) Math.floor(targetBottom) - 1;
        int maxBy = (int) Math.floor(currentBottom);

        float bestSurfaceY = Float.NEGATIVE_INFINITY;
        for (int bx = minBx; bx <= maxBx; bx++) {
            for (int by = minBy; by <= maxBy; by++) {
                for (int bz = minBz; bz <= maxBz; bz++) {
                    BlockState blockState = zone.getBlockState(bx, by, bz);
                    if (!CheatModules.liquidWalk.shouldWalkOn(blockState)) {
                        continue;
                    }

                    BlockState blockAbove = zone.getBlockState(bx, by + 1, bz);
                    if (blockAbove != null && blockAbove.isFluid) {
                        continue;
                    }

                    float surfaceY = by + 1.0F;
                    if (currentBottom < surfaceY - LIQUID_SURFACE_EPSILON
                            || targetBottom > surfaceY + LIQUID_SURFACE_EPSILON
                            || surfaceY <= bestSurfaceY) {
                        continue;
                    }

                    bestSurfaceY = surfaceY;
                }
            }
        }

        if (bestSurfaceY == Float.NEGATIVE_INFINITY) {
            return;
        }

        targetPosition.y = bestSurfaceY - localBoundingBox.min.y;
        velocity.y = 0.0F;
        onceVelocity.y = 0.0F;
        europa_client$liquidWalkSurfaceCollision = true;
    }

    @Inject(method = "updateConstraints", at = @At("RETURN"))
    private void europa_client$keepLiquidSurfaceGrounded(Zone zone, Vector3 targetPosition, CallbackInfo ci) {
        if (!europa_client$liquidWalkSurfaceCollision) {
            return;
        }

        isOnGround = true;
        floorFriction = 1.0F;
    }
}
