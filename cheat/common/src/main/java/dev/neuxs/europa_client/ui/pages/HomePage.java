package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import dev.neuxs.europa_client.utils.rendering.FontRenderer;

public class HomePage {
    
    public HomePage() {}

    public static void renderContent(SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        FontRenderer.drawText(
                spriteBatch,
                glyphLayout,
                "cosmicreach",
                "Hello, World",
                500, 300,
                Color.RED,
                Align.center,
                0f, false
        );
    }
}
