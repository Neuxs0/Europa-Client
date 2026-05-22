package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.entities.PlayerController;
import finalforeach.cosmicreach.entities.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(PlayerController.class)
public abstract class PlayerControllerMixin {
    @Shadow
    Player player;

    @Inject(method = "update", at = @At("HEAD"))
    private void europa_client$prepareFly(float deltaTime, CallbackInfo ci) {
        if (Modules.fly != null) {
            Modules.fly.prepare(player);
        }
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void europa_client$applyFly(float deltaTime, CallbackInfo ci) {
        if (Modules.fly != null) {
            Modules.fly.apply(player);
        }
    }
}
