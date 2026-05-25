package dev.neuxs.europa_client.utils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.rendering.RenderOrder;
import finalforeach.cosmicreach.rendering.meshes.MeshData;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;

public final class XrayChunkMeshAppender {
    private XrayChunkMeshAppender() {
    }

    public static Array<MeshData> appendEnclosedOres(Chunk chunk, Array<MeshData> meshData) {
        if (chunk == null || chunk.getBlockData() == null) {
            return meshData;
        }

        IBlockData<BlockState> blockData = chunk.getBlockData();
        Array<MeshData> result = meshData;
        short[] blockLights = new short[8];
        int[] skyLights = new int[] {15, 15, 15, 15, 15, 15, 15, 15};
        boolean addedOre = false;

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockState blockState = blockData.getBlockValue(x, y, z);
                    if (!XrayRendering.isOre(blockState) || !isEnclosed(chunk, x, y, z)) {
                        continue;
                    }

                    if (result == null) {
                        result = new Array<>(3);
                    } else if (!addedOre) {
                        Array<MeshData> copy = new Array<>(result.size + 1);
                        copy.addAll(result);
                        result = copy;
                    }

                    MeshData oreMeshData = getOrCreateMeshData(result, blockState);
                    blockState.addVertices(
                            oreMeshData,
                            chunk.getBlockX() + x,
                            chunk.getBlockY() + y,
                            chunk.getBlockZ() + z,
                            0,
                            blockLights,
                            skyLights
                    );
                    addedOre = true;
                }
            }
        }

        return result;
    }

    private static MeshData getOrCreateMeshData(Array<MeshData> meshData, BlockState blockState) {
        GameShader shader = GameShader.getShaderForBlockState(blockState);
        RenderOrder renderOrder = RenderOrder.getRenderOrderForBlockState(blockState);

        for (int i = 0; i < meshData.size; i++) {
            MeshData existing = meshData.get(i);
            if (existing.shader == shader && existing.renderOrder == renderOrder) {
                return existing;
            }
        }

        MeshData created = new MeshData(new FloatArray(1024), null, shader, renderOrder);
        meshData.add(created);
        return created;
    }

    private static boolean isEnclosed(Chunk chunk, int x, int y, int z) {
        Zone zone = chunk.getZone();
        int worldX = chunk.getBlockX() + x;
        int worldY = chunk.getBlockY() + y;
        int worldZ = chunk.getBlockZ() + z;

        return isPosXOccluding(zone.getBlockState(chunk, worldX - 1, worldY, worldZ))
                && isNegXOccluding(zone.getBlockState(chunk, worldX + 1, worldY, worldZ))
                && isPosYOccluding(zone.getBlockState(chunk, worldX, worldY - 1, worldZ))
                && isNegYOccluding(zone.getBlockState(chunk, worldX, worldY + 1, worldZ))
                && isPosZOccluding(zone.getBlockState(chunk, worldX, worldY, worldZ - 1))
                && isNegZOccluding(zone.getBlockState(chunk, worldX, worldY, worldZ + 1));
    }

    private static boolean isPosXOccluding(BlockState blockState) {
        return blockState == null || blockState.isPosXFaceOccluding;
    }

    private static boolean isNegXOccluding(BlockState blockState) {
        return blockState == null || blockState.isNegXFaceOccluding;
    }

    private static boolean isPosYOccluding(BlockState blockState) {
        return blockState == null || blockState.isPosYFaceOccluding;
    }

    private static boolean isNegYOccluding(BlockState blockState) {
        return blockState == null || blockState.isNegYFaceOccluding;
    }

    private static boolean isPosZOccluding(BlockState blockState) {
        return blockState == null || blockState.isPosZFaceOccluding;
    }

    private static boolean isNegZOccluding(BlockState blockState) {
        return blockState == null || blockState.isNegZFaceOccluding;
    }
}
