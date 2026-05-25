package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.audio.SoundManager;
import finalforeach.cosmicreach.entities.PlayerController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(PlayerController.class)
public abstract class ZoomPlayerControllerMixin {
    @Unique
    private final Vector3 europa_client$zoomCameraDirection = new Vector3();
    @Unique
    private final Vector3 europa_client$previousCameraDirection = new Vector3();
    @Unique
    private boolean europa_client$hasZoomCameraDirection;

    @Inject(method = "updateCamera", at = @At("HEAD"))
    private void captureEuropaPreviousCameraDirection(PerspectiveCamera playerCamera, CallbackInfo ci) {
        europa_client$previousCameraDirection.set(playerCamera.direction);
    }

    @Inject(method = "updateCamera", at = @At("TAIL"))
    private void smoothEuropaZoomCamera(PerspectiveCamera playerCamera, CallbackInfo ci) {
        if (Modules.zoom == null || !Modules.zoom.shouldSmoothCamera()) {
            europa_client$hasZoomCameraDirection = false;
            return;
        }

        if (!europa_client$hasZoomCameraDirection) {
            europa_client$zoomCameraDirection.set(europa_client$previousCameraDirection);
            europa_client$hasZoomCameraDirection = true;
        }

        float alpha = Math.min(Gdx.graphics.getDeltaTime() * 4.0f, 1.0f);
        europa_client$zoomCameraDirection.slerp(playerCamera.direction, alpha).nor();
        playerCamera.direction.set(europa_client$zoomCameraDirection);
        playerCamera.update();
        SoundManager.INSTANCE.updateCamera(playerCamera);
    }
}
