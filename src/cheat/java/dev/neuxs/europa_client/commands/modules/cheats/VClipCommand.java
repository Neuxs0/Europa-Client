package dev.neuxs.europa_client.commands.modules.cheats;

import com.badlogic.gdx.math.Vector3;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.utils.Chat;
import finalforeach.cosmicreach.entities.GameEntity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;

public class VClipCommand extends ClientCommand {

    @Override
    public void run() {
        if (args.length != 2) {
            Client.clientChat.addMessage(null, "Usage: " + commandPrefix() + "vclip <distance>");
            return;
        }

        float distance;
        try {
            distance = parseFloatArg(args[1]);
        } catch (NumberFormatException ex) {
            Client.clientChat.addMessage(null,
                    Chat.getClientPrefix() + "Invalid number format. Use a valid float value.");
            return;
        }

        if (Float.isNaN(distance) || Float.isInfinite(distance)) {
            Client.clientChat.addMessage(null,
                    Chat.getClientPrefix() + "Invalid distance value, please provide a finite number.");
            return;
        }

        Player player = InGame.getLocalPlayer();
        if (player == null || player.getEntity() == null) {
            Client.clientChat.addMessage(null, Chat.getClientPrefix() + "No local player found.");
            return;
        }

        GameEntity entity = player.getEntity();
        Vector3 position = entity.getPosition();

        player.setPosition(position.x, position.y + distance, position.z);
        entity.velocity.setZero();
        Client.clientChat.addMessage(null, Chat.getClientPrefix() + "VClipped " + distance + " blocks.");
    }

    @Override
    public String getDescription() {
        return "Clips vertically by the provided distance.";
    }
}
