package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.graphics.Camera;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.FullbrightLighting;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkShader.class)
public abstract class ChunkShaderMixin {
    @Inject(method = "bind", at = @At("TAIL"))
    private void europa_client$applyFullbright(Camera worldCamera, CallbackInfo ci) {
        ChunkShader shader = (ChunkShader) (Object) this;

        if (Modules.fullbright != null && Modules.fullbright.isEnabled()) {
            shader.bindOptionalUniform3f("skyAmbientColor",
                    FullbrightLighting.MAX_DAYLIGHT_BRIGHTNESS,
                    FullbrightLighting.MAX_DAYLIGHT_BRIGHTNESS,
                    FullbrightLighting.MAX_DAYLIGHT_BRIGHTNESS);
            shader.bindOptionalUniform3f("u_sunDirection", 0.0F, 1.0F, 0.0F);
            shader.bindOptionalUniform3f("worldAmbientColor", 0.0F, 0.0F, 0.0F);
            shader.bindOptionalFloat("u_fullbright", 1.0F);
        } else {
            shader.bindOptionalFloat("u_fullbright", 0.0F);
        }

        if (Modules.noFog != null && Modules.noFog.isEnabled()) {
            shader.bindOptionalFloat("u_fogDensity", 0.0F);
        }

        shader.bindOptionalFloat("u_xray", Modules.xray != null && Modules.xray.isEnabled() ? 1.0F : 0.0F);
    }
}
