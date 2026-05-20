package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;
import dev.neuxs.europa_client.utils.rendering.ui.Slider;

public class CheatsPage extends Page {
    private Viewport viewport;
    private final BoxRenderer pageContainer;
    private final Slider textSlider;

    public CheatsPage(BoxRenderer pageContainer) {
        super("Cheats", pageContainer);
        this.pageContainer = pageContainer;
        this.textSlider = new Slider();
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
    public void addRenderers(RenderUtil renderUtil) {
        renderUtil.addRenderer(this.textSlider);
    }

    @Override
    public void removeRenderers(RenderUtil renderUtil) {
        renderUtil.removeRenderer(this.textSlider);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        textSlider.update(viewport);
    }

    @Override
    public void dispose(RenderUtil renderUtil) {
        super.dispose(renderUtil);
    }
}
