package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import dev.neuxs.europa_client.ui.GUI;
import dev.neuxs.europa_client.accessor.InGameAccessor;
import finalforeach.cosmicreach.entities.PlayerController;
import finalforeach.cosmicreach.gamestates.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGame.class)
public abstract class InGameMixin extends GameState implements InGameAccessor {

    @Shadow
    static PlayerController playerController;

    // Thanks to Zombi for telling me about this
    @Inject(method = "switchAwayTo", at = @At("HEAD"), cancellable = true)
    private void preventUnloadOnSwitch(GameState gameState, CallbackInfo ci) {
        if (gameState instanceof GUI) {
            if (Gdx.input.getInputProcessor() != null) {
                Gdx.input.setInputProcessor(null);
            }
            ci.cancel();
        }
    }

    @Override
    public PlayerController getPlayerController_accessor() {
        return playerController;
    }

    protected InGameMixin() {}
}