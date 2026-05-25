package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import dev.neuxs.europa_client.utils.ColorUtils;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;
import dev.neuxs.europa_client.utils.rendering.RenderUtil;

@SuppressWarnings("unused")
public class Scrollbar {
    private static final float MIN_THUMB_HEIGHT = 24f;

    private final BoxRenderer track = new BoxRenderer();
    private final BoxRenderer thumb = new BoxRenderer();
    private boolean renderersAdded;
    private boolean dragging;
    private float dragOffsetY;
    private float x;
    private float y;
    private float width;
    private float height;
    private float thumbY;
    private float thumbHeight;

    public Scrollbar() {
        track.setFillColor(ColorUtils.color(20, 20, 20, 120));
        track.setBorderRadius(3f);
        track.setZIndex(25);

        thumb.setFillColor(ColorUtils.color(190, 190, 190, 210));
        thumb.setBorderRadius(3f);
        thumb.setZIndex(26);
    }

    public void layout(float x, float y, float width, float height, ScrollState scrollState) {
        this.x = x;
        this.y = y;
        this.width = Math.max(0f, width);
        this.height = Math.max(0f, height);

        track.setPos(this.x, this.y);
        track.setSize(this.width, this.height);

        float contentHeight = Math.max(scrollState.getContentHeight(), scrollState.getViewportHeight());
        float visibleRatio = contentHeight <= 0f ? 1f : scrollState.getViewportHeight() / contentHeight;
        thumbHeight = MathUtils.clamp(this.height * visibleRatio, Math.min(MIN_THUMB_HEIGHT, this.height), this.height);

        float travel = Math.max(0f, this.height - thumbHeight);
        float progress = scrollState.getMaxOffset() <= 0f ? 0f : scrollState.getOffset() / scrollState.getMaxOffset();
        thumbY = this.y + travel * (1f - progress);

        thumb.setPos(this.x, thumbY);
        thumb.setSize(this.width, thumbHeight);
    }

    public void syncRenderers(RenderUtil renderUtil, ScrollState scrollState) {
        boolean visible = scrollState.getMaxOffset() > 0f && width > 0f && height > 0f;
        if (visible && !renderersAdded) {
            renderUtil.addRenderer(track);
            renderUtil.addRenderer(thumb);
            renderersAdded = true;
        } else if (!visible && renderersAdded) {
            removeRenderers(renderUtil);
        }
    }

    public void removeRenderers(RenderUtil renderUtil) {
        renderUtil.removeRenderer(track);
        renderUtil.removeRenderer(thumb);
        renderersAdded = false;
        dragging = false;
    }

    public boolean handleTouchDown(float worldX, float worldY, int button, ScrollState scrollState) {
        if (button != Input.Buttons.LEFT || scrollState.getMaxOffset() <= 0f || !contains(worldX, worldY)) {
            return false;
        }

        if (containsThumb(worldX, worldY)) {
            dragging = true;
            dragOffsetY = worldY - thumbY;
        } else {
            dragOffsetY = thumbHeight / 2f;
            updateOffsetFromThumbCenter(worldY, scrollState);
            dragging = true;
        }
        return true;
    }

    public boolean handleTouchDragged(float worldX, float worldY, ScrollState scrollState) {
        if (!dragging) {
            return false;
        }

        updateOffsetFromThumbY(worldY - dragOffsetY, scrollState);
        return true;
    }

    public boolean handleTouchUp() {
        boolean wasDragging = dragging;
        dragging = false;
        return wasDragging;
    }

    private void updateOffsetFromThumbCenter(float worldY, ScrollState scrollState) {
        updateOffsetFromThumbY(worldY - thumbHeight / 2f, scrollState);
    }

    private void updateOffsetFromThumbY(float nextThumbY, ScrollState scrollState) {
        float travel = Math.max(0f, height - thumbHeight);
        if (travel <= 0f) {
            scrollState.setOffset(0f);
            return;
        }

        float clampedThumbY = MathUtils.clamp(nextThumbY, y, y + travel);
        float progress = 1f - ((clampedThumbY - y) / travel);
        scrollState.setOffset(progress * scrollState.getMaxOffset());
    }

    private boolean contains(float worldX, float worldY) {
        return worldX >= x && worldX <= x + width && worldY >= y && worldY <= y + height;
    }

    private boolean containsThumb(float worldX, float worldY) {
        return worldX >= x && worldX <= x + width && worldY >= thumbY && worldY <= thumbY + thumbHeight;
    }
}
