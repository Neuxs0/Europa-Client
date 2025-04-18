package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;
import dev.neuxs.europa_client.utils.rendering.Renderer;

public class HomePage {
    
    public HomePage() {}

    public static void renderContent(Matrix4 projectionMatrix) {
        Renderer.drawText(
                projectionMatrix,
                "cosmicreach",
                "Hello, World",
                500, 300,
                Color.WHITE,
                Align.center,
                0f, false
        );
    }
}
