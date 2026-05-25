package dev.neuxs.europa_client.modules.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import finalforeach.cosmicreach.BlockGame;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.settings.Setting;
import dev.neuxs.europa_client.utils.Chat;
import finalforeach.cosmicreach.audio.SoundManager;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.GameEntity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.settings.ControlSettings;
import finalforeach.cosmicreach.settings.Controls;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.world.Zone;

@SuppressWarnings("unchecked")
public class Freecam extends Module {
    private static final float CAMERA_HALF_SIZE = 0.2f;
    private static final float MAX_COLLISION_STEP = 0.25f;
    private static final float SPRINT_SPEED_MULTIPLIER = 1.2f;

    private final Vector3 position = new Vector3();
    private final Vector3 direction = new Vector3(0.0f, 0.0f, -1.0f);
    private final Vector3 movement = new Vector3();
    private final Vector3 forward = new Vector3();
    private final Vector3 right = new Vector3();
    private final Vector3 candidatePosition = new Vector3();
    private final Vector3 lookAxis = new Vector3();
    private final BoundingBox cameraBounds = new BoundingBox();
    private final Array<BoundingBox> blockBounds = new Array<>(BoundingBox.class);

    private boolean initialized;
    private boolean couldMoveLastFrame;
    private float startMouseX;
    private float startMouseY;

    public Freecam(int keybind, boolean defaultEnabled) {
        super("Freecam", keybind, defaultEnabled);
        customSettings.put("speed", new Setting<>("speed", 8.0f, value -> value >= 1.0f)
                .withRange(1.0f, 30.0f)
                .withDisplayName("Speed"));
        customSettings.put("horizontalMovement", new Setting<>("horizontalMovement", true)
                .withDisplayName("Horizontal Movement")
                .withDescription("Move forward and backward along the horizontal plane instead of camera pitch"));
        customSettings.put("playerInteraction", new Setting<>("playerInteraction", true)
                .withDisplayName("Player Interaction")
                .withDescription("Use the player's view for block and entity interactions while freecam is enabled"));
        customSettings.put("disableOnDamage", new Setting<>("disableOnDamage", true)
                .withDisplayName("Disable On Damage")
                .withDescription("Disable freecam when the local player takes damage"));
    }

    @Override
    public void enable(boolean messaging) {
        setEnabled(true);
        initialized = false;
        initialize(InGame.getLocalPlayer());
        if (messaging) {
            Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Freecam enabled");
        }
    }

    @Override
    public void disable(boolean messaging) {
        setEnabled(false);
        initialized = false;
        couldMoveLastFrame = false;
        Player player = InGame.getLocalPlayer();
        if (player != null && player.getEntity() != null) {
            player.getEntity().gravityModifier = 1.0f;
        }
        if (messaging) {
            Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Freecam disabled");
        }
    }

    public void update(Player player, Zone zone, float deltaTime) {
        if (!isEnabled() || player == null || player.getEntity() == null || zone == null) {
            return;
        }

        initialize(player);
        freezePlayer(player.getEntity());
        updateLook();

        movement.setZero();
        float forwardInput = Controls.forwardPressed() - Controls.backwardPressed();
        float strafeInput = Controls.rightPressed() - Controls.leftPressed();
        float verticalInput = 0.0f;
        if (Controls.jumpPressed()) {
            verticalInput += 1.0f;
        }
        if (Controls.crouchPressed()) {
            verticalInput -= 1.0f;
        }

        forward.set(direction);
        if (usesHorizontalMovement()) {
            forward.y = 0.0f;
        }
        if (!forward.isZero()) {
            forward.nor();
        }
        right.set(forward).crs(0.0f, 1.0f, 0.0f);
        if (!right.isZero()) {
            right.nor();
        }

        movement.mulAdd(forward, forwardInput)
                .mulAdd(right, strafeInput)
                .add(0.0f, verticalInput, 0.0f);

        if (Controls.sprintPressed()) {
            player.isSprinting = true;
        }
        if (movement.isZero()) {
            player.isSprinting = false;
            return;
        }

        movement.nor().scl(getMovementSpeed(player) * deltaTime);
        if (canPhaseThroughBlocks()) {
            position.add(movement);
        } else {
            moveWithCollision(zone, movement.x, movement.y, movement.z);
        }
    }

