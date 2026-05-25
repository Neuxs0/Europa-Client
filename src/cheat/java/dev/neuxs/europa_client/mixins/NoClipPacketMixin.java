package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.CheatModules;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.networking.packets.entities.NoClipPacket;
import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("unused")
@Mixin(NoClipPacket.class)
public abstract class NoClipPacketMixin {

    @Shadow
    private boolean shouldNoClip;

    @Overwrite
    public void handle(NetworkIdentity identity, ChannelHandlerContext ctx) {
        if (CheatModules.noClip == null) {
            return;
        }

        Player player = identity.getPlayer();
        if (!this.shouldNoClip && CheatModules.noClip.isEnabled()) {
            CheatModules.noClip.setNoClip(player, true);
            return;
        }
        CheatModules.noClip.setNoClip(player, this.shouldNoClip);
    }
}
