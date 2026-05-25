package dev.neuxs.europa_client.utils;

public final class ShaderDecisions {
    private ShaderDecisions() {
    }

    public static float getFullbrightUniformValue(boolean fullbrightEnabled) {
        return fullbrightEnabled ? 1.0F : 0.0F;
    }

    public static float getFogDensityOverride(boolean noFogEnabled, float vanillaFogDensity) {
        return noFogEnabled ? 0.0F : vanillaFogDensity;
    }
}
