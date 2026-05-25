package dev.neuxs.europa_client.commands.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.CheatModules;
import dev.neuxs.europa_client.utils.Chat;

public class LiquidWalkCommand extends ClientCommand {
    @Override
    public void run() {
        if (args.length == 1) {
            CheatModules.liquidWalk.toggle(true);
            return;
        }

        if (args.length == 4 && args[1].equalsIgnoreCase("set")) {
            Boolean value = parseBoolean(args[3]);
            if (value == null) {
                Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Invalid value. Use true or false.");
                return;
            }

            if (args[2].equalsIgnoreCase("water")) {
                CheatModules.liquidWalk.setWalksOnWater(value);
                return;
            }
            if (args[2].equalsIgnoreCase("lava")) {
                CheatModules.liquidWalk.setWalksOnLava(value);
                return;
            }
        }

        Client.clientChat.addMessage(null, "Usage: " + commandPrefix() + "liquidwalk OR "
                + commandPrefix() + "liquidwalk set <water|lava> <true|false>");
    }

    @Override
    public String getDescription() {
        return "Toggles liquid walk. Use '" + commandPrefix()
                + "liquidwalk set <water|lava> <true|false>' to configure liquids.";
    }

    private Boolean parseBoolean(String rawValue) {
        return switch (rawValue.trim().toLowerCase()) {
            case "true", "on", "yes", "1" -> true;
            case "false", "off", "no", "0" -> false;
            default -> null;
        };
    }
}
