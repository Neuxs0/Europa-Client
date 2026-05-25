package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.ui.PingTracker;
import finalforeach.cosmicreach.networking.client.ClientNetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@SuppressWarnings("unused")
@Mixin(ClientNetworkManager.class)
public abstract class ClientNetworkManagerMixin {
    @Inject(method = "connectToServer", at = @At("HEAD"))
    private static void europaClient$recordPingTarget(String address, Runnable onConnect, Consumer<Throwable> onFailure, CallbackInfo ci) {
        PingTracker.setTarget(address);
    }
}
