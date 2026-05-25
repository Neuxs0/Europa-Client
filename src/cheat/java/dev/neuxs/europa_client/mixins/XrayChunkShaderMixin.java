package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.graphics.Camera;
import dev.neuxs.europa_client.modules.CheatModules;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkShader.class)
public abstract class XrayChunkShaderMixin {
    @Inject(method = "bind", at = @At("TAIL"))
    private void europa_client$applyXray(Camera worldCamera, CallbackInfo ci) {
        ChunkShader shader = (ChunkShader) (Object) this;
        shader.bindOptionalFloat("u_xray", CheatModules.xray != null && CheatModules.xray.isEnabled() ? 1.0F : 0.0F);
    }
}
