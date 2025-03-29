package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.ui.Dropdown;

public class UtilitiesPage extends Page {
    private Viewport viewport;
    private Vector4 pageDim;
    private final BoxRenderer pageContainer;
    private final Dropdown testDropdown = new Dropdown();

    public UtilitiesPage(BoxRenderer pageContainer) {
        super("Utilities", pageContainer);
        this.pageContainer = pageContainer;
        this.pageDim = new Vector4(pageContainer.getPosX(), pageContainer.getPosY(), pageContainer.getWidth(), pageContainer.getHeight());
    }

    @Override
    public void create(Viewport viewport, float width, float height) {
        super.create(viewport, width, height);
        this.viewport = viewport;

        testDropdown.addOption("test1");
        testDropdown.addOption("test2");
        testDropdown.addOption("test3");
    }

    @Override
    public void resize(float width, float height) {
        super.resize(width, height);
        this.pageDim.set(pageContainer.getPosX(), pageContainer.getPosY(), pageContainer.getWidth(), pageContainer.getHeight());
        testDropdown.setPosition(
                pageDim.x + (pageDim.z / 2) - (testDropdown.getWidth() / 2),
                pageDim.y + (pageDim.w / 2) - (testDropdown.getHeight() / 2)
        );
    }

    @Override
    public void renderShape(ShapeRenderer shapeRenderer) {
        super.renderShape(shapeRenderer);
        testDropdown.renderShape(shapeRenderer, viewport);
    }

    @Override
    public void renderText(SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        super.renderText(spriteBatch, glyphLayout);
        testDropdown.renderText(spriteBatch, glyphLayout, viewport);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
