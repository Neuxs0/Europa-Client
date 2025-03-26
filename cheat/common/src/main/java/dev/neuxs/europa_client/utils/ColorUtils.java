package dev.neuxs.europa_client.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

@SuppressWarnings("unused")
public class ColorUtils {
    public static Color color(int r, int g, int b, int a) {
        float red = MathUtils.clamp(r, 0, 255) / 255.0f;
        float green = MathUtils.clamp(g, 0, 255) / 255.0f;
        float blue = MathUtils.clamp(b, 0, 255) / 255.0f;
        float alpha = MathUtils.clamp(a, 0, 255) / 255.0f;
        return new Color(red, green, blue, alpha);
    }

    public static Color color(int r, int g, int b) {
        return color(r, g, b, 255);
    }
}