    public boolean applyCamera(PerspectiveCamera camera, Player player) {
        if (!isEnabled() || camera == null || player == null || player.getEntity() == null) {
            return false;
        }

        initialize(player);
        camera.position.set(position);
        camera.direction.set(direction);
        if (camera.direction.isZero()) {
            camera.direction.set(0.0f, 0.0f, -1.0f);
        }
        camera.up.set(Vector3.Y);
        camera.update();
        SoundManager.INSTANCE.updateCamera(camera);
        return true;
    }

    public boolean shouldShowHand() {
        return !isEnabled();
    }

    public void setSpeed(float newSpeed) {
        Setting<Float> speedSetting = (Setting<Float>) customSettings.get("speed");
        speedSetting.setValue(newSpeed);
        Client.clientChat.addMessage(null, "Freecam speed set to " + speedSetting.getValue());
    }

    public float getSpeed() {
        Setting<Float> speedSetting = (Setting<Float>) customSettings.get("speed");
        return speedSetting.getValue();
    }

    private float getMovementSpeed(Player player) {
        float speed = getSpeed();
        if (player.isSprinting) {
            speed *= SPRINT_SPEED_MULTIPLIER;
        }
        return speed;
    }

    public void setHorizontalMovement(boolean horizontalMovement) {
        Setting<Boolean> horizontalMovementSetting = (Setting<Boolean>) customSettings.get("horizontalMovement");
        horizontalMovementSetting.setValue(horizontalMovement);
        Client.clientChat.addMessage(null, "Freecam horizontal movement set to " + horizontalMovementSetting.getValue());
    }

    public boolean usesHorizontalMovement() {
        Setting<Boolean> horizontalMovementSetting = (Setting<Boolean>) customSettings.get("horizontalMovement");
        return horizontalMovementSetting.getValue();
    }

    public void setPlayerInteraction(boolean playerInteraction) {
        Setting<Boolean> playerInteractionSetting = (Setting<Boolean>) customSettings.get("playerInteraction");
        playerInteractionSetting.setValue(playerInteraction);
        Client.clientChat.addMessage(null, "Freecam player interaction set to " + playerInteractionSetting.getValue());
    }

    public boolean usesPlayerInteraction() {
        Setting<Boolean> playerInteractionSetting = (Setting<Boolean>) customSettings.get("playerInteraction");
        return playerInteractionSetting == null || playerInteractionSetting.getValue();
    }

    public void setDisableOnDamage(boolean disableOnDamage) {
        Setting<Boolean> disableOnDamageSetting = (Setting<Boolean>) customSettings.get("disableOnDamage");
        disableOnDamageSetting.setValue(disableOnDamage);
        Client.clientChat.addMessage(null, "Freecam disable on damage set to " + disableOnDamageSetting.getValue());
    }

    public boolean disablesOnDamage() {
        Setting<Boolean> disableOnDamageSetting = (Setting<Boolean>) customSettings.get("disableOnDamage");
        return disableOnDamageSetting == null || disableOnDamageSetting.getValue();
    }

    public void onLocalPlayerDamaged(float amount) {
        if (amount <= 0.0f || !isEnabled() || !disablesOnDamage()) {
            return;
        }

        disable(shouldNotify(true));
    }

    private boolean canPhaseThroughBlocks() {
        return "Cheat".equalsIgnoreCase(Client.getClientType());
    }

    private void initialize(Player player) {
        if (initialized || player == null || player.getEntity() == null) {
            return;
        }

        GameEntity entity = player.getEntity();
        position.set(entity.position).add(entity.viewPositionOffset);
        direction.set(entity.viewDirection);
        if (direction.isZero()) {
            direction.set(0.0f, 0.0f, -1.0f);
        }
        direction.nor();
        initialized = true;
    }

