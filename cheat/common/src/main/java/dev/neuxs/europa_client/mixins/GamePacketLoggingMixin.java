package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.networking.GamePacket;
import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"unused", "ConstantConditions"})
@Mixin(GamePacket.class)
public abstract class GamePacketLoggingMixin {

    @Shadow public short packetID;

    @Inject(
            method = "flushToContext(Lio/netty/channel/ChannelHandlerContext;)V",
            at = @At("HEAD")
    )
    private void cosmicreach$logSentPacket(ChannelHandlerContext ctx, CallbackInfo ci) {
        GamePacket self = (GamePacket) (Object) this;
        if (Modules.packetInspectorEnabled) {
            Client.LOGGER.info("[PACKET SEND] ID: {}, Type: {}", self.packetID, self.getClass().getSimpleName());
        }
    }
}