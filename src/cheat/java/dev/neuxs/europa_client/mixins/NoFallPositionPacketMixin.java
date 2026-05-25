package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.CheatModules;
import finalforeach.cosmicreach.entities.GameEntity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.networking.packets.entities.EntityPositionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(EntityPositionPacket.class)
public abstract class NoFallPositionPacketMixin {
    @Shadow
    public boolean isOnGround;

    @Inject(method = "setEntity", at = @At("TAIL"))
    private void europa_client$spoofGrounded(GameEntity entity, CallbackInfo ci) {
        if (CheatModules.noFall == null || !CheatModules.noFall.isEnabled()) {
            return;
        }

        Player localPlayer = InGame.getLocalPlayer();
        if (localPlayer != null && localPlayer.getEntity() == entity) {
            this.isOnGround = true;
        }
    }
}
