package dev.neuxs.europa_client.modules.cheats;

import dev.neuxs.europa_client.Client;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;

public class NoClip {
    public int keyBind;
    public static boolean enabled = false;
    // New static field for controlling no-clip movement speed.
    private static float speed = 1.0f; // default no-clip speed

    public NoClip(int keybind) {
        this.keyBind = keybind;
    }

    public static void setNoClip(Player player, boolean noClip) {
        player.getEntity().noClip = noClip;
        if (noClip) {
            player.getEntity().velocity.setZero();
        }
    }

    public static void toggleNoClip() {
        NoClip.setNoClip(InGame.getLocalPlayer(), !enabled);
        if (enabled) Client.LOGGER.info("Disabled NoClip");
        else Client.LOGGER.info("Enabled NoClip");
        enabled = !enabled;
    }

    /**
     * Sets the no-clip movement speed.
     * Values are clamped between 0.1 and 10.0.
     *
     * @param newSpeed the new speed value.
     */
    public static void setSpeed(float newSpeed) {
        // Clamp the speed value to be within 0.1 and 10.0.
        newSpeed = Math.max(0.1f, Math.min(newSpeed, 10.0f));
        speed = newSpeed;
        Client.LOGGER.info("No-clip speed set to " + speed);
    }

    /**
     * Returns the current no-clip speed.
     *
     * @return the current speed
     */
    public static float getSpeed() {
        return speed;
    }
}
