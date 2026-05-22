package dev.neuxs.europa_client.mixins;

import dev.neuxs.europa_client.modules.ui.PingTracker;
import dev.neuxs.europa_client.modules.ui.TpsTracker;
import finalforeach.cosmicreach.TickRunner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(TickRunner.class)
public abstract class TickRunnerMixin {
    @Inject(
            method = "runTicks",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/badlogic/gdx/utils/Array;forEach(Ljava/util/function/Consumer;)V"
            )
    )
    private void europa_client$recordTickStart(CallbackInfo ci) {
        TpsTracker.recordLocalTickStart();
        PingTracker.recordLocalServerTickStart();
    }

    @Inject(
            method = "runTicks",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/badlogic/gdx/utils/Array;forEach(Ljava/util/function/Consumer;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void europa_client$recordTickEnd(CallbackInfo ci) {
        TpsTracker.recordLocalTickEnd();
        PingTracker.recordLocalServerTickEnd();
    }
}
