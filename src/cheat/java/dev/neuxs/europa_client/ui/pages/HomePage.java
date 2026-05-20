package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;
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

        selectedProfile.setText("Selected Profile: " + "None");

        closeInfo.setText("Press ESC to close this menu");
    }

    @Override
    public void resize(float width, float height) {
        super.resize(width, height);
        this.pageDim = new Vector4(pageContainer.getPosX(), pageContainer.getPosY(), pageContainer.getWidth(), pageContainer.getHeight());

        clientName.setPos(
                pageDim.x + 5f,
                pageDim.y + (pageDim.w - (clientName.getTextHeight(viewport) + 5f))
        );
        clientType.setPos(
                pageDim.x + 5f,
                pageDim.y + (pageDim.w - ((clientName.getTextHeight(viewport) + 5f) + (clientType.getTextHeight(viewport) + 5f)))
        );
        selectedProfile.setPos(
                pageDim.x + (pageDim.z / 2 - (selectedProfile.getTextWidth(viewport) / 2f)),
                pageDim.y + (pageDim.w / 2 - (selectedProfile.getTextHeight(viewport) / 2f))
        );
        closeInfo.setPos(
                pageDim.x + 5f,
                pageDim.y + 5f
        );
    }

    @Override
    public void addRenderers(RenderUtil renderUtil) {
        renderUtil.addRenderer(clientName);
        renderUtil.addRenderer(clientType);
        renderUtil.addRenderer(selectedProfile);
        renderUtil.addRenderer(closeInfo);
    }

    @Override
    public void removeRenderers(RenderUtil renderUtil) {
        renderUtil.removeRenderer(clientName);
        renderUtil.removeRenderer(clientType);
        renderUtil.removeRenderer(selectedProfile);
        renderUtil.removeRenderer(closeInfo);
    }
}
