package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import dev.neuxs.europa_client.utils.XrayRendering;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.rendering.items.ItemModelBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelBlock.class)
public abstract class XrayItemModelBlockMixin {
    @Inject(method = "<init>", at = @At("HEAD"))
    private static void europa_client$suppressXrayItemMeshBuild(BlockState blockState, CallbackInfo ci) {
        XrayRendering.suppress();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void europa_client$unsuppressXrayItemMeshBuild(BlockState blockState, CallbackInfo ci) {
        XrayRendering.unsuppress();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void europa_client$suppressXrayItemRender(
            Vector3 worldPosition,
            Camera worldCamera,
            Matrix4 modelMatrix,
            boolean useWorldLighting,
            boolean allowFog,
            ItemStack itemStack,
            Color slotColor,
            CallbackInfo ci
    ) {
        XrayRendering.suppress();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void europa_client$unsuppressXrayItemRender(
            Vector3 worldPosition,
            Camera worldCamera,
            Matrix4 modelMatrix,
            boolean useWorldLighting,
            boolean allowFog,
            ItemStack itemStack,
            Color slotColor,
            CallbackInfo ci
    ) {
        XrayRendering.unsuppress();
    }
}
