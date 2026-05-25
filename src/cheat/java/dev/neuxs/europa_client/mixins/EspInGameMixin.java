package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.CheatModules;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(InGame.class)
public abstract class EspInGameMixin {
    @Inject(method = "renderWorld", at = @At("TAIL"))
    private void europa_client$renderEsp(Zone zone, CallbackInfo ci) {
        if (CheatModules.esp == null || !CheatModules.esp.isEnabled()) {
            return;
        }

        InGame inGame = (InGame) (Object) this;
        CheatModules.esp.render(zone, inGame.getWorldCamera(), inGame.viewport, inGame.newUiViewport);
    }
}
