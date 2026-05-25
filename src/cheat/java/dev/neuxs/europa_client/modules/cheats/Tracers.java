package dev.neuxs.europa_client.modules.cheats;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.settings.Setting;
import dev.neuxs.europa_client.utils.Chat;
import finalforeach.cosmicreach.entities.GameEntity;
import finalforeach.cosmicreach.entities.PlayerPerspective;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.Zone;

@SuppressWarnings("unchecked")
public class Tracers extends Module {
    private static final float LINE_WIDTH = 2.0f;
    private static final Color TRACER_COLOR = new Color(1.0f, 0.22f, 0.14f, 0.95f);

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final BoundingBox bounds = new BoundingBox();
    private final Vector3 target = new Vector3();
    private final Vector3 projected = new Vector3();
    private final Vector3 tmp = new Vector3();
    private final Vector3 cameraRight = new Vector3();

    public Tracers(int keybind, boolean defaultEnabled) {
        super("Tracers", keybind, defaultEnabled);
        customSettings.put("targetUser", new Setting<>("targetUser", false)
                .withDisplayName("Target Self")
                .withDescription("Include your own player entity"));
        customSettings.put("targetEntities", new Setting<>("targetEntities", false)
                .withDisplayName("Target Entities")
                .withDescription("Include non-player entities"));
    }

    @Override
    public void enable(boolean messaging) {
        setEnabled(true);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Tracers enabled");
    }

    @Override
    public void disable(boolean messaging) {
        setEnabled(false);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Tracers disabled");
    }

    public void render(Zone zone, Camera worldCamera, Viewport worldViewport, Viewport uiViewport) {
        if (!isEnabled() || zone == null || worldCamera == null || uiViewport == null || uiViewport.getCamera() == null) {
            return;
        }

        Player localPlayer = InGame.getLocalPlayer();
        Array<Player> players = zone.getPlayers();
        if (players == null) {
            return;
        }

        uiViewport.apply();
        shapeRenderer.setProjectionMatrix(uiViewport.getCamera().combined);
        beginShapeRender();
        try {
            for (Player player : players) {
                GameEntity entity = getTargetEntity(player, localPlayer);
                if (entity == null) {
                    continue;
                }

                drawTracerTo(entity, worldCamera, worldViewport, uiViewport);
            }

            if (shouldTargetEntities()) {
                Array<GameEntity> entities = zone.getAllEntities();
                for (GameEntity entity : entities) {
                    if (!isTargetEntity(entity, players, localPlayer)) {
                        continue;
                    }

                    drawTracerTo(entity, worldCamera, worldViewport, uiViewport);
                }
            }
        } finally {
            endShapeRender();
        }
    }

    private GameEntity getTargetEntity(Player player, Player localPlayer) {
        if (player == null) {
            return null;
        }
        if (player == localPlayer && !shouldRenderLocalPlayer()) {
            return null;
        }

        GameEntity entity = player.getEntity();
        return entity == null || entity.isDead() ? null : entity;
    }

    private boolean isTargetEntity(GameEntity entity, Array<Player> players, Player localPlayer) {
        if (entity == null || entity.isDead()) {
            return false;
        }
        if (localPlayer != null && entity == localPlayer.getEntity()) {
            return false;
        }
        for (Player player : players) {
            if (player != null && player.getEntity() == entity) {
                return false;
            }
        }
        return true;
    }

    private void drawTracerTo(GameEntity entity, Camera worldCamera, Viewport worldViewport, Viewport uiViewport) {
        entity.getBoundingBox(bounds);
        bounds.getCenter(target);

        if (isInFrontOfCamera(worldCamera, target)) {
            projected.set(target);
            if (worldViewport != null) {
                worldViewport.project(projected);
            } else {
                worldCamera.project(projected);
            }
            projected.y = Gdx.graphics.getHeight() - projected.y;
            uiViewport.unproject(projected);
        } else if (!projectBehindCameraTarget(worldCamera, uiViewport)) {
            return;
        }

        float startX = uiViewport.getWorldWidth() * 0.5f;
        float startY = uiViewport.getWorldHeight() * 0.5f;
        shapeRenderer.line(startX, startY, projected.x, projected.y);
    }

    private boolean projectBehindCameraTarget(Camera camera, Viewport uiViewport) {
        float centerX = uiViewport.getWorldWidth() * 0.5f;
        float centerY = uiViewport.getWorldHeight() * 0.5f;
        float halfWidth = centerX;
        float halfHeight = centerY;

        tmp.set(target).sub(camera.position);
        cameraRight.set(camera.direction).crs(camera.up).nor();

        float directionX = tmp.dot(cameraRight);
        float directionY = tmp.dot(camera.up);
        if (Math.abs(directionX) < 0.0001f && Math.abs(directionY) < 0.0001f) {
            directionY = -1.0f;
        }

        float scale = Math.max(
                Math.abs(directionX) / Math.max(halfWidth, 0.0001f),
                Math.abs(directionY) / Math.max(halfHeight, 0.0001f)
        );
        if (scale <= 0.0f) {
            return false;
        }

        projected.set(centerX + directionX / scale, centerY + directionY / scale, 0.0f);
        return true;
    }

    private boolean isInFrontOfCamera(Camera camera, Vector3 point) {
        return tmp.set(point).sub(camera.position).dot(camera.direction) > 0.01f;
    }

    private void beginShapeRender() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glLineWidth(LINE_WIDTH);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(TRACER_COLOR);
    }

    private void endShapeRender() {
        if (shapeRenderer.isDrawing()) {
            shapeRenderer.end();
        }
        Gdx.gl.glLineWidth(1.0f);
    }

    private boolean shouldTargetUser() {
        Setting<Boolean> setting = (Setting<Boolean>) customSettings.get("targetUser");
        return setting != null && setting.getValue();
    }

    private boolean shouldTargetEntities() {
        Setting<Boolean> setting = (Setting<Boolean>) customSettings.get("targetEntities");
        return setting != null && setting.getValue();
    }

    private boolean shouldRenderLocalPlayer() {
        return shouldTargetUser()
                && ((Modules.freecam != null && Modules.freecam.isEnabled())
                || !PlayerPerspective.is(PlayerPerspective.FIRST_PERSON));
    }
}
