package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.XrayRendering;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.rendering.IMeshData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockState.class)
public abstract class BlockStateMixin {
    @Inject(method = "addVertices(Lfinalforeach/cosmicreach/rendering/IMeshData;IIII[S[I)V", at = @At("HEAD"))
    private void europa_client$beginXrayBlock(
            IMeshData meshData,
            int x,
            int y,
            int z,
            int opaqueBitmask,
            short[] lights,
            int[] skyLights,
            CallbackInfo ci
    ) {
        if (Modules.xray != null && Modules.xray.isEnabled()) {
            XrayRendering.beginBlock((BlockState) (Object) this);
        }
    }

    @Inject(method = "addVertices(Lfinalforeach/cosmicreach/rendering/IMeshData;IIII[S[I)V", at = @At("TAIL"))
    private void europa_client$endXrayBlock(
            IMeshData meshData,
            int x,
            int y,
            int z,
            int opaqueBitmask,
            short[] lights,
            int[] skyLights,
            CallbackInfo ci
    ) {
        if (Modules.xray != null && Modules.xray.isEnabled()) {
            XrayRendering.endBlock();
        }
    }
}
