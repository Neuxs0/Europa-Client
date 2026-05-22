package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.settings.ProfileManager;
import dev.neuxs.europa_client.utils.ColorUtils;
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
    private final TextRenderer leftClickInfo = new TextRenderer();
    private final TextRenderer rightClickInfo = new TextRenderer();
    private final TextRenderer closeInfo = new TextRenderer();
    private String lastProfileText = "";

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

        refreshProfileText();

        leftClickInfo.setText("Left click to toggle");
        leftClickInfo.setTextColor(ColorUtils.color(220, 220, 220, 220));
        leftClickInfo.setScale(0.75f);
        rightClickInfo.setText("Right click to expand");
        rightClickInfo.setTextColor(ColorUtils.color(220, 220, 220, 220));
        rightClickInfo.setScale(0.75f);
        closeInfo.setText("ESC to exit");
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
        leftClickInfo.fitToBox(viewport, pageDim.z - 10f, 20f);
        leftClickInfo.setScale(Math.min(leftClickInfo.getScale(), 0.75f));
        leftClickInfo.setPos(
                pageDim.x + 5f,
                closeInfo.getPosY() + closeInfo.getTextHeight(viewport) + 5f
        );
        rightClickInfo.fitToBox(viewport, pageDim.z - 10f, 20f);
        rightClickInfo.setScale(Math.min(rightClickInfo.getScale(), 0.75f));
        rightClickInfo.setPos(
                pageDim.x + 5f,
                leftClickInfo.getPosY() + leftClickInfo.getTextHeight(viewport) + 3f
        );
    }

    @Override
    public void addRenderers(RenderUtil renderUtil) {
        renderUtil.addRenderer(clientName);
        renderUtil.addRenderer(clientType);
        renderUtil.addRenderer(selectedProfile);
        renderUtil.addRenderer(leftClickInfo);
        renderUtil.addRenderer(rightClickInfo);
        renderUtil.addRenderer(closeInfo);
    }

    @Override
    public void removeRenderers(RenderUtil renderUtil) {
        renderUtil.removeRenderer(clientName);
        renderUtil.removeRenderer(clientType);
        renderUtil.removeRenderer(selectedProfile);
        renderUtil.removeRenderer(leftClickInfo);
        renderUtil.removeRenderer(rightClickInfo);
        renderUtil.removeRenderer(closeInfo);
    }

    @Override
    public void update(float deltaTime) {
        String previousText = lastProfileText;
        refreshProfileText();
        if (!previousText.equals(lastProfileText)) {
            resize(0, 0);
        }
    }

    private void refreshProfileText() {
        lastProfileText = "Selected Profile: " + ProfileManager.getActiveProfileName();
        selectedProfile.setText(lastProfileText);
    }
}
