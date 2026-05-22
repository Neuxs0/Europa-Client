package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.rendering.items.ItemModelBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelBlock.class)
public abstract class ItemModelBlockMixin {
    private static final float FULLBRIGHT_ITEM_BRIGHTNESS = 0.80F;

    @Shadow
    @Final
    private static Color color;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/badlogic/gdx/graphics/Color;mul(Lcom/badlogic/gdx/graphics/Color;)Lcom/badlogic/gdx/graphics/Color;"
            )
    )
    private void europa_client$applyFullbrightItemLighting(
            Vector3 worldPosition,
            Camera worldCamera,
            Matrix4 modelMatrix,
            boolean useWorldLighting,
            boolean allowFog,
            ItemStack itemStack,
            Color slotColor,
            CallbackInfo ci
    ) {
        if (useWorldLighting && Modules.fullbright != null && Modules.fullbright.isEnabled()) {
            color.set(
                    FULLBRIGHT_ITEM_BRIGHTNESS,
                    FULLBRIGHT_ITEM_BRIGHTNESS,
                    FULLBRIGHT_ITEM_BRIGHTNESS,
                    1.0F
            );
        }
    }
}
