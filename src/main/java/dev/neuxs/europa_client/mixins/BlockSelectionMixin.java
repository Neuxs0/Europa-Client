package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.graphics.Camera;
import dev.neuxs.europa_client.ui.GUI;
import finalforeach.cosmicreach.BlockSelection;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(BlockSelection.class)
public class BlockSelectionMixin {

    @Inject(method = "raycast", at = @At("HEAD"), cancellable = true)
    private void europa_client$disableRaycastWhenGuiOpen(Zone zone, Camera worldCamera, Player player, CallbackInfo ci) {
        if (GameState.currentGameState instanceof GUI) {
            BlockSelection.enabled = false;
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void europa_client$disableHeldItemClickWhenGuiOpen(Camera worldCamera, CallbackInfo ci) {
        if (GameState.currentGameState instanceof GUI) {
            ci.cancel();
        }
    }
}
