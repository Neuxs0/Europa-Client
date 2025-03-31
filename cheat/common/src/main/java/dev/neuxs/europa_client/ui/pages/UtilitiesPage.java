package dev.neuxs.europa_client.ui.pages;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.TextRenderer;
import dev.neuxs.europa_client.utils.rendering.ui.Button;

import java.util.ArrayList;
import java.util.List;

public class UtilitiesPage extends Page {
    private Viewport viewport;
    private Vector4 pageDim; // x y z w (x, y, width, height)
    private final BoxRenderer pageContainer;
    private final Button searchInput = new Button(); // TODO: Replace with TextInput
    private final Button sortButton = new Button();
    private final List<Button> moduleButtons = new ArrayList<>();
    private final TextRenderer leftClickText = new TextRenderer();
    private final TextRenderer rightClickText = new TextRenderer();
    private final float padding = 5f;
    private final float elementSpacing = 5f;
    private final float topBarHeight = 25f;
    private final float sortButtonWidth = 50f;
    private final float moduleButtonHeight = 30f;
    private final int modulesPerRow = 2;


    public UtilitiesPage(BoxRenderer pageContainer) {
        super("Utilities", pageContainer);
        this.pageContainer = pageContainer;
        this.pageDim = new Vector4(pageContainer.getPosX(), pageContainer.getPosY(), pageContainer.getWidth(), pageContainer.getHeight());
    }

    @Override
    public void create(Viewport viewport, float width, float height) {
        super.create(viewport, width, height);
        this.viewport = viewport;

        searchInput.setSize(150, topBarHeight);
        searchInput.getTextRenderer().setText("Search...");
        searchInput.getTextRenderer().setAlignment(Align.left);
        searchInput.getTextRenderer().setX(searchInput.getX() + 5f);

        sortButton.setSize(sortButtonWidth, topBarHeight);
        sortButton.getTextRenderer().setText("Sort");

        for (Module module : Modules.utilModuleList) {
            Button moduleButton = new Button();
            moduleButton.getTextRenderer().setText(module.getId());
            moduleButton.setSize(100, moduleButtonHeight);
            moduleButtons.add(moduleButton);
        }

        leftClickText.setText("Left click to toggle");
        leftClickText.setColor(ColorUtils.color(255, 255, 255, 255));

        rightClickText.setText("Right click to expand");
        rightClickText.setColor(ColorUtils.color(255, 255, 255, 255));

        resize(width, height);
    }

    @Override
    public void resize(float width, float height) {
        super.resize(width, height);
        this.pageDim.set(pageContainer.getPosX(), pageContainer.getPosY(), pageContainer.getWidth(), pageContainer.getHeight());

        float currentX = pageDim.x + padding;
        float currentY = pageDim.y + pageDim.w - padding - topBarHeight;

        float searchWidth = pageDim.z - padding * 2 - sortButtonWidth - elementSpacing;
        searchInput.setSize(searchWidth, topBarHeight);
        searchInput.setPosition(currentX, currentY);
        searchInput.getTextRenderer().setPosition(
                searchInput.getX() + 5f,
                searchInput.getY() + (searchInput.getHeight() / 2f) + (searchInput.getTextRenderer().getHeight(viewport) / 2f)
        );

        currentX += searchWidth + elementSpacing;
        sortButton.setPosition(currentX, currentY);

        float moduleGridWidth = pageDim.z - padding * 2;
        float moduleButtonWidth = (moduleGridWidth - elementSpacing * (modulesPerRow - 1)) / modulesPerRow;

        for (int i = 0; i < moduleButtons.size(); i++) {
            Button btn = moduleButtons.get(i);
            int col = i % modulesPerRow;
            int row = i / modulesPerRow;

            float buttonX = pageDim.x + padding + col * (moduleButtonWidth + elementSpacing);
            float buttonY = pageDim.y + pageDim.w - padding - topBarHeight - elementSpacing - (row + 1) * moduleButtonHeight - row * elementSpacing;

            btn.setSize(moduleButtonWidth, moduleButtonHeight);
            btn.setPosition(buttonX, buttonY);
        }

        float textY = pageDim.y + padding;
        leftClickText.setPosition(pageDim.x + padding, textY + leftClickText.getHeight(viewport) / 1.5f);
        rightClickText.setPosition(pageDim.x + pageDim.z - padding, textY + rightClickText.getHeight(viewport) / 1.5f);
    }

    @Override
    public void renderShape(ShapeRenderer shapeRenderer) {
        super.renderShape(shapeRenderer);

        searchInput.renderShape(shapeRenderer, viewport);
        sortButton.renderShape(shapeRenderer, viewport);
        for (Button btn : moduleButtons) {
            btn.renderShape(shapeRenderer, viewport);
        }
    }

    @Override
    public void renderText(SpriteBatch spriteBatch, GlyphLayout glyphLayout) {
        super.renderText(spriteBatch, glyphLayout);

        searchInput.renderText(spriteBatch, glyphLayout, viewport);
        sortButton.renderText(spriteBatch, glyphLayout, viewport);
        for (Button btn : moduleButtons) {
            btn.renderText(spriteBatch, glyphLayout, viewport);
        }
        leftClickText.render(spriteBatch, glyphLayout, viewport);
        rightClickText.render(spriteBatch, glyphLayout, viewport);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        searchInput.update(viewport);
        sortButton.update(viewport);
        for (Button btn : moduleButtons) {
            btn.update(viewport);
        }

    }

    @Override
    public void dispose() {
        super.dispose();
        moduleButtons.clear();
    }
}