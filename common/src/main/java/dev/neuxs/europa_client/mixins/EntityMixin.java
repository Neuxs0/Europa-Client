package dev.neuxs.europa_client.mixins;

import finalforeach.cosmicreach.entities.Entity;
import dev.neuxs.europa_client.modules.cheats.NoClip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Mixin injection to modify the movement delta for no-clip movement.
 *
 * This targets the call to posDiff.set(float, float, float) in
 * Entity.updatePositions(Zone, float) so that when no-clip is enabled the
 * delta is scaled by NoClip.getSpeed().
 */
@Mixin(Entity.class)
public abstract class EntityMixin {

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
        Entity self = (Entity) (Object) this;
        return self.noClip ? x * NoClip.getSpeed() : x;
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
        Entity self = (Entity) (Object) this;
        return self.noClip ? y * NoClip.getSpeed() : y;
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
        Entity self = (Entity) (Object) this;
        return self.noClip ? z * NoClip.getSpeed() : z;
    }
}
