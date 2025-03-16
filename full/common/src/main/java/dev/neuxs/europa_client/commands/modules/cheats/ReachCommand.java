package dev.neuxs.europa_client.commands.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.Chat;

public class ReachCommand extends ClientCommand {

    @Override
    public void run() {
        if (args.length == 1) {
            Modules.reach.toggle(true);
            return;
        }

        // If additional arguments are provided, check for setting speed.
        // Expected format: "#reach set speed <value>"
        if (args.length >= 4 && args[1].equalsIgnoreCase("set") &&
                args[2].equalsIgnoreCase("distance")) {

            try {
                float reachValue = Float.parseFloat(args[3].trim());

                if (Float.isNaN(reachValue) || Float.isInfinite(reachValue)) {
                    Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Invalid distance value, please provide a finite number.");
                    return;
                }

                Modules.reach.setReachDistance(reachValue);
            } catch (NumberFormatException ex) {
                Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Invalid number format. Use a valid float value.");
            }
        } else {
            Client.clientChat.addMessage(null, "Usage: #reach OR #reach set distance <value>");
        }
    }

    @Override
    public String getDescription() {
        return "Toggles reach cheat. Use '#reach set distance <value>' to set the reach distance.";
    }
}
