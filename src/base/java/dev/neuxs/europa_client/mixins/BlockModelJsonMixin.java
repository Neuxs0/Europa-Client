package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.FullbrightLighting;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJson;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockModelJson.class)
public abstract class BlockModelJsonMixin {
    @Shadow
    String fragShader;

    @ModifyVariable(method = "addVert", at = @At("HEAD"), argsOnly = true, index = 5)
    private int europa_client$applyFullbrightSkylight(int skyLight) {
        if (Modules.fullbright != null && Modules.fullbright.isEnabled() && !isWaterShader()) {
            return FullbrightLighting.MAX_SKYLIGHT;
        }

        return skyLight;
    }

    private boolean isWaterShader() {
        return this.fragShader != null && this.fragShader.contains("chunk-water");
    }
}
