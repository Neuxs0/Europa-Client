package dev.neuxs.europa_client.commands.modules.utils;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.Chat;

public class FreecamCommand extends ClientCommand {
    @Override
    public void run() {
        if (args.length == 1) {
            Modules.freecam.toggle(true);
            return;
        }

        if (args.length >= 4 && args[1].equalsIgnoreCase("set")) {
            if (args[2].equalsIgnoreCase("speed")) {
                try {
                    float speedValue = parseFloatArg(args[3]);
                    if (Float.isNaN(speedValue) || Float.isInfinite(speedValue)) {
                        Client.clientChat.addMessage(null,
                                Chat.getClientPrefix() + "Invalid speed value, please provide a finite number.");
                        return;
                    }

                    Modules.freecam.setSpeed(speedValue);
                } catch (NumberFormatException ex) {
                    Client.clientChat.addMessage(null,
                            Chat.getClientPrefix() + "Invalid number format. Use a valid float value.");
                }
            } else if (args[2].equalsIgnoreCase("horizontalMovement")) {
                Boolean horizontalMovement = parseBooleanArg(args[3]);
                if (horizontalMovement == null) {
                    Client.clientChat.addMessage(null,
                            Chat.getClientPrefix() + "Invalid boolean value. Use true or false.");
                    return;
                }

                Modules.freecam.setHorizontalMovement(horizontalMovement);
            } else if (args[2].equalsIgnoreCase("playerInteraction")) {
                Boolean playerInteraction = parseBooleanArg(args[3]);
                if (playerInteraction == null) {
                    Client.clientChat.addMessage(null,
                            Chat.getClientPrefix() + "Invalid boolean value. Use true or false.");
                    return;
                }

                Modules.freecam.setPlayerInteraction(playerInteraction);
            } else if (args[2].equalsIgnoreCase("disableOnDamage")) {
                Boolean disableOnDamage = parseBooleanArg(args[3]);
                if (disableOnDamage == null) {
                    Client.clientChat.addMessage(null,
                            Chat.getClientPrefix() + "Invalid boolean value. Use true or false.");
                    return;
                }

                Modules.freecam.setDisableOnDamage(disableOnDamage);
            } else {
                Client.clientChat.addMessage(null, getUsage());
            }
        } else {
            Client.clientChat.addMessage(null, getUsage());
        }
    }

    @Override
    public String getDescription() {
        return "Toggles freecam. Use '" + commandPrefix() + "freecam set <setting> <value>' to change settings.";
    }

    private String getUsage() {
        return "Usage: " + commandPrefix() + "freecam OR " + commandPrefix() +
                "freecam set speed <value> OR " + commandPrefix() +
                "freecam set horizontalMovement <true|false> OR " + commandPrefix() +
                "freecam set playerInteraction <true|false> OR " + commandPrefix() +
                "freecam set disableOnDamage <true|false>";
    }

    private Boolean parseBooleanArg(String value) {
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }
        return null;
    }
}
