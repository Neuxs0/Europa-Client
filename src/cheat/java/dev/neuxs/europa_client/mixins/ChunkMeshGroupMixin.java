package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.utils.Array;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.XrayChunkMeshAppender;
import finalforeach.cosmicreach.rendering.ChunkMeshGroup;
import finalforeach.cosmicreach.rendering.meshes.MeshData;
import finalforeach.cosmicreach.world.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkMeshGroup.class)
public abstract class ChunkMeshGroupMixin {
    @Inject(method = "getMeshData", at = @At("RETURN"), cancellable = true)
    private static void europa_client$appendXrayOreMeshes(
            Chunk chunk,
            CallbackInfoReturnable<Array<MeshData>> cir
    ) {
        if (Modules.xray != null && Modules.xray.isEnabled()) {
            cir.setReturnValue(XrayChunkMeshAppender.appendEnclosedOres(chunk, cir.getReturnValue()));
        }
    }
}
