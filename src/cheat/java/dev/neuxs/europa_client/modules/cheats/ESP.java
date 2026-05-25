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
public class ESP extends Module {
    private static final float LINE_WIDTH = 2.0f;
    private static final float BOUNDS_PADDING = 0.01f;
    private static final Color BOX_COLOR = new Color(0.08f, 1.0f, 0.42f, 0.95f);

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final BoundingBox bounds = new BoundingBox();
    private final Vector3[] corners = new Vector3[] {
            new Vector3(), new Vector3(), new Vector3(), new Vector3(),
            new Vector3(), new Vector3(), new Vector3(), new Vector3()
    };
    private final Vector3 projected = new Vector3();
    private final Vector3 tmp = new Vector3();

    public ESP(int keybind, boolean defaultEnabled) {
        super("ESP", keybind, defaultEnabled);
        customSettings.put("threeDimensional", new Setting<>("threeDimensional", false)
                .withDisplayName("3D Boxes")
                .withDescription("Draw the target hitbox in world space and ignore block depth"));
        customSettings.put("targetUser", new Setting<>("targetUser", false)
                .withDisplayName("Target Self")
                .withDescription("Include your own player entity"));
    }

    @Override
    public void enable(boolean messaging) {
        setEnabled(true);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "ESP enabled");
    }

    @Override
    public void disable(boolean messaging) {
        setEnabled(false);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "ESP disabled");
    }

    public void render(Zone zone, Camera worldCamera, Viewport worldViewport, Viewport uiViewport) {
        if (!isEnabled() || zone == null || worldCamera == null) {
            return;
        }

        Player localPlayer = InGame.getLocalPlayer();
        Array<Player> players = zone.getPlayers();
        if (players == null || players.size == 0) {
            return;
        }

        if (shouldRenderThreeDimensional()) {
            renderThreeDimensional(players, localPlayer, worldCamera);
        } else if (uiViewport != null && uiViewport.getCamera() != null) {
            renderTwoDimensional(players, localPlayer, worldCamera, worldViewport, uiViewport);
        }
    }

    private void renderTwoDimensional(
            Array<Player> players,
            Player localPlayer,
            Camera worldCamera,
            Viewport worldViewport,
            Viewport uiViewport
    ) {
        uiViewport.apply();
        shapeRenderer.setProjectionMatrix(uiViewport.getCamera().combined);
        beginShapeRender(ShapeRenderer.ShapeType.Line);
        try {
            for (Player player : players) {
                GameEntity entity = getTargetEntity(player, localPlayer);
                if (entity == null) {
                    continue;
                }
                updateExpandedBounds(entity);
                if (!worldCamera.frustum.boundsInFrustum(bounds)) {
                    continue;
                }

                drawProjectedBox(worldCamera, worldViewport, uiViewport);
            }
        } finally {
            endShapeRender();
        }
    }

    private void renderThreeDimensional(Array<Player> players, Player localPlayer, Camera worldCamera) {
        worldCamera.update();
        shapeRenderer.setProjectionMatrix(worldCamera.combined);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(false);
        beginShapeRender(ShapeRenderer.ShapeType.Line);
        try {
            for (Player player : players) {
                GameEntity entity = getTargetEntity(player, localPlayer);
                if (entity == null) {
                    continue;
                }
                updateExpandedBounds(entity);
                if (!worldCamera.frustum.boundsInFrustum(bounds)) {
                    continue;
                }

                setCorners(bounds);
                drawWorldBox();
            }
        } finally {
            endShapeRender();
            Gdx.gl.glDepthMask(true);
            Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
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

    private void updateExpandedBounds(GameEntity entity) {
        entity.getBoundingBox(bounds);
        bounds.min.sub(BOUNDS_PADDING);
        bounds.max.add(BOUNDS_PADDING);
        bounds.update();
    }

    private void drawProjectedBox(Camera worldCamera, Viewport worldViewport, Viewport uiViewport) {
        setCorners(bounds);

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        boolean hasProjectedCorner = false;

        for (Vector3 corner : corners) {
            if (!isInFrontOfCamera(worldCamera, corner)) {
                continue;
            }

            projected.set(corner);
            if (worldViewport != null) {
                worldViewport.project(projected);
            } else {
                worldCamera.project(projected);
            }
            projected.y = Gdx.graphics.getHeight() - projected.y;
            uiViewport.unproject(projected);

            minX = Math.min(minX, projected.x);
            minY = Math.min(minY, projected.y);
            maxX = Math.max(maxX, projected.x);
            maxY = Math.max(maxY, projected.y);
            hasProjectedCorner = true;
        }

        if (!hasProjectedCorner || maxX <= minX || maxY <= minY) {
            return;
        }

        shapeRenderer.rect(minX, minY, maxX - minX, maxY - minY);
    }

    private boolean isInFrontOfCamera(Camera camera, Vector3 point) {
        return tmp.set(point).sub(camera.position).dot(camera.direction) > 0.01f;
    }

    private void drawWorldBox() {
        drawEdge(0, 1);
        drawEdge(1, 3);
        drawEdge(3, 2);
        drawEdge(2, 0);
        drawEdge(4, 5);
        drawEdge(5, 7);
        drawEdge(7, 6);
        drawEdge(6, 4);
        drawEdge(0, 4);
        drawEdge(1, 5);
        drawEdge(2, 6);
        drawEdge(3, 7);
    }

    private void drawEdge(int a, int b) {
        Vector3 start = corners[a];
        Vector3 end = corners[b];
        shapeRenderer.line(start.x, start.y, start.z, end.x, end.y, end.z);
    }

    private void setCorners(BoundingBox box) {
        corners[0].set(box.min.x, box.min.y, box.min.z);
        corners[1].set(box.max.x, box.min.y, box.min.z);
        corners[2].set(box.min.x, box.max.y, box.min.z);
        corners[3].set(box.max.x, box.max.y, box.min.z);
        corners[4].set(box.min.x, box.min.y, box.max.z);
        corners[5].set(box.max.x, box.min.y, box.max.z);
        corners[6].set(box.min.x, box.max.y, box.max.z);
        corners[7].set(box.max.x, box.max.y, box.max.z);
    }

    private void beginShapeRender(ShapeRenderer.ShapeType shapeType) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glLineWidth(LINE_WIDTH);
        shapeRenderer.begin(shapeType);
        shapeRenderer.setColor(BOX_COLOR);
    }

    private void endShapeRender() {
        if (shapeRenderer.isDrawing()) {
            shapeRenderer.end();
        }
        Gdx.gl.glLineWidth(1.0f);
    }

    private boolean shouldRenderThreeDimensional() {
        Setting<Boolean> setting = (Setting<Boolean>) customSettings.get("threeDimensional");
        return setting != null && setting.getValue();
    }

    private boolean shouldTargetUser() {
        Setting<Boolean> setting = (Setting<Boolean>) customSettings.get("targetUser");
        return setting != null && setting.getValue();
    }

    private boolean shouldRenderLocalPlayer() {
        return shouldTargetUser()
                && ((Modules.freecam != null && Modules.freecam.isEnabled())
                || !PlayerPerspective.is(PlayerPerspective.FIRST_PERSON));
    }
}
