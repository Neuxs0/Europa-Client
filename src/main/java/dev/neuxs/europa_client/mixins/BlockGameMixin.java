package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.BlockGame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(BlockGame.class)
public abstract class BlockGameMixin {
    @Inject(method = "dispose", at = @At("HEAD"))
    private void europa_client$disableFreecamOnGameDispose(CallbackInfo ci) {
        if (Modules.freecam != null && Modules.freecam.isEnabled()) {
            Modules.freecam.toggle(false);
        }
    }
}
