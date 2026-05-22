package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.graphics.Camera;
import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.entities.GameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameEntity.class)
public abstract class EntityMixin {
    @Inject(method = "renderModelAfterMatrixSet", at = @At("HEAD"))
    private void europa_client$applyFullbrightEntityLighting(Camera worldCamera, boolean shouldRender, CallbackInfo ci) {
        if (Modules.fullbright != null && Modules.fullbright.isEnabled()) {
            ((GameEntity) (Object) this).modelLightColor.set(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
