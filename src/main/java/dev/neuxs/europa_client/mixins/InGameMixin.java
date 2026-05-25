package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.Gdx;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.ui.GUI;
import dev.neuxs.europa_client.ui.HudEditor;
import dev.neuxs.europa_client.accessor.InGameAccessor;
import dev.neuxs.europa_client.modules.ui.HudManager;
import finalforeach.cosmicreach.entities.PlayerController;
import finalforeach.cosmicreach.gamestates.*;
import finalforeach.cosmicreach.ui.UI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(InGame.class)
public abstract class InGameMixin extends GameState implements InGameAccessor {

    @Shadow
    static PlayerController playerController;

    @Inject(method = "unloadWorld", at = @At("HEAD"))
    private void europa_client$disableFreecamOnWorldUnload(CallbackInfo ci) {
        disableFreecam();
    }

    // Thanks to tympanicblock61 for telling me about this
    @Inject(method = "switchAwayTo", at = @At("HEAD"), cancellable = true)
    private void preventUnloadOnSwitch(GameState gameState, CallbackInfo ci) {
        if (gameState instanceof GUI || gameState instanceof HudEditor) {
            if (Gdx.input.getInputProcessor() != null) {
                Gdx.input.setInputProcessor(null);
            }
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderEuropaHud(CallbackInfo ci) {
        if (HudManager.isInGameHudSuppressed()) {
            return;
        }
        if (UI.renderDebugInfo) {
            return;
        }

        HudManager.render(this.newUiViewport);
    }

    @Override
    public PlayerController europa_client$getPlayerController_accessor() {
        return playerController;
    }

    private static void disableFreecam() {
        if (Modules.freecam != null && Modules.freecam.isEnabled()) {
            Modules.freecam.toggle(false);
        }
    }

    protected InGameMixin() {}
}
