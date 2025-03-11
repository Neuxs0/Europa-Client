package dev.neuxs.europa_client.commands.modules;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.cheats.NoClip;
import dev.neuxs.europa_client.utils.Chat;
import finalforeach.cosmicreach.chat.IChat;

public class SetNoClipSpeedCommand extends ClientCommand {

    @Override
    public void run() {
        if (args.length < 2 || args[1].trim().isEmpty()) {
            Client.clientChat.addMessage(null, "Usage: #setNoClipSpeed <speed>");
            return;
        }

        try {
            float speedValue = Float.parseFloat(args[1].trim());

            if (Float.isNaN(speedValue) || Float.isInfinite(speedValue)) {
                Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Invalid speed value, please provide a finite number.");
                return;
            }

            NoClip.setSpeed(speedValue);
        } catch (NumberFormatException ex) {
            Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Invalid number format. Use a valid float value.");
        }
    }

    @Override
    public String getDescription() {
        return "Sets the speed for no-clip. 1.0 is default. Acceptable range: 0.1 to 10.0.";
    }
}
