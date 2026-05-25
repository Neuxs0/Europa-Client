package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.CheatModules;
import finalforeach.cosmicreach.entities.player.Gamemode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(Gamemode.class)
public abstract class GamemodeMixin {
    @Inject(method = "canInstantBreak", at = @At("HEAD"), cancellable = true)
    private void europa_client$instaBreak(CallbackInfoReturnable<Boolean> cir) {
        if (CheatModules.instaBreak != null && CheatModules.instaBreak.isEnabled()) {
            cir.setReturnValue(true);
        }
    }
}
