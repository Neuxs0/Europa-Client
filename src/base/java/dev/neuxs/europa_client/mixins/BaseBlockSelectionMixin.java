package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.graphics.Camera;
import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.BlockSelection;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(BlockSelection.class)
public class BaseBlockSelectionMixin {
    @Inject(method = "raycast", at = @At("HEAD"), cancellable = true)
    private void europa_client$disableWorldInteractions(Zone zone, Camera worldCamera, Player player, CallbackInfo ci) {
        if (Modules.freecam == null || !Modules.freecam.isEnabled() || Modules.freecam.usesPlayerInteraction()) {
            return;
        }

        BlockSelection.enabled = false;
        ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void europa_client$disableBlockOutline(Camera worldCamera, CallbackInfo ci) {
        if (Modules.freecam == null || !Modules.freecam.isEnabled() || Modules.freecam.usesPlayerInteraction()) {
            return;
        }

        ci.cancel();
    }
}
