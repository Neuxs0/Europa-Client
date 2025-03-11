package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.Client;
import finalforeach.cosmicreach.gamestates.YouDiedMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(YouDiedMenu.class)
public class YouDiedMenuMixin {
    @Inject(method = "create", at = @At("HEAD"))
    private void onCreate(CallbackInfo ci) {
        Client.playerDied = true;
    }
}
