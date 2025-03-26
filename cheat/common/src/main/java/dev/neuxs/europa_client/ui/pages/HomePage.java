package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.math.Matrix4;
import dev.neuxs.europa_client.utils.rendering.Renderer;

public class HomePage {
    
    public HomePage() {}

    public static void renderContent(Matrix4 projectionMatrix) {
        Renderer.drawBox(
                projectionMatrix,
                313, // 5 pixel padding
                110, // 5 pixel padding
                20,
                20,
                Renderer.color(255, 0, 255, 255)
        );
    }
}
