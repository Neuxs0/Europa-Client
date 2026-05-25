package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.ui.UI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(UI.class)
public abstract class ZoomUIMixin {
    @Inject(method = "scrolled", at = @At("HEAD"), cancellable = true)
    private void handleEuropaZoomScroll(float amountX, float amountY, CallbackInfoReturnable<Boolean> cir) {
        if (Modules.zoom != null && Modules.zoom.handleScroll(amountY)) {
            cir.setReturnValue(true);
        }
    }
}
