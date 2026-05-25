package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.ui.GUI;
import finalforeach.cosmicreach.BlockSelection;
import finalforeach.cosmicreach.entities.GameEntity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(BlockSelection.class)
public class BlockSelectionMixin {
    @Unique
    private final Vector3 europa_client$originalCameraPosition = new Vector3();

    @Unique
    private final Vector3 europa_client$originalCameraDirection = new Vector3();

    @Unique
    private boolean europa_client$usingPlayerInteractionCamera;

    @Inject(method = "raycast", at = @At("HEAD"), cancellable = true)
    private void europa_client$disableRaycastWhenGuiOpen(Zone zone, Camera worldCamera, Player player, CallbackInfo ci) {
        if (GameState.currentGameState instanceof GUI) {
            BlockSelection.enabled = false;
            ci.cancel();
            return;
        }

        if (Modules.freecam == null || !Modules.freecam.isEnabled() || !Modules.freecam.usesPlayerInteraction()) {
            return;
        }

        GameEntity entity = player == null ? null : player.getEntity();
        if (worldCamera == null || entity == null) {
            return;
        }

        europa_client$originalCameraPosition.set(worldCamera.position);
        europa_client$originalCameraDirection.set(worldCamera.direction);
        europa_client$usingPlayerInteractionCamera = true;

        worldCamera.position.set(entity.position).add(entity.viewPositionOffset);
        worldCamera.direction.set(entity.viewDirection);
    }

    @Inject(method = "raycast", at = @At("RETURN"))
    private void europa_client$restoreFreecamCameraAfterRaycast(Zone zone, Camera worldCamera, Player player, CallbackInfo ci) {
        if (!europa_client$usingPlayerInteractionCamera || worldCamera == null) {
            return;
        }

        worldCamera.position.set(europa_client$originalCameraPosition);
        worldCamera.direction.set(europa_client$originalCameraDirection);
        europa_client$usingPlayerInteractionCamera = false;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void europa_client$disableHeldItemClickWhenGuiOpen(Camera worldCamera, CallbackInfo ci) {
        if (GameState.currentGameState instanceof GUI) {
            ci.cancel();
        }
    }
}
