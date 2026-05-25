package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.entities.PlayerController;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(PlayerController.class)
public abstract class FreecamPlayerControllerMixin {
    @Shadow
    Player player;

    @Inject(method = "updateMovement", at = @At("HEAD"), cancellable = true)
    private void europa_client$updateFreecam(Zone zone, CallbackInfo ci) {
        if (Modules.freecam == null || !Modules.freecam.isEnabled()) {
            return;
        }

        Modules.freecam.update(player, zone, Gdx.graphics.getDeltaTime());
        ci.cancel();
    }

    @Inject(method = "updateCamera", at = @At("HEAD"), cancellable = true)
    private void europa_client$applyFreecam(PerspectiveCamera playerCamera, CallbackInfo ci) {
        if (Modules.freecam != null && Modules.freecam.applyCamera(playerCamera, player)) {
            ci.cancel();
        }
    }
}
