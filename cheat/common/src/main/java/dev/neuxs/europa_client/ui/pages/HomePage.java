package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;

public class HomePage extends Page {
    private Viewport viewport;
    private Vector4 pageDim;
    private final BoxRenderer pageContainer;
    private final TextRenderer clientName = new TextRenderer();
    private final TextRenderer clientType = new TextRenderer();
    private final TextRenderer selectedProfile = new TextRenderer();
    private final TextRenderer closeInfo = new TextRenderer();

    public HomePage(BoxRenderer pageContainer) {
        super("Home", pageContainer);
        this.pageContainer = pageContainer;
        this.pageDim = new Vector4(pageContainer.getPosX(), pageContainer.getPosY(), pageContainer.getWidth(), pageContainer.getHeight());
    }

    @Override
    public void create(Viewport viewport, float width, float height) {
        super.create(viewport, width, height);
        this.viewport = viewport;

        clientName.setText(Client.MOD_NAME + " v" + Client.VERSION);

        clientType.setText(Client.CLIENT_TYPE + " Version");

        selectedProfile.setText("Selected Profile: " + "None  ∨ ∧");

        closeInfo.setText("Press ESC to close this menu");
    }

    @Override
    public void resize(float width, float height) {
        super.resize(width, height);
        this.pageDim = new Vector4(pageContainer.getPosX(), pageContainer.getPosY(), pageContainer.getWidth(), pageContainer.getHeight());

        clientName.setPosition(
                pageDim.x + 5f,
                pageDim.y + (pageDim.w - (clientName.getHeight(viewport) + 5f))
        );
        clientType.setPosition(
                pageDim.x + 5f,
                pageDim.y + (pageDim.w - ((clientName.getHeight(viewport) + 5f) + (clientType.getHeight(viewport) + 5f)))
        );
        selectedProfile.setPosition(
                pageDim.x + (pageDim.z / 2 - (selectedProfile.getWidth(viewport) / 2f)),
                pageDim.y + (pageDim.w / 2 - (selectedProfile.getHeight(viewport) / 2f))
        );
        closeInfo.setPosition(
                pageDim.x + 5f,
                pageDim.y + 5f
        );
    }

    @Override
    public void renderShape(ShapeRenderer shapeRenderer) {
        super.renderShape(shapeRenderer);
    }

    @Override
    public void renderText(SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        super.renderText(spriteBatch, glyphLayout);

        clientName.render(spriteBatch, glyphLayout, viewport);
        clientType.render(spriteBatch, glyphLayout, viewport);
        selectedProfile.render(spriteBatch, glyphLayout, viewport);
        closeInfo.render(spriteBatch, glyphLayout, viewport);
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
