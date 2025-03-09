package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.cheats.NoClip;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.networking.packets.entities.NoClipPacket;
import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NoClipPacket.class)
public abstract class NoClipPacketMixin {

    @Shadow
    private boolean shouldNoClip;

    /**
     * Replaces the handle method with a client-authoritative version
     * that always applies the no-clip state.
     */
    @Overwrite
    public void handle(NetworkIdentity identity, ChannelHandlerContext ctx) {
        Player player = identity.getPlayer();
        NoClip.setNoClip(player, this.shouldNoClip);
    }
}
