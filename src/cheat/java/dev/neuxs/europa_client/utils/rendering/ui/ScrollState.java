package dev.neuxs.europa_client.utils.rendering.ui;

import com.badlogic.gdx.math.MathUtils;

@SuppressWarnings("unused")
public class ScrollState {
    private float viewportX;
    private float viewportY;
    private float viewportWidth;
    private float viewportHeight;
    private float contentHeight;
    private float offset;
    private float scrollStep = 32f;

    public void setViewport(float x, float y, float width, float height) {
        viewportX = x;
        viewportY = y;
        viewportWidth = Math.max(0f, width);
        viewportHeight = Math.max(0f, height);
        clampOffset();
    }

    public void setContentHeight(float contentHeight) {
        this.contentHeight = Math.max(0f, contentHeight);
        clampOffset();
    }

    public void scroll(float amount) {
        setOffset(offset + amount * scrollStep);
    }

    public void setOffset(float offset) {
        this.offset = offset;
        clampOffset();
    }

    public boolean contains(float worldX, float worldY) {
        return worldX >= viewportX && worldX <= viewportX + viewportWidth
                && worldY >= viewportY && worldY <= viewportY + viewportHeight;
    }

    public boolean isFullyVisible(float y, float height) {
        return y >= viewportY && y + height <= viewportY + viewportHeight;
    }

    private void clampOffset() {
        offset = MathUtils.clamp(offset, 0f, getMaxOffset());
    }

    public float getMaxOffset() {
        return Math.max(0f, contentHeight - viewportHeight);
    }

    public float getOffset() {
        return offset;
    }

    public float getViewportX() {
        return viewportX;
    }

    public float getViewportY() {
        return viewportY;
    }

    public float getViewportWidth() {
        return viewportWidth;
    }

    public float getViewportHeight() {
        return viewportHeight;
    }

    public float getContentHeight() {
        return contentHeight;
    }

    public float getScrollStep() {
        return scrollStep;
    }

    public void setScrollStep(float scrollStep) {
        this.scrollStep = Math.max(1f, scrollStep);
    }
}
