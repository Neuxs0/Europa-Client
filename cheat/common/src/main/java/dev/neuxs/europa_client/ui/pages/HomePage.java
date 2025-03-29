package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.managers.font.FontManager;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;

public class HomePage extends Page {
    private final FontManager fontManager = new FontManager();
    private Viewport viewport;
    private final BoxRenderer pageContainer;
    private final TextRenderer clientName = new TextRenderer();
    private final TextRenderer clientType = new TextRenderer();
    private final TextRenderer selectedProfile = new TextRenderer();
    private final TextRenderer closeInfo = new TextRenderer();
    private float pageX;
    private float pageY;
    private float pageW;
    private float pageH;

    public HomePage(BoxRenderer pageContainer) {
        super("Home", pageContainer);
        this.pageContainer = pageContainer;
        this.pageX = pageContainer.getPosX();
        this.pageY = pageContainer.getPosY();
        this.pageW = pageContainer.getWidth();
        this.pageH = pageContainer.getHeight();
    }

    @Override
    public void create(Viewport viewport, float width, float height) {
        super.create(viewport, width, height);
        this.viewport = viewport;

        clientName.setText(Client.MOD_NAME + " v" + Client.VERSION);

        clientType.setText(Client.CLIENT_TYPE + " Version");

        selectedProfile.setText("Selected Profile: " + "None");

        closeInfo.setText("Press ESC to close this menu");
    }

    @Override
    public void resize(float width, float height) {
        super.resize(width, height);

        this.pageX = pageContainer.getPosX();
        this.pageY = pageContainer.getPosY();
        this.pageW = pageContainer.getWidth();
        this.pageH = pageContainer.getHeight();

        clientName.setPosition(
                pageX + 5f,
                pageY + (pageH - (clientName.getHeight(viewport) + 5f))
        );
        clientType.setPosition(
                pageX + 5f,
                pageY + (pageH - ((clientName.getHeight(viewport) + 5f) + (clientType.getHeight(viewport) + 5f)))
        );
        selectedProfile.setPosition(
                pageX + (pageW / 2 - (selectedProfile.getWidth(viewport) / 2f)),
                pageY + (pageH / 2 - (selectedProfile.getHeight(viewport) / 2f))
        );
        closeInfo.setPosition(
                pageX + 5f,
                pageY + 5f
        );
    }

    @Override
    public void renderShape(ShapeRenderer shapeRenderer) {
        super.renderShape(shapeRenderer);
    }

    @Override
    public void renderText(SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        super.renderText(spriteBatch, glyphLayout);

        clientName.render(spriteBatch, glyphLayout);
        clientType.render(spriteBatch, glyphLayout);
        selectedProfile.render(spriteBatch, glyphLayout);
        closeInfo.render(spriteBatch, glyphLayout);
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