    private void updateLook() {
        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();

        boolean shouldMoveCamera = BlockGame.isFocused && !UI.uiNeedMouse && GameState.currentGameState == GameState.IN_GAME;
        if (!shouldMoveCamera) {
            startMouseX = screenX;
            startMouseY = screenY;
            Gdx.input.setCursorCatched(false);
            couldMoveLastFrame = false;
            return;
        }

        if (GameState.currentGameState.firstFrame || !couldMoveLastFrame) {
            startMouseX = screenX;
            startMouseY = screenY;
        }
        couldMoveLastFrame = Gdx.input.isCursorCatched();

        if (Gdx.input.isCursorCatched()) {
            float ySign = ControlSettings.invertedMouse.getValue() ? -1.0f : 1.0f;
            float screenDim = Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            float mouseSensitivity = ControlSettings.mouseSensitivity.getValue();
            float deltaX = mouseSensitivity * (screenX - startMouseX) / screenDim;
            float deltaY = ySign * mouseSensitivity * (startMouseY - screenY) / screenDim;

            if (Float.isNaN(deltaX)) {
                deltaX = 0.0f;
            }
            if (Float.isNaN(deltaY)) {
                deltaY = 0.0f;
            }

            float invControllerSensitivityX = 50.0f;
            float invControllerSensitivityY = 100.0f;
            float controllerX = Controls.getRightXAxis() / invControllerSensitivityX;
            float controllerY = -Controls.getRightYAxis() / invControllerSensitivityY;
            if (Math.abs(controllerX) > 0.015f / invControllerSensitivityX) {
                deltaX += controllerX * Gdx.graphics.getDeltaTime() * 60.0f;
            }
            if (Math.abs(controllerY) > 0.015f / invControllerSensitivityY) {
                deltaY += controllerY * Gdx.graphics.getDeltaTime() * 60.0f;
            }

            lookAxis.set(direction).crs(0.0f, 1.0f, 0.0f);
            lookAxis.y = 0.0f;
            if (!lookAxis.isZero()) {
                lookAxis.nor();
            }

            float oldX = direction.x;
            float oldY = direction.y;
            float oldZ = direction.z;
            direction.rotate(deltaY * 360.0f, lookAxis.x, lookAxis.y, lookAxis.z);

            if (Math.signum(oldX) != Math.signum(direction.x) || Math.signum(oldZ) != Math.signum(direction.z)) {
                if (Math.abs(direction.y) < Math.abs(oldY)) {
                    direction.x = oldX;
                    direction.y = oldY;
                    direction.z = oldZ;
                } else {
                    direction.x = Math.abs(direction.x) * Math.signum(oldX);
                    direction.z = Math.abs(direction.z) * Math.signum(oldZ);
                }
            }

            direction.rotate(deltaX * -360.0f, 0.0f, 1.0f, 0.0f);
            direction.nor();
        }

        Gdx.input.setCursorCatched(true);
        startMouseX = screenX;
        startMouseY = screenY;
    }

    private void freezePlayer(GameEntity entity) {
        entity.gravityModifier = 0.0f;
        entity.velocity.setZero();
        entity.onceVelocity.setZero();
        entity.accelerationSetZero();
    }

    private void moveWithCollision(Zone zone, float x, float y, float z) {
        moveAxisWithCollision(zone, x, 0.0f, 0.0f);
        moveAxisWithCollision(zone, 0.0f, y, 0.0f);
        moveAxisWithCollision(zone, 0.0f, 0.0f, z);
    }

    private void moveAxisWithCollision(Zone zone, float x, float y, float z) {
        float distance = Vector3.len(x, y, z);
        if (distance <= 0.0f) {
            return;
        }

        int steps = Math.max(1, (int) Math.ceil(distance / MAX_COLLISION_STEP));
        float stepX = x / steps;
        float stepY = y / steps;
        float stepZ = z / steps;
        for (int i = 0; i < steps; i++) {
            candidatePosition.set(position).add(stepX, stepY, stepZ);
            if (intersectsSolidBlock(zone, candidatePosition)) {
                return;
            }
            position.set(candidatePosition);
        }
    }

    private boolean intersectsSolidBlock(Zone zone, Vector3 cameraPosition) {
        cameraBounds.min.set(cameraPosition).sub(CAMERA_HALF_SIZE, CAMERA_HALF_SIZE, CAMERA_HALF_SIZE);
        cameraBounds.max.set(cameraPosition).add(CAMERA_HALF_SIZE, CAMERA_HALF_SIZE, CAMERA_HALF_SIZE);
        cameraBounds.update();

        int minX = (int) Math.floor(cameraBounds.min.x);
        int minY = (int) Math.floor(cameraBounds.min.y);
        int minZ = (int) Math.floor(cameraBounds.min.z);
        int maxX = (int) Math.floor(cameraBounds.max.x);
        int maxY = (int) Math.floor(cameraBounds.max.y);
        int maxZ = (int) Math.floor(cameraBounds.max.z);

        for (int bx = minX; bx <= maxX; bx++) {
            for (int by = minY; by <= maxY; by++) {
                for (int bz = minZ; bz <= maxZ; bz++) {
                    BlockState blockState = zone.getBlockState(bx, by, bz);
                    if (blockState == null || blockState.walkThrough) {
                        continue;
                    }

                    blockState.getAllBoundingBoxes(blockBounds, bx, by, bz);
                    for (BoundingBox blockBound : blockBounds) {
                        if (blockBound.intersects(cameraBounds)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
