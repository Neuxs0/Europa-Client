package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.math.collision.Ray;
import dev.neuxs.europa_client.modules.CheatModules;
import finalforeach.cosmicreach.EntityRaycasts;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
@Mixin(EntityRaycasts.class)
public abstract class EntityRaycastsMixin {
    @Shadow
    private float maximumRaycastDist;

    @Inject(method = "raycastForEntities", at = @At("HEAD"))
    private void europa_client$updateMaximumRaycastDistance(Zone zone, Ray ray, Player player,
                                                            CallbackInfoReturnable<Boolean> cir) {
        if (CheatModules.reach != null && CheatModules.reach.isEnabled()) {
            this.maximumRaycastDist = CheatModules.reach.getReachDistance();
        }
    }
}
