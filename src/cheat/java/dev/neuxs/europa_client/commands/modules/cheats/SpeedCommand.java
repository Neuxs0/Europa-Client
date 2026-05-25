package dev.neuxs.europa_client.commands.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.CheatModules;
import dev.neuxs.europa_client.utils.Chat;

public class SpeedCommand extends ClientCommand {

    @Override
    public void run() {
        if (args.length == 1) {
            CheatModules.speed.toggle(true);
            return;
        }

        if (args.length >= 4 && args[1].equalsIgnoreCase("set") &&
                (args[2].equalsIgnoreCase("speed") || args[2].equalsIgnoreCase("jetpackSpeed"))) {

            try {
                float speedValue = parseFloatArg(args[3]);

                if (Float.isNaN(speedValue) || Float.isInfinite(speedValue)) {
                    Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Invalid speed value, please provide a finite number.");
                    return;
                }

                if (args[2].equalsIgnoreCase("jetpackSpeed")) {
                    CheatModules.speed.setJetpackSpeed(speedValue);
                } else {
                    CheatModules.speed.setSpeed(speedValue);
                }
            } catch (NumberFormatException ex) {
                Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Invalid number format. Use a valid float value.");
            }
        } else {
            Client.clientChat.addMessage(null, "Usage: " + commandPrefix() + "speed OR " + commandPrefix() + "speed set <speed|jetpackSpeed> <value>");
        }
    }

    @Override
    public String getDescription() {
        return "Toggles speed cheat. Use '" + commandPrefix() + "speed set <speed|jetpackSpeed> <value>' to set the speed.";
    }
}
