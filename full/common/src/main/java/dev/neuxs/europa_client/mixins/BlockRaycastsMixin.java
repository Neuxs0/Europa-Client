package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.BlockRaycasts;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.world.Zone;
import com.badlogic.gdx.math.collision.Ray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRaycasts.class)
public abstract class BlockRaycastsMixin {
    @Shadow
    private float maximumRaycastDist;

    @Inject(method = "raycast", at = @At("HEAD"))
    private void updateMaximumRaycastDistance(Zone zone, Ray ray, Player player,
                                              ItemStack heldItemStack,
                                              boolean breakPressed,
                                              boolean placePressed,
                                              CallbackInfo ci) {
        this.maximumRaycastDist = Modules.reach.getReachDistance();
    }
}
