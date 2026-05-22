package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.gamestates.InGame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(targets = "finalforeach.cosmicreach.ui.InGameUI$1")
public abstract class InGameUIHotbarMixin {
    @Inject(method = "act", at = @At("TAIL"))
    private void applyEuropaHotbarPosition(float delta, CallbackInfoReturnable<Boolean> cir) {
        if (Modules.vanillaHotbar != null && InGame.IN_GAME != null) {
            Modules.vanillaHotbar.applyToCurrentHotbar(InGame.IN_GAME.newUiViewport);
        }
        if (Modules.vanillaHealthbar != null && InGame.IN_GAME != null) {
            Modules.vanillaHealthbar.applyToCurrentHealthbar(InGame.IN_GAME.newUiViewport);
        }
    }
}
