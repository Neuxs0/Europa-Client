package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.Client;
import finalforeach.cosmicreach.ui.UI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(UI.class)
public class UIMixin {
    @Inject(method = "render", at = @At("TAIL"))
    public void render(CallbackInfo ci){
        Client.render();
    }
}