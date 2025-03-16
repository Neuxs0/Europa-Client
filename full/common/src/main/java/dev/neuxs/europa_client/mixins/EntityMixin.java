package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.entities.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

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
        Entity self = (Entity) (Object) this;
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
        Entity self = (Entity) (Object) this;
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
