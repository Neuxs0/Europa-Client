package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(InGame.class)
public abstract class ZoomInGameMixin extends GameState {
    @Shadow
    private static PerspectiveCamera rawWorldCamera;

    @Inject(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/badlogic/gdx/utils/viewport/Viewport;apply()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void applyEuropaZoomFov(Zone zone, CallbackInfo ci) {
        if (Modules.zoom != null && rawWorldCamera != null) {
            rawWorldCamera.fieldOfView = Modules.zoom.getZoomedFov(rawWorldCamera.fieldOfView);
            rawWorldCamera.update();
        }
    }

    @Redirect(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lfinalforeach/cosmicreach/rendering/items/ItemRenderer;renderHeldItem(Lcom/badlogic/gdx/math/Vector3;Lfinalforeach/cosmicreach/items/ItemStack;Lcom/badlogic/gdx/graphics/PerspectiveCamera;)V"
            )
    )
    private void hideEuropaZoomHand(Vector3 heldItemPosition, ItemStack itemStack, PerspectiveCamera camera) {
        if (Modules.zoom == null || Modules.zoom.shouldShowHand()) {
            ItemRenderer.renderHeldItem(heldItemPosition, itemStack, camera);
        }
    }
}
