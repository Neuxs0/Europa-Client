package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.FullbrightLighting;
import finalforeach.cosmicreach.rendering.items.IItemRenderParams;
import finalforeach.cosmicreach.rendering.items.ItemThingModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemThingModel.class)
public abstract class ItemThingModelMixin {
    @Shadow
    @Final
    private static Color tintColor;

    @Inject(
            method = "renderGeneric",
            at = @At(
                    value = "INVOKE",
                    target = "Lfinalforeach/cosmicreach/rendering/shaders/GameShader;bind(Lcom/badlogic/gdx/graphics/Camera;)V"
            )
    )
    private void europa_client$applyFullbrightItemLighting(
            Vector3 worldPosition,
            Camera worldCamera,
            Matrix4 modelMatrix,
            boolean isInSlot,
            IItemRenderParams renderParams,
            CallbackInfo ci
    ) {
        if (!isInSlot && Modules.fullbright != null && Modules.fullbright.isEnabled()) {
            tintColor.set(
                    FullbrightLighting.MAX_DAYLIGHT_BRIGHTNESS,
                    FullbrightLighting.MAX_DAYLIGHT_BRIGHTNESS,
                    FullbrightLighting.MAX_DAYLIGHT_BRIGHTNESS,
                    1.0F
            );
        }
    }
}
