package dev.neuxs.europa_client.modules.ui;

import finalforeach.cosmicreach.entities.GameEntity;

public final class VelocityTracker {
    private static volatile Snapshot snapshot = new Snapshot(false, 0f);

    private VelocityTracker() {
    }

    public static void recordLocalPlayerMovement(GameEntity entity, float deltaTime) {
        if (entity == null || entity.position == null || entity.lastPosition == null || deltaTime <= 0f) {
            snapshot = new Snapshot(false, 0f);
            return;
        }

        float deltaX = entity.position.x - entity.lastPosition.x;
        float deltaY = entity.position.y - entity.lastPosition.y;
        float deltaZ = entity.position.z - entity.lastPosition.z;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        snapshot = new Snapshot(true, distance / deltaTime);
    }

    public static void reset() {
        snapshot = new Snapshot(false, 0f);
    }

    public static Snapshot getSnapshot() {
        return snapshot;
    }

    public record Snapshot(boolean available, float blocksPerSecond) {
    }
}
