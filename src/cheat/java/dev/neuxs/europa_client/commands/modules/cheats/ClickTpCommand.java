package dev.neuxs.europa_client.commands.modules.cheats;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.CheatModules;

public class ClickTpCommand extends ClientCommand {

    @Override
    public void run() {
        if (args.length == 1) {
            CheatModules.clickTp.toggle(true);
            return;
        }

        if (args.length >= 4 && args[1].equalsIgnoreCase("set") &&
                args[2].equalsIgnoreCase("button")) {
            try {
                CheatModules.clickTp.setButton(args[3]);
            } catch (IllegalArgumentException ex) {
                Client.clientChat.addMessage(null, ex.getMessage());
            }
        } else {
            Client.clientChat.addMessage(null,
                    "Usage: " + commandPrefix() + "clicktp OR " + commandPrefix() + "clicktp set button <left|middle|right>");
        }
    }

    @Override
    public String getDescription() {
        return "Toggles click teleport. Use '" + commandPrefix() + "clicktp set button <left|middle|right>' to change the click button.";
    }
}
