package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.entities.player.PlayerEntity;
import finalforeach.cosmicreach.singletons.IClientSingletons;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Mixin(PlayerEntity.class)
public abstract class FreecamPlayerEntityMixin {
    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lfinalforeach/cosmicreach/singletons/IClientSingletons;isFirstPerson()Z"
            )
    )
    private boolean europa_client$showLocalPlayerInFreecam(IClientSingletons clientSingletons) {
        if (Modules.freecam != null && Modules.freecam.isEnabled()) {
            return false;
        }
        return clientSingletons.isFirstPerson();
    }
}
