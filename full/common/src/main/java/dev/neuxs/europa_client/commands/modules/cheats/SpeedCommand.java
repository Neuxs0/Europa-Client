package dev.neuxs.europa_client.commands.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.Chat;

public class SpeedCommand extends ClientCommand {

    @Override
    public void run() {
        if (args.length == 1) {
            Modules.speed.toggle(true);
            return;
        }

        // If additional arguments are provided, check for setting speed.
        // Expected format: "#speed set speed <value>"
        if (args.length >= 4 && args[1].equalsIgnoreCase("set") &&
                args[2].equalsIgnoreCase("speed")) {

            try {
                float speedValue = Float.parseFloat(args[3].trim());

                if (Float.isNaN(speedValue) || Float.isInfinite(speedValue)) {
                    Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Invalid speed value, please provide a finite number.");
                    return;
                }

                Modules.speed.setSpeed(speedValue);
            } catch (NumberFormatException ex) {
                Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Invalid number format. Use a valid float value.");
            }
        } else {
            Client.clientChat.addMessage(null, "Usage: #speed OR #speed set speed <value>");
        }
    }

    @Override
    public String getDescription() {
        return "Toggles speed cheat. Use '#speed set speed <value>' to set the speed.";
    }
}
