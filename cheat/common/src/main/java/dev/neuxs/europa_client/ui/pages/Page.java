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

    public boolean keyDown(int keycode) { return false; }
    public boolean keyUp(int keycode) { return false; }
    public boolean keyTyped(char character) { return false; }
    public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    public boolean mouseMoved(int screenX, int screenY) { return false; }
    public boolean scrolled(float amountX, float amountY) { return false; }
}
