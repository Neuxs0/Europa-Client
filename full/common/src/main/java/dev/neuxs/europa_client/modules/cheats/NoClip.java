package dev.neuxs.europa_client.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.utils.Chat;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;

public class NoClip extends Module {
    public int keyBind;

    private static boolean enabled;
    private static float speed = 1.0f;


    public NoClip(int keybind, boolean defaultEnabled) {
        super(keybind, defaultEnabled);
    }


    public static void setNoClip(Player player, boolean noClip) {
        player.getEntity().noClip = noClip;
        if (noClip) {
            player.getEntity().velocity.setZero();
        }
    }

    public static void setSpeed(float newSpeed) {
        speed = Math.max(0.1f, Math.min(newSpeed, 10.0f));
        Client.clientChat.addMessage(null, "No-clip speed set to " + speed);
    }

    public static float getSpeed() {
        return speed;
    }

    public static void toggle(boolean messaging) {
        enabled = !enabled;
        NoClip.setNoClip(InGame.getLocalPlayer(), enabled);
        if (enabled && messaging) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "No-clip enabled");
        else Client.clientChat.addMessage(null, Chat.getClientPrefix() + "No-clip disabled");
    }

    public static void enable() {
        enabled = true;
    }

    public static void disable() {
        enabled = false;
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
