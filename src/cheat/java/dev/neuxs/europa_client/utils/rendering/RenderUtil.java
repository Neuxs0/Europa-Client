package dev.neuxs.europa_client.utils.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unused")
public class RenderUtil implements Disposable {
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final GlyphLayout glyphLayout;
    private final List<Renderer> elements = new ArrayList<>();
    private final List<Renderer> toAdd = new ArrayList<>();
    private final List<Renderer> toRemove = new ArrayList<>();
    private final Comparator<Renderer> zIndexComparator = Comparator.comparingInt(Renderer::getZIndex);
    private boolean needsSync = false;
    public enum RenderType {
        NONE, SHAPE, SPRITE, SHAPE_SPRITE
    }

    public RenderUtil() {
        this.shapeRenderer = new ShapeRenderer();
        this.spriteBatch = new SpriteBatch();
        this.glyphLayout = new GlyphLayout();
    }

    public void renderAll(Matrix4 projectionMatrix, Viewport viewport) {
        elements.sort(zIndexComparator);

        RenderType currentBatchType = RenderType.NONE;

        for (Renderer element : elements) {
            RenderType requiredType = element.getRenderType();

            if (requiredType != currentBatchType && requiredType != RenderType.SHAPE_SPRITE) {
                endBatch(currentBatchType);
                currentBatchType = beginBatch(requiredType, element.getShapeType(), projectionMatrix);
            }

            if (requiredType == RenderType.SHAPE) {
                element.renderShape(viewport, shapeRenderer);
            } else if (requiredType == RenderType.SPRITE) {
                element.renderSprite(viewport, spriteBatch, glyphLayout);
            } else if (requiredType == RenderType.SHAPE_SPRITE) {
                if (RenderType.SHAPE != currentBatchType) {
                    endBatch(currentBatchType);
                    currentBatchType = beginBatch(RenderType.SHAPE, element.getShapeType(), projectionMatrix);
                }

                element.renderShape(viewport, shapeRenderer);
                endBatch(currentBatchType);
                currentBatchType = beginBatch(RenderType.SPRITE, element.getShapeType(), projectionMatrix);
                element.renderSprite(viewport, spriteBatch, glyphLayout);
            } else {
                Client.LOGGER.error("Cannot render {}: Unknown RenderType", element.getClass().getSimpleName());
            }
        }

        endBatch(currentBatchType);
    }

    private RenderType beginBatch(RenderType renderType, ShapeRenderer.ShapeType shapeType, Matrix4 projectionMatrix) {
        try {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            if (renderType == RenderType.SHAPE) {
                shapeRenderer.setProjectionMatrix(projectionMatrix);
                shapeRenderer.begin(shapeType);
                return RenderType.SHAPE;
            } else if (renderType == RenderType.SPRITE) {
                spriteBatch.setProjectionMatrix(projectionMatrix);
                spriteBatch.begin();
                return RenderType.SPRITE;
            }
        } catch (IllegalStateException e) {
            Client.LOGGER.error("Error beginning batch for type {}: {}", renderType, e.getMessage());
            if (shapeRenderer.isDrawing()) shapeRenderer.end();
            if (spriteBatch.isDrawing()) spriteBatch.end();
            return RenderType.NONE;
        }
        return RenderType.NONE;
    }

    private void endBatch(RenderType type) {
        try {
            if (type == RenderType.SHAPE && shapeRenderer.isDrawing()) {
                shapeRenderer.end();
            } else if (type == RenderType.SPRITE && spriteBatch.isDrawing()) {
                spriteBatch.end();
            }
        } catch (IllegalStateException e) {
            Client.LOGGER.error("Error ending batch for type {}: {}", type, e.getMessage());
        }
    }

    public void syncRenderers() {
        if (!needsSync) return;

        synchronized (toAdd) {
            elements.addAll(toAdd);
            toAdd.clear();
        }
        synchronized (toRemove) {
            elements.removeAll(toRemove);
            toRemove.clear();
        }
        needsSync = false;
    }

    @Override
    public void dispose() {
        endBatch(RenderType.SHAPE);
        endBatch(RenderType.SPRITE);
        shapeRenderer.dispose();
        spriteBatch.dispose();
        elements.clear();
    }

    public List<Renderer> getRenderers() {
        return elements;
    }

    public void addRenderer(Renderer renderer) {
        if (renderer != null) {
            synchronized (toAdd) {
                toAdd.add(renderer);
                needsSync = true;
            }
        }
    }
    public void removeRenderer(Renderer renderer) {
        if (renderer != null) {
            synchronized (toRemove) {
                toRemove.add(renderer);
                needsSync = true;
            }
        }
    }
}
