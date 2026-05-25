package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public abstract class Page {
    private Viewport viewport;
    private final String pageTitle;

    public Page(String pageTitle, BoxRenderer pageContainer) {
        this.pageTitle = pageTitle;
    }

    public void create(Viewport viewport, float width, float height) {}

    public void resize(float width, float height) {}

    public void addRenderers(RenderUtil renderUtil) {}

    public void removeRenderers(RenderUtil renderUtil) {}

    public void update(float deltaTime) {}

    public void dispose(RenderUtil renderUtil) {
        removeRenderers(renderUtil);
    }

    public String getTitle() { return pageTitle; }
}
