package dev.neuxs.europa_client.modules.cheats;

import com.badlogic.gdx.math.Vector3;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.ControlsMovementInput;
import dev.neuxs.europa_client.modules.MovementInput;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.settings.Setting;
import dev.neuxs.europa_client.utils.Chat;
import finalforeach.cosmicreach.entities.GameEntity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;

@SuppressWarnings("unchecked")
public class Fly extends Module {
    private static final Vector3 FORWARD = new Vector3();
    private static final Vector3 RIGHT = new Vector3();
    private static final Vector3 MOVEMENT = new Vector3();
    private static final Vector3 VELOCITY = new Vector3();
    private MovementInput movementInput;

    public Fly(int keybind, boolean defaultEnabled) {
        super("Fly", keybind, defaultEnabled);
        this.movementInput = new ControlsMovementInput();
        customSettings.put("speed", new Setting<>("speed", 8.0f, value -> value >= 1.0f)
                .withRange(1.0f, 20.0f));
    }

    public void setMovementInput(MovementInput movementInput) {
        this.movementInput = movementInput == null ? new ControlsMovementInput() : movementInput;
    }

    @Override
    public void enable(boolean messaging) {
        setEnabled(true);
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Fly enabled");
    }

    @Override
    public void disable(boolean messaging) {
        setEnabled(false);
        Player player = getLocalPlayerSafely();
        if (player != null && player.getEntity() != null) {
            player.getEntity().gravityModifier = 1.0f;
        }
        if (messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Fly disabled");
    }

    public void apply(Player player) {
        if (!isEnabled() || player == null || player.getEntity() == null) {
            return;
        }

        apply(player.getEntity());
    }

    public void apply(GameEntity entity) {
        if (!isEnabled() || entity == null) {
            return;
        }

        suppressVanillaVerticalPhysics(entity);

        entity.velocity.set(getVelocity(entity));
    }

    public Vector3 getVelocity(GameEntity entity) {
        return getMotion(entity, getSpeed());
    }

    public float getPositionDeltaX(GameEntity entity, float deltaTime) {
        return getMotion(entity, getSpeed() * deltaTime).x;
    }

    public float getPositionDeltaY(GameEntity entity, float deltaTime) {
        return getMotion(entity, getSpeed() * deltaTime).y;
    }

    public float getPositionDeltaZ(GameEntity entity, float deltaTime) {
        return getMotion(entity, getSpeed() * deltaTime).z;
    }

    public boolean isActiveFor(GameEntity entity) {
        Player localPlayer = InGame.getLocalPlayer();
        return isEnabled() && localPlayer != null && localPlayer.getEntity() == entity;
    }

    private Vector3 getMotion(GameEntity entity, float speed) {
        float forwardInput = movementInput.forward() - movementInput.backward();
        float strafeInput = movementInput.right() - movementInput.left();
        float verticalInput = 0.0f;
        if (movementInput.jump()) {
            verticalInput += 1.0f;
        }
        if (movementInput.crouch()) {
            verticalInput -= 1.0f;
        }

        FORWARD.set(entity.viewDirection);
        FORWARD.y = 0.0f;
        if (!FORWARD.isZero()) {
            FORWARD.nor();
        }

        RIGHT.set(FORWARD).crs(0.0f, 1.0f, 0.0f).nor();
        MOVEMENT.setZero()
                .mulAdd(FORWARD, forwardInput)
                .mulAdd(RIGHT, strafeInput);

        if (!MOVEMENT.isZero()) {
            MOVEMENT.nor().scl(speed);
        }

        return VELOCITY.set(MOVEMENT.x, verticalInput * speed, MOVEMENT.z);
    }

    public void prepare(Player player) {
        if (!isEnabled() || player == null || player.getEntity() == null) {
            return;
        }

        prepare(player.getEntity());
    }

    public void prepare(GameEntity entity) {
        if (!isEnabled() || entity == null) {
            return;
        }

        suppressVanillaVerticalPhysics(entity);
    }

    private void suppressVanillaVerticalPhysics(GameEntity entity) {
        entity.gravityModifier = 0.0f;
        entity.onceVelocity.setZero();
        entity.accelerationSetZero();
    }

    public float getSpeed() {
        Setting<Float> speedSetting = (Setting<Float>) customSettings.get("speed");
        return speedSetting.getValue();
    }

    public void setSpeed(float newSpeed) {
        Setting<Float> speedSetting = (Setting<Float>) customSettings.get("speed");
        speedSetting.setValue(newSpeed);
        Client.clientChat.addMessage(null, "Fly speed set to " + speedSetting.getValue());
    }

    private Player getLocalPlayerSafely() {
        try {
            return InGame.getLocalPlayer();
        } catch (RuntimeException | LinkageError e) {
            return null;
        }
    }
}
