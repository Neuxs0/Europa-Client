package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.graphics.Camera;
import dev.neuxs.europa_client.Client;
import finalforeach.cosmicreach.BlockRaycasts;
import finalforeach.cosmicreach.BlockSelection;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(BlockSelection.class)
public abstract class ClickTpBlockSelectionMixin {
    @Shadow
    private BlockRaycasts blockRaycasts;

    @Shadow
    private BlockState selectedBlockState;

    @Shadow
    public BlockPosition selectedBlockPos;

    @Inject(
            method = "raycast",
            at = @At(
                    value = "INVOKE",
                    target = "Lfinalforeach/cosmicreach/BlockRaycasts;raycast(Lfinalforeach/cosmicreach/world/Zone;Lcom/badlogic/gdx/math/collision/Ray;Lfinalforeach/cosmicreach/entities/player/Player;Lfinalforeach/cosmicreach/items/ItemStack;ZZ)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void europa_client$discardInvalidClickTpRaycast(Zone zone, Camera worldCamera, Player player, CallbackInfo ci) {
        if (blockRaycasts == null || blockRaycasts.selectedBlockState == null) {
            return;
        }

        BlockPosition raycastBlockPos = blockRaycasts.selectedBlockPos;
        if (raycastBlockPos != null && raycastBlockPos.isValid()) {
            return;
        }

        blockRaycasts.selectedBlockState = null;
        if (raycastBlockPos != null) {
            raycastBlockPos.set(null, 0, 0, 0);
        }
        selectedBlockState = null;
        if (selectedBlockPos != null) {
            selectedBlockPos.set(null, 0, 0, 0);
        }
        BlockSelection.enabled = false;

        Client.LOGGER.debug("Discarded invalid block raycast result after Click-TP movement.");
        ci.cancel();
    }
}
