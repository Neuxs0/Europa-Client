package dev.neuxs.europa_client.commands.modules.cheats;

import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.CheatModules;

public class JetpackHeightCommand extends ClientCommand {
    @Override
    public void run() {
        CheatModules.jetpackHeight.toggle(true);
    }

    @Override
    public String getDescription() {
        return "Toggles unlimited jetpack height.";
    }
}
