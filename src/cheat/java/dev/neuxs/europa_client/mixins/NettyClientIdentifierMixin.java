package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.ui.PingTracker;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.client.netty.NettyClient;
import finalforeach.cosmicreach.networking.packets.meta.ProtocolSyncPacket;
import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(NettyClient.class)
public abstract class NettyClientIdentifierMixin {
    private static final int MAX_IDENTIFIER_LENGTH = 96;

    @Shadow
    public ChannelHandlerContext ctx;

    @Inject(method = "send(Lfinalforeach/cosmicreach/networking/GamePacket;)V", at = @At("HEAD"))
    private void europaClient$appendClientIdentifier(GamePacket packet, CallbackInfo ci) {
        ChannelHandlerContext currentContext = this.ctx;
        if (currentContext != null && currentContext.channel() != null) {
            PingTracker.recordOutboundPacket(currentContext.channel().remoteAddress());
        } else {
            PingTracker.recordOutboundPacket();
        }

        if (packet instanceof ProtocolSyncPacket protocolSyncPacket) {
            String identifier = sanitizedIdentifier();
            if (protocolSyncPacket.gameVersion != null && !protocolSyncPacket.gameVersion.contains(identifier)) {
                protocolSyncPacket.gameVersion += " | " + identifier;
            }
        }
    }

    private static String sanitizedIdentifier() {
        String identifier = Client.getNetworkIdentifier();
        StringBuilder safeIdentifier = new StringBuilder(Math.min(identifier.length(), MAX_IDENTIFIER_LENGTH));
        for (int i = 0; i < identifier.length() && safeIdentifier.length() < MAX_IDENTIFIER_LENGTH; i++) {
            char character = identifier.charAt(i);
            if (character >= 0x20 && character <= 0x7E) {
                safeIdentifier.append(character);
            }
        }
        return safeIdentifier.toString();
    }
}
