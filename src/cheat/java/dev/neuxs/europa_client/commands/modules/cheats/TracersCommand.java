package dev.neuxs.europa_client.commands.modules.cheats;

import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.CheatModules;

public class TracersCommand extends ClientCommand {
    @Override
    public void run() {
        CheatModules.tracers.toggle(true);
    }

    @Override
    public String getDescription() {
        return "Toggles tracers.";
    }
}
