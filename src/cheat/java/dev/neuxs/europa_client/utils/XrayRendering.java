package dev.neuxs.europa_client.utils;

import finalforeach.cosmicreach.blocks.BlockState;

public final class XrayRendering {
    private static final ThreadLocal<Boolean> CURRENT_BLOCK_IS_ORE = ThreadLocal.withInitial(() -> false);

    private XrayRendering() {
    }

    public static void beginBlock(BlockState blockState) {
        CURRENT_BLOCK_IS_ORE.set(isOre(blockState));
    }

    public static void endBlock() {
        CURRENT_BLOCK_IS_ORE.remove();
    }

    public static boolean isCurrentBlockOre() {
        return CURRENT_BLOCK_IS_ORE.get();
    }

    public static boolean isOre(BlockState blockState) {
        if (blockState == null) {
            return false;
        }

        String blockId = blockState.getBlockId();
        if (blockId == null) {
            blockId = blockState.stringId;
        }

        return blockId != null && blockId.contains(":ore_");
    }
}
