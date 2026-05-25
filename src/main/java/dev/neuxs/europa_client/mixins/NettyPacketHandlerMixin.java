package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.modules.ui.PingTracker;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.networking.netty.NettyPacketHandler;
import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(NettyPacketHandler.class)
public abstract class NettyPacketHandlerMixin {

    @Shadow
    public abstract NetworkIdentity getIdentity(ChannelHandlerContext ctx);

    @Inject(
            method = "channelRead(Lio/netty/channel/ChannelHandlerContext;Ljava/lang/Object;)V",
            at = @At("HEAD")
    )
    private void europaClient$recordInboundPacket(ChannelHandlerContext ctx, Object msg, CallbackInfo ci) {
        if (ctx != null && ctx.channel() != null) {
            PingTracker.recordInboundPacket(ctx.channel().remoteAddress());
        }
    }

    @ModifyVariable(
            method = "channelRead(Lio/netty/channel/ChannelHandlerContext;Ljava/lang/Object;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lfinalforeach/cosmicreach/networking/GamePacket;receive(Lio/netty/buffer/ByteBuf;)V",
                    shift = At.Shift.AFTER
            )
    )
    private GamePacket cosmicreach$logReceivedPacket(GamePacket packet,
                                                     ChannelHandlerContext ctx,
                                                     Object msg
    ) {
        if (packet != null && Modules.packetInspector.isEnabled()) {
            NetworkIdentity identity = this.getIdentity(ctx);

            String side = "UNKNOWN";
            if (identity != null) {
                if (identity.getSide() != null) {
                    side = identity.getSide().name();
                } else {
                    side = "NO_SIDE";
                }
            } else {
                Client.LOGGER.warn("[PACKET RECV] Identity was null when retrieved in Mixin for packet ID: {}, Type: {}", packet.packetID, packet.getClass().getSimpleName());
            }
            Client.LOGGER.info("[PACKET RECV] ({}) ID: {}, Type: {}", side, packet.packetID, packet.getClass().getSimpleName());
        } else {
            if (Modules.packetInspector.isEnabled()) {
                Client.LOGGER.warn("[PACKET RECV] ModifyVariable intercepted a null packet after receive().");
            }
        }
        return packet;
    }
}
