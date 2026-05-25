package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.CheatModules;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.FullbrightLighting;
import dev.neuxs.europa_client.utils.XrayRendering;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJson;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModelJson.class)
public abstract class XrayBlockModelJsonMixin {
    @Shadow
    String fragShader;

    @Inject(method = "initShader", at = @At("HEAD"))
    private void europa_client$useXrayCapableChunkShader(CallbackInfo ci) {
        if (this.fragShader == null || this.fragShader.equals("base:shaders/chunk.frag.glsl")) {
            this.fragShader = "europa_client:shaders/xray/chunk.frag.glsl";
        }
    }

    @ModifyVariable(
            method = "addVertices(Lfinalforeach/cosmicreach/rendering/IMeshData;IIII[S[I)V",
            at = @At("HEAD"),
            argsOnly = true,
            index = 5
    )
    private int europa_client$showBuriedOreFaces(int opaqueBitmask) {
        if (CheatModules.xray != null
                && CheatModules.xray.isEnabled()
                && !XrayRendering.isSuppressed()
                && XrayRendering.isCurrentBlockOre()) {
            return 0;
        }

        return opaqueBitmask;
    }

    @ModifyVariable(method = "addVert", at = @At("HEAD"), argsOnly = true, index = 5)
    private int europa_client$applyXraySkylight(int skyLight) {
        if (CheatModules.xray != null
                && CheatModules.xray.isEnabled()
                && !XrayRendering.isSuppressed()
                && !isWaterShader()) {
            return XrayRendering.isCurrentBlockOre() ? FullbrightLighting.MAX_SKYLIGHT : 1;
        }

        return skyLight;
    }

    @ModifyVariable(method = "addVert", at = @At("HEAD"), argsOnly = true, index = 4)
    private short europa_client$markFullbrightXrayOre(short blockLight) {
        if (CheatModules.xray != null
                && CheatModules.xray.isEnabled()
                && !XrayRendering.isSuppressed()
                && Modules.fullbright != null
                && Modules.fullbright.isEnabled()
                && !isWaterShader()
                && XrayRendering.isCurrentBlockOre()) {
            return (short) 15;
        }

        return blockLight;
    }

    private boolean isWaterShader() {
        return this.fragShader != null && this.fragShader.contains("chunk-water");
    }
}
