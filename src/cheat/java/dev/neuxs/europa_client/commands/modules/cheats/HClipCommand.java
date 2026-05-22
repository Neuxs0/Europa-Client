package dev.neuxs.europa_client.commands.modules.cheats;

import com.badlogic.gdx.math.Vector3;
import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.utils.Chat;
import finalforeach.cosmicreach.entities.GameEntity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;

public class HClipCommand extends ClientCommand {

    @Override
    public void run() {
        if (args.length != 2) {
            Client.clientChat.addMessage(null, "Usage: " + commandPrefix() + "hclip <distance>");
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
        Vector3 viewDirection = entity.viewDirection;
        Vector3 position = entity.getPosition();
        float horizontalLength = (float) Math.sqrt(viewDirection.x * viewDirection.x + viewDirection.z * viewDirection.z);

        if (horizontalLength == 0.0F) {
            Client.clientChat.addMessage(null,
                    Chat.getClientPrefix() + "Could not determine horizontal view direction.");
            return;
        }

        float offsetX = viewDirection.x / horizontalLength * distance;
        float offsetZ = viewDirection.z / horizontalLength * distance;

        player.setPosition(position.x + offsetX, position.y, position.z + offsetZ);
        entity.velocity.setZero();
        Client.clientChat.addMessage(null, Chat.getClientPrefix() + "HClipped " + distance + " blocks.");
    }

    @Override
    public String getDescription() {
        return "Clips horizontally by the provided distance.";
    }
}
