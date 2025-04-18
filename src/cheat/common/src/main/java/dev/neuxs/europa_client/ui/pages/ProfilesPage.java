package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;

public class ProfilesPage extends Page {
    private Viewport viewport;
    private final BoxRenderer pageContainer;

    public ProfilesPage(BoxRenderer pageContainer) {
        super("Profiles", pageContainer);
        this.pageContainer = pageContainer;
    }

    @Override
    public void create(Viewport viewport, float width, float height) {
        super.create(viewport, width, height);
        this.viewport = viewport;
    }

    @Override
    public void resize(float width, float height) {
        super.resize(width, height);
    }

    @Override
    public void addRenderers(RenderUtil renderUtil) {}

    @Override
    public void removeRenderers(RenderUtil renderUtil) {}


    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
    }

    @Override
    public void dispose(RenderUtil renderUtil) {
        super.dispose(renderUtil);
    }
}
