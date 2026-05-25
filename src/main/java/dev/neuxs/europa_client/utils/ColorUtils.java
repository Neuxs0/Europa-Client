package dev.neuxs.europa_client.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

@SuppressWarnings("unused")
public class ColorUtils {
    public static final Color TRANSPARENT = color(0, 0, 0, 0);
    public static final Color WHITE = color(255, 255, 255);
    public static final Color BLACK = color(0, 0, 0);
    public static final Color RED = color(255, 0, 0);
    public static final Color GREEN = color(0, 255, 0);
    public static final Color BLUE = color(0, 0, 255);

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
