package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.ui.TpsTracker;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.networking.packets.EndTickPacket;
import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(EndTickPacket.class)
public abstract class EndTickPacketMixin {
    @Shadow
    long worldTick;

    @Inject(method = "handle", at = @At("HEAD"))
    private void europa_client$recordRemoteTick(NetworkIdentity identity, ChannelHandlerContext ctx, CallbackInfo ci) {
        if (identity != null && !identity.isServer()) {
            TpsTracker.recordRemoteTick(worldTick);
        }
    }
}
