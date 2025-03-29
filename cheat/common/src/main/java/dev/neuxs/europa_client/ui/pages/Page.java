package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public abstract class Page {
    private Viewport viewport;
    private BoxRenderer pageContainer;
    private final String pageTitle;

    public Page(String pageTitle, BoxRenderer pageContainer) {
        this.pageTitle = pageTitle;
    }

    public void create(Viewport viewport, float width, float height) {

    }

    public void resize(float width, float height) {}

    public void renderShape(ShapeRenderer shapeRenderer) {}

    public void renderText(SpriteBatch spriteBatch, GlyphLayout glyphLayout) {}

    public void update(float deltaTime) {}

    public void dispose() {}

    public String getTitle() { return pageTitle; }
}
